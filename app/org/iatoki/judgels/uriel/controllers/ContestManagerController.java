package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.jophiel.commons.JophielUtils;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestManager;
import org.iatoki.judgels.uriel.ContestManagerCreateForm;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.UserService;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.Authorized;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.manager.listCreateManagersView;
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
public class ContestManagerController extends Controller {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;
    private final UserService userRoleService;

    public ContestManagerController(ContestService contestService, UserService userRoleService) {
        this.contestService = contestService;
        this.userRoleService = userRoleService;
    }

    @AddCSRFToken
    public Result viewManagers(long contestId) {
        return listCreateManagers(contestId, 0, "id", "asc", "");
    }

    @AddCSRFToken
    public Result listCreateManagers(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);

        if (isSupervisorOrAbove(contest)) {
            Page<ContestManager> contestManagers = contestService.pageContestManagersByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = ControllerUtils.getInstance().isAdmin();

            Form<ContestManagerCreateForm> form = Form.form(ContestManagerCreateForm.class);

            return showListCreateManager(contestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @Authorized(value = {"admin"})
    @RequireCSRFCheck
    public Result postCreateManager(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        Form<ContestManagerCreateForm> form = Form.form(ContestManagerCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            Page<ContestManager> contestManagers = contestService.pageContestManagersByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = ControllerUtils.getInstance().isAdmin();

            return showListCreateManager(contestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
        } else {
            ContestManagerCreateForm contestManagerCreateForm = form.get();
            String userJid = JophielUtils.verifyUsername(contestManagerCreateForm.username);
            if ((userJid != null) && (!contestService.isContestManagerInContestByUserJid(contest.getJid(), userJid))) {
                userRoleService.upsertUserFromJophielUserJid(userJid);
                contestService.createContestManager(contest.getId(), userJid);

                ControllerUtils.getInstance().addActivityLog("Add manager " + contestManagerCreateForm.username + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestManagerController.viewManagers(contest.getId()));
            } else {
                form.reject("error.manager.create.userJid.invalid");

                Page<ContestManager> contestManagers = contestService.pageContestManagersByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

                boolean canUpdate = ControllerUtils.getInstance().isAdmin();

                return showListCreateManager(contestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
            }
        }
    }

    private Result showListCreateManager(Page<ContestManager> contestManagers, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, Form<ContestManagerCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(listCreateManagersView.render(contest.getId(), contestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("manager.list"), c));
        appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
              new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
              new InternalLink(Messages.get("manager.managers"), routes.ContestManagerController.viewManagers(contest.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Managers");

        ControllerUtils.getInstance().addActivityLog("Open list of managers in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

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

    private Result tryEnteringContest(Contest contest) {
        if (isAllowedToEnterContest(contest)) {
            return redirect(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()));
        } else {
            return redirect(routes.ContestController.viewContest(contest.getId()));
        }
    }
}
