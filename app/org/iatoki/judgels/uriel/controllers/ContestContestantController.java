package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.jophiel.Jophiel;
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
import org.iatoki.judgels.uriel.services.ContestSupervisorService;
import org.iatoki.judgels.uriel.services.UserService;
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

    private final Jophiel jophiel;
    private final ContestService contestService;
    private final ContestContestantService contestContestantService;
    private final ContestSupervisorService contestSupervisorService;
    private final UserService userService;

    @Inject
    public ContestContestantController(Jophiel jophiel, ContestService contestService, ContestContestantService contestContestantService, ContestSupervisorService contestSupervisorService, UserService userService) {
        this.jophiel = jophiel;
        this.contestService = contestService;
        this.contestContestantService = contestContestantService;
        this.contestSupervisorService = contestSupervisorService;
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
        if (!ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Page<ContestContestant> contestContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        boolean canUpdate = isAllowedToSuperviseContestants(contest);

        Form<ContestContestantCreateForm> form = Form.form(ContestContestantCreateForm.class);
        Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);

        return showListCreateContestant(contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, form, form2, contest);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateContestant(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!isAllowedToSuperviseContestants(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Form<ContestContestantCreateForm> contestContestantCreateForm = Form.form(ContestContestantCreateForm.class).bindFromRequest();

        if (formHasErrors(contestContestantCreateForm)) {
            Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);

            Page<ContestContestant> contestContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
            boolean canUpdate = isAllowedToSuperviseContestants(contest);

            return showListCreateContestant(contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, contestContestantCreateForm, form2, contest);
        }

        ContestContestantCreateForm contestContestantCreateData = contestContestantCreateForm.get();
        try {
            String userJid = jophiel.verifyUsername(contestContestantCreateData.username);
            if (userJid == null) {
                Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);
                contestContestantCreateForm.reject("error.contestant.create.userNotExist");

                Page<ContestContestant> contestContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
                boolean canUpdate = isAllowedToSuperviseContestants(contest);

                return showListCreateContestant(contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, contestContestantCreateForm, form2, contest);
            }

            if (contestContestantService.isContestantInContest(contest.getJid(), userJid)) {
                Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);
                contestContestantCreateForm.reject("error.contestant.create.userIsAlreadyContestant");

                Page<ContestContestant> contestContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
                boolean canUpdate = isAllowedToSuperviseContestants(contest);

                return showListCreateContestant(contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, contestContestantCreateForm, form2, contest);
            }

            userService.upsertUserFromJophielUserJid(userJid);
            contestContestantService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.valueOf(contestContestantCreateData.status));

            UrielControllerUtils.getInstance().addActivityLog("Add contestant " + contestContestantCreateData.username + " in contest " + contest.getName() + ".");

            return redirect(routes.ContestContestantController.viewContestants(contest.getId()));
        } catch (IOException e) {
            Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);
            contestContestantCreateForm.reject("error.contestant.create.userNotExist");

            Page<ContestContestant> contestContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
            boolean canUpdate = isAllowedToSuperviseContestants(contest);
            return showListCreateContestant(contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, contestContestantCreateForm, form2, contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateContestant(long contestId, long contestContestantId) throws ContestNotFoundException, ContestContestantNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestContestantService.findContestantInContestById(contestContestantId);
        if (!isAllowedToSuperviseContestants(contest) || !contestContestant.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        ContestContestantUpdateForm contestContestantUpsertData = new ContestContestantUpdateForm();
        contestContestantUpsertData.status = contestContestant.getStatus().name();
        Form<ContestContestantUpdateForm> contestContestantUpsertForm = Form.form(ContestContestantUpdateForm.class).fill(contestContestantUpsertData);

        UrielControllerUtils.getInstance().addActivityLog("Try to update contestant " + contestContestant.getUserJid() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateContestant(contestContestantUpsertForm, contest, contestContestant);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateContestant(long contestId, long contestContestantId) throws ContestNotFoundException, ContestContestantNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestContestantService.findContestantInContestById(contestContestantId);
        if (!isAllowedToSuperviseContestants(contest) || !contestContestant.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Form<ContestContestantUpdateForm> contestContestantUpdateForm = Form.form(ContestContestantUpdateForm.class).bindFromRequest();

        if (formHasErrors(contestContestantUpdateForm)) {
            return showUpdateContestant(contestContestantUpdateForm, contest, contestContestant);
        }

        ContestContestantUpdateForm contestContestantUpdateData = contestContestantUpdateForm.get();
        contestContestantService.updateContestContestant(contestContestant.getId(), ContestContestantStatus.valueOf(contestContestantUpdateData.status));

        UrielControllerUtils.getInstance().addActivityLog("Update contestant " + contestContestant.getUserJid() + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestContestantController.viewContestants(contest.getId()));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadContestant(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!isAllowedToSuperviseContestants(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
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
                        String userJid = jophiel.verifyUsername(username);
                        if (userJid != null) {
                            if (!contestContestantService.isContestantInContest(contest.getJid(), userJid)) {
                                userService.upsertUserFromJophielUserJid(userJid);
                                contestContestantService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.APPROVED);
                            } else {
                                failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.contestant.isAlreadyContestant")));
                            }
                        } else {
                            failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.contestant.userNotExist")));
                        }
                    } catch (IOException e) {
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

    private Result showListCreateContestant(Page<ContestContestant> contestContestants, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, Form<ContestContestantCreateForm> form, Form<ContestContestantUploadForm> form2, Contest contest) {
        LazyHtml content = new LazyHtml(listCreateContestantsView.render(contest.getId(), contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, form, form2, jophiel.getAutoCompleteEndPoint()));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.list"), c));

        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
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
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
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

        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
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
        return UrielControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest) || (ContestControllerUtils.getInstance().isSupervisor(contest) && contestSupervisorService.findContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid()).getContestPermission().isAllowed(ContestPermissions.CONTESTANT));
    }
}
