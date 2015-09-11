package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.messageView;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantNotFoundException;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.UploadResult;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.forms.ContestContestantCreateForm;
import org.iatoki.judgels.uriel.forms.ContestContestantUpdateForm;
import org.iatoki.judgels.uriel.forms.ContestContestantUploadForm;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.UserService;
import org.iatoki.judgels.uriel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.uriel.views.html.contest.contestant.listCreateContestantsView;
import org.iatoki.judgels.uriel.views.html.contest.contestant.updateContestantView;
import org.iatoki.judgels.uriel.views.html.uploadResultView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public class ContestContestantController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 1000;

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
        return listCreateContestants(contestId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listCreateContestants(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isSupervisorOrAbove(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Page<ContestContestant> contestContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        boolean canUpdate = !contest.isLocked() && isAllowedToSuperviseContestants(contest);
        boolean canDelete = !contest.isLocked() && ContestControllerUtils.getInstance().isManagerOrAbove(contest, IdentityUtils.getUserJid());

        Form<ContestContestantCreateForm> contestContestantCreateForm = Form.form(ContestContestantCreateForm.class);
        Form<ContestContestantUploadForm> contestContestantUploadForm = Form.form(ContestContestantUploadForm.class);

        return showListCreateContestant(contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, canDelete, contestContestantCreateForm, contestContestantUploadForm, contest);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateContestant(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !isAllowedToSuperviseContestants(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestContestantCreateForm> contestContestantCreateForm = Form.form(ContestContestantCreateForm.class).bindFromRequest();
        boolean canDelete = ContestControllerUtils.getInstance().isManagerOrAbove(contest, IdentityUtils.getUserJid());

        if (formHasErrors(contestContestantCreateForm)) {
            return showListCreateContestantWithContestantCreateForm(pageIndex, orderBy, orderDir, filterString, true, canDelete, contestContestantCreateForm, contest);
        }

        ContestContestantCreateForm contestContestantCreateData = contestContestantCreateForm.get();
        try {
            JophielUser jophielUser = jophielPublicAPI.findUserByUsername(contestContestantCreateData.username);
            if (jophielUser == null) {
                contestContestantCreateForm.reject("error.contestant.create.userNotExist");

                return showListCreateContestantWithContestantCreateForm(pageIndex, orderBy, orderDir, filterString, true, canDelete, contestContestantCreateForm, contest);
            }

            if (contestContestantService.isContestantInContest(contest.getJid(), jophielUser.getJid())) {
                contestContestantCreateForm.reject("error.contestant.create.userIsAlreadyContestant");

                return showListCreateContestantWithContestantCreateForm(pageIndex, orderBy, orderDir, filterString, true, canDelete, contestContestantCreateForm, contest);
            }

            userService.upsertUserFromJophielUser(jophielUser, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            contestContestantService.createContestContestant(contest.getJid(), jophielUser.getJid(), ContestContestantStatus.valueOf(contestContestantCreateData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            UrielControllerUtils.getInstance().addActivityLog("Add contestant " + contestContestantCreateData.username + " in contest " + contest.getName() + ".");

            return redirect(routes.ContestContestantController.viewContestants(contest.getId()));
        } catch (JudgelsAPIClientException e) {
            contestContestantCreateForm.reject("error.contestant.create.userNotExist");

            return showListCreateContestantWithContestantCreateForm(pageIndex, orderBy, orderDir, filterString, true, canDelete, contestContestantCreateForm, contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateContestant(long contestId, long contestContestantId) throws ContestNotFoundException, ContestContestantNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestContestantService.findContestantInContestById(contestContestantId);
        if (contest.isLocked() || !isAllowedToSuperviseContestants(contest) || !contestContestant.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestContestantUpdateForm contestContestantUpdateData = new ContestContestantUpdateForm();
        contestContestantUpdateData.status = contestContestant.getStatus().name();
        Form<ContestContestantUpdateForm> contestContestantUpdateForm = Form.form(ContestContestantUpdateForm.class).fill(contestContestantUpdateData);

        UrielControllerUtils.getInstance().addActivityLog("Try to update contestant " + contestContestant.getUserJid() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateContestant(contestContestantUpdateForm, contest, contestContestant);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateContestant(long contestId, long contestContestantId) throws ContestNotFoundException, ContestContestantNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestContestantService.findContestantInContestById(contestContestantId);
        if (contest.isLocked() || !isAllowedToSuperviseContestants(contest) || !contestContestant.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestContestantUpdateForm> contestContestantUpdateForm = Form.form(ContestContestantUpdateForm.class).bindFromRequest();

        if (formHasErrors(contestContestantUpdateForm)) {
            return showUpdateContestant(contestContestantUpdateForm, contest, contestContestant);
        }

        ContestContestantUpdateForm contestContestantUpdateData = contestContestantUpdateForm.get();
        contestContestantService.updateContestContestant(contestContestant.getId(), ContestContestantStatus.valueOf(contestContestantUpdateData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog("Update contestant " + contestContestant.getUserJid() + " in contest " + contest.getName() + ".");

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

                UrielControllerUtils.getInstance().addActivityLog("Upload contestants in contest " + contest.getName() + ".");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        List<UploadResult> failedUploads = failedUploadsBuilder.build();

        return showUploadContestantResult(failedUploads, contest);
    }

    @Transactional
    public Result deleteContestant(long contestId, long contestContestantId) throws ContestNotFoundException, ContestContestantNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestContestantService.findContestantInContestById(contestContestantId);
        if (contest.isLocked() || !ContestControllerUtils.getInstance().isManagerOrAbove(contest, IdentityUtils.getUserJid()) || !contestContestant.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        contestContestantService.deleteContestContestant(contestContestant.getId());

        UrielControllerUtils.getInstance().addActivityLog("Delete contestant " + JidCacheServiceImpl.getInstance().getDisplayName(contestContestant.getUserJid()) + ".");

        return redirect(routes.UserController.index());
    }

    private Result showListCreateContestantWithContestantCreateForm(long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, boolean canDelete, Form<ContestContestantCreateForm> contestContestantCreateForm, Contest contest) {
        Form<ContestContestantUploadForm> contestContestantUploadForm = Form.form(ContestContestantUploadForm.class);

        Page<ContestContestant> pageOfContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        return showListCreateContestant(pageOfContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, canDelete, contestContestantCreateForm, contestContestantUploadForm, contest);
    }

    private Result showListCreateContestant(Page<ContestContestant> contestContestants, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, boolean canDelete, Form<ContestContestantCreateForm> contestContestantCreateForm, Form<ContestContestantUploadForm> contestContestantUploadForm, Contest contest) {
        LazyHtml content = new LazyHtml(listCreateContestantsView.render(contest.getId(), contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, canDelete, contestContestantCreateForm, contestContestantUploadForm, jophielPublicAPI.getUserAutocompleteAPIEndpoint()));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.list"), c));

        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("contestant.list"), routes.ContestContestantController.viewContestants(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Contestants");

        UrielControllerUtils.getInstance().addActivityLog("Open list of contestants in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateContestant(Form<ContestContestantUpdateForm> form, Contest contest, ContestContestant contestContestant) {
        LazyHtml content = new LazyHtml(updateContestantView.render(contest.getId(), contestContestant.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.update"), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);

        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestContestantController.viewContestants(contest.getId())),
                new InternalLink(Messages.get("contestant.update"), routes.ContestContestantController.updateContestant(contest.getId(), contestContestant.getId()))
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
                        .add(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.jumpToContestants(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseContestants(Contest contest) {
        return ContestControllerUtils.getInstance().isPermittedToSupervise(contest, ContestPermissions.CONTESTANT, IdentityUtils.getUserJid());
    }
}
