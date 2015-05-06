package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.accessTypesLayout;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.commons.views.html.layouts.messageView;
import org.iatoki.judgels.jophiel.commons.JophielUtils;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantCreateForm;
import org.iatoki.judgels.uriel.ContestContestantNotFoundException;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestContestantUpdateForm;
import org.iatoki.judgels.uriel.ContestContestantUploadForm;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.UploadResult;
import org.iatoki.judgels.uriel.UserService;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public class ContestContestantController extends BaseController {

    private static final long PAGE_SIZE = 1000;

    private final ContestService contestService;
    private final UserService userService;

    public ContestContestantController(ContestService contestService, UserService userService) {
        this.contestService = contestService;
        this.userService = userService;
    }

    @AddCSRFToken
    public Result viewContestants(long contestId) throws ContestNotFoundException {
        return listCreateContestants(contestId, 0, "id", "asc", "");
    }

    @AddCSRFToken
    public Result listCreateContestants(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            Page<ContestContestant> contestContestants = contestService.pageContestContestantsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = isAllowedToSuperviseContestants(contest);

            Form<ContestContestantCreateForm> form = Form.form(ContestContestantCreateForm.class);
            Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);

            return showListCreateContestant(contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, form, form2, contest);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateContestant(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseContestants(contest)) {
            Form<ContestContestantCreateForm> form = Form.form(ContestContestantCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);

                Page<ContestContestant> contestContestants = contestService.pageContestContestantsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
                boolean canUpdate = isAllowedToSuperviseContestants(contest);

                return showListCreateContestant(contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, form, form2, contest);
            } else {
                ContestContestantCreateForm contestContestantCreateForm = form.get();
                String userJid = JophielUtils.verifyUsername(contestContestantCreateForm.username);
                if ((userJid != null) && (!contestService.isContestContestantInContestByUserJid(contest.getJid(), userJid))) {
                    userService.upsertUserFromJophielUserJid(userJid);
                    contestService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.valueOf(contestContestantCreateForm.status));

                    ControllerUtils.getInstance().addActivityLog("Add contestant " + contestContestantCreateForm.username + " in contest " + contest.getName() + ".");

                    return redirect(routes.ContestContestantController.viewContestants(contest.getId()));
                } else {
                    Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);
                    form.reject("error.contestant.create.userJid.invalid");

                    Page<ContestContestant> contestContestants = contestService.pageContestContestantsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
                    boolean canUpdate = isAllowedToSuperviseContestants(contest);

                    return showListCreateContestant(contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, form, form2, contest);
                }
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateContestant(long contestId, long contestContestantId) throws ContestNotFoundException, ContestContestantNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestService.findContestContestantByContestContestantId(contestContestantId);
        if (isAllowedToSuperviseContestants(contest) && contestContestant.getContestJid().equals(contest.getJid())) {
            ContestContestantUpdateForm contestContestantUpsertForm = new ContestContestantUpdateForm(contestContestant);
            Form<ContestContestantUpdateForm> form = Form.form(ContestContestantUpdateForm.class).fill(contestContestantUpsertForm);

            ControllerUtils.getInstance().addActivityLog("Try to update contestant " + contestContestant.getUserJid() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateContestant(form, contest, contestContestant);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateContestant(long contestId, long contestContestantId) throws ContestNotFoundException, ContestContestantNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestService.findContestContestantByContestContestantId(contestContestantId);
        if (isAllowedToSuperviseContestants(contest) && contestContestant.getContestJid().equals(contest.getJid())) {
            Form<ContestContestantUpdateForm> form = Form.form(ContestContestantUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateContestant(form, contest, contestContestant);
            } else {
                ContestContestantUpdateForm contestContestantUpdateForm = form.get();
                contestService.updateContestContestant(contestContestant.getId(), ContestContestantStatus.valueOf(contestContestantUpdateForm.status));

                ControllerUtils.getInstance().addActivityLog("Update contestant " + contestContestant.getUserJid() + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestContestantController.viewContestants(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUploadContestant(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (isAllowedToSuperviseContestants(contest)) {
            ImmutableList.Builder<UploadResult> failedUploadsBuilder = ImmutableList.builder();
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart file;

            file = body.getFile("usernames");
            if (file != null) {
                File userFile = file.getFile();
                try {
                    String[] usernames = FileUtils.readFileToString(userFile).split("\n");
                    for (String username : usernames) {
                        String userJid = JophielUtils.verifyUsername(username);
                        if (userJid != null) {
                            if (!contestService.isContestContestantInContestByUserJid(contest.getJid(), userJid)) {
                                userService.upsertUserFromJophielUserJid(userJid);
                                contestService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.APPROVED);
                            } else {
                                failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.contestant.isAlreadyContestant")));
                            }
                        } else {
                            failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.contestant.userNotExist")));
                        }
                    }

                    ControllerUtils.getInstance().addActivityLog("Upload contestants in contest " + contest.getName() + ".");

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            List<UploadResult> failedUploads = failedUploadsBuilder.build();

            return showUploadContestantResult(failedUploads, contest);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    private Result showListCreateContestant(Page<ContestContestant> contestContestants, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, Form<ContestContestantCreateForm> form, Form<ContestContestantUploadForm> form2, Contest contest){
        LazyHtml content = new LazyHtml(listCreateContestantsView.render(contest.getId(), contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate, form, form2));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.list"), c));

        appendSubtabsLayout(content, contest);ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("contestant.list"), routes.ContestContestantController.viewContestants(contest.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Contestants");

        ControllerUtils.getInstance().addActivityLog("Open list of contestants in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateContestant(Form<ContestContestantUpdateForm> form, Contest contest, ContestContestant contestContestant){
        LazyHtml content = new LazyHtml(updateContestantView.render(contest.getId(), contestContestant.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.update"), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);

        appendBreadcrumbsLayout(content, contest,
              new InternalLink(Messages.get("status.supervisor"), routes.ContestContestantController.viewContestants(contest.getId())),
              new InternalLink(Messages.get("contestant.update"), routes.ContestContestantController.updateContestant(contest.getId(), contestContestant.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Contestant - Update");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUploadContestantResult(List<UploadResult> failedUploads, Contest contest) {
        LazyHtml content;
        if (failedUploads.size() > 0) {
            content = new LazyHtml(uploadResultView.render(failedUploads));
        } else {
            content = new LazyHtml(messageView.render(Messages.get("contestant.upload.success")));
        }
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.upload.result"), c));

        appendSubtabsLayout(content, contest);ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
              new InternalLink(Messages.get("contestant.list"), routes.ContestContestantController.viewContestants(contest.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Contestants - Upload Result");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendSubtabsLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.jumpToContestants(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseContestants(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest) || (ContestControllerUtils.getInstance().isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isContestant());
    }
}
