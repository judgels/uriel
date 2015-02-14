package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.accessTypesLayout;
import org.iatoki.judgels.commons.views.html.layouts.baseLayout;
import org.iatoki.judgels.commons.views.html.layouts.breadcrumbsLayout;
import org.iatoki.judgels.commons.views.html.layouts.headerFooterLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.leftSidebarLayout;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.gabriel.GradingSource;
import org.iatoki.judgels.jophiel.commons.JophielUtils;
import org.iatoki.judgels.sandalphon.commons.SandalphonUtils;
import org.iatoki.judgels.sandalphon.commons.SubmissionAdapters;
import org.iatoki.judgels.sandalphon.commons.SubmissionException;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestAnnouncement;
import org.iatoki.judgels.uriel.ContestAnnouncementStatus;
import org.iatoki.judgels.uriel.ContestAnnouncementUpsertForm;
import org.iatoki.judgels.uriel.ContestClarification;
import org.iatoki.judgels.uriel.ContestClarificationCreateForm;
import org.iatoki.judgels.uriel.ContestClarificationStatus;
import org.iatoki.judgels.uriel.ContestClarificationUpdateForm;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantCreateForm;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestContestantUpdateForm;
import org.iatoki.judgels.uriel.ContestManager;
import org.iatoki.judgels.uriel.ContestManagerCreateForm;
import org.iatoki.judgels.uriel.ContestSupervisor;
import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.ContestProblemCreateForm;
import org.iatoki.judgels.uriel.ContestProblemStatus;
import org.iatoki.judgels.uriel.ContestProblemUpdateForm;
import org.iatoki.judgels.uriel.ContestScope;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.ContestSubmission;
import org.iatoki.judgels.uriel.ContestSubmissionService;
import org.iatoki.judgels.uriel.ContestSupervisorCreateForm;
import org.iatoki.judgels.uriel.ContestSupervisorUpdateForm;
import org.iatoki.judgels.uriel.ContestType;
import org.iatoki.judgels.uriel.ContestUpsertForm;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.UserRoleService;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.Authorized;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;

import org.iatoki.judgels.uriel.views.html.contest.admin.general.createAdminView;

import org.iatoki.judgels.uriel.views.html.contest.admin.manager.createAdminManagerView;
import org.iatoki.judgels.uriel.views.html.contest.admin.manager.listAdminManagersView;

import org.iatoki.judgels.uriel.views.html.contest.contestant.announcement.listContestantAnnouncementsView;

import org.iatoki.judgels.uriel.views.html.contest.contestant.clarification.createContestantClarificationView;
import org.iatoki.judgels.uriel.views.html.contest.contestant.clarification.listContestantClarificationsView;

import org.iatoki.judgels.uriel.views.html.contest.contestant.problem.listContestantProblemsView;
import org.iatoki.judgels.uriel.views.html.contest.contestant.problem.viewContestantProblemView;

import org.iatoki.judgels.uriel.views.html.contest.contestant.submission.listContestantSubmissionsView;

import org.iatoki.judgels.uriel.views.html.contest.manager.general.updateManagerGeneralView;

import org.iatoki.judgels.uriel.views.html.contest.manager.supervisor.createManagerSupervisorView;
import org.iatoki.judgels.uriel.views.html.contest.manager.supervisor.listManagerSupervisorsView;
import org.iatoki.judgels.uriel.views.html.contest.manager.supervisor.updateManagerSupervisorView;

import org.iatoki.judgels.uriel.views.html.contest.supervisor.announcement.createSupervisorAnnouncementView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.announcement.listSupervisorAnnouncementsView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.announcement.updateSupervisorAnnouncementView;

import org.iatoki.judgels.uriel.views.html.contest.supervisor.clarification.listSupervisorClarificationsView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.clarification.updateSupervisorClarificationView;

import org.iatoki.judgels.uriel.views.html.contest.supervisor.contestant.createSupervisorContestantView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.contestant.listSupervisorContestantsView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.contestant.updateSupervisorContestantView;

import org.iatoki.judgels.uriel.views.html.contest.supervisor.problem.createSupervisorProblemView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.problem.listSupervisorProblemsView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.problem.updateSupervisorProblemView;

import org.iatoki.judgels.uriel.views.html.contest.contestTimeLayout;
import org.iatoki.judgels.uriel.views.html.contest.listView;
import org.iatoki.judgels.uriel.views.html.contest.viewView;

import play.Play;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Date;
import java.util.List;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class ContestController extends Controller {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;
    private final UserRoleService userRoleService;
    private final ContestSubmissionService submissionService;

    public ContestController(ContestService contestService, UserRoleService userRoleService, ContestSubmissionService submissionService) {
        this.contestService = contestService;
        this.userRoleService = userRoleService;
        this.submissionService = submissionService;
    }

    /* list ********************************************************************************************************* */

    public Result index() {
        return list(0, "id", "asc", "");
    }


    /* view ********************************************************************************************************* */

    public Result view(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        Page<ContestContestant> contestContestants = contestService.pageContestContestantsByContestJid(contest.getJid(), 0, PAGE_SIZE, orderBy, orderDir, filterString);

        boolean isRegistered = contestService.isContestContestantInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
        boolean isAdmin = UrielUtils.hasRole("admin");
        boolean isAllowedToEnter = true;

        LazyHtml content = new LazyHtml(viewView.render(contest, contestContestants, pageIndex, orderBy, orderDir, filterString, isRegistered, isAdmin, isAllowedToEnter));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.contest") + " #" + contest.getId() + ": " + contest.getName(), c));

        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index())
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }


    /* register ******************************************************************************************************* */
    public Result register(long contestId) {
        Contest contest = contestService.findContestById(contestId);

        if (!contestService.isContestContestantInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid())) {
            contestService.createContestContestant(contest.getId(), IdentityUtils.getUserJid(), ContestContestantStatus.APPROVED);
        }

        return redirect(routes.ContestController.index());
    }

    /* admin/create ************************************************************************************************* */

    @Authorized(value = {"admin"})
    @AddCSRFToken
    public Result create() {
        Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class);

        return showCreate(form);
    }

    @Authorized(value = {"admin"})
    @RequireCSRFCheck
    public Result postCreate() {
        Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreate(form);
        } else {
            ContestUpsertForm contestUpsertForm = form.get();
            contestService.createContest(contestUpsertForm.name, contestUpsertForm.description, ContestType.valueOf(contestUpsertForm.type), ContestScope.valueOf(contestUpsertForm.scope), ContestStyle.valueOf(contestUpsertForm.style), UrielUtils.convertStringToDate(contestUpsertForm.startTime), UrielUtils.convertStringToDate(contestUpsertForm.endTime));

            return redirect(routes.ContestController.index());
        }
    }


    /* admin/manager ************************************************************************************************ */

    @Authorized(value = {"admin"})
    public Result viewAdminManagers(long contestId) {
        return listAdminManagers(contestId, 0, "id", "asc", "");
    }

    @Authorized(value = {"admin"})
    public Result listAdminManagers(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        Page<ContestManager> contestManager = contestService.pageContestManagersByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listAdminManagersView.render(contest.getId(), contestManager, pageIndex, orderBy, orderDir, filterString));

        content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("manager.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createAdminManager(contestId)), c));
        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("manager.managers"), routes.ContestController.viewAdminManagers(contestId))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }


    @Authorized(value = {"admin"})
    @AddCSRFToken
    public Result createAdminManager(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        Form<ContestManagerCreateForm> form = Form.form(ContestManagerCreateForm.class);

        return showCreateAdminManager(form, contest);
    }

    @Authorized(value = {"admin"})
    @RequireCSRFCheck
    public Result postCreateAdminManager(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        Form<ContestManagerCreateForm> form = Form.form(ContestManagerCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateAdminManager(form, contest);
        } else {
            ContestManagerCreateForm contestManagerCreateForm = form.get();
            if ((JophielUtils.verifyUserJid(contestManagerCreateForm.userJid)) && (!contestService.isContestManagerInContestByUserJid(contest.getJid(), contestManagerCreateForm.userJid))) {
                userRoleService.upsertUserRoleFromJophielUserJid(contestManagerCreateForm.userJid);
                contestService.createContestManager(contest.getId(), contestManagerCreateForm.userJid);

                return redirect(routes.ContestController.viewAdminManagers(contest.getId()));
            } else {
                form.reject("error.manager.create.userJid.invalid");
                return showCreateAdminManager(form, contest);
            }
        }
    }

    /* manager/view ************************************************************************************************* */

    public Result viewManagerGeneral(long contestId) {
        return TODO;
    }


    /* manager/update *********************************************************************************************** */

    @AddCSRFToken
    public Result updateManagerGeneral(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        ContestUpsertForm contestUpsertForm = new ContestUpsertForm(contest);
        Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).fill(contestUpsertForm);

        return showUpdateManagerGeneral(form, contest);
    }

    @RequireCSRFCheck
    public Result postUpdateManagerGeneral(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return showUpdateManagerGeneral(form, contest);
        } else {
            ContestUpsertForm contestUpsertForm = form.get();
            contestService.updateContest(contest.getId(), contestUpsertForm.name, contestUpsertForm.description, ContestType.valueOf(contestUpsertForm.type), ContestScope.valueOf(contestUpsertForm.scope), ContestStyle.valueOf(contestUpsertForm.style), UrielUtils.convertStringToDate(contestUpsertForm.startTime), UrielUtils.convertStringToDate(contestUpsertForm.endTime));

            return redirect(routes.ContestController.viewManagerGeneral(contestId));
        }
    }


    /* manager/supervisor ************************************************************************************************* */

    public Result viewManagerSupervisors(long contestId) {
        return listManagerSupervisors(contestId, 0, "id", "asc", "");
    }

    public Result listManagerSupervisors(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "supervisor")) {
            Page<ContestSupervisor> contestPermissionPage = contestService.pageContestSupervisorsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            LazyHtml content = new LazyHtml(listManagerSupervisorsView.render(contest.getId(), contestPermissionPage, pageIndex, orderBy, orderDir, filterString));

            content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("supervisor.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createManagerSupervisor(contestId)), c));
            appendTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contestId)),
                    new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestController.viewManagerSupervisors(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }

    @AddCSRFToken
    public Result createManagerSupervisor(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "supervisor")) {
            Form<ContestSupervisorCreateForm> form = Form.form(ContestSupervisorCreateForm.class);

            return showCreateManagerSupervisor(form, contest);
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postCreateManagerSupervisor(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "supervisor")) {
            Form<ContestSupervisorCreateForm> form = Form.form(ContestSupervisorCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateManagerSupervisor(form, contest);
            } else {
                ContestSupervisorCreateForm contestSupervisorCreateForm = form.get();
                if ((JophielUtils.verifyUserJid(contestSupervisorCreateForm.userJid)) && (!contestService.isContestSupervisorInContestByUserJid(contest.getJid(), contestSupervisorCreateForm.userJid))) {
                    userRoleService.upsertUserRoleFromJophielUserJid(contestSupervisorCreateForm.userJid);
                    contestService.createContestSupervisor(contest.getId(), contestSupervisorCreateForm.userJid, contestSupervisorCreateForm.announcement, contestSupervisorCreateForm.problem, contestSupervisorCreateForm.submission, contestSupervisorCreateForm.clarification, contestSupervisorCreateForm.contestant);

                    return redirect(routes.ContestController.viewManagerSupervisors(contest.getId()));
                } else {
                    form.reject("error.supervisor.create.userJid.invalid");
                    return showCreateManagerSupervisor(form, contest);
                }
            }
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }

    @AddCSRFToken
    public Result updateManagerSupervisor(long contestId, long contestSupervisorId) {
        Contest contest = contestService.findContestById(contestId);
        ContestSupervisor contestSupervisor = contestService.findContestSupervisrByContestSupervisorId(contestSupervisorId);
        if ((checkIfPermitted(contest, "supervisor")) && (contestSupervisor.getContestJid().equals(contest.getJid()))) {
            ContestSupervisorUpdateForm contestSupervisorUpdateForm = new ContestSupervisorUpdateForm(contestSupervisor);
            Form<ContestSupervisorUpdateForm> form = Form.form(ContestSupervisorUpdateForm.class).fill(contestSupervisorUpdateForm);

            return showUpdateManagerSupervisor(form, contest, contestSupervisor);
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postUpdateManagerSupervisor(long contestId, long contestSupervisorId) {
        Contest contest = contestService.findContestById(contestId);
        ContestSupervisor contestSupervisor = contestService.findContestSupervisrByContestSupervisorId(contestSupervisorId);
        if ((checkIfPermitted(contest, "supervisor")) && (contestSupervisor.getContestJid().equals(contest.getJid()))) {
            Form<ContestSupervisorUpdateForm> form = Form.form(ContestSupervisorUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateManagerSupervisor(form, contest, contestSupervisor);
            } else {
                ContestSupervisorUpdateForm contestSupervisorUpdateForm = form.get();
                contestService.updateContestSupervisor(contestSupervisor.getId(), contestSupervisorUpdateForm.announcement, contestSupervisorUpdateForm.problem, contestSupervisorUpdateForm.submission, contestSupervisorUpdateForm.clarification, contestSupervisorUpdateForm.contestant);

                return redirect(routes.ContestController.viewManagerSupervisors(contest.getId()));
            }
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }


    /* supervisor/announcement ************************************************************************************** */

    public Result viewSupervisorAnnouncements(long contestId) {
        return listSupervisorAnnouncements(contestId, 0, "timeUpdate", "desc", "");
    }

    public Result listSupervisorAnnouncements(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "announcement")) {
            Page<ContestAnnouncement> contestAnnouncements = contestService.pageContestAnnouncementsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, null);

            LazyHtml content = new LazyHtml(listSupervisorAnnouncementsView.render(contest.getId(), contestAnnouncements, pageIndex, orderBy, orderDir, filterString));

            content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("announcement.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createSupervisorAnnouncement(contest.getId())), c));
            content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantAnnouncements(contest.getId()), routes.ContestController.viewSupervisorAnnouncements(contest.getId()), c));
            appendTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                    new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.viewSupervisorAnnouncements(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }

    @AddCSRFToken
    public Result createSupervisorAnnouncement(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "announcement")) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class);

            return showCreateSupervisorAnnouncement(form, contest);
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postCreateSupervisorAnnouncement(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "announcement")) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateSupervisorAnnouncement(form, contest);
            } else {
                ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = form.get();
                contestService.createContestAnnouncement(contest.getId(), contestAnnouncementUpsertForm.title, contestAnnouncementUpsertForm.content, ContestAnnouncementStatus.valueOf(contestAnnouncementUpsertForm.status));

                return redirect(routes.ContestController.viewSupervisorAnnouncements(contest.getId()));
            }
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }

    @AddCSRFToken
    public Result updateSupervisorAnnouncement(long contestId, long contestAnnouncementId) {
        Contest contest = contestService.findContestById(contestId);
        ContestAnnouncement contestAnnouncement = contestService.findContestAnnouncementByContestAnnouncementId(contestAnnouncementId);
        if ((checkIfPermitted(contest, "announcement")) && (contestAnnouncement.getContestJid().equals(contest.getJid()))) {
            ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = new ContestAnnouncementUpsertForm(contestAnnouncement);
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).fill(contestAnnouncementUpsertForm);

            return showUpdateSupervisorAnnouncement(form, contest, contestAnnouncement);
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postUpdateSupervisorAnnouncement(long contestId, long contestAnnouncementId) {
        Contest contest = contestService.findContestById(contestId);
        ContestAnnouncement contestAnnouncement = contestService.findContestAnnouncementByContestAnnouncementId(contestAnnouncementId);
        if ((checkIfPermitted(contest, "announcement")) && (contestAnnouncement.getContestJid().equals(contest.getJid()))) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateSupervisorAnnouncement(form, contest, contestAnnouncement);
            } else {
                ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = form.get();
                contestService.updateContestAnnouncement(contestAnnouncement.getId(), contestAnnouncementUpsertForm.title, contestAnnouncementUpsertForm.content, ContestAnnouncementStatus.valueOf(contestAnnouncementUpsertForm.status));

                return redirect(routes.ContestController.viewSupervisorAnnouncements(contest.getId()));
            }
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }


    /* supervisor/problem ******************************************************************************************* */

    public Result viewSupervisorProblems(long contestId) {
        return listSupervisorProblems(contestId, 0, "alias", "asc", "");
    }


    public Result listSupervisorProblems(long contestId, long page, String sortBy, String orderBy, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "problem")) {
            Page<ContestProblem> contestProblemPage = contestService.pageContestProblemsByContestJid(contest.getJid(), page, PAGE_SIZE, sortBy, orderBy, filterString, null);

            LazyHtml content = new LazyHtml(listSupervisorProblemsView.render(contest.getId(), contestProblemPage, page, sortBy, orderBy, filterString));

            content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("problem.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createSupervisorProblem(contestId)), c));
            content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantProblems(contest.getId()), routes.ContestController.viewSupervisorProblems(contest.getId()), c));
            appendTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                    new InternalLink("problem.problems", routes.ContestController.viewSupervisorProblems(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return redirect(routes.ContestController.viewContestantProblems(contest.getId()));
        }
    }

    @AddCSRFToken
    public Result createSupervisorProblem(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "problem")) {
            Form<ContestProblemCreateForm> form = Form.form(ContestProblemCreateForm.class);

            return showCreateSupervisorProblem(form, contest);
        } else {
            return redirect(routes.ContestController.viewContestantProblems(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postCreateSupervisorProblem(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "problem")) {
            Form<ContestProblemCreateForm> form = Form.form(ContestProblemCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateSupervisorProblem(form, contest);
            } else {
                ContestProblemCreateForm contestProblemCreateForm = form.get();
                if ((SandalphonUtils.verifyProblemJid(contestProblemCreateForm.problemJid)) && (!contestService.isContestProblemInContestByProblemJid(contest.getJid(), contestProblemCreateForm.problemJid))) {
                    contestService.createContestProblem(contest.getId(), contestProblemCreateForm.problemJid, contestProblemCreateForm.problemSecret, contestProblemCreateForm.alias, contestProblemCreateForm.submissionsLimit, ContestProblemStatus.valueOf(contestProblemCreateForm.status));

                    return redirect(routes.ContestController.viewSupervisorProblems(contest.getId()));
                } else {
                    form.reject("error.problem.create.problemJid.invalid");
                    return showCreateSupervisorProblem(form, contest);
                }
            }
        } else {
            return redirect(routes.ContestController.viewContestantProblems(contest.getId()));
        }
    }

    @AddCSRFToken
    public Result updateSupervisorProblem(long contestId, long contestProblemId) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);
        if ((checkIfPermitted(contest, "problem")) && (contestProblem.getContestJid().equals(contest.getJid()))) {
            ContestProblemUpdateForm contestProblemUpdateForm = new ContestProblemUpdateForm(contestProblem);
            Form<ContestProblemUpdateForm> form = Form.form(ContestProblemUpdateForm.class).fill(contestProblemUpdateForm);

            return showUpdateSupervisorProblem(form, contest, contestProblem);
        } else {
            return redirect(routes.ContestController.viewContestantProblems(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postUpdateSupervisorProblem(long contestId, long contestProblemId) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);
        if ((checkIfPermitted(contest, "problem")) && (contestProblem.getContestJid().equals(contest.getJid()))) {
            Form<ContestProblemUpdateForm> form = Form.form(ContestProblemUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateSupervisorProblem(form, contest, contestProblem);
            } else {
                ContestProblemUpdateForm contestProblemUpdateForm = form.get();
                contestService.updateContestProblem(contestProblem.getId(), contestProblemUpdateForm.problemSecret, contestProblemUpdateForm.alias, contestProblemUpdateForm.submissionsLimit, ContestProblemStatus.valueOf(contestProblemUpdateForm.status));

                return redirect(routes.ContestController.viewSupervisorProblems(contest.getId()));
            }
        } else {
            return redirect(routes.ContestController.viewContestantProblems(contest.getId()));
        }
    }


    /* supervisor/clarification ************************************************************************************* */

    public Result viewSupervisorClarifications(long contestId) {
        return listSupervisorClarifications(contestId, 0, "timeCreate", "desc", "");
    }

    public Result listSupervisorClarifications(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "clarification")) {
            Page<ContestClarification> contestClarifications = contestService.pageContestClarificationsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, null);

            LazyHtml content = new LazyHtml(listSupervisorClarificationsView.render(contest.getId(), contestClarifications, pageIndex, orderBy, orderDir, filterString));

            content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.list"), c));
            content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantClarifications(contest.getId()), routes.ContestController.viewSupervisorClarifications(contest.getId()), c));
            appendTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                    new InternalLink("clarification.clarifications", routes.ContestController.viewSupervisorClarifications(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return redirect(routes.ContestController.viewContestantClarifications(contest.getId()));
        }
    }

    @AddCSRFToken
    public Result updateSupervisorClarification(long contestId, long contestClarificationId) {
        Contest contest = contestService.findContestById(contestId);
        ContestClarification contestClarification = contestService.findContestClarificationByContestClarificationId(contestClarificationId);
        if ((checkIfPermitted(contest, "clarification")) && (contestClarification.getContestJid().equals(contest.getJid()))) {
            ContestClarificationUpdateForm contestClarificationUpsertForm = new ContestClarificationUpdateForm(contestClarification);
            Form<ContestClarificationUpdateForm> form = Form.form(ContestClarificationUpdateForm.class).fill(contestClarificationUpsertForm);

            return showUpdateSupervisorClarification(form, contest, contestClarification);
        } else {
            return redirect(routes.ContestController.viewContestantClarifications(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postUpdateSupervisorClarification(long contestId, long contestClarificationId) {
        Contest contest = contestService.findContestById(contestId);
        ContestClarification contestClarification = contestService.findContestClarificationByContestClarificationId(contestClarificationId);
        if ((checkIfPermitted(contest, "clarification")) && (contestClarification.getContestJid().equals(contest.getJid()))) {
            Form<ContestClarificationUpdateForm> form = Form.form(ContestClarificationUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateSupervisorClarification(form, contest, contestClarification);
            } else {
                ContestClarificationUpdateForm contestClarificationUpdateForm = form.get();
                contestService.updateContestClarification(contestClarification.getId(), contestClarificationUpdateForm.answer, ContestClarificationStatus.valueOf(contestClarificationUpdateForm.status));

                return redirect(routes.ContestController.viewSupervisorClarifications(contest.getId()));
            }
        } else {
            return redirect(routes.ContestController.viewContestantClarifications(contest.getId()));
        }
    }


    /* supervisor/contestant **************************************************************************************** */

    public Result viewSupervisorContestants(long contestId) {
        return listSupervisorContestants(contestId, 0, "id", "asc", "");
    }

    public Result listSupervisorContestants(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contestant")) {
            Page<ContestContestant> contestContestants = contestService.pageContestContestantsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            LazyHtml content = new LazyHtml(listSupervisorContestantsView.render(contest.getId(), contestContestants, pageIndex, orderBy, orderDir, filterString));

            content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("contestant.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createSupervisorContestant(contestId)), c));
            appendTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                    new InternalLink("contestant.contestants", routes.ContestController.viewSupervisorContestants(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }

    @AddCSRFToken
    public Result createSupervisorContestant(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contestant")) {
            Form<ContestContestantCreateForm> form = Form.form(ContestContestantCreateForm.class);

            return showCreateSupervisorContestant(form, contest);
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postCreateSupervisorContestant(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contestant")) {
            Form<ContestContestantCreateForm> form = Form.form(ContestContestantCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateSupervisorContestant(form, contest);
            } else {
                ContestContestantCreateForm contestContestantCreateForm = form.get();
                if ((JophielUtils.verifyUserJid(contestContestantCreateForm.userJid)) && (!contestService.isContestContestantInContestByUserJid(contest.getJid(), contestContestantCreateForm.userJid))) {
                    userRoleService.upsertUserRoleFromJophielUserJid(contestContestantCreateForm.userJid);
                    contestService.createContestContestant(contest.getId(), contestContestantCreateForm.userJid, ContestContestantStatus.valueOf(contestContestantCreateForm.status));

                    return redirect(routes.ContestController.viewSupervisorContestants(contest.getId()));
                } else {
                    form.reject("error.contestant.create.userJid.invalid");
                    return showCreateSupervisorContestant(form, contest);
                }
            }
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }

    @AddCSRFToken
    public Result updateSupervisorContestant(long contestId, long contestContestantId) {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestService.findContestContestantByContestContestantId(contestContestantId);
        if ((checkIfPermitted(contest, "contestant")) && (contestContestant.getContestJid().equals(contest.getJid()))) {
            ContestContestantUpdateForm contestContestantUpsertForm = new ContestContestantUpdateForm(contestContestant);
            Form<ContestContestantUpdateForm> form = Form.form(ContestContestantUpdateForm.class).fill(contestContestantUpsertForm);

            return showUpdateSupervisorContestant(form, contest, contestContestant);
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postUpdateSupervisorContestant(long contestId, long contestContestantId) {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestService.findContestContestantByContestContestantId(contestContestantId);
        if ((checkIfPermitted(contest, "contestant")) && (contestContestant.getContestJid().equals(contest.getJid()))) {
            Form<ContestContestantUpdateForm> form = Form.form(ContestContestantUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateSupervisorContestant(form, contest, contestContestant);
            } else {
                ContestContestantUpdateForm contestContestantUpdateForm = form.get();
                contestService.updateContestContestant(contestContestant.getId(), ContestContestantStatus.valueOf(contestContestantUpdateForm.status));

                return redirect(routes.ContestController.viewSupervisorContestants(contest.getId()));
            }
        } else {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        }
    }


    /* supervisor/submission **************************************************************************************** */

    public Result viewSupervisorSubmissions(long contestId) {
        return listSupervisorSubmissions(contestId, 0, "id", "asc", "");
    }

    public Result listSupervisorSubmissions(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        return TODO;
    }


    /* contestant/announcement *************************************************************************************** */

    public Result viewContestantAnnouncements(long contestId) {
        return listContestantAnnouncements(contestId, 0, "id", "asc", "");
    }

    public Result listContestantAnnouncements(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contest")) {
            Page<ContestAnnouncement> contestAnnouncements = contestService.pageContestAnnouncementsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, ContestAnnouncementStatus.PUBLISHED.name());

            LazyHtml content = new LazyHtml(listContestantAnnouncementsView.render(contest.getId(), contestAnnouncements, pageIndex, orderBy, orderDir, filterString));
            content.appendLayout(c -> heading3Layout.render(Messages.get("announcement.announcements"), c));

            if (checkIfPermitted(contest, "announcement")) {
                content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantAnnouncements(contest.getId()), routes.ContestController.viewSupervisorAnnouncements(contest.getId()), c));
            }

            appendTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                    new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.viewContestantAnnouncements(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return forbidden();
        }
    }

    /* contestant/problem *************************************************************************************** */

    public Result viewContestantProblems(long contestId) {
        return listContestantProblems(contestId, 0, "alias", "asc", "");
    }

    public Result listContestantProblems(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contest")) {
            Page<ContestProblem> contestProblems = contestService.pageContestProblemsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, ContestProblemStatus.OPEN.name());

            LazyHtml content = new LazyHtml(listContestantProblemsView.render(contest.getId(), contestProblems, pageIndex, orderBy, orderDir, filterString));
            content.appendLayout(c -> heading3Layout.render(Messages.get("problem.problems"), c));

            if (checkIfPermitted(contest, "problem")) {
                content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantProblems(contest.getId()), routes.ContestController.viewSupervisorProblems(contest.getId()), c));
            }
            appendTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                    new InternalLink(Messages.get("problem.problems"), routes.ContestController.viewContestantProblems(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return forbidden();
        }
    }

    public Result viewContestantProblem(long contestId, long contestProblemId) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);
        if ((checkIfPermitted(contest, "contest")) && (contestProblem.getContestJid().equals(contest.getJid()))) {
            int tOTPCode = SandalphonUtils.calculateTOTPCode(contestProblem.getProblemSecret(), System.currentTimeMillis());
            String requestUrl = SandalphonUtils.getTOTPEndpoint(contestProblem.getProblemJid(), tOTPCode, Play.langCookieName(), routes.ContestController.postSubmitContestantProblem(contestId, contestProblem.getProblemJid()).absoluteURL(request())).toString();
            LazyHtml content = new LazyHtml(viewContestantProblemView.render(requestUrl));

            appendTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                    new InternalLink(contestProblem.getAlias(), routes.ContestController.viewContestantProblem(contest.getId(), contestProblem.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return forbidden();
        }
    }

    public Result postSubmitContestantProblem(long contestId, String problemJid) {
        Contest contest = contestService.findContestById(contestId);

        Http.MultipartFormData body = request().body().asMultipartFormData();

        String gradingLanguage = body.asFormUrlEncoded().get("language")[0];
        String gradingEngine = body.asFormUrlEncoded().get("engine")[0];
        long gradingLastUpdateTime = Long.parseLong(body.asFormUrlEncoded().get("gradingLastUpdateTime")[0]);

        ContestProblem contestProblem = contestService.findContestProblemByContestJidAndContestProblemJid(contest.getJid(), problemJid);

        try {
            GradingSource source = SubmissionAdapters.fromGradingEngine(gradingEngine).createGradingSourceFromNewSubmission(body);
            String submissionJid = submissionService.submit(contest.getJid(),problemJid, gradingLanguage, gradingEngine, gradingLastUpdateTime, source);
            SubmissionAdapters.fromGradingEngine(gradingEngine).storeSubmissionFiles(UrielProperties.getInstance().getSubmissionDir(), submissionJid, source);
        } catch (SubmissionException e) {
            flash("submissionError", e.getMessage());

            return redirect(routes.ContestController.viewContestantProblem(contestId, contestProblem.getId()));
        }

        return redirect(routes.ContestController.viewContestantSubmissions(contestId));
    }


    /* contestant/submission **************************************************************************************** */

    public Result viewContestantSubmissions(long contestId) {
        return listContestantSubmissions(contestId, 0, "id", "desc", "");
    }

    public Result listContestantSubmissions(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);

        Page<ContestSubmission> submissions = submissionService.pageContestSubmissionsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, IdentityUtils.getUserJid(), null);

        LazyHtml content = new LazyHtml(listContestantSubmissionsView.render(contestId, submissions, pageIndex, orderBy, orderDir, filterString));

        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));

        if (checkIfPermitted(contest, "submission")) {
            content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantSubmissions(contest.getId()), routes.ContestController.viewSupervisorSubmissions(contest.getId()), c));
        }

        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("submission.submissions"), routes.ContestController.viewContestantSubmissions(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    public Result viewContestantSubmission(long contestId, long submissionId) {
        Contest contest = contestService.findContestById(contestId);
        ContestSubmission submission = submissionService.findContestSubmissionById(contest.getJid(), submissionId);

        GradingSource source = SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).createGradingSourceFromPastSubmission(UrielProperties.getInstance().getSubmissionDir(), submission.getJid());

        LazyHtml content = new LazyHtml(SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).renderViewSubmission(submission, source));

        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("submission.view"), routes.ContestController.viewContestantSubmission(contest.getId(), submission.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    /* contestant/clarification ************************************************************************************* */

    public Result viewContestantClarifications(long contestId) {
        return listContestantClarifications(contestId, 0, "timeCreate", "desc", "");
    }

    public Result listContestantClarifications(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contest")) {
            Page<ContestClarification> contestClarifications = contestService.pageContestClarificationsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, IdentityUtils.getUserJid());

            LazyHtml content = new LazyHtml(listContestantClarificationsView.render(contest.getId(), contestClarifications, pageIndex, orderBy, orderDir, filterString));

            content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("clarification.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createContestantClarification(contest.getId())), c));
            if (checkIfPermitted(contest, "clarification")) {
                content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantClarifications(contest.getId()), routes.ContestController.viewSupervisorClarifications(contest.getId()), c));
            }
            appendTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                    new InternalLink(Messages.get("clarification.clarifications"), routes.ContestController.viewContestantClarifications(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return forbidden();
        }
    }

    @AddCSRFToken
    public Result createContestantClarification(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        Form<ContestClarificationCreateForm> form = Form.form(ContestClarificationCreateForm.class);

        return showCreateContestantClarification(form, contest);
    }

    @RequireCSRFCheck
    public Result postCreateContestantClarification(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        Form<ContestClarificationCreateForm> form = Form.form(ContestClarificationCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateContestantClarification(form, contest);
        } else {
            ContestClarificationCreateForm contestClarificationCreateForm = form.get();
            contestService.createContestClarification(contest.getId(), contestClarificationCreateForm.title, contestClarificationCreateForm.question, contestClarificationCreateForm.topicJid);

            return redirect(routes.ContestController.viewContestantClarifications(contest.getId()));
        }
    }


    /* list ********************************************************************************************************* */

    public Result list(long pageIndex, String orderBy, String orderDir, String filterString) {
        Page<Contest> contests = contestService.pageContests(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listView.render(contests, pageIndex, orderBy, orderDir, filterString));
        if (UrielUtils.hasRole("admin")) {
            content.appendLayout(c -> headingWithActionLayout.render(Messages.get("contest.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.create()), c));
        } else {
            content.appendLayout(c -> headingLayout.render(Messages.get("contest.list"), c));
        }
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index())
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private boolean checkIfPermitted(Contest contest, String permission) {
        switch (permission) {
            case "contest": {
                return (UrielUtils.hasRole("admin") || contestService.isContestContestantInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid()) || contestService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid()));
            }
            case "announcement": {
                if (UrielUtils.hasRole("admin")) {
                    return true;
                } else if (contestService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid())) {
                    ContestSupervisor contestSupervisor = contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid());
                    return contestSupervisor.isAnnouncement();
                } else {
                    return contestService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
                }
            }
            case "problem": {
                if (UrielUtils.hasRole("admin")) {
                    return true;
                } else if (contestService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid())) {
                    ContestSupervisor contestSupervisor = contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid());
                    return contestSupervisor.isProblem();
                } else {
                    return contestService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
                }
            }
            case "submission": {
                if (UrielUtils.hasRole("admin")) {
                    return true;
                } else if (contestService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid())) {
                    ContestSupervisor contestSupervisor = contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid());
                    return contestSupervisor.isSubmission();
                } else {
                    return contestService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
                }
            }
            case "clarification": {
                if (UrielUtils.hasRole("admin")) {
                    return true;
                } else if (contestService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid())) {
                    ContestSupervisor contestSupervisor = contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid());
                    return contestSupervisor.isClarification();
                } else {
                    return contestService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
                }
            }
            case "contestant": {
                if (UrielUtils.hasRole("admin")) {
                    return true;
                } else if (contestService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid())) {
                    ContestSupervisor contestSupervisor = contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid());
                    return contestSupervisor.isContestant();
                } else {
                    return contestService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
                }
            }
            case "supervisor": {
                return (UrielUtils.hasRole("admin") || contestService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid()));
            }
            case "manager": {
                return UrielUtils.hasRole("admin");
            }
            default: {
                return false;
            }
        }
    }

    private Result showCreate(Form<ContestUpsertForm> form) {
        LazyHtml content = new LazyHtml(createAdminView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.create"), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(Messages.get("contest.create"), routes.ContestController.create())
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }


    private Result showCreateAdminManager(Form<ContestManagerCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createAdminManagerView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("manager.create"), c));
        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink("manager.create", routes.ContestController.createAdminManager(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateManagerGeneral(Form<ContestUpsertForm> form, Contest contest) {
        LazyHtml content = new LazyHtml(updateManagerGeneralView.render(form, contest.getId()));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contest.update"), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("contest.update"), routes.ContestController.viewManagerGeneral(contest.getId()))
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    private Result showCreateManagerSupervisor(Form<ContestSupervisorCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createManagerSupervisorView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("supervisor.create"), c));
        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("supervisor.create"), routes.ContestController.createManagerSupervisor(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateManagerSupervisor(Form<ContestSupervisorUpdateForm> form, Contest contest, ContestSupervisor contestSupervisor){
        LazyHtml content = new LazyHtml(updateManagerSupervisorView.render(contest.getId(), contestSupervisor.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("supervisor.update"), c));
        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("supervisor.update"), routes.ContestController.viewManagerSupervisors(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showCreateSupervisorAnnouncement(Form<ContestAnnouncementUpsertForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createSupervisorAnnouncementView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("announcement.create"), c));
        content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantAnnouncements(contest.getId()), routes.ContestController.viewSupervisorAnnouncements(contest.getId()), c));
        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("announcement.create"), routes.ContestController.createSupervisorAnnouncement(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateSupervisorAnnouncement(Form<ContestAnnouncementUpsertForm> form, Contest contest, ContestAnnouncement contestAnnouncement){
        LazyHtml content = new LazyHtml(updateSupervisorAnnouncementView.render(contest.getId(), contestAnnouncement.getId(), form));
        content.appendLayout(c -> headingLayout.render(Messages.get("announcement.update"), c));
        content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantAnnouncements(contest.getId()), routes.ContestController.viewSupervisorAnnouncements(contest.getId()), c));
        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("announcement.update"), routes.ContestController.viewSupervisorAnnouncements(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showCreateSupervisorProblem(Form<ContestProblemCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createSupervisorProblemView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("problem.create"), c));
        content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantProblems(contest.getId()), routes.ContestController.viewSupervisorProblems(contest.getId()), c));
        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("problem.create"), routes.ContestController.createSupervisorProblem(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateSupervisorProblem(Form<ContestProblemUpdateForm> form, Contest contest, ContestProblem contestProblem){
        LazyHtml content = new LazyHtml(updateSupervisorProblemView.render(contest.getId(), contestProblem.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("problem.update"), c));
        content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantProblems(contest.getId()), routes.ContestController.viewSupervisorProblems(contest.getId()), c));
        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("problem.update"), routes.ContestController.viewSupervisorProblems(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateSupervisorClarification(Form<ContestClarificationUpdateForm> form, Contest contest, ContestClarification contestClarification){
        LazyHtml content = new LazyHtml(updateSupervisorClarificationView.render(contest.getId(), contestClarification, form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.update"), c));
        content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantClarifications(contest.getId()), routes.ContestController.viewSupervisorClarifications(contest.getId()), c));
        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("clarification.update"), routes.ContestController.viewSupervisorClarifications(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showCreateSupervisorContestant(Form<ContestContestantCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createSupervisorContestantView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.create"), c));
        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantProblems(contest.getId())),
                new InternalLink(Messages.get("contestant.create"), routes.ContestController.viewContestantAnnouncements(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateSupervisorContestant(Form<ContestContestantUpdateForm> form, Contest contest, ContestContestant contestContestant){
        LazyHtml content = new LazyHtml(updateSupervisorContestantView.render(contest.getId(), contestContestant.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.update"), c));
        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("contestant.update"), routes.ContestController.viewSupervisorContestants(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }


    private Result showCreateContestantClarification(Form<ContestClarificationCreateForm> form, Contest contest){
        List<ContestProblem> contestProblemList = contestService.findOpenedContestProblemByContestJid(contest.getJid());

        LazyHtml content = new LazyHtml(createContestantClarificationView.render(contest, form, contestProblemList));
        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.create"), c));
        if (checkIfPermitted(contest, "clarification")) {
            content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewContestantClarifications(contest.getId()), routes.ContestController.viewSupervisorClarifications(contest.getId()), c));
        }
        appendTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("clarification.create"), routes.ContestController.viewContestantAnnouncements(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }


    private void appendTabsLayout(LazyHtml content, Contest contest) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.viewContestantAnnouncements(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("problem.problems"), routes.ContestController.viewContestantProblems(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("submission.submissions"), routes.ContestController.viewContestantSubmissions(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("clarification.clarifications"), routes.ContestController.viewContestantClarifications(contest.getId())));

        if (checkIfPermitted(contest, "contestant")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())));
        }

        if (checkIfPermitted(contest, "supervisor")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestController.viewManagerSupervisors(contest.getId())));
        }

        if (checkIfPermitted(contest, "manager")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("manager.managers"), routes.ContestController.viewAdminManagers(contest.getId())));
        }

        content.appendLayout(c -> contestTimeLayout.render(contest.getEndTime(), c));
        content.appendLayout(c -> tabLayout.render(internalLinkBuilder.build(), c));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.contest") + " #" + contest.getId() + ": " + contest.getName(), c));
    }

    private void appendTemplateLayout(LazyHtml content) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()));

        if (UrielUtils.hasRole("admin")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("userRole.userRoles"), routes.UserRoleController.index()));
        }

        content.appendLayout(c -> leftSidebarLayout.render(
            IdentityUtils.getUsername(),
            IdentityUtils.getUserRealName(),
            org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.profile(routes.ContestController.index().absoluteURL(request())).absoluteURL(request()),
            org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.logout(routes.ApplicationController.index().absoluteURL(request())).absoluteURL(request()),
            internalLinkBuilder.build(), c)
        );
        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("TODO", c));
    }

    private Result lazyOk(LazyHtml content) {
        return getResult(content, Http.Status.OK);
    }

    private Result getResult(LazyHtml content, int statusCode) {
        switch (statusCode) {
            case Http.Status.OK:
                return ok(content.render(0));
            case Http.Status.NOT_FOUND:
                return notFound(content.render(0));
            default:
                return badRequest(content.render(0));
        }
    }

    private boolean isAdmin() {
        return UrielUtils.hasRole("admin");
    }

    private boolean isContestant(Contest contest) {
        return contestService.isContestContestantInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isSupervisor() {
        return true;
    }

    private boolean isContestStarted(Contest contest) {
        return new Date().compareTo(contest.getStartTime()) >= 0;
    }

    private boolean isContestEnded(Contest contest) {
        return new Date().compareTo(contest.getEndTime()) > 0;
    }

    private boolean isAllowedToEnterContest(Contest contest) {
        return isAdmin() || (isContestant(contest) && isContestStarted(contest));
    }

    private boolean isAllowedToDoContest(Contest contest) {
        return new Date().compareTo(contest.getEndTime()) < 0;
    }

    private boolean isAllowedToSuperviseAnnouncement(Contest contest) {
        return true;
    }
}
