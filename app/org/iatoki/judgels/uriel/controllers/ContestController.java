package org.iatoki.judgels.uriel.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.ListTableSelectionForm;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.accessTypesLayout;
import org.iatoki.judgels.commons.views.html.layouts.alertLayout;
import org.iatoki.judgels.commons.views.html.layouts.baseLayout;
import org.iatoki.judgels.commons.views.html.layouts.breadcrumbsLayout;
import org.iatoki.judgels.commons.views.html.layouts.headerFooterLayout;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.commons.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.heading3WithActionsLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.sidebarLayout;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.gabriel.GradingLanguageRegistry;
import org.iatoki.judgels.gabriel.GradingSource;
import org.iatoki.judgels.gabriel.commons.GabrielUtils;
import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.gabriel.commons.SubmissionAdapters;
import org.iatoki.judgels.gabriel.commons.SubmissionException;
import org.iatoki.judgels.gabriel.commons.SubmissionService;
import org.iatoki.judgels.jophiel.commons.JophielUtils;
import org.iatoki.judgels.sandalphon.commons.SandalphonUtils;
import org.iatoki.judgels.sandalphon.commons.programming.LanguageRestrictionAdapter;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestAnnouncement;
import org.iatoki.judgels.uriel.ContestAnnouncementStatus;
import org.iatoki.judgels.uriel.ContestAnnouncementUpsertForm;
import org.iatoki.judgels.uriel.ContestClarification;
import org.iatoki.judgels.uriel.ContestClarificationChangeForm;
import org.iatoki.judgels.uriel.ContestClarificationCreateForm;
import org.iatoki.judgels.uriel.ContestClarificationStatus;
import org.iatoki.judgels.uriel.ContestClarificationUpdateForm;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantCreateForm;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestContestantUpdateForm;
import org.iatoki.judgels.uriel.ContestContestantUploadForm;
import org.iatoki.judgels.uriel.ContestManager;
import org.iatoki.judgels.uriel.ContestManagerCreateForm;
import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.ContestProblemCreateForm;
import org.iatoki.judgels.uriel.ContestProblemStatus;
import org.iatoki.judgels.uriel.ContestProblemUpdateForm;
import org.iatoki.judgels.uriel.ContestScope;
import org.iatoki.judgels.uriel.ContestScopeConfig;
import org.iatoki.judgels.uriel.ContestScopeConfigPrivate;
import org.iatoki.judgels.uriel.ContestScopeConfigPrivateForm;
import org.iatoki.judgels.uriel.ContestScopeConfigPublic;
import org.iatoki.judgels.uriel.ContestScopeConfigPublicForm;
import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.ContestStyleConfig;
import org.iatoki.judgels.uriel.ContestStyleConfigICPC;
import org.iatoki.judgels.uriel.ContestStyleConfigICPCForm;
import org.iatoki.judgels.uriel.ContestStyleConfigIOI;
import org.iatoki.judgels.uriel.ContestStyleConfigIOIForm;
import org.iatoki.judgels.uriel.ContestSupervisor;
import org.iatoki.judgels.uriel.ContestSupervisorCreateForm;
import org.iatoki.judgels.uriel.ContestSupervisorUpdateForm;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.ContestTeamCoach;
import org.iatoki.judgels.uriel.ContestTeamCoachCreateForm;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.ContestTeamMemberCreateForm;
import org.iatoki.judgels.uriel.ContestTeamUpsertForm;
import org.iatoki.judgels.uriel.ContestType;
import org.iatoki.judgels.uriel.ContestTypeConfig;
import org.iatoki.judgels.uriel.ContestTypeConfigStandard;
import org.iatoki.judgels.uriel.ContestTypeConfigStandardForm;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualForm;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.ContestUpsertForm;
import org.iatoki.judgels.uriel.JidCacheService;
import org.iatoki.judgels.uriel.ScoreAdapter;
import org.iatoki.judgels.uriel.ScoreAdapters;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.UserRoleService;
import org.iatoki.judgels.uriel.commons.ContestScoreState;
import org.iatoki.judgels.uriel.commons.IOIScoreboardContent;
import org.iatoki.judgels.uriel.commons.IOIScoreboardEntry;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.Authorized;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.admin.general.createAdminView;
import org.iatoki.judgels.uriel.views.html.contest.admin.manager.createAdminManagerView;
import org.iatoki.judgels.uriel.views.html.contest.admin.manager.listAdminManagersView;
import org.iatoki.judgels.uriel.views.html.contest.contestTimeLayout;
import org.iatoki.judgels.uriel.views.html.contest.contestant.announcement.listContestantAnnouncementsView;
import org.iatoki.judgels.uriel.views.html.contest.contestant.clarification.createContestantClarificationView;
import org.iatoki.judgels.uriel.views.html.contest.contestant.clarification.updateContestantClarificationView;
import org.iatoki.judgels.uriel.views.html.contest.contestant.clarification.listContestantClarificationsView;
import org.iatoki.judgels.uriel.views.html.contest.contestant.problem.listContestantProblemsView;
import org.iatoki.judgels.uriel.views.html.contest.contestant.problem.viewContestantProblemView;
import org.iatoki.judgels.uriel.views.html.contest.contestant.submission.listContestantSubmissionsView;
import org.iatoki.judgels.uriel.views.html.contest.listView;
import org.iatoki.judgels.uriel.views.html.contest.manager.general.updateManagerGeneralView;
import org.iatoki.judgels.uriel.views.html.contest.manager.specific.updateManagerSpecificView;
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
import org.iatoki.judgels.uriel.views.html.contest.supervisor.submission.listSupervisorSubmissionsView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.team.createSupervisorTeamView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.team.listSupervisorTeamsView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.team.updateSupervisorTeamView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.team.viewSupervisorTeamView;
import org.iatoki.judgels.uriel.views.html.contest.viewView;
import org.iatoki.judgels.uriel.views.html.layouts.accessTypeByStatusLayout;
import play.Play;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class ContestController extends Controller {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;
    private final UserRoleService userRoleService;
    private final SubmissionService submissionService;

    public ContestController(ContestService contestService, UserRoleService userRoleService, SubmissionService submissionService) {
        this.contestService = contestService;
        this.userRoleService = userRoleService;
        this.submissionService = submissionService;
    }

    /* list ********************************************************************************************************* */

    public Result index() {
        return list(0, "id", "asc", "");
    }


    /* view ********************************************************************************************************* */

    public Result view(long contestId) {
        return viewAndListRegistrants(contestId, 0, "id", "asc", "");
    }

    public Result viewAndListRegistrants(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);

        if (isAllowedToViewContest(contest)) {
            Page<ContestContestant> contestContestants = contestService.pageContestContestantsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            LazyHtml content = new LazyHtml(viewView.render(contest, contestContestants, pageIndex, orderBy, orderDir, filterString, isAllowedToRegisterContest(contest), isContestant(contest) && !isContestEnded(contest), isAllowedToEnterContest(contest), isAllowedToManageContest(contest)));
            content.appendLayout(c -> headingLayout.render(contest.getName(), c));
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return redirect(routes.ContestController.index());
        }
    }


    /* register ******************************************************************************************************* */
    public Result register(long contestId) {
        Contest contest = contestService.findContestById(contestId);

        if (isAllowedToRegisterContest(contest)) {
            contestService.createContestContestant(contest.getId(), IdentityUtils.getUserJid(), ContestContestantStatus.APPROVED);
        }

        return redirect(routes.ContestController.view(contestId));
    }

    /* enter contest ******************************************************************************************************* */
    public Result enter(long contestId) {
        Contest contest = contestService.findContestById(contestId);

        if (isAllowedToEnterContest(contest)) {
            boolean isContestantInContest = (isContestant(contest));
            if (contest.isVirtual()) {
                ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
                ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
                if ((contestTypeConfigVirtual.getStartTrigger().equals(ContestTypeConfigVirtualStartTrigger.COACH)) && (isCoach(contest))) {
                    contestService.enterContestAsCoach(contest.getJid(), IdentityUtils.getUserJid());
                } else if (isContestantInContest) {
                    contestService.enterContestAsContestant(contest.getJid(), IdentityUtils.getUserJid());
                }
            } else if (isContestantInContest) {
                contestService.enterContestAsContestant(contest.getJid(), IdentityUtils.getUserJid());
            }
        }

        return redirect(routes.ContestController.viewContestantAnnouncements(contestId));
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
            contestService.createContest(contestUpsertForm.name, contestUpsertForm.description, ContestType.valueOf(contestUpsertForm.type), ContestScope.valueOf(contestUpsertForm.scope), ContestStyle.valueOf(contestUpsertForm.style), UrielUtils.convertStringToDate(contestUpsertForm.startTime), UrielUtils.convertStringToDate(contestUpsertForm.endTime), UrielUtils.convertStringToDate(contestUpsertForm.clarificationEndTime), contestUpsertForm.isUsingScoreboard, contestUpsertForm.isIncognitoScoreboard);

            return redirect(routes.ContestController.index());
        }
    }


    /* admin/manager ************************************************************************************************ */

    public Result viewAdminManagers(long contestId) {
        return listAdminManagers(contestId, 0, "id", "asc", "");
    }

    public Result listAdminManagers(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);

        if (isSupervisorOrAbove(contest)) {
            Page<ContestManager> contestManager = contestService.pageContestManagersByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = isAdmin();

            LazyHtml content = new LazyHtml(listAdminManagersView.render(contest.getId(), contestManager, pageIndex, orderBy, orderDir, filterString));

            if (canUpdate) {
                content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("manager.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createAdminManager(contestId)), c));
            } else {
                content.appendLayout(c -> heading3Layout.render(Messages.get("manager.list"), c));
            }

            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("manager.managers"), routes.ContestController.viewAdminManagers(contestId))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
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
            String userJid = JophielUtils.verifyUsername(contestManagerCreateForm.username);
            if ((userJid != null) && (!contestService.isContestManagerInContestByUserJid(contest.getJid(), userJid))) {
                userRoleService.upsertUserRoleFromJophielUserJid(userJid);
                contestService.createContestManager(contest.getId(), userJid);

                return redirect(routes.ContestController.viewAdminManagers(contest.getId()));
            } else {
                form.reject("error.manager.create.userJid.invalid");
                return showCreateAdminManager(form, contest);
            }
        }
    }

    /* manager/update *********************************************************************************************** */

    @AddCSRFToken
    public Result updateManagerGeneral(long contestId) {
        Contest contest = contestService.findContestById(contestId);

        if (isAllowedToManageContest(contest)) {
            ContestUpsertForm contestUpsertForm = new ContestUpsertForm(contest);
            Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).fill(contestUpsertForm);
            return showUpdateManagerGeneral(form, contest);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateManagerGeneral(long contestId) {
        Contest contest = contestService.findContestById(contestId);

        if (isAllowedToManageContest(contest)) {
            Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).bindFromRequest();

            if (form.hasErrors()) {
                return showUpdateManagerGeneral(form, contest);
            } else {
                ContestUpsertForm contestUpsertForm = form.get();
                contestService.updateContest(contest.getId(), contestUpsertForm.name, contestUpsertForm.description, ContestType.valueOf(contestUpsertForm.type), ContestScope.valueOf(contestUpsertForm.scope), ContestStyle.valueOf(contestUpsertForm.style), UrielUtils.convertStringToDate(contestUpsertForm.startTime), UrielUtils.convertStringToDate(contestUpsertForm.endTime), UrielUtils.convertStringToDate(contestUpsertForm.clarificationEndTime), contestUpsertForm.isUsingScoreboard, contestUpsertForm.isIncognitoScoreboard);

                return redirect(routes.ContestController.view(contestId));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }


    /* manager/supervisor ************************************************************************************************* */

    public Result viewManagerSupervisors(long contestId) {
        return listManagerSupervisors(contestId, 0, "id", "asc", "");
    }

    public Result listManagerSupervisors(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isSupervisorOrAbove(contest)) {
            Page<ContestSupervisor> contestPermissionPage = contestService.pageContestSupervisorsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = isAllowedToManageSupervisors(contest);

            LazyHtml content = new LazyHtml(listManagerSupervisorsView.render(contest.getId(), contestPermissionPage, pageIndex, orderBy, orderDir, filterString, canUpdate));

            if (canUpdate) {
                content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("supervisor.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createManagerSupervisor(contestId)), c));
            } else {
                content.appendLayout(c -> heading3Layout.render(Messages.get("supervisor.list"), c));
            }

            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContestantAnnouncements(contestId)),
                    new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestController.viewManagerSupervisors(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result createManagerSupervisor(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToManageSupervisors(contest)) {
            Form<ContestSupervisorCreateForm> form = Form.form(ContestSupervisorCreateForm.class);

            return showCreateManagerSupervisor(form, contest);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateManagerSupervisor(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToManageSupervisors(contest)) {
            Form<ContestSupervisorCreateForm> form = Form.form(ContestSupervisorCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateManagerSupervisor(form, contest);
            } else {
                ContestSupervisorCreateForm contestSupervisorCreateForm = form.get();
                String userJid = JophielUtils.verifyUsername(contestSupervisorCreateForm.username);
                if ((userJid != null) && (!contestService.isContestSupervisorInContestByUserJid(contest.getJid(), userJid))) {
                    userRoleService.upsertUserRoleFromJophielUserJid(userJid);
                    contestService.createContestSupervisor(contest.getId(), userJid, contestSupervisorCreateForm.announcement, contestSupervisorCreateForm.problem, contestSupervisorCreateForm.submission, contestSupervisorCreateForm.clarification, contestSupervisorCreateForm.contestant);

                    return redirect(routes.ContestController.viewManagerSupervisors(contest.getId()));
                } else {
                    form.reject("error.supervisor.create.userJid.invalid");
                    return showCreateManagerSupervisor(form, contest);
                }
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateManagerSupervisor(long contestId, long contestSupervisorId) {
        Contest contest = contestService.findContestById(contestId);
        ContestSupervisor contestSupervisor = contestService.findContestSupervisorByContestSupervisorId(contestSupervisorId);
        if (isAllowedToManageSupervisors(contest) && contestSupervisor.getContestJid().equals(contest.getJid())) {
            ContestSupervisorUpdateForm contestSupervisorUpdateForm = new ContestSupervisorUpdateForm(contestSupervisor);
            Form<ContestSupervisorUpdateForm> form = Form.form(ContestSupervisorUpdateForm.class).fill(contestSupervisorUpdateForm);

            return showUpdateManagerSupervisor(form, contest, contestSupervisor);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateManagerSupervisor(long contestId, long contestSupervisorId) {
        Contest contest = contestService.findContestById(contestId);
        ContestSupervisor contestSupervisor = contestService.findContestSupervisorByContestSupervisorId(contestSupervisorId);
        if (isAllowedToManageSupervisors(contest) && contestSupervisor.getContestJid().equals(contest.getJid())) {
            Form<ContestSupervisorUpdateForm> form = Form.form(ContestSupervisorUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateManagerSupervisor(form, contest, contestSupervisor);
            } else {
                ContestSupervisorUpdateForm contestSupervisorUpdateForm = form.get();
                contestService.updateContestSupervisor(contestSupervisor.getId(), contestSupervisorUpdateForm.announcement, contestSupervisorUpdateForm.problem, contestSupervisorUpdateForm.submission, contestSupervisorUpdateForm.clarification, contestSupervisorUpdateForm.contestant);

                return redirect(routes.ContestController.viewManagerSupervisors(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateContestSpecificConfig(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToManageContest(contest)) {
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());

            Form<?> form1 = null;
            if (contest.isStandard()) {
                ContestTypeConfigStandard contestTypeConfigStandard = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigStandard.class);
                Form<ContestTypeConfigStandardForm> form = Form.form(ContestTypeConfigStandardForm.class);
                form = form.fill(new ContestTypeConfigStandardForm(UrielUtils.convertDateToString(new Date(contestTypeConfigStandard.getScoreboardFreezeTime())), contestTypeConfigStandard.isOfficialScoreboardAllowed()));
                form1 = form;

            } else if (contest.isVirtual()) {
                ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
                Form<ContestTypeConfigVirtualForm> form = Form.form(ContestTypeConfigVirtualForm.class);
                form = form.fill(new ContestTypeConfigVirtualForm(contestTypeConfigVirtual.getContestDuration(), contestTypeConfigVirtual.getStartTrigger().name()));
                form1 = form;
            }

            Form form2 = null;
            if (contest.isPrivate()) {
                ContestScopeConfigPrivate contestScopeConfigPrivate = new Gson().fromJson(contestConfiguration.getScopeConfig(), ContestScopeConfigPrivate.class);
                Form<ContestScopeConfigPrivateForm> form = Form.form(ContestScopeConfigPrivateForm.class);
                form = form.fill(new ContestScopeConfigPrivateForm());
                form2 = form;
            } else if (contest.isPublic()) {
                ContestScopeConfigPublic contestScopeConfigPublic = new Gson().fromJson(contestConfiguration.getScopeConfig(), ContestScopeConfigPublic.class);
                Form<ContestScopeConfigPublicForm> form = Form.form(ContestScopeConfigPublicForm.class);
                form = form.fill(new ContestScopeConfigPublicForm(UrielUtils.convertDateToString(new Date(contestScopeConfigPublic.getRegisterStartTime())), UrielUtils.convertDateToString(new Date(contestScopeConfigPublic.getRegisterEndTime())), contestScopeConfigPublic.getMaxRegistrants()));
                form2 = form;
            }

            Form form3 = null;
            if (contest.isICPC()) {
                ContestStyleConfigICPC contestStyleConfigICPC = new Gson().fromJson(contestConfiguration.getStyleConfig(), ContestStyleConfigICPC.class);
                Form<ContestStyleConfigICPCForm> form = Form.form(ContestStyleConfigICPCForm.class);
                form = form.fill(new ContestStyleConfigICPCForm(contestStyleConfigICPC.getTimePenalty(), LanguageRestrictionAdapter.getFormIsAllowedAllFromLanguageRestriction(contestStyleConfigICPC.getLanguageRestriction()), LanguageRestrictionAdapter.getFormAllowedLanguageNamesFromLanguageRestriction(contestStyleConfigICPC.getLanguageRestriction())));
                form3 = form;
            } else if (contest.isIOI()) {
                ContestStyleConfigIOI contestStyleConfigIOI = new Gson().fromJson(contestConfiguration.getStyleConfig(), ContestStyleConfigIOI.class);
                Form<ContestStyleConfigIOIForm> form = Form.form(ContestStyleConfigIOIForm.class);
                form = form.fill(new ContestStyleConfigIOIForm(LanguageRestrictionAdapter.getFormIsAllowedAllFromLanguageRestriction(contestStyleConfigIOI.getLanguageRestriction()), LanguageRestrictionAdapter.getFormAllowedLanguageNamesFromLanguageRestriction(contestStyleConfigIOI.getLanguageRestriction())));
                form3 = form;
            }

            return showUpdateContestSpecificConfig(form1, form2, form3, contest);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateContestSpecificConfig(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToManageContest(contest)) {
            Form form1 = null;
            if (contest.isStandard()) {
                form1 = Form.form(ContestTypeConfigStandardForm.class).bindFromRequest();
            } else if (contest.isVirtual()) {
                form1 = Form.form(ContestTypeConfigVirtualForm.class).bindFromRequest();
            }

            Form form2 = null;
            if (contest.isPrivate()) {
                form2 = Form.form(ContestScopeConfigPrivateForm.class).bindFromRequest();
            } else if (contest.isPublic()) {
                form2 = Form.form(ContestScopeConfigPublicForm.class).bindFromRequest();
            }

            Form form3 = null;
            if (contest.isICPC()) {
                form3 = Form.form(ContestStyleConfigICPCForm.class).bindFromRequest();
            } else if (contest.isIOI()) {
                form3 = Form.form(ContestStyleConfigIOIForm.class).bindFromRequest();
            }

            if ((form1.hasErrors() || form1.hasGlobalErrors()) || (form2.hasErrors() || form2.hasGlobalErrors()) || (form3.hasErrors() || form3.hasGlobalErrors())) {
                return showUpdateContestSpecificConfig(form1, form2, form3, contest);
            } else {
                boolean check = true;

                ContestTypeConfig contestTypeConfig = null;
                if (contest.isStandard()) {
                    ContestTypeConfigStandardForm data = (ContestTypeConfigStandardForm) form1.get();
                    Date scoreboardFreezeTime = UrielUtils.convertStringToDate(data.scoreboardFreezeTime);
                    if ((scoreboardFreezeTime.before(contest.getStartTime())) && (scoreboardFreezeTime.after(contest.getEndTime()))) {
                        form1.reject("error.contest.config.specific.invalid_freeze_time");
                        check = false;
                    }
                    contestTypeConfig = new ContestTypeConfigStandard(scoreboardFreezeTime.getTime(), data.isOfficialScoreboardAllowed);
                } else if (contest.isVirtual()) {
                    ContestTypeConfigVirtualForm data = (ContestTypeConfigVirtualForm) form1.get();
                    long contestTotalDuration = contest.getEndTime().getTime() - contest.getStartTime().getTime();
                    if (data.contestDuration > contestTotalDuration) {
                        form1.reject("error.contest.config.specific.invalid_contest_duration");
                        check = false;
                    }
                    contestTypeConfig = new ContestTypeConfigVirtual(data.contestDuration, ContestTypeConfigVirtualStartTrigger.valueOf(data.startTrigger));
                }

                ContestScopeConfig contestScopeConfig = null;
                if (contest.isPrivate()) {
                    ContestScopeConfigPrivateForm data = (ContestScopeConfigPrivateForm) form2.get();
                    contestScopeConfig = new ContestScopeConfigPrivate();
                } else if (contest.isPublic()) {
                    ContestScopeConfigPublicForm data = (ContestScopeConfigPublicForm) form2.get();
                    Date registerStartTime = UrielUtils.convertStringToDate(data.registerStartTime);
                    Date registerEndTime = UrielUtils.convertStringToDate(data.registerEndTime);
                    if ((registerStartTime.after(registerEndTime)) || (registerEndTime.after(contest.getEndTime()))) {
                        form2.reject("error.contest.config.specific.invalidRegisterTime");
                        check = false;
                    }
                    contestScopeConfig = new ContestScopeConfigPublic(registerStartTime.getTime(), registerEndTime.getTime(), data.maxRegistrants);
                }

                ContestStyleConfig contestStyleConfig = null;
                if (contest.isICPC()) {
                    ContestStyleConfigICPCForm data = (ContestStyleConfigICPCForm) form3.get();
                    contestStyleConfig = new ContestStyleConfigICPC(data.timePenalty, LanguageRestrictionAdapter.createLanguageRestrictionFromForm(data.allowedLanguageNames, data.isAllowedAll));
                } else if (contest.isIOI()) {
                    ContestStyleConfigIOIForm data = (ContestStyleConfigIOIForm) form3.get();
                    contestStyleConfig = new ContestStyleConfigIOI(LanguageRestrictionAdapter.createLanguageRestrictionFromForm(data.allowedLanguageNames, data.isAllowedAll));
                }


                if (check) {
                    contestService.updateContestConfigurationByContestJid(contest.getJid(), contestTypeConfig, contestScopeConfig, contestStyleConfig);
                    return redirect(routes.ContestController.updateContestSpecificConfig(contest.getId()));
                } else {
                    return showUpdateContestSpecificConfig(form1, form2, form3, contest);
                }
            }
        } else {
            return tryEnteringContest(contest);
        }
    }


    /* supervisor/announcement ************************************************************************************** */

    public Result viewSupervisorAnnouncements(long contestId) {
        return listSupervisorAnnouncements(contestId, 0, "id", "desc", "");
    }

    public Result listSupervisorAnnouncements(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseAnnouncements(contest)) {
            Page<ContestAnnouncement> contestAnnouncements = contestService.pageContestAnnouncementsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, null);

            LazyHtml content = new LazyHtml(listSupervisorAnnouncementsView.render(contest.getId(), contestAnnouncements, pageIndex, orderBy, orderDir, filterString));

            content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("announcement.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createSupervisorAnnouncement(contest.getId())), c));
            content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantAnnouncements(contest.getId()), routes.ContestController.viewSupervisorAnnouncements(contest.getId()), c));
            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.viewSupervisorAnnouncements(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result createSupervisorAnnouncement(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseAnnouncements(contest)) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class);

            return showCreateSupervisorAnnouncement(form, contest);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateSupervisorAnnouncement(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseAnnouncements(contest)) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateSupervisorAnnouncement(form, contest);
            } else {
                ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = form.get();
                contestService.createContestAnnouncement(contest.getId(), contestAnnouncementUpsertForm.title, contestAnnouncementUpsertForm.content, ContestAnnouncementStatus.valueOf(contestAnnouncementUpsertForm.status));

                return redirect(routes.ContestController.viewSupervisorAnnouncements(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateSupervisorAnnouncement(long contestId, long contestAnnouncementId) {
        Contest contest = contestService.findContestById(contestId);
        ContestAnnouncement contestAnnouncement = contestService.findContestAnnouncementByContestAnnouncementId(contestAnnouncementId);
        if (isAllowedToSuperviseAnnouncements(contest) && contestAnnouncement.getContestJid().equals(contest.getJid())) {
            ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = new ContestAnnouncementUpsertForm(contestAnnouncement);
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).fill(contestAnnouncementUpsertForm);

            return showUpdateSupervisorAnnouncement(form, contest, contestAnnouncement);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateSupervisorAnnouncement(long contestId, long contestAnnouncementId) {
        Contest contest = contestService.findContestById(contestId);
        ContestAnnouncement contestAnnouncement = contestService.findContestAnnouncementByContestAnnouncementId(contestAnnouncementId);
        if (isAllowedToSuperviseAnnouncements(contest) && contestAnnouncement.getContestJid().equals(contest.getJid())) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateSupervisorAnnouncement(form, contest, contestAnnouncement);
            } else {
                ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = form.get();
                contestService.updateContestAnnouncement(contestAnnouncement.getId(), contestAnnouncementUpsertForm.title, contestAnnouncementUpsertForm.content, ContestAnnouncementStatus.valueOf(contestAnnouncementUpsertForm.status));

                return redirect(routes.ContestController.viewSupervisorAnnouncements(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }


    /* supervisor/problem ******************************************************************************************* */

    public Result viewSupervisorProblems(long contestId) {
        return listSupervisorProblems(contestId, 0, "alias", "asc", "");
    }

    public Result listSupervisorProblems(long contestId, long page, String sortBy, String orderBy, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseProblems(contest)) {
            Page<ContestProblem> contestProblemPage = contestService.pageContestProblemsByContestJid(contest.getJid(), page, PAGE_SIZE, sortBy, orderBy, filterString, null);

            LazyHtml content = new LazyHtml(listSupervisorProblemsView.render(contest.getId(), contestProblemPage, page, sortBy, orderBy, filterString));

            content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("problem.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createSupervisorProblem(contestId)), c));
            content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantProblems(contest.getId()), routes.ContestController.viewSupervisorProblems(contest.getId()), c));
            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("problem.problem"), routes.ContestController.viewContestantProblems(contest.getId())),
                    new InternalLink(Messages.get("status.supervisor"), routes.ContestController.viewSupervisorProblems(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result createSupervisorProblem(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseProblems(contest)) {
            Form<ContestProblemCreateForm> form = Form.form(ContestProblemCreateForm.class);
            form = form.fill(new ContestProblemCreateForm(0));

            return showCreateSupervisorProblem(form, contest);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateSupervisorProblem(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseProblems(contest)) {
            Form<ContestProblemCreateForm> form = Form.form(ContestProblemCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateSupervisorProblem(form, contest);
            } else {
                ContestProblemCreateForm contestProblemCreateForm = form.get();
                String problemName = SandalphonUtils.verifyProblemJid(contestProblemCreateForm.problemJid);
                if ((problemName != null) && (!contestService.isContestProblemInContestByProblemJidOrAlias(contest.getJid(), contestProblemCreateForm.problemJid, contestProblemCreateForm.alias))) {
                    try {
                        boolean processed = false;
                        while (!processed) {
                            if (GabrielUtils.getScoreboardLock().tryLock()) {
                                contestService.createContestProblem(contest.getId(), contestProblemCreateForm.problemJid, contestProblemCreateForm.problemSecret, contestProblemCreateForm.alias, contestProblemCreateForm.submissionsLimit, ContestProblemStatus.valueOf(contestProblemCreateForm.status));
                                JidCacheService.getInstance().putDisplayName(contestProblemCreateForm.problemJid, problemName, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
                                GabrielUtils.getScoreboardLock().unlock();
                                processed = true;
                            } else {
                                Thread.sleep(TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS));
                            }
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    return redirect(routes.ContestController.viewSupervisorProblems(contest.getId()));
                } else {
                    form.reject("error.problem.create.problemJidOrAlias.invalid");
                    return showCreateSupervisorProblem(form, contest);
                }
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateSupervisorProblem(long contestId, long contestProblemId) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);
        if (isAllowedToSuperviseProblems(contest) && contestProblem.getContestJid().equals(contest.getJid())) {
            ContestProblemUpdateForm contestProblemUpdateForm = new ContestProblemUpdateForm(contestProblem);
            Form<ContestProblemUpdateForm> form = Form.form(ContestProblemUpdateForm.class).fill(contestProblemUpdateForm);

            return showUpdateSupervisorProblem(form, contest, contestProblem);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateSupervisorProblem(long contestId, long contestProblemId) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);
        if (isAllowedToSuperviseProblems(contest) && contestProblem.getContestJid().equals(contest.getJid())) {
            Form<ContestProblemUpdateForm> form = Form.form(ContestProblemUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateSupervisorProblem(form, contest, contestProblem);
            } else {
                ContestProblemUpdateForm contestProblemUpdateForm = form.get();

                try {
                    boolean processed = false;
                    while (!processed) {
                        if (GabrielUtils.getScoreboardLock().tryLock()) {
                            contestService.updateContestProblem(contestProblem.getId(), contestProblemUpdateForm.problemSecret, contestProblemUpdateForm.alias, contestProblemUpdateForm.submissionsLimit, ContestProblemStatus.valueOf(contestProblemUpdateForm.status));
                            GabrielUtils.getScoreboardLock().unlock();
                            processed = true;
                        } else {
                            Thread.sleep(TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS));
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return redirect(routes.ContestController.viewSupervisorProblems(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }


    /* supervisor/clarification ************************************************************************************* */

    public Result viewSupervisorClarifications(long contestId) {
        return listSupervisorClarifications(contestId, 0, "id", "desc", "");
    }

    public Result listSupervisorClarifications(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseClarifications(contest)) {
            Page<ContestClarification> contestClarifications = contestService.pageContestClarificationsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, null);

            LazyHtml content = new LazyHtml(listSupervisorClarificationsView.render(contest.getId(), contestClarifications, pageIndex, orderBy, orderDir, filterString));

            content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.list"), c));
            content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantClarifications(contest.getId()), routes.ContestController.viewSupervisorClarifications(contest.getId()), c));
            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("clarification.clarifications"), routes.ContestController.viewContestantClarifications(contest.getId())),
                    new InternalLink(Messages.get("status.supervisor"), routes.ContestController.viewSupervisorClarifications(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateSupervisorClarification(long contestId, long contestClarificationId) {
        Contest contest = contestService.findContestById(contestId);
        ContestClarification contestClarification = contestService.findContestClarificationByContestClarificationId(contestClarificationId);
        if (isAllowedToSuperviseClarifications(contest) && contestClarification.getContestJid().equals(contest.getJid())) {
            ContestClarificationUpdateForm contestClarificationUpsertForm = new ContestClarificationUpdateForm(contestClarification);
            Form<ContestClarificationUpdateForm> form = Form.form(ContestClarificationUpdateForm.class).fill(contestClarificationUpsertForm);

            return showUpdateSupervisorClarification(form, contest, contestClarification);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateSupervisorClarification(long contestId, long contestClarificationId) {
        Contest contest = contestService.findContestById(contestId);
        ContestClarification contestClarification = contestService.findContestClarificationByContestClarificationId(contestClarificationId);
        if (isAllowedToSuperviseClarifications(contest) && contestClarification.getContestJid().equals(contest.getJid())) {
            Form<ContestClarificationUpdateForm> form = Form.form(ContestClarificationUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateSupervisorClarification(form, contest, contestClarification);
            } else {
                ContestClarificationUpdateForm contestClarificationUpdateForm = form.get();
                contestService.updateContestClarification(contestClarification.getId(), contestClarificationUpdateForm.answer, ContestClarificationStatus.valueOf(contestClarificationUpdateForm.status));

                return redirect(routes.ContestController.viewSupervisorClarifications(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }


    /* supervisor/contestant **************************************************************************************** */

    public Result viewSupervisorContestants(long contestId) {
        return listSupervisorContestants(contestId, 0, "id", "asc", "");
    }

    public Result listSupervisorContestants(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isSupervisorOrAbove(contest)) {
            Page<ContestContestant> contestContestants = contestService.pageContestContestantsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = isAllowedToSuperviseContestants(contest);

            LazyHtml content = new LazyHtml(listSupervisorContestantsView.render(contest.getId(), contestContestants, pageIndex, orderBy, orderDir, filterString, canUpdate));

            if (canUpdate) {
                content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("contestant.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createSupervisorContestant(contestId)), c));
            } else {
                content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.list"), c));
            }

            content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestController.viewSupervisorTeams(contest.getId()))), c));
            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result createSupervisorContestant(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseContestants(contest)) {
            Form<ContestContestantCreateForm> form = Form.form(ContestContestantCreateForm.class);
            Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);

            return showCreateSupervisorContestant(form, form2, contest);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateSupervisorContestant(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseContestants(contest)) {
            Form<ContestContestantCreateForm> form = Form.form(ContestContestantCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);
                return showCreateSupervisorContestant(form, form2, contest);
            } else {
                ContestContestantCreateForm contestContestantCreateForm = form.get();
                String userJid = JophielUtils.verifyUsername(contestContestantCreateForm.username);
                if ((userJid != null) && (!contestService.isContestContestantInContestByUserJid(contest.getJid(), userJid))) {
                    userRoleService.upsertUserRoleFromJophielUserJid(userJid);
                    try {
                        boolean processed = false;
                        while (!processed) {
                            if (GabrielUtils.getScoreboardLock().tryLock()) {
                                contestService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.valueOf(contestContestantCreateForm.status));
                                GabrielUtils.getScoreboardLock().unlock();
                                processed = true;
                            } else {
                                Thread.sleep(TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS));
                            }
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    return redirect(routes.ContestController.viewSupervisorContestants(contest.getId()));
                } else {
                    Form<ContestContestantUploadForm> form2 = Form.form(ContestContestantUploadForm.class);
                    form.reject("error.contestant.create.userJid.invalid");
                    return showCreateSupervisorContestant(form, form2, contest);
                }
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateSupervisorContestant(long contestId, long contestContestantId) {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestService.findContestContestantByContestContestantId(contestContestantId);
        if (isAllowedToSuperviseContestants(contest) && contestContestant.getContestJid().equals(contest.getJid())) {
            ContestContestantUpdateForm contestContestantUpsertForm = new ContestContestantUpdateForm(contestContestant);
            Form<ContestContestantUpdateForm> form = Form.form(ContestContestantUpdateForm.class).fill(contestContestantUpsertForm);

            return showUpdateSupervisorContestant(form, contest, contestContestant);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateSupervisorContestant(long contestId, long contestContestantId) {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestService.findContestContestantByContestContestantId(contestContestantId);
        if (isAllowedToSuperviseContestants(contest) && contestContestant.getContestJid().equals(contest.getJid())) {
            Form<ContestContestantUpdateForm> form = Form.form(ContestContestantUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateSupervisorContestant(form, contest, contestContestant);
            } else {
                ContestContestantUpdateForm contestContestantUpdateForm = form.get();
                try {
                    boolean processed = false;
                    while (!processed) {
                        if (GabrielUtils.getScoreboardLock().tryLock()) {
                            contestService.updateContestContestant(contestContestant.getId(), ContestContestantStatus.valueOf(contestContestantUpdateForm.status));
                            GabrielUtils.getScoreboardLock().unlock();
                            processed = true;
                        } else {
                            Thread.sleep(TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS));
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return redirect(routes.ContestController.viewSupervisorContestants(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUploadSupervisorContestant(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        if (isAllowedToSuperviseContestants(contest)) {
            file = body.getFile("usernames");
            if (file != null) {
                File userFile = file.getFile();
                try {
                    boolean processed = false;
                    while (!processed) {
                        if (GabrielUtils.getScoreboardLock().tryLock()) {
                            String[] usernames = FileUtils.readFileToString(userFile).split("\n");
                            for (String username : usernames) {
                                String userJid = JophielUtils.verifyUsername(username);
                                if ((userJid != null) && (!contestService.isContestContestantInContestByUserJid(contest.getJid(), userJid))) {
                                    userRoleService.upsertUserRoleFromJophielUserJid(userJid);
                                    contestService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.APPROVED);
                                }
                            }
                            GabrielUtils.getScoreboardLock().unlock();
                            processed = true;
                        } else {
                            Thread.sleep(TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS));
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return redirect(routes.ContestController.viewSupervisorContestants(contest.getId()));
        } else {
            return tryEnteringContest(contest);
        }
    }

    /* supervisor/team **************************************************************************************** */

    public Result viewSupervisorTeams(long contestId) {
        return listSupervisorTeams(contestId, 0, "id", "asc", "");
    }

    public Result listSupervisorTeams(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isSupervisorOrAbove(contest)) {
            Page<ContestTeam> contestTeams = contestService.pageContestTeamsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = isAllowedToSuperviseContestants(contest);

            LazyHtml content = new LazyHtml(listSupervisorTeamsView.render(contest.getId(), contestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate));

            if (canUpdate) {
                content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("team.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createSupervisorTeam(contestId)), c));
            } else {
                content.appendLayout(c -> heading3Layout.render(Messages.get("team.list"), c));
            }

            content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestController.viewSupervisorTeams(contest.getId()))), c));
            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())),
                    new InternalLink(Messages.get("team.teams"), routes.ContestController.viewSupervisorTeams(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result createSupervisorTeam(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseContestants(contest)) {
            Form<ContestTeamUpsertForm> form = Form.form(ContestTeamUpsertForm.class);

            return showCreateSupervisorTeam(form, contest);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateSupervisorTeam(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseContestants(contest)) {
            Form<ContestTeamUpsertForm> form = Form.form(ContestTeamUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateSupervisorTeam(form, contest);
            } else {
                ContestTeamUpsertForm contestTeamUpsertForm = form.get();

                Http.MultipartFormData body = request().body().asMultipartFormData();
                Http.MultipartFormData.FilePart teamImage = body.getFile("teamImage");

                if (teamImage != null) {
                    contestService.createContestTeam(contest.getId(), contestTeamUpsertForm.name, teamImage.getFile(), FilenameUtils.getExtension(teamImage.getFilename()));
                } else {
                    contestService.createContestTeam(contest.getId(), contestTeamUpsertForm.name);
                }
                return redirect(routes.ContestController.viewSupervisorTeams(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateSupervisorTeam(long contestId, long contestTeamId) {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            ContestTeamUpsertForm contestTeamUpsertForm = new ContestTeamUpsertForm(contestTeam);
            Form<ContestTeamUpsertForm> form = Form.form(ContestTeamUpsertForm.class).fill(contestTeamUpsertForm);

            return showUpdateSupervisorTeam(form, contest, contestTeam);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateSupervisorTeam(long contestId, long contestTeamId) {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            Form<ContestTeamUpsertForm> form = Form.form(ContestTeamUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateSupervisorTeam(form, contest, contestTeam);
            } else {
                ContestTeamUpsertForm contestTeamUpsertForm = form.get();
                Http.MultipartFormData body = request().body().asMultipartFormData();
                Http.MultipartFormData.FilePart teamImage = body.getFile("teamImage");

                if (teamImage != null) {
                    contestService.updateContestTeam(contest.getId(), contestTeamUpsertForm.name, teamImage.getFile(), FilenameUtils.getExtension(teamImage.getFilename()));
                } else {
                    contestService.updateContestTeam(contest.getId(), contestTeamUpsertForm.name);
                }
                return redirect(routes.ContestController.viewSupervisorTeams(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result viewSupervisorTeam(long contestId, long contestTeamId) {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        if (isSupervisorOrAbove(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class);
            Form<ContestTeamMemberCreateForm> form2 = Form.form(ContestTeamMemberCreateForm.class);

            return showViewSupervisorTeam(form, form2, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateSupervisorTeamCoach(long contestId, long contestTeamId) {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                Form<ContestTeamMemberCreateForm> form2 = Form.form(ContestTeamMemberCreateForm.class);
                return showViewSupervisorTeam(form, form2, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
            } else {
                ContestTeamCoachCreateForm contestTeamCoachCreateForm = form.get();

                String userJid = JophielUtils.verifyUsername(contestTeamCoachCreateForm.username);
                if ((userJid != null) && (!contestService.isUserInAnyTeamByContestJid(contest.getJid(), userJid))) {
                    contestService.createContestTeamCoach(contestTeam.getJid(), userJid);

                    return redirect(routes.ContestController.viewSupervisorTeam(contest.getId(), contestTeam.getId()));
                } else {
                    form.reject("team.user_already_has_team");
                    Form<ContestTeamMemberCreateForm> form2 = Form.form(ContestTeamMemberCreateForm.class);

                    return showViewSupervisorTeam(form, form2, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                }
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result removeSupervisorTeamCoach(long contestId, long contestTeamId, long contestTeamCoachId) {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        ContestTeamCoach contestTeamCoach = contestService.findContestTeamCoachByContestTeamCoachId(contestTeamCoachId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid()) && contestTeamCoach.getTeamJid().equals(contestTeam.getJid())) {
            contestService.removeContestTeamCoachByContestTeamCoachId(contestTeamCoach.getId());

            return redirect(routes.ContestController.viewSupervisorTeam(contest.getId(), contestTeam.getId()));
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateSupervisorTeamMember(long contestId, long contestTeamId) {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            Form<ContestTeamMemberCreateForm> form2 = Form.form(ContestTeamMemberCreateForm.class).bindFromRequest();

            if (form2.hasErrors() || form2.hasGlobalErrors()) {
                Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class);
                return showViewSupervisorTeam(form, form2, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
            } else {
                ContestTeamMemberCreateForm contestTeamMemberCreateForm = form2.get();

                String userJid = JophielUtils.verifyUsername(contestTeamMemberCreateForm.username);
                if ((userJid != null) && (!contestService.isUserInAnyTeamByContestJid(contest.getJid(), userJid)) && (contestService.isContestContestantInContestByUserJid(contest.getJid(), userJid))) {
                    contestService.createContestTeamMember(contestTeam.getJid(), userJid);

                    return redirect(routes.ContestController.viewSupervisorTeam(contest.getId(), contestTeam.getId()));
                } else {
                    form2.reject("team.user_already_has_team");
                    Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class);

                    return showViewSupervisorTeam(form, form2, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                }
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result removeSupervisorTeamMember(long contestId, long contestTeamId, long contestTeamMemberId) {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        ContestTeamMember contestTeamMember = contestService.findContestTeamMemberByContestTeamMemberId(contestTeamMemberId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid()) && contestTeamMember.getTeamJid().equals(contestTeam.getJid())) {
            contestService.removeContestTeamMemberByContestTeamMemberId(contestTeamMember.getId());

            return redirect(routes.ContestController.viewSupervisorTeam(contest.getId(), contestTeam.getId()));
        } else {
            return tryEnteringContest(contest);
        }
    }

    /* supervisor/submission **************************************************************************************** */

    public Result viewSupervisorSubmissions(long contestId) {
        return listSupervisorSubmissions(contestId, 0, "id", "desc", null, null);
    }

    public Result listSupervisorSubmissions(long contestId, long pageIndex, String orderBy, String orderDir, String contestantJid, String problemJid) {
        Contest contest = contestService.findContestById(contestId);

        if (isAllowedToSuperviseSubmissions(contest)) {

            String actualContestantJid = "(none)".equals(contestantJid) ? null : contestantJid;
            String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

            Page<Submission> submissions = submissionService.pageSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, actualContestantJid, actualProblemJid, contest.getJid());
            Map<String, String> problemJidToAliasMap = contestService.findProblemJidToAliasMapByContestJid(contest.getJid());
            List<ContestContestant> contestants = contestService.findAllContestContestantsByContestJid(contest.getJid());
            List<String> contestantJids = Lists.transform(contestants, c -> c.getUserJid());

            Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

            LazyHtml content = new LazyHtml(listSupervisorSubmissionsView.render(contestId, submissions, contestantJids, problemJidToAliasMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, actualContestantJid, actualProblemJid));

            content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));

            content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantSubmissions(contest.getId()), routes.ContestController.viewSupervisorSubmissions(contest.getId()), c));

            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("submission.submissions"), routes.ContestController.viewContestantSubmissions(contest.getId())),
                    new InternalLink(Messages.get("status.supervisor"), routes.ContestController.viewSupervisorSubmissions(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result regradeSupervisorSubmission(long contestId, long submissionId, long pageIndex, String orderBy, String orderDir, String contestantJid, String problemJid) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseSubmissions(contest)) {

            Submission submission = submissionService.findSubmissionById(submissionId);
            GradingSource source = SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).createGradingSourceFromPastSubmission(UrielProperties.getInstance().getSubmissionDir(), submission.getJid());
            submissionService.regrade(submission.getJid(), source);

            return redirect(routes.ContestController.listSupervisorSubmissions(contestId, pageIndex, orderBy, orderDir, contestantJid, problemJid));
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result regradeSupervisorSubmissions(long contestId, long pageIndex, String orderBy, String orderDir, String contestantJid, String problemJid) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseSubmissions(contest)) {
            ListTableSelectionForm data = Form.form(ListTableSelectionForm.class).bindFromRequest().get();

            List<Submission> submissions;

            if (data.selectAll) {
                submissions = submissionService.findSubmissionsWithoutGradingsByFilters(orderBy, orderDir, contestantJid, problemJid, contest.getJid());
            } else if (data.selectJids != null) {
                submissions = submissionService.findSubmissionsWithoutGradingsByJids(data.selectJids);
            } else {
                return redirect(routes.ContestController.listSupervisorSubmissions(contestId, pageIndex, orderBy, orderDir, contestantJid, problemJid));
            }

            for (Submission submission : submissions) {
                GradingSource source = SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).createGradingSourceFromPastSubmission(UrielProperties.getInstance().getSubmissionDir(), submission.getJid());
                submissionService.regrade(submission.getJid(), source);
            }

            return redirect(routes.ContestController.listSupervisorSubmissions(contestId, pageIndex, orderBy, orderDir, contestantJid, problemJid));
        } else {
            return tryEnteringContest(contest);
        }
    }

    /* supervisor/scoreboard **************************************************************************************** */

    public Result viewSupervisorScoreboard(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if ((contest.isUsingScoreboard()) && (isAllowedToSuperviseScoreboard(contest))) {
            ContestScoreboard contestScoreboard = contestService.findContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL);
            ScoreAdapter adapter = ScoreAdapters.fromContestStyle(contest.getStyle());
            Scoreboard scoreboard = contestScoreboard.getScoreboard();
            LazyHtml content = new LazyHtml(adapter.renderScoreboard(scoreboard, contestScoreboard.getLastUpdateTime(), JidCacheService.getInstance(), IdentityUtils.getUserJid(), false, scoreboard.getState().getContestantJids().stream().collect(Collectors.toSet())));

            content.appendLayout(c -> heading3WithActionsLayout.render(Messages.get("scoreboard.scoreboard"), new InternalLink[] {new InternalLink(Messages.get("scoreboard.refresh"), routes.ContestController.refreshAllScoreboard(contest.getId())), new InternalLink(Messages.get("data.download"), routes.ContestController.downloadContestDataAsXLS(contest.getId()))}, c));

            content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantScoreboard(contest.getId()), routes.ContestController.viewSupervisorScoreboard(contest.getId()), c));

            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("scoreboard.scoreboard"), routes.ContestController.viewContestantScoreboard(contest.getId())),
                    new InternalLink(Messages.get("status.supervisor"), routes.ContestController.viewSupervisorScoreboard(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result refreshAllScoreboard(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if ((contest.isUsingScoreboard()) && (isAllowedToSuperviseScoreboard(contest))) {
            try {
                if (GabrielUtils.getScoreboardLock().tryLock(10, TimeUnit.SECONDS)) {
                    ScoreAdapter adapter = ScoreAdapters.fromContestStyle(contest.getStyle());
                    ContestScoreState state = contestService.getContestStateByJid(contest.getJid());

                    List<Submission> submissions = submissionService.findAllSubmissionsByContestJid(contest.getJid());

                    ScoreboardContent content = adapter.computeScoreboardContent(state, submissions, contestService.getMapContestantJidToImageUrlInContest(contest.getJid()));
                    Scoreboard scoreboard = adapter.createScoreboard(state, content);
                    contestService.updateContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL, scoreboard);

                    if (contest.isStandard()) {
                        refreshFrozenScoreboard(contest, adapter, state);
                    }

                    GabrielUtils.getScoreboardLock().unlock();
                }
                return redirect(routes.ContestController.viewSupervisorScoreboard(contest.getId()));
            } catch (InterruptedException e) {
                return internalServerError();
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result downloadContestDataAsXLS(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if ((contest.isUsingScoreboard()) && (isAllowedToSuperviseScoreboard(contest))) {
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestScoreboard contestScoreboard = contestService.findContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL);
            ContestScoreState contestScoreState = contestScoreboard.getScoreboard().getState();

            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet(Messages.get("problem.problems"));

            int rowNum = 0;
            int cellNum = 0;
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(cellNum++);
            cell.setCellValue(Messages.get("problem.alias"));
            cell = row.createCell(cellNum++);
            cell.setCellValue(Messages.get("problem.name"));
            for (int i=0;i<contestScoreState.getProblemJids().size();++i) {
                row = sheet.createRow(rowNum++);
                cellNum = 0;
                cell = row.createCell(cellNum++);
                cell.setCellValue(contestScoreState.getProblemAliases().get(i));
                cell = row.createCell(cellNum++);
                cell.setCellValue(JidCacheService.getInstance().getDisplayName(contestScoreState.getProblemJids().get(i)));
            }

            sheet = workbook.createSheet(Messages.get("team.teams"));

            List<ContestTeam> contestTeams = contestService.findAllContestTeams(contest.getJid());
            rowNum = 0;
            cellNum = 0;
            row = sheet.createRow(rowNum++);
            cell = row.createCell(cellNum++);
            cell.setCellValue(Messages.get("team.name"));
            cell = row.createCell(cellNum++);
            cell.setCellValue(Messages.get("team.coach.name"));
            cell = row.createCell(cellNum++);
            cell.setCellValue(Messages.get("team.member.name"));
            for (ContestTeam contestTeam : contestTeams) {
                row = sheet.createRow(rowNum++);
                cell = row.createCell(cellNum++);
                cell.setCellValue(contestTeam.getName());

                List<ContestTeamCoach> contestTeamCoaches = contestService.findContestTeamCoachesByTeamJid(contestTeam.getContestJid());
                List<ContestTeamMember> contestTeamMembers = contestService.findContestTeamMembersByTeamJid(contestTeam.getJid());
                if (contestTeamCoaches.size() > 0) {
                    cell = row.createCell(1);
                    cell.setCellValue(JidCacheService.getInstance().getDisplayName(contestTeamCoaches.get(0).getCoachJid()));
                }
                if (contestTeamMembers.size() > 0) {
                    cell = row.createCell(2);
                    cell.setCellValue(JidCacheService.getInstance().getDisplayName(contestTeamMembers.get(0).getMemberJid()));
                }
                int max = Math.max(contestTeamCoaches.size(), contestTeamMembers.size());
                for (int i=1;i<max;++i) {
                    row = sheet.createRow(rowNum++);
                    if (contestTeamCoaches.size() > i) {
                        cell = row.createCell(1);
                        cell.setCellValue(JidCacheService.getInstance().getDisplayName(contestTeamCoaches.get(i).getCoachJid()));
                    }
                    if (contestTeamMembers.size() > i) {
                        cell = row.createCell(2);
                        cell.setCellValue(JidCacheService.getInstance().getDisplayName(contestTeamMembers.get(i).getMemberJid()));
                    }
                }
            }

            sheet = workbook.createSheet("Rank");
            if (contest.isIOI()) {
                IOIScoreboardContent ioiScoreboardContent = (IOIScoreboardContent) contestScoreboard.getScoreboard().getContent();
                rowNum = 0;
                row = sheet.createRow(rowNum++);

                cellNum = 0;
                cell = row.createCell(cellNum++);
                cell.setCellValue("Rank");
                cell = row.createCell(cellNum++);
                cell.setCellValue("Contestant");
                cell = row.createCell(cellNum++);
                cell.setCellValue("Total");
                for (String s : contestScoreState.getProblemAliases()) {
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(s);
                }

                for (IOIScoreboardEntry entry : ioiScoreboardContent.getEntries()) {
                    row = sheet.createRow(rowNum++);
                    cellNum = 0;

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(entry.rank);
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(JidCacheService.getInstance().getDisplayName(entry.contestantJid));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(entry.totalScores);
                    for (Integer score : entry.scores) {
                        cell = row.createCell(cellNum++);
                        cell.setCellValue(score);
                    }
                }
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    workbook.write(baos);
                    baos.close();
                    response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    response().setHeader("Content-Disposition", "attachment; filename=\"" + contest.getName()+ ".xls\"");
                    return ok(baos.toByteArray());
                } catch (IOException e) {
                    return internalServerError();
                }
            } else {
                // TODO FOR ACM ICPC
                return internalServerError();
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    /* contestant/announcement *************************************************************************************** */

    public Result viewContestantAnnouncements(long contestId) {
        return listContestantAnnouncements(contestId, 0, "timeUpdate", "desc", "");
    }

    public Result listContestantAnnouncements(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            Page<ContestAnnouncement> contestAnnouncements = contestService.pageContestAnnouncementsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, ContestAnnouncementStatus.PUBLISHED.name());
            contestService.readContestAnnouncements(IdentityUtils.getUserJid(), contestAnnouncements.getData().stream().map(c -> c.getId()).collect(Collectors.toList()));

            LazyHtml content = new LazyHtml(listContestantAnnouncementsView.render(contest.getId(), contestAnnouncements, pageIndex, orderBy, orderDir, filterString));
            content.appendLayout(c -> heading3Layout.render(Messages.get("announcement.announcements"), c));

            if (isAllowedToSuperviseAnnouncements(contest)) {
                content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantAnnouncements(contest.getId()), routes.ContestController.viewSupervisorAnnouncements(contest.getId()), c));
            }

            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.viewContestantAnnouncements(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    /* contestant/problem *************************************************************************************** */

    public Result viewContestantProblems(long contestId) {
        return listContestantProblems(contestId, 0, "alias", "asc", "");
    }

    public Result listContestantProblems(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            Page<ContestProblem> contestProblems = contestService.pageContestProblemsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, ContestProblemStatus.OPEN.name());
            ImmutableList.Builder<ContestProblem> replacementBuilder = ImmutableList.builder();
            for (ContestProblem contestProblem : contestProblems.getData()) {
                contestProblem.setTotalSubmissions(submissionService.countSubmissionsByContestJidByUser(contest.getJid(), contestProblem.getProblemJid(), IdentityUtils.getUserJid()));
                replacementBuilder.add(contestProblem);
            }
            contestProblems = new Page<>(replacementBuilder.build(), contestProblems.getTotalRowsCount(), contestProblems.getPageIndex(), contestProblems.getPageSize());

            LazyHtml content = new LazyHtml(listContestantProblemsView.render(contest.getId(), contestProblems, pageIndex, orderBy, orderDir, filterString));
            content.appendLayout(c -> heading3Layout.render(Messages.get("problem.problems"), c));

            if (isAllowedToSuperviseProblems(contest)) {
                content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantProblems(contest.getId()), routes.ContestController.viewSupervisorProblems(contest.getId()), c));
            }
            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("problem.problems"), routes.ContestController.viewContestantProblems(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result viewContestantProblem(long contestId, long contestProblemId) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);
        if (isAllowedToEnterContest(contest) && isAllowedToViewProblem(contest, contestProblem)) {
            long submissionLeft = -1;
            if (contestProblem.getSubmissionsLimit() != 0) {
                submissionLeft = contestProblem.getSubmissionsLimit() - submissionService.countSubmissionsByContestJidByUser(contest.getJid(), contestProblem.getProblemJid(), IdentityUtils.getUserJid());
            }

            int tOTPCode = SandalphonUtils.calculateTOTPCode(contestProblem.getProblemSecret(), System.currentTimeMillis());
            String requestUrl = SandalphonUtils.getTOTPEndpoint(contestProblem.getProblemJid(), tOTPCode, Play.langCookieName(), routes.ContestController.postSubmitContestantProblem(contestId, contestProblem.getProblemJid()).absoluteURL(request())).toString();
            String requestBody = "";

            ContestConfiguration config = contestService.findContestConfigurationByContestJid(contest.getJid());
            String styleConfig = config.getStyleConfig();

            if (contest.isICPC()) {
                requestBody = new Gson().toJson(new Gson().fromJson(styleConfig, ContestStyleConfigICPC.class).getLanguageRestriction());
            } else if (contest.isIOI()) {
                requestBody = new Gson().toJson(new Gson().fromJson(styleConfig, ContestStyleConfigIOI.class).getLanguageRestriction());
            }

            LazyHtml content = new LazyHtml(viewContestantProblemView.render(requestUrl, requestBody, submissionLeft));

            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("problem.problems"), routes.ContestController.viewContestantProblems(contest.getId())),
                    new InternalLink(contestProblem.getAlias(), routes.ContestController.viewContestantProblem(contest.getId(), contestProblem.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result renderImage(long contestId, long contestProblemId, String imageFilename) {
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);

        URI imageUri = SandalphonUtils.getRenderImageUri(contestProblem.getProblemJid(), imageFilename);

        return redirect(imageUri.toString());
    }

    public Result postSubmitContestantProblem(long contestId, String problemJid) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestJidAndContestProblemJid(contest.getJid(), problemJid);

        if (isAllowedToDoContest(contest) && contestProblem.getContestJid().equals(contest.getJid())) {

            if ((contestProblem.getSubmissionsLimit() == 0) || (submissionService.countSubmissionsByContestJidByUser(contest.getJid(), contestProblem.getProblemJid(), IdentityUtils.getUserJid()) < contestProblem.getSubmissionsLimit())) {
                Http.MultipartFormData body = request().body().asMultipartFormData();

                String gradingLanguage = body.asFormUrlEncoded().get("language")[0];
                String gradingEngine = body.asFormUrlEncoded().get("engine")[0];

                try {
                    GradingSource source = SubmissionAdapters.fromGradingEngine(gradingEngine).createGradingSourceFromNewSubmission(body);
                    String submissionJid = submissionService.submit(problemJid, contest.getJid(), gradingEngine, gradingLanguage, ImmutableSet.of(), source);
                    SubmissionAdapters.fromGradingEngine(gradingEngine).storeSubmissionFiles(UrielProperties.getInstance().getSubmissionDir(), submissionJid, source);
                } catch (SubmissionException e) {
                    flash("submissionError", e.getMessage());

                    return redirect(routes.ContestController.viewContestantProblem(contestId, contestProblem.getId()));
                }
            } else {
                flash("submissionError", "submission.limit.reached");
                return redirect(routes.ContestController.viewContestantProblem(contestId, contestProblem.getId()));
            }

            return redirect(routes.ContestController.viewContestantSubmissions(contestId));
        } else {
            return tryEnteringContest(contest);
        }
    }


    /* contestant/submission **************************************************************************************** */

    public Result viewContestantSubmissions(long contestId) {
        return listContestantSubmissions(contestId, 0, "id", "desc", "");
    }

    public Result listContestantSubmissions(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);

        if (isAllowedToEnterContest(contest)) {

            Page<Submission> submissions = submissionService.pageSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, IdentityUtils.getUserJid(), null, contest.getJid());
            Map<String, String> problemJidToAliasMap = contestService.findProblemJidToAliasMapByContestJid(contest.getJid());
            Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

            LazyHtml content = new LazyHtml(listContestantSubmissionsView.render(contestId, submissions, problemJidToAliasMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, filterString));

            content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));

            if (isAllowedToSuperviseSubmissions(contest)) {
                content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantSubmissions(contest.getId()), routes.ContestController.viewSupervisorSubmissions(contest.getId()), c));
            }

            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("submission.submissions"), routes.ContestController.viewContestantSubmissions(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result viewContestantSubmission(long contestId, long submissionId) {
        Contest contest = contestService.findContestById(contestId);
        Submission submission = submissionService.findSubmissionById(submissionId);

        if (isAllowedToEnterContest(contest) && isAllowedToViewSubmission(contest, submission)) {
            GradingSource source = SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).createGradingSourceFromPastSubmission(UrielProperties.getInstance().getSubmissionDir(), submission.getJid());
            String authorName = JidCacheService.getInstance().getDisplayName(submission.getAuthorJid());
            ContestProblem contestProblem = contestService.findContestProblemByContestJidAndContestProblemJid(contest.getJid(), submission.getProblemJid());
            String contestProblemAlias = contestProblem.getAlias();
            String contestProblemName = JidCacheService.getInstance().getDisplayName(contestProblem.getProblemJid());
            String gradingLanguageName = GradingLanguageRegistry.getInstance().getLanguage(submission.getGradingLanguage()).getName();

            LazyHtml content = new LazyHtml(SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).renderViewSubmission(submission, source, authorName, contestProblemAlias, contestProblemName, gradingLanguageName, contest.getName()));

            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("submission.view"), routes.ContestController.viewContestantSubmission(contest.getId(), submission.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    /* contestant/clarification ************************************************************************************* */

    public Result viewContestantClarifications(long contestId) {
        return listContestantClarifications(contestId, 0, "id", "desc", "");
    }

    public Result listContestantClarifications(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            Page<ContestClarification> contestClarifications;
            boolean coach = isCoach(contest);
            if (coach) {
                ContestTeam contestTeam = contestService.findContestTeamJidByContestJidAndCoachJid(contest.getJid(), IdentityUtils.getUserJid());
                List<ContestTeamMember> contestTeamMembers = contestService.findContestTeamMembersByTeamJid(contestTeam.getJid());
                contestClarifications = contestService.pageContestClarificationsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, contestTeamMembers.stream().map(ct -> ct.getMemberJid()).collect(Collectors.toList()));
            } else {
                contestClarifications = contestService.pageContestClarificationsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, ImmutableList.of(IdentityUtils.getUserJid()));
            }
            contestService.readContestClarifications(IdentityUtils.getUserJid(), contestClarifications.getData().stream().map(c -> c.getId()).collect(Collectors.toList()));

            LazyHtml content = new LazyHtml(listContestantClarificationsView.render(contest, contestClarifications, pageIndex, orderBy, orderDir, filterString, coach));

            if (contest.isClarificationTimeValid()) {
                content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("clarification.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createContestantClarification(contest.getId())), c));
            } else {
                content.appendLayout(c -> alertLayout.render(Messages.get("clarification.time_ended"), c));
                content.appendLayout(c -> headingLayout.render(Messages.get("clarification.list"), c));
            }
            if (isAllowedToSuperviseClarifications(contest)) {
                content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantClarifications(contest.getId()), routes.ContestController.viewSupervisorClarifications(contest.getId()), c));
            }
            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("clarification.clarifications"), routes.ContestController.viewContestantClarifications(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateContestantClarification(long contestId, long contestClarificationId) {
        Contest contest = contestService.findContestById(contestId);
        ContestClarification contestClarification = contestService.findContestClarificationByContestClarificationId(contestClarificationId);
        if (isCoach(contest) && !contestClarification.isAnswered() && contestClarification.getContestJid().equals(contest.getJid())) {
            ContestClarificationChangeForm contestClarificationChangeForm = new ContestClarificationChangeForm(contestClarification);
            Form<ContestClarificationChangeForm> form = Form.form(ContestClarificationChangeForm.class).fill(contestClarificationChangeForm);
            form = form.fill(contestClarificationChangeForm);

            return showUpdateContestantClarification(form, contest, contestClarification);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateContestantClarification(long contestId, long contestClarificationId) {
        Contest contest = contestService.findContestById(contestId);
        ContestClarification contestClarification = contestService.findContestClarificationByContestClarificationId(contestClarificationId);
        if (isCoach(contest) && !contestClarification.isAnswered() && contestClarification.getContestJid().equals(contest.getJid())) {
            Form<ContestClarificationChangeForm> form = Form.form(ContestClarificationChangeForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateContestantClarification(form, contest, contestClarification);
            } else {
                ContestClarificationChangeForm contestClarificationChangeForm = form.get();
                contestService.updateContestClarification(contestClarification.getId(), contestClarificationChangeForm.title, contestClarificationChangeForm.question);

                return redirect(routes.ContestController.viewContestantClarifications(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    /* contestant/scoreboard **************************************************************************************** */

    public Result viewContestantScoreboard(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if ((contest.isUsingScoreboard()) && (isAllowedToEnterContest(contest))) {
            ContestScoreboard contestScoreboard;
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            if ((contest.isStandard()) && ((new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigStandard.class)).getScoreboardFreezeTime() < System.currentTimeMillis()) && (!(new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigStandard.class)).isOfficialScoreboardAllowed())) {
                if (contestService.isContestScoreboardExistByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.FROZEN)) {
                    contestScoreboard = contestService.findContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.FROZEN);
                } else {
                    contestScoreboard = null;
                }
            } else {
                contestScoreboard = contestService.findContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL);
            }
            ScoreAdapter adapter = ScoreAdapters.fromContestStyle(contest.getStyle());
            LazyHtml content;
            if (contestScoreboard == null) {
                content = new LazyHtml(adapter.renderScoreboard(null, null, JidCacheService.getInstance(), IdentityUtils.getUserJid(), false, ImmutableSet.of()));
            } else {
                Scoreboard scoreboard = contestScoreboard.getScoreboard();
                if (contest.isIncognitoScoreboard()) {
                    if (isCoach(contest)) {
                        List<ContestTeamMember> contestTeamMembers = contestService.findContestTeamMembersByContestJidAndCoachJid(contest.getJid(), IdentityUtils.getUserJid());
                        content = new LazyHtml(adapter.renderScoreboard(scoreboard, contestScoreboard.getLastUpdateTime(), JidCacheService.getInstance(), IdentityUtils.getUserJid(), true, contestTeamMembers.stream().map(ct -> ct.getMemberJid()).collect(Collectors.toSet())));
                    } else {
                        content = new LazyHtml(adapter.renderScoreboard(scoreboard, contestScoreboard.getLastUpdateTime(), JidCacheService.getInstance(), IdentityUtils.getUserJid(), true, ImmutableSet.of(IdentityUtils.getUserJid())));
                    }
                } else {
                    content = new LazyHtml(adapter.renderScoreboard(scoreboard, contestScoreboard.getLastUpdateTime(), JidCacheService.getInstance(), IdentityUtils.getUserJid(), false, scoreboard.getState().getContestantJids().stream().collect(Collectors.toSet())));
                }
            }

            if (isAllowedToSuperviseScoreboard(contest)) {
                content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantScoreboard(contest.getId()), routes.ContestController.viewSupervisorScoreboard(contest.getId()), c));
            }

            appendTabsLayout(content, contest);
            appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                    new InternalLink(Messages.get("scoreboard.scoreboard"), routes.ContestController.viewContestantScoreboard(contest.getId()))
            ));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result createContestantClarification(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToDoContest(contest)) {
            if (contest.isClarificationTimeValid()) {
                Form<ContestClarificationCreateForm> form = Form.form(ContestClarificationCreateForm.class);

                return showCreateContestantClarification(form, contest);
            } else {
                return redirect(routes.ContestController.viewContestantClarifications(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateContestantClarification(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToDoContest(contest)) {
            if (contest.isClarificationTimeValid()) {
                Form<ContestClarificationCreateForm> form = Form.form(ContestClarificationCreateForm.class).bindFromRequest();

                if (form.hasErrors() || form.hasGlobalErrors()) {
                    return showCreateContestantClarification(form, contest);
                } else {
                    ContestClarificationCreateForm contestClarificationCreateForm = form.get();
                    contestService.createContestClarification(contest.getId(), contestClarificationCreateForm.title, contestClarificationCreateForm.question, contestClarificationCreateForm.topicJid);

                    return redirect(routes.ContestController.viewContestantClarifications(contest.getId()));
                }
            } else {
                return redirect(routes.ContestController.viewContestantClarifications(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }


    /* list ********************************************************************************************************* */

    public Result list(long pageIndex, String orderBy, String orderDir, String filterString) {
        Page<Contest> contests = contestService.pageAllowedContests(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, IdentityUtils.getUserJid(), isAdmin());

        LazyHtml content = new LazyHtml(listView.render(contests, pageIndex, orderBy, orderDir, filterString));
        if (isAdmin()) {
            content.appendLayout(c -> headingWithActionLayout.render(Messages.get("contest.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.create()), c));
        } else {
            content.appendLayout(c -> headingLayout.render(Messages.get("contest.list"), c));
        }

        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index())
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    /* ajax ********************************************************************************************************* */

    public Result unreadAnnouncement(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            long unreadCount = contestService.getUnreadContestAnnouncementsCount(IdentityUtils.getUserJid(), contest.getJid());
            ObjectNode objectNode = Json.newObject();
            objectNode.put("success", true);
            objectNode.put("count", unreadCount);
            return ok(objectNode);
        } else {
            ObjectNode objectNode = Json.newObject();
            objectNode.put("success", false);
            return ok(objectNode);
        }
    }

    public Result unreadClarification(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            long unreadCount = contestService.getUnreadContestClarificationsCount(IdentityUtils.getUserJid(), contest.getJid());
            ObjectNode objectNode = Json.newObject();
            objectNode.put("success", true);
            objectNode.put("count", unreadCount);
            return ok(objectNode);
        } else {
            ObjectNode objectNode = Json.newObject();
            objectNode.put("success", false);
            return ok(objectNode);
        }
    }

    public Result unansweredClarification(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseClarifications(contest)) {
            long unreadCount = contestService.getUnansweredContestClarificationsCount(contest.getJid());
            ObjectNode objectNode = Json.newObject();
            objectNode.put("success", true);
            objectNode.put("count", unreadCount);
            return ok(objectNode);
        } else {
            ObjectNode objectNode = Json.newObject();
            objectNode.put("success", false);
            return ok(objectNode);
        }
    }

    public Result renderTeamAvatarImage(String imageName) {
        File image = contestService.getTeamAvatarImageFile(imageName);
        if (!image.exists()) {
            return notFound();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        response().setHeader("Cache-Control", "no-transform,public,max-age=300,s-maxage=900");
        response().setHeader("Last-Modified", sdf.format(new Date(image.lastModified())));

        if (request().hasHeader("If-Modified-Since")) {
            try {
                Date lastUpdate = sdf.parse(request().getHeader("If-Modified-Since"));
                if (image.lastModified() > lastUpdate.getTime()) {
                    BufferedImage in = ImageIO.read(image);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    String type = FilenameUtils.getExtension(image.getAbsolutePath());

                    ImageIO.write(in, type, baos);
                    return ok(baos.toByteArray()).as("image/" + type);
                } else {
                    return status(304);
                }
            } catch (ParseException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                BufferedImage in = ImageIO.read(image);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                String type = FilenameUtils.getExtension(image.getAbsolutePath());

                ImageIO.write(in, type, baos);
                return ok(baos.toByteArray()).as("image/" + type);
            } catch (IOException e) {
                return internalServerError();
            }
        }
    }

    private void refreshFrozenScoreboard(Contest contest, ScoreAdapter adapter, ContestScoreState state) {
        ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
        List<Submission> submissions = submissionService.findAllSubmissionsByContestJidBeforeTime(contest.getJid(), new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigStandard.class).getScoreboardFreezeTime());

        ScoreboardContent content = adapter.computeScoreboardContent(state, submissions, contestService.getMapContestantJidToImageUrlInContest(contest.getJid()));
        Scoreboard scoreboard = adapter.createScoreboard(state, content);
        contestService.updateContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.FROZEN, scoreboard);
    }

    private Result showCreate(Form<ContestUpsertForm> form) {
        LazyHtml content = new LazyHtml(createAdminView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.create"), c));
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(Messages.get("contest.create"), routes.ContestController.create())
        ));
        appendTemplateLayout(content);
        return lazyOk(content);
    }


    private Result showCreateAdminManager(Form<ContestManagerCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createAdminManagerView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("manager.create"), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("manager.managers"), routes.ContestController.viewAdminManagers(contest.getId())),
                new InternalLink(Messages.get("manager.create"), routes.ContestController.createAdminManager(contest.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateManagerGeneral(Form<ContestUpsertForm> form, Contest contest) {
        LazyHtml content = new LazyHtml(updateManagerGeneralView.render(form, contest));
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contest.config.general"), routes.ContestController.updateManagerGeneral(contest.getId())), new InternalLink(Messages.get("contest.config.specific"), routes.ContestController.updateContestSpecificConfig(contest.getId()))), c));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.contest") + " #" + contest.getId() + ": " + contest.getName(), c));
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("commons.update"), routes.ContestController.updateManagerGeneral(contest.getId()))
        ));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    private Result showUpdateContestSpecificConfig(Form form1, Form form2, Form form3, Contest contest) {
        LazyHtml content = new LazyHtml(updateManagerSpecificView.render(contest, form1, form2, form3));
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contest.config.general"), routes.ContestController.updateManagerGeneral(contest.getId())), new InternalLink(Messages.get("contest.config.specific"), routes.ContestController.updateContestSpecificConfig(contest.getId()))), c));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.contest") + " #" + contest.getId() + ": " + contest.getName(), c));
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("commons.update"), routes.ContestController.updateManagerGeneral(contest.getId())),
                new InternalLink(Messages.get("contest.config.specific"), routes.ContestController.updateContestSpecificConfig(contest.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showCreateManagerSupervisor(Form<ContestSupervisorCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createManagerSupervisorView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("supervisor.create"), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestController.viewManagerSupervisors(contest.getId())),
                new InternalLink(Messages.get("supervisor.create"), routes.ContestController.createManagerSupervisor(contest.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateManagerSupervisor(Form<ContestSupervisorUpdateForm> form, Contest contest, ContestSupervisor contestSupervisor){
        LazyHtml content = new LazyHtml(updateManagerSupervisorView.render(contest.getId(), contestSupervisor.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("supervisor.update"), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestController.viewManagerSupervisors(contest.getId())),
                new InternalLink(Messages.get("supervisor.update"), routes.ContestController.viewManagerSupervisors(contest.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showCreateSupervisorAnnouncement(Form<ContestAnnouncementUpsertForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createSupervisorAnnouncementView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("announcement.create"), c));
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantAnnouncements(contest.getId()), routes.ContestController.viewSupervisorAnnouncements(contest.getId()), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.viewSupervisorAnnouncements(contest.getId())),
                new InternalLink(Messages.get("announcement.create"), routes.ContestController.createSupervisorAnnouncement(contest.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateSupervisorAnnouncement(Form<ContestAnnouncementUpsertForm> form, Contest contest, ContestAnnouncement contestAnnouncement){
        LazyHtml content = new LazyHtml(updateSupervisorAnnouncementView.render(contest.getId(), contestAnnouncement.getId(), form));
        content.appendLayout(c -> headingLayout.render(Messages.get("announcement.update"), c));
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantAnnouncements(contest.getId()), routes.ContestController.viewSupervisorAnnouncements(contest.getId()), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.viewContestantAnnouncements(contest.getId())),
                new InternalLink(Messages.get("announcement.update"), routes.ContestController.viewSupervisorAnnouncements(contest.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showCreateSupervisorProblem(Form<ContestProblemCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createSupervisorProblemView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("problem.create"), c));
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantProblems(contest.getId()), routes.ContestController.viewSupervisorProblems(contest.getId()), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("problem.problem"), routes.ContestController.viewContestantProblems(contest.getId())),
                new InternalLink(Messages.get("problem.update"), routes.ContestController.viewSupervisorProblems(contest.getId())),
                new InternalLink(Messages.get("problem.create"), routes.ContestController.createSupervisorProblem(contest.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateSupervisorProblem(Form<ContestProblemUpdateForm> form, Contest contest, ContestProblem contestProblem){
        LazyHtml content = new LazyHtml(updateSupervisorProblemView.render(contest.getId(), contestProblem.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("problem.update"), c));
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantProblems(contest.getId()), routes.ContestController.viewSupervisorProblems(contest.getId()), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("problem.problem"), routes.ContestController.viewContestantProblems(contest.getId())),
                new InternalLink(Messages.get("problem.update"), routes.ContestController.viewSupervisorProblems(contest.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateSupervisorClarification(Form<ContestClarificationUpdateForm> form, Contest contest, ContestClarification contestClarification){
        LazyHtml content = new LazyHtml(updateSupervisorClarificationView.render(contest.getId(), contestClarification, form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.update"), c));
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantClarifications(contest.getId()), routes.ContestController.viewSupervisorClarifications(contest.getId()), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("clarification.clarifications"), routes.ContestController.viewContestantClarifications(contest.getId())),
                new InternalLink(Messages.get("clarification.update"), routes.ContestController.viewSupervisorClarifications(contest.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showCreateSupervisorContestant(Form<ContestContestantCreateForm> form, Form<ContestContestantUploadForm> form2, Contest contest){
        LazyHtml content = new LazyHtml(createSupervisorContestantView.render(contest.getId(), form, form2));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.create"), c));
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestController.viewSupervisorTeams(contest.getId()))), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantProblems(contest.getId())),
                new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())),
                new InternalLink(Messages.get("contestant.create"), routes.ContestController.createSupervisorContestant(contest.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateSupervisorContestant(Form<ContestContestantUpdateForm> form, Contest contest, ContestContestant contestContestant){
        LazyHtml content = new LazyHtml(updateSupervisorContestantView.render(contest.getId(), contestContestant.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.update"), c));
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestController.viewSupervisorTeams(contest.getId()))), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("contestant.contestant"), routes.ContestController.viewSupervisorContestants(contest.getId())),
                new InternalLink(Messages.get("contestant.update"), routes.ContestController.updateSupervisorContestant(contest.getId(), contestContestant.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showCreateSupervisorTeam(Form<ContestTeamUpsertForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createSupervisorTeamView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("team.create"), c));
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestController.viewSupervisorTeams(contest.getId()))), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContestantProblems(contest.getId())),
                new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())),
                new InternalLink(Messages.get("team.teams"), routes.ContestController.viewSupervisorTeams(contest.getId())),
                new InternalLink(Messages.get("team.create"), routes.ContestController.createSupervisorTeam(contest.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateSupervisorTeam(Form<ContestTeamUpsertForm> form, Contest contest, ContestTeam contestTeam){
        LazyHtml content = new LazyHtml(updateSupervisorTeamView.render(contest.getId(), contestTeam.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("team.update"), c));
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestController.viewSupervisorTeams(contest.getId()))), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())),
                new InternalLink(Messages.get("team.teams"), routes.ContestController.viewSupervisorTeams(contest.getId())),
                new InternalLink(Messages.get("team.update"), routes.ContestController.updateSupervisorTeam(contest.getId(), contestTeam.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showViewSupervisorTeam(Form<ContestTeamCoachCreateForm> form, Form<ContestTeamMemberCreateForm> form2, Contest contest, ContestTeam contestTeam, List<ContestTeamCoach> contestTeamCoaches, List<ContestTeamMember> contestTeamMembers, boolean canUpdate) {
        LazyHtml content = new LazyHtml(viewSupervisorTeamView.render(contest.getId(), contestTeam, form, form2, contestTeamCoaches, contestTeamMembers, canUpdate));
        content.appendLayout(c -> heading3Layout.render(Messages.get("team.view"), c));
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestController.viewSupervisorTeams(contest.getId()))), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())),
                new InternalLink(Messages.get("team.teams"), routes.ContestController.viewSupervisorTeams(contest.getId())),
                new InternalLink(Messages.get("team.view"), routes.ContestController.viewSupervisorTeam(contest.getId(), contestTeam.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showCreateContestantClarification(Form<ContestClarificationCreateForm> form, Contest contest){
        List<ContestProblem> contestProblemList = contestService.findOpenedContestProblemByContestJid(contest.getJid());

        LazyHtml content = new LazyHtml(createContestantClarificationView.render(contest, form, contestProblemList));
        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.create"), c));
        if (isAllowedToSuperviseClarifications(contest)) {
            content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestController.viewContestantClarifications(contest.getId()), routes.ContestController.viewSupervisorClarifications(contest.getId()), c));
        }
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("clarification.clarifications"), routes.ContestController.viewContestantClarifications(contest.getId())),
                new InternalLink(Messages.get("clarification.create"), routes.ContestController.createContestantClarification(contest.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private Result showUpdateContestantClarification(Form<ContestClarificationChangeForm> form, Contest contest, ContestClarification contestClarification){
        LazyHtml content = new LazyHtml(updateContestantClarificationView.render(contest, contestClarification, form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.update"), c));
        appendTabsLayout(content, contest);
        appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.view(contest.getId())),
                new InternalLink(Messages.get("clarification.clarifications"), routes.ContestController.viewContestantClarifications(contest.getId())),
                new InternalLink(Messages.get("clarification.update"), routes.ContestController.updateContestantClarification(contest.getId(), contestClarification.getId()))
        ));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, List<InternalLink> links) {
        content.appendLayout(c -> breadcrumbsLayout.render(links, c));
    }

    private void appendTabsLayout(LazyHtml content, Contest contest) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.viewContestantAnnouncements(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("problem.problems"), routes.ContestController.viewContestantProblems(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("submission.submissions"), routes.ContestController.viewContestantSubmissions(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("clarification.clarifications"), routes.ContestController.viewContestantClarifications(contest.getId())));

        if (contest.isUsingScoreboard()) {
            internalLinkBuilder.add(new InternalLink(Messages.get("scoreboard.scoreboard"), routes.ContestController.viewContestantScoreboard(contest.getId())));
        }

        if (isSupervisorOrAbove(contest)) {
            internalLinkBuilder.add(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.viewSupervisorContestants(contest.getId())));
            internalLinkBuilder.add(new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestController.viewManagerSupervisors(contest.getId())));
            internalLinkBuilder.add(new InternalLink(Messages.get("manager.managers"), routes.ContestController.viewAdminManagers(contest.getId())));
        }

        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);

            content.appendLayout(c -> contestTimeLayout.render(contest.getStartTime(), new Date(contestContestant.getContestEnterTime() + contestTypeConfigVirtual.getContestDuration()), c));
        } else {
            content.appendLayout(c -> contestTimeLayout.render(contest.getStartTime(), contest.getEndTime(), c));
        }
        content.appendLayout(c -> tabLayout.render(internalLinkBuilder.build(), c));
        content.appendLayout(c -> headingLayout.render(contest.getName(), c));
    }

    private void appendSidebarLayout(LazyHtml content) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()));

        if (isAdmin()) {
            internalLinkBuilder.add(new InternalLink(Messages.get("userRole.userRoles"), routes.UserRoleController.index()));
        }

        content.appendLayout(c -> sidebarLayout.render(
            IdentityUtils.getUsername(),
            IdentityUtils.getUserRealName(),
            org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.profile(org.iatoki.judgels.uriel.controllers.routes.ApplicationController.afterProfile(routes.ContestController.index().absoluteURL(request())).absoluteURL(request())).absoluteURL(request()),
            org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.logout(routes.ApplicationController.index().absoluteURL(request())).absoluteURL(request()),
            internalLinkBuilder.build(), c)
        );
    }

    private void appendTemplateLayout(LazyHtml content) {
        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("Contests", c));
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

    private boolean isContestEnded(Contest contest) {
        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);

            return (System.currentTimeMillis() > (contestContestant.getContestEnterTime() + contestTypeConfigVirtual.getContestDuration()));
        } else {
            return new Date().after(contest.getEndTime());
        }
    }

    private boolean isAllowedToViewContest(Contest contest) {
        return isAdmin() || isManager(contest) || isSupervisor(contest) || contest.isPublic() || isContestant(contest);
    }

    private boolean isAllowedToManageContest(Contest contest) {
        return isAdmin() || isManager(contest);
    }

    private boolean isAllowedToManageSupervisors(Contest contest) {
        return isAdmin() || isManager(contest);
    }

    private boolean isAllowedToRegisterContest(Contest contest) {
        boolean result = !isContestant(contest) && !isContestEnded(contest);
        if (contest.isPublic()) {
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestScopeConfigPublic contestScopeConfigPublic = new Gson().fromJson(contestConfiguration.getScopeConfig(), ContestScopeConfigPublic.class);

            result = result && (contestScopeConfigPublic.getRegisterStartTime() < System.currentTimeMillis()) && (contestScopeConfigPublic.getRegisterEndTime() > System.currentTimeMillis()) && ((contestScopeConfigPublic.getMaxRegistrants() == 0) || (contestService.getContestContestantCount(contest.getJid()) < contestScopeConfigPublic.getMaxRegistrants()));
        } else {
            result = false;
        }

        return result;
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

    private boolean isAllowedToDoContest(Contest contest) {
        return isAdmin() || isManager(contest) || isSupervisor(contest) || (isContestant(contest) && (contestService.isContestEntered(contest.getJid(), IdentityUtils.getUserJid()))  && isContestStarted(contest) && !isContestEnded(contest));
    }

    private boolean isSupervisorOrAbove(Contest contest) {
        return isAdmin() || isManager(contest) || (isSupervisor(contest));
    }

    private boolean isAllowedToSuperviseAnnouncements(Contest contest) {
        return isAdmin() || isManager(contest) || (isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isAnnouncement());
    }

    private boolean isAllowedToSuperviseProblems(Contest contest) {
        return isAdmin() || isManager(contest) || (isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isProblem());
    }

    private boolean isAllowedToSuperviseSubmissions(Contest contest) {
        return isAdmin() || isManager(contest) || (isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isSubmission());
    }

    private boolean isAllowedToSuperviseClarifications(Contest contest) {
        return isAdmin() || isManager(contest) || (isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isClarification());
    }

    private boolean isAllowedToSuperviseContestants(Contest contest) {
        return isAdmin() || isManager(contest) || (isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isContestant());
    }

    private boolean isAllowedToSuperviseScoreboard(Contest contest) {
        return isAdmin() || isManager(contest) || isSupervisor(contest);
    }

    private boolean isAllowedToViewProblem(Contest contest, ContestProblem contestProblem) {
        return contestProblem.getContestJid().equals(contest.getJid()) && (isAdmin() || isAllowedToSuperviseProblems(contest) || contestProblem.getStatus() == ContestProblemStatus.OPEN);
    }

    private boolean isAllowedToViewSubmission(Contest contest, Submission submission) {
        return submission.getContestJid().equals(contest.getJid()) && (isAdmin() || isAllowedToSuperviseSubmissions(contest) || submission.getAuthorJid().equals(IdentityUtils.getUserJid()));
    }

    private Result tryEnteringContest(Contest contest) {
        if (isAllowedToEnterContest(contest)) {
            return redirect(routes.ContestController.viewContestantAnnouncements(contest.getId()));
        } else {
            return redirect(routes.ContestController.view(contest.getId()));
        }
    }
}
