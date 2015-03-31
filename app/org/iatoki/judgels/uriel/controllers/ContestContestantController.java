package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.accessTypesLayout;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.commons.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.gabriel.commons.GabrielUtils;
import org.iatoki.judgels.jophiel.commons.JophielUtils;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantCreateForm;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestContestantUpdateForm;
import org.iatoki.judgels.uriel.ContestContestantUploadForm;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.UserRoleService;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.contestant.createContestantView;
import org.iatoki.judgels.uriel.views.html.contest.contestant.listContestantsView;
import org.iatoki.judgels.uriel.views.html.contest.contestant.updateContestantView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public class ContestContestantController extends Controller {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;
    private final UserRoleService userRoleService;

    public ContestContestantController(ContestService contestService, UserRoleService userRoleService) {
        this.contestService = contestService;
        this.userRoleService = userRoleService;
    }


    public Result viewContestants(long contestId) {
        return listContestants(contestId, 0, "id", "asc", "");
    }

    public Result listContestants(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isSupervisorOrAbove(contest)) {
            Page<ContestContestant> contestContestants = contestService.pageContestContestantsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = isAllowedToSuperviseContestants(contest);

            LazyHtml content = new LazyHtml(listContestantsView.render(contest.getId(), contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate));
            if (canUpdate) {
                content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("contestant.list"), new InternalLink(Messages.get("commons.create"), routes.ContestContestantController.createContestant(contestId)), c));
            } else {
                content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.list"), c));
            }
            content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
            appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                    new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Contestants");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result createContestant(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseContestants(contest)) {
            Form<ContestContestantCreateForm> form = Form.form(ContestContestantCreateForm.class);
            Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);

            return showCreateContestant(form, form2, contest);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateContestant(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseContestants(contest)) {
            Form<ContestContestantCreateForm> form = Form.form(ContestContestantCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);
                return showCreateContestant(form, form2, contest);
            } else {
                ContestContestantCreateForm contestContestantCreateForm = form.get();
                String userJid = JophielUtils.verifyUsername(contestContestantCreateForm.username);
                if ((userJid != null) && (!contestService.isContestContestantInContestByUserJid(contest.getJid(), userJid))) {
                    userRoleService.upsertUserRoleFromJophielUserJid(userJid);
                    contestService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.valueOf(contestContestantCreateForm.status));

                    return redirect(routes.ContestContestantController.viewContestants(contest.getId()));
                } else {
                    Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);
                    form.reject("error.contestant.create.userJid.invalid");
                    return showCreateContestant(form, form2, contest);
                }
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateContestant(long contestId, long contestContestantId) {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestService.findContestContestantByContestContestantId(contestContestantId);
        if (isAllowedToSuperviseContestants(contest) && contestContestant.getContestJid().equals(contest.getJid())) {
            ContestContestantUpdateForm contestContestantUpsertForm = new ContestContestantUpdateForm(contestContestant);
            Form<ContestContestantUpdateForm> form = Form.form(ContestContestantUpdateForm.class).fill(contestContestantUpsertForm);

            return showUpdateContestant(form, contest, contestContestant);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateContestant(long contestId, long contestContestantId) {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestService.findContestContestantByContestContestantId(contestContestantId);
        if (isAllowedToSuperviseContestants(contest) && contestContestant.getContestJid().equals(contest.getJid())) {
            Form<ContestContestantUpdateForm> form = Form.form(ContestContestantUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateContestant(form, contest, contestContestant);
            } else {
                ContestContestantUpdateForm contestContestantUpdateForm = form.get();
                contestService.updateContestContestant(contestContestant.getId(), ContestContestantStatus.valueOf(contestContestantUpdateForm.status));

                return redirect(routes.ContestContestantController.viewContestants(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUploadContestant(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        if (isAllowedToSuperviseContestants(contest)) {
            file = body.getFile("usernames");
            if (file != null) {
                File userFile = file.getFile();
                try {
                    String[] usernames = FileUtils.readFileToString(userFile).split("\n");
                    for (String username : usernames) {
                        String userJid = JophielUtils.verifyUsername(username);
                        if ((userJid != null) && (!contestService.isContestContestantInContestByUserJid(contest.getJid(), userJid))) {
                            userRoleService.upsertUserRoleFromJophielUserJid(userJid);
                            contestService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.APPROVED);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return redirect(routes.ContestContestantController.viewContestants(contest.getId()));
        } else {
            return tryEnteringContest(contest);
        }
    }

    private Result showCreateContestant(Form<ContestContestantCreateForm> form, Form<ContestContestantUploadForm> form2, Contest contest){
        LazyHtml content = new LazyHtml(createContestantView.render(contest.getId(), form, form2));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.create"), c));
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
        appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())),
                new InternalLink(Messages.get("contestant.create"), routes.ContestContestantController.createContestant(contest.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Contestant - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateContestant(Form<ContestContestantUpdateForm> form, Contest contest, ContestContestant contestContestant){
        LazyHtml content = new LazyHtml(updateContestantView.render(contest.getId(), contestContestant.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.update"), c));
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
        appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                new InternalLink(Messages.get("contestant.contestant"), routes.ContestContestantController.viewContestants(contest.getId())),
                new InternalLink(Messages.get("contestant.update"), routes.ContestContestantController.updateContestant(contest.getId(), contestContestant.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Contestant - Update");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendTabsLayout(LazyHtml content, Contest contest) {
        Date contestEndTime = contest.getEndTime();
        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
            contestEndTime = new Date(contestContestant.getContestEnterTime() + contestTypeConfigVirtual.getContestDuration());
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, isSupervisorOrAbove(contest), contestEndTime);
    }

    private boolean isManager(Contest contest) {
        return contestService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isSupervisor(Contest contest) {
        return contestService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isCoach(Contest contest) {
        return contestService.isUserCoachInAnyTeamByContestJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isContestant(Contest contest) {
        return contestService.isContestContestantInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isContestStarted(Contest contest) {
        return (!new Date().before(contest.getStartTime()));
    }

    private boolean isAllowedToEnterContest(Contest contest) {
        if (ControllerUtils.getInstance().isAdmin() || isManager(contest) || isSupervisor(contest)) {
            return true;
        }
        if (contest.isStandard()) {
            return ((isContestant(contest) && isContestStarted(contest)) || (isCoach(contest)));
        } else {
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
            if (contestTypeConfigVirtual.getStartTrigger().equals(ContestTypeConfigVirtualStartTrigger.CONTESTANT)) {
                return (isContestant(contest) && (isContestStarted(contest)));
            } else {
                return ((isContestStarted(contest)) && (isCoach(contest) || (isContestant(contest) && (contestService.isContestEntered(contest.getJid(), IdentityUtils.getUserJid())))));
            }
        }
    }

    private boolean isSupervisorOrAbove(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest) || (isSupervisor(contest));
    }

    private boolean isAllowedToSuperviseContestants(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest) || (isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isContestant());
    }

    private Result tryEnteringContest(Contest contest) {
        if (isAllowedToEnterContest(contest)) {
            return redirect(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()));
        } else {
            return redirect(routes.ContestController.viewContest(contest.getId()));
        }
    }
}
