package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.commons.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.jophiel.commons.JophielUtils;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestSupervisor;
import org.iatoki.judgels.uriel.ContestSupervisorCreateForm;
import org.iatoki.judgels.uriel.ContestSupervisorUpdateForm;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.UserService;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.createSupervisorView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.listSupervisorsView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.updateSupervisorView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Date;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public class ContestSupervisorController extends Controller {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;
    private final UserService userRoleService;

    public ContestSupervisorController(ContestService contestService, UserService userRoleService) {
        this.contestService = contestService;
        this.userRoleService = userRoleService;
    }

    public Result viewSupervisors(long contestId) {
        return listSupervisors(contestId, 0, "id", "asc", "");
    }

    public Result listSupervisors(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isSupervisorOrAbove(contest)) {
            Page<ContestSupervisor> contestPermissionPage = contestService.pageContestSupervisorsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = isAllowedToManageSupervisors(contest);

            LazyHtml content = new LazyHtml(listSupervisorsView.render(contest.getId(), contestPermissionPage, pageIndex, orderBy, orderDir, filterString, canUpdate));
            if (canUpdate) {
                content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("supervisor.list"), new InternalLink(Messages.get("commons.create"), routes.ContestSupervisorController.createSupervisor(contestId)), c));
            } else {
                content.appendLayout(c -> heading3Layout.render(Messages.get("supervisor.list"), c));
            }
            appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                  new InternalLink(contest.getName(), routes.ContestController.viewContest(contestId)),
                  new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestSupervisorController.viewSupervisors(contest.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Supervisors");

            ControllerUtils.getInstance().addActivityLog("List all supervisors in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result createSupervisor(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToManageSupervisors(contest)) {
            Form<ContestSupervisorCreateForm> form = Form.form(ContestSupervisorCreateForm.class);

            ControllerUtils.getInstance().addActivityLog("Try to add supervisor in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showCreateSupervisor(form, contest);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateSupervisor(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToManageSupervisors(contest)) {
            Form<ContestSupervisorCreateForm> form = Form.form(ContestSupervisorCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateSupervisor(form, contest);
            } else {
                ContestSupervisorCreateForm contestSupervisorCreateForm = form.get();
                String userJid = JophielUtils.verifyUsername(contestSupervisorCreateForm.username);
                if ((userJid != null) && (!contestService.isContestSupervisorInContestByUserJid(contest.getJid(), userJid))) {
                    userRoleService.upsertUserFromJophielUserJid(userJid);
                    contestService.createContestSupervisor(contest.getId(), userJid, contestSupervisorCreateForm.announcement, contestSupervisorCreateForm.problem, contestSupervisorCreateForm.submission, contestSupervisorCreateForm.clarification, contestSupervisorCreateForm.contestant);

                    ControllerUtils.getInstance().addActivityLog("Add " + contestSupervisorCreateForm.username + " as supervisor in contest " + contest.getName() + ".");

                    return redirect(routes.ContestSupervisorController.viewSupervisors(contest.getId()));
                } else {
                    form.reject("error.supervisor.create.userJid.invalid");
                    return showCreateSupervisor(form, contest);
                }
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateSupervisor(long contestId, long contestSupervisorId) {
        Contest contest = contestService.findContestById(contestId);
        ContestSupervisor contestSupervisor = contestService.findContestSupervisorByContestSupervisorId(contestSupervisorId);
        if (isAllowedToManageSupervisors(contest) && contestSupervisor.getContestJid().equals(contest.getJid())) {
            ContestSupervisorUpdateForm contestSupervisorUpdateForm = new ContestSupervisorUpdateForm(contestSupervisor);
            Form<ContestSupervisorUpdateForm> form = Form.form(ContestSupervisorUpdateForm.class).fill(contestSupervisorUpdateForm);

            ControllerUtils.getInstance().addActivityLog("Try to update supervisor " + contestSupervisor.getUserJid() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateSupervisor(form, contest, contestSupervisor);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateSupervisor(long contestId, long contestSupervisorId) {
        Contest contest = contestService.findContestById(contestId);
        ContestSupervisor contestSupervisor = contestService.findContestSupervisorByContestSupervisorId(contestSupervisorId);
        if (isAllowedToManageSupervisors(contest) && contestSupervisor.getContestJid().equals(contest.getJid())) {
            Form<ContestSupervisorUpdateForm> form = Form.form(ContestSupervisorUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateSupervisor(form, contest, contestSupervisor);
            } else {
                ContestSupervisorUpdateForm contestSupervisorUpdateForm = form.get();
                contestService.updateContestSupervisor(contestSupervisor.getId(), contestSupervisorUpdateForm.announcement, contestSupervisorUpdateForm.problem, contestSupervisorUpdateForm.submission, contestSupervisorUpdateForm.clarification, contestSupervisorUpdateForm.contestant);

                ControllerUtils.getInstance().addActivityLog("Update supervisor " + contestSupervisor.getUserJid() + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestSupervisorController.viewSupervisors(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    private Result showCreateSupervisor(Form<ContestSupervisorCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createSupervisorView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("supervisor.create"), c));
        appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestSupervisorController.viewSupervisors(contest.getId())),
                new InternalLink(Messages.get("supervisor.create"), routes.ContestSupervisorController.createSupervisor(contest.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Supervisor - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateSupervisor(Form<ContestSupervisorUpdateForm> form, Contest contest, ContestSupervisor contestSupervisor){
        LazyHtml content = new LazyHtml(updateSupervisorView.render(contest.getId(), contestSupervisor.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("supervisor.update"), c));
        appendTabsLayout(content, contest);
        ControllerUtils.getInstance(). appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestSupervisorController.viewSupervisors(contest.getId())),
                new InternalLink(Messages.get("supervisor.update"), routes.ContestSupervisorController.updateSupervisor(contest.getId(), contestSupervisor.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Supervisor - Update");

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

    private boolean isAdmin() {
        return UrielUtils.hasRole("admin");
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

    private boolean isAllowedToManageSupervisors(Contest contest) {
        return isAdmin() || isManager(contest);
    }

    private boolean isAllowedToEnterContest(Contest contest) {
        if (isAdmin() || isManager(contest) || isSupervisor(contest)) {
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
        return isAdmin() || isManager(contest) || (isSupervisor(contest));
    }

    private Result tryEnteringContest(Contest contest) {
        if (isAllowedToEnterContest(contest)) {
            return redirect(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()));
        } else {
            return redirect(routes.ContestController.viewContest(contest.getId()));
        }
    }
}
