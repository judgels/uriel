package org.iatoki.judgels.uriel.contest.contestant;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.EnumUtils;
import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.jophiel.activity.BasicActivityKeys;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.messageView;
import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.contest.ContestNotFoundException;
import org.iatoki.judgels.uriel.contest.supervisor.ContestPermissions;
import org.iatoki.judgels.uriel.UploadResult;
import org.iatoki.judgels.uriel.UrielControllerUtils;
import org.iatoki.judgels.uriel.contest.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.contest.ContestService;
import org.iatoki.judgels.uriel.user.UserService;
import org.iatoki.judgels.uriel.jid.JidCacheServiceImpl;
import org.iatoki.judgels.uriel.contest.contestant.html.listAddContestantsView;
import org.iatoki.judgels.uriel.contest.contestant.html.editContestantView;
import org.iatoki.judgels.uriel.html.uploadResultView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
public class ContestContestantController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 1000;
    private static final String CONTESTANT = "contestant";
    private static final String CONTEST = "contest";

    private final JophielPublicAPI jophielPublicAPI;
    private final ContestService contestService;
    private final ContestContestantService contestContestantService;
    private final UserService userService;

    @Inject
    public ContestContestantController(JophielPublicAPI jophielPublicAPI, ContestService contestService, ContestContestantService contestContestantService, UserService userService) {
        this.jophielPublicAPI = jophielPublicAPI;
        this.contestService = contestService;
        this.contestContestantService = contestContestantService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewContestants(long contestId) throws ContestNotFoundException {
        return listAddContestants(contestId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listAddContestants(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isSupervisorOrAbove(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Page<ContestContestant> contestContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        boolean canUpdate = !contest.isLocked() && isAllowedToSuperviseContestants(contest);
        boolean canDelete = !contest.isLocked() && ContestControllerUtils.getInstance().isManagerOrAbove(contest, IdentityUtils.getUserJid());

        Form<ContestContestantAddForm> contestContestantCreateForm = Form.form(ContestContestantAddForm.class);
        Form<ContestContestantUploadForm> contestContestantUploadForm = Form.form(ContestContestantUploadForm.class);

        return showlistAddContestant(contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, canDelete, contestContestantCreateForm, contestContestantUploadForm, contest);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postAddContestant(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !isAllowedToSuperviseContestants(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestContestantAddForm> contestContestantCreateForm = Form.form(ContestContestantAddForm.class).bindFromRequest();
        boolean canDelete = ContestControllerUtils.getInstance().isManagerOrAbove(contest, IdentityUtils.getUserJid());

        if (formHasErrors(contestContestantCreateForm)) {
            return showlistAddContestantWithContestantAddForm(pageIndex, orderBy, orderDir, filterString, true, canDelete, contestContestantCreateForm, contest);
        }

        ContestContestantAddForm contestContestantCreateData = contestContestantCreateForm.get();

        if (!EnumUtils.isValidEnum(ContestContestantStatus.class, contestContestantCreateData.status)) {
            return showlistAddContestantWithContestantAddForm(pageIndex, orderBy, orderDir, filterString, true, canDelete, contestContestantCreateForm, contest);
        }

        JophielUser jophielUser;
        try {
            jophielUser = jophielPublicAPI.findUserByUsername(contestContestantCreateData.username);
        } catch (JudgelsAPIClientException e) {
            contestContestantCreateForm.reject("error.contestant.create.userNotExist");

            return showlistAddContestantWithContestantAddForm(pageIndex, orderBy, orderDir, filterString, true, canDelete, contestContestantCreateForm, contest);
        }

        if (jophielUser == null) {
            contestContestantCreateForm.reject("error.contestant.create.userNotExist");

            return showlistAddContestantWithContestantAddForm(pageIndex, orderBy, orderDir, filterString, true, canDelete, contestContestantCreateForm, contest);
        }

        if (contestContestantService.isContestantInContest(contest.getJid(), jophielUser.getJid())) {
            contestContestantCreateForm.reject("error.contestant.create.userIsAlreadyContestant");

            return showlistAddContestantWithContestantAddForm(pageIndex, orderBy, orderDir, filterString, true, canDelete, contestContestantCreateForm, contest);
        }

        userService.upsertUserFromJophielUser(jophielUser, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        contestContestantService.createContestContestant(contest.getJid(), jophielUser.getJid(), ContestContestantStatus.valueOf(contestContestantCreateData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.ADD_IN.construct(CONTEST, contest.getJid(), contest.getName(), CONTESTANT, jophielUser.getJid(), jophielUser.getUsername()));

        return redirect(routes.ContestContestantController.viewContestants(contest.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editContestant(long contestId, long contestContestantId) throws ContestNotFoundException, ContestContestantNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestContestantService.findContestantInContestById(contestContestantId);
        if (contest.isLocked() || !isAllowedToSuperviseContestants(contest) || !contestContestant.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestContestantEditForm contestContestantEditData = new ContestContestantEditForm();
        contestContestantEditData.status = contestContestant.getStatus().name();
        Form<ContestContestantEditForm> contestContestantEditForm = Form.form(ContestContestantEditForm.class).fill(contestContestantEditData);

        return showEditContestant(contestContestantEditForm, contest, contestContestant);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditContestant(long contestId, long contestContestantId) throws ContestNotFoundException, ContestContestantNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestContestantService.findContestantInContestById(contestContestantId);
        if (contest.isLocked() || !isAllowedToSuperviseContestants(contest) || !contestContestant.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestContestantEditForm> contestContestantEditForm = Form.form(ContestContestantEditForm.class).bindFromRequest();

        if (formHasErrors(contestContestantEditForm)) {
            return showEditContestant(contestContestantEditForm, contest, contestContestant);
        }

        ContestContestantEditForm contestContestantEditData = contestContestantEditForm.get();

        if (!EnumUtils.isValidEnum(ContestContestantStatus.class, contestContestantEditData.status)) {
            return showEditContestant(contestContestantEditForm, contest, contestContestant);
        }

        contestContestantService.updateContestContestant(contestContestant.getId(), ContestContestantStatus.valueOf(contestContestantEditData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.EDIT_IN.construct(CONTEST, contest.getJid(), contest.getName(), CONTESTANT, contestContestant.getUserJid(), JidCacheServiceImpl.getInstance().getDisplayName(contestContestant.getUserJid())));

        return redirect(routes.ContestContestantController.viewContestants(contest.getId()));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadContestant(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (contest.isLocked() || !isAllowedToSuperviseContestants(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ImmutableList.Builder<UploadResult> failedUploadsBuilder = ImmutableList.builder();
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        file = body.getFile("usernames");
        if (file != null) {
            File userFile = file.getFile();
            try {
                String[] usernames = FileUtils.readFileToString(userFile).split("\n");
                for (String username : usernames) {
                    try {
                        JophielUser jophielUser = jophielPublicAPI.findUserByUsername(username);
                        if (jophielUser != null) {
                            if (!contestContestantService.isContestantInContest(contest.getJid(), jophielUser.getJid())) {
                                userService.upsertUserFromJophielUser(jophielUser, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
                                contestContestantService.createContestContestant(contest.getJid(), jophielUser.getJid(), ContestContestantStatus.APPROVED, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
                            } else {
                                failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.contestant.isAlreadyContestant")));
                            }
                        } else {
                            failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.contestant.userNotExist")));
                        }
                    } catch (JudgelsAPIClientException e) {
                        failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.contestant.userNotExist")));
                    }
                }

                UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.UPLOAD_IN.construct(CONTEST, contest.getJid(), contest.getName(), CONTESTANT, null, userFile.getName()));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        List<UploadResult> failedUploads = failedUploadsBuilder.build();

        return showUploadContestantResult(failedUploads, contest);
    }

    @Transactional
    public Result removeContestant(long contestId, long contestContestantId) throws ContestNotFoundException, ContestContestantNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestContestantService.findContestantInContestById(contestContestantId);
        if (contest.isLocked() || !ContestControllerUtils.getInstance().isManagerOrAbove(contest, IdentityUtils.getUserJid()) || !contestContestant.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        contestContestantService.deleteContestContestant(contestContestant.getId());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.REMOVE_FROM.construct(CONTEST, contest.getJid(), contest.getName(), CONTESTANT, contestContestant.getUserJid(), JidCacheServiceImpl.getInstance().getDisplayName(contestContestant.getUserJid())));

        return redirect(routes.ContestContestantController.viewContestants(contest.getId()));
    }

    private Result showlistAddContestantWithContestantAddForm(long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, boolean canDelete, Form<ContestContestantAddForm> contestContestantAddForm, Contest contest) {
        Form<ContestContestantUploadForm> contestContestantUploadForm = Form.form(ContestContestantUploadForm.class);

        Page<ContestContestant> pageOfContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        return showlistAddContestant(pageOfContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, canDelete, contestContestantAddForm, contestContestantUploadForm, contest);
    }

    private Result showlistAddContestant(Page<ContestContestant> contestContestants, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, boolean canDelete, Form<ContestContestantAddForm> contestContestantCreateForm, Form<ContestContestantUploadForm> contestContestantUploadForm, Contest contest) {
        LazyHtml content = new LazyHtml(listAddContestantsView.render(contest.getId(), contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, canDelete, contestContestantCreateForm, contestContestantUploadForm, jophielPublicAPI.getUserAutocompleteAPIEndpoint()));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.list"), c));

        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("contestant.list"), routes.ContestContestantController.viewContestants(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Contestants");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditContestant(Form<ContestContestantEditForm> form, Contest contest, ContestContestant contestContestant) {
        LazyHtml content = new LazyHtml(editContestantView.render(contest.getId(), contestContestant.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.update"), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);

        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestContestantController.viewContestants(contest.getId())),
                new InternalLink(Messages.get("contestant.update"), routes.ContestContestantController.editContestant(contest.getId(), contestContestant.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Contestant - Update");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUploadContestantResult(List<UploadResult> failedUploads, Contest contest) {
        LazyHtml content;
        if (failedUploads.size() > 0) {
            content = new LazyHtml(uploadResultView.render(failedUploads));
        } else {
            content = new LazyHtml(messageView.render(Messages.get("contestant.upload.success")));
        }
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.upload.result"), c));

        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("contestant.list"), routes.ContestContestantController.viewContestants(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Contestants - Upload Result");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("contestant.contestants"), org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToContestants(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseContestants(Contest contest) {
        return ContestControllerUtils.getInstance().isPermittedToSupervise(contest, ContestPermissions.CONTESTANT, IdentityUtils.getUserJid());
    }
}
