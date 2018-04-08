package org.iatoki.judgels.uriel.contest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.EnumUtils;
import org.iatoki.judgels.jophiel.activity.BasicActivityKeys;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.actionsLayout;
import org.iatoki.judgels.play.views.html.layouts.alertLayout;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionsLayout;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.sandalphon.problem.programming.grading.LanguageRestrictionAdapter;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestant;
import org.iatoki.judgels.uriel.contest.contestant.organization.ContestContestantOrganization;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestantStatus;
import org.iatoki.judgels.uriel.contest.style.icpc.ICPCContestStyleConfig;
import org.iatoki.judgels.uriel.contest.scoreboard.ioi.IOIContestStyleConfig;
import org.iatoki.judgels.uriel.activity.UrielActivityKeys;
import org.iatoki.judgels.uriel.UrielControllerUtils;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.contest.style.ContestStyle;
import org.iatoki.judgels.uriel.contest.style.ContestStyleConfig;
import org.iatoki.judgels.uriel.contest.style.icpc.ICPCContestStyleConfigForm;
import org.iatoki.judgels.uriel.contest.style.ioi.IOIContestStyleConfigForm;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.Authorized;
import org.iatoki.judgels.uriel.controllers.securities.GuestView;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.contest.password.ContestEnterWithPasswordForm;
import org.iatoki.judgels.uriel.contest.module.ContestModule;
import org.iatoki.judgels.uriel.contest.module.ContestModuleUtils;
import org.iatoki.judgels.uriel.contest.module.ContestModules;
import org.iatoki.judgels.uriel.contest.module.organization.ContestOrganizationForm;
import org.iatoki.judgels.uriel.contest.module.registration.ContestRegistrationModule;
import org.iatoki.judgels.uriel.contest.contestant.password.ContestContestantPasswordService;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestantService;
import org.iatoki.judgels.uriel.contest.module.ContestModuleService;
import org.iatoki.judgels.uriel.contest.html.createContestView;
import org.iatoki.judgels.uriel.contest.html.editContestView;
import org.iatoki.judgels.uriel.contest.html.listContestsView;
import org.iatoki.judgels.uriel.contest.module.html.listModulesView;
import org.iatoki.judgels.uriel.contest.module.organization.html.organizationFormView;
import org.iatoki.judgels.uriel.contest.style.html.editContestSpecificView;
import org.iatoki.judgels.uriel.contest.html.viewContestView;
import org.iatoki.judgels.uriel.contest.html.viewContestWithPasswordView;
import org.iatoki.judgels.uriel.contest.html.viewRegistrantsLayoutView;
import org.iatoki.judgels.uriel.contest.html.viewVirtualContestLayout;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

@Singleton
public final class ContestController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String CONTEST = "contest";

    private final ContestContestantPasswordService contestContestantPasswordService;
    private final ContestContestantService contestContestantService;
    private final ContestModuleService contestModuleService;
    private final ContestService contestService;

    @Inject
    public ContestController(ContestContestantPasswordService contestContestantPasswordService, ContestContestantService contestContestantService, ContestModuleService contestModuleService, ContestService contestService) {
        this.contestContestantPasswordService = contestContestantPasswordService;
        this.contestContestantService = contestContestantService;
        this.contestModuleService = contestModuleService;
        this.contestService = contestService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result jumpToAnnouncements(long contestId) {
        return redirect(org.iatoki.judgels.uriel.contest.announcement.routes.ContestAnnouncementController.viewPublishedAnnouncements(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result jumpToProblems(long contestId) {
        return redirect(org.iatoki.judgels.uriel.contest.problem.routes.ContestProblemController.viewUsedProblems(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result jumpToSubmissions(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.submission.programming.routes.ContestProgrammingSubmissionController.viewSubmissions(contestId));
        }

        return redirect(org.iatoki.judgels.uriel.contest.submission.programming.routes.ContestProgrammingSubmissionController.viewScreenedSubmissions(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result jumpToScoreboards(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.scoreboard.routes.ContestScoreboardController.viewOfficialScoreboard(contestId));
        }

        return redirect(org.iatoki.judgels.uriel.contest.scoreboard.routes.ContestScoreboardController.viewScoreboard(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result jumpToClarifications(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.clarification.routes.ContestClarificationController.viewClarifications(contestId));
        }

        return redirect(org.iatoki.judgels.uriel.contest.clarification.routes.ContestClarificationController.viewScreenedClarifications(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result jumpToContestants(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        return redirect(org.iatoki.judgels.uriel.contest.contestant.routes.ContestContestantController.viewContestants(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result jumpToTeams(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isCoach(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.team.routes.ContestTeamController.viewScreenedTeams(contestId));
        }

        return redirect(org.iatoki.judgels.uriel.contest.team.routes.ContestTeamController.viewTeams(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result jumpToPasswords(long contestId) throws ContestNotFoundException {
        return redirect(org.iatoki.judgels.uriel.contest.password.routes.ContestPasswordController.viewContestantPasswords(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result jumpToSupervisors(long contestId) {
        return redirect(org.iatoki.judgels.uriel.contest.supervisor.routes.ContestSupervisorController.viewSupervisors(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result jumpToManagers(long contestId) {
        return redirect(org.iatoki.judgels.uriel.contest.manager.routes.ContestManagerController.viewManagers(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result jumpToFiles(long contestId) {
        return redirect(org.iatoki.judgels.uriel.contest.file.routes.ContestFileController.viewFiles(contestId));
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result index() {
        return listAllowedContests(0, "id", "desc", "");
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result listAllowedContests(long pageIndex, String orderBy, String orderDir, String filterString) {
        Page<Contest> pageOfContests;
        if (UrielControllerUtils.getInstance().isAdmin()) {
            pageOfContests = contestService.getPageOfContests(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
        } else {
            pageOfContests = contestService.getPageOfAllowedContests(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, IdentityUtils.getUserJid());
        }

        LazyHtml content = new LazyHtml(listContestsView.render(pageOfContests, pageIndex, orderBy, orderDir, filterString));
        if (UrielControllerUtils.getInstance().isAdmin()) {
            content.appendLayout(c -> headingWithActionLayout.render(Messages.get("contest.list"), new InternalLink(Messages.get("commons.create"), org.iatoki.judgels.uriel.contest.routes.ContestController.createContest()), c));
        } else {
            content.appendLayout(c -> headingLayout.render(Messages.get("contest.list"), c));
        }
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("contest.contests"), org.iatoki.judgels.uriel.contest.routes.ContestController.index())
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contests");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.containsModule(ContestModules.REGISTRATION)) {
            return viewContestAndListRegistrants(contestId, 0, "id", "desc", "");
        }

        if (!ContestControllerUtils.getInstance().isAllowedToViewContest(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.index());
        }

        LazyHtml content = getBasicViewContestContent(contest);

        ImmutableList.Builder<InternalLink> actionsBuilder = ImmutableList.builder();
        addBasicActionsInViewContest(contest, actionsBuilder);
        content.appendLayout(c -> actionsLayout.render(actionsBuilder.build(), false, c));

        if (contest.isLocked()) {
            content.appendLayout(c -> alertLayout.render(Messages.get("contest.isLocked"), c));
        }
        if (!UrielUtils.isGuest() && ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            ImmutableList.Builder<InternalLink> manageActionsBuilder = ImmutableList.builder();
            manageActionsBuilder.add(new InternalLink(Messages.get("commons.update"), org.iatoki.judgels.uriel.contest.routes.ContestController.editContestGeneralConfig(contest.getId())));
            if ((UrielControllerUtils.getInstance().isAdmin()) && (ContestControllerUtils.getInstance().hasContestEnded(contest))) {
                if (contest.isLocked()) {
                    manageActionsBuilder.add(new InternalLink(Messages.get("contest.unlock"), org.iatoki.judgels.uriel.contest.routes.ContestController.unlockContest(contest.getId())));
                } else {
                    manageActionsBuilder.add(new InternalLink(Messages.get("contest.lock"), org.iatoki.judgels.uriel.contest.routes.ContestController.lockContest(contest.getId())));
                }
            }

            content.appendLayout(c -> headingWithActionsLayout.render(contest.getName(), manageActionsBuilder.build(), c));
        } else {
            content.appendLayout(c -> headingLayout.render(contest.getName(), c));
        }
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), org.iatoki.judgels.uriel.contest.routes.ContestController.index()),
                new InternalLink(contest.getName(), org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contest.getId()))
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - View");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewContestAndListRegistrants(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToViewContest(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.index());
        }

        LazyHtml content = getBasicViewContestContent(contest);

        ImmutableList.Builder<InternalLink> actionsBuilder = ImmutableList.builder();

        if (!UrielUtils.isGuest() && ContestControllerUtils.getInstance().isAllowedToRegisterContest(contest, IdentityUtils.getUserJid())) {
            actionsBuilder.add(new InternalLink(Messages.get("contest.register"), org.iatoki.judgels.uriel.contest.routes.ContestController.registerToAContest(contest.getId())));
        }

        if (!UrielUtils.isGuest() && ContestControllerUtils.getInstance().isAllowedToUnregisterContest(contest, IdentityUtils.getUserJid())) {
            actionsBuilder.add(new InternalLink(Messages.get("contest.unregister"), org.iatoki.judgels.uriel.contest.routes.ContestController.unregisterFromAContest(contest.getId())));
        }
        addBasicActionsInViewContest(contest, actionsBuilder);

        content.appendLayout(c -> actionsLayout.render(actionsBuilder.build(), false, c));

        Page<ContestContestant> pageOfContestContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
        content.appendLayout(c -> viewRegistrantsLayoutView.render(contest, pageOfContestContestants, pageIndex, orderBy, orderDir, filterString, c));

        if (!UrielUtils.isGuest() && ContestControllerUtils.getInstance().isContestant(contest, IdentityUtils.getUserJid()) && !ContestControllerUtils.getInstance().hasContestEnded(contest)) {
            content.appendLayout(c -> alertLayout.render(Messages.get("contest.registeredAndNotStarted"), c));
        }

        if (!UrielUtils.isGuest() && ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            content.appendLayout(c -> headingWithActionLayout.render(contest.getName(), new InternalLink(Messages.get("commons.update"), org.iatoki.judgels.uriel.contest.routes.ContestController.editContestGeneralConfig(contest.getId())), c));
        } else {
            content.appendLayout(c -> headingLayout.render(contest.getName(), c));
        }
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("contest.contests"), org.iatoki.judgels.uriel.contest.routes.ContestController.index()),
              new InternalLink(contest.getName(), org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contest.getId()))
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - View");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result registerToAContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToRegisterContest(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.index());
        }

        if (contest.containsModule(ContestModules.ORGANIZATION)) {
            return inputOrganizationRegistration(contestId);
        }

        ContestRegistrationModule contestRegistrationModule = (ContestRegistrationModule) contest.getModule(ContestModules.REGISTRATION);
        ContestContestantStatus contestContestantStatus = ContestContestantStatus.APPROVED;
        if (contestRegistrationModule.isManualApproval()) {
            contestContestantStatus = ContestContestantStatus.IN_CONFIRMATION;
        }
        contestContestantService.createContestContestant(contest.getJid(), IdentityUtils.getUserJid(), contestContestantStatus, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(UrielActivityKeys.REGISTER.construct(CONTEST, contest.getJid(), contest.getName()));

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result inputOrganizationRegistration(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToRegisterContest(contest, IdentityUtils.getUserJid()) || !contest.containsModule(ContestModules.ORGANIZATION)) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.index());
        }

        Form<ContestOrganizationForm> form = Form.form(ContestOrganizationForm.class);

        LazyHtml content = new LazyHtml(organizationFormView.render(contestId, form));
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Register Contest");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postRegisterToAContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToRegisterContest(contest, IdentityUtils.getUserJid()) || !contest.containsModule(ContestModules.ORGANIZATION)) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.index());
        }

        Form<ContestOrganizationForm> form = Form.form(ContestOrganizationForm.class).bindFromRequest();
        String organization = form.get().organization;

        ContestRegistrationModule contestRegistrationModule = (ContestRegistrationModule) contest.getModule(ContestModules.REGISTRATION);
        ContestContestantStatus contestContestantStatus = ContestContestantStatus.APPROVED;
        if (contestRegistrationModule.isManualApproval()) {
            contestContestantStatus = ContestContestantStatus.IN_CONFIRMATION;
        }
        contestContestantService.createContestContestant(contest.getJid(), IdentityUtils.getUserJid(), contestContestantStatus, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        contestContestantService.createContestContestantOrganization(contest.getJid(), IdentityUtils.getUserJid(), organization, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(UrielActivityKeys.REGISTER.construct(CONTEST, contest.getJid(), contest.getName()));

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result unregisterFromAContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToUnregisterContest(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.index());
        }

        ContestContestant contestContestant = contestContestantService.findContestantInContestAndJid(contest.getJid(), IdentityUtils.getUserJid());
        contestContestantService.deleteContestContestant(contestContestant.getId());

        if (contest.containsModule(ContestModules.ORGANIZATION)) {
            ContestContestantOrganization contestContestantOrganization = contestContestantService.findContestantOrganizationInContestAndJid(contest.getJid(), IdentityUtils.getUserJid());
            contestContestantService.deleteContestContestantOrganization(contestContestantOrganization.getId());
        }

        UrielControllerUtils.getInstance().addActivityLog(UrielActivityKeys.UNREGISTER.construct(CONTEST, contest.getJid(), contest.getName()));

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result enterContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.index());
        }

        if (!ContestControllerUtils.getInstance().isCoachOrAbove(contest, IdentityUtils.getUserJid()) && !(contest.containsModule(ContestModules.REGISTRATION) || contest.containsModule(ContestModules.LIMITED)) && !ContestControllerUtils.getInstance().isContestant(contest, IdentityUtils.getUserJid())) {
            contestContestantService.createContestContestant(contest.getJid(), IdentityUtils.getUserJid(), ContestContestantStatus.APPROVED, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToAnnouncements(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result enterContestWithPassword(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToViewContest(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.index());
        }

        Form<ContestEnterWithPasswordForm> contestEnterWithPasswordForm = Form.form(ContestEnterWithPasswordForm.class).bindFromRequest();

        if (formHasErrors(contestEnterWithPasswordForm)) {
            flash("password", Messages.get("contestant.password.incorrect"));
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contest.getId()));
        }

        String password = contestEnterWithPasswordForm.get().password;
        String correctPassword = contestContestantPasswordService.getContestantPassword(contest.getJid(), IdentityUtils.getUserJid());

        if (correctPassword == null) {
            flash("password", Messages.get("contestant.password.notAvailable"));
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contest.getId()));
        } else if (!correctPassword.equals(password)) {
            flash("password", Messages.get("contestant.password.incorrect"));
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contest.getId()));
        }

        ContestControllerUtils.getInstance().establishContestWithPasswordCookie(correctPassword);

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToAnnouncements(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result startContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToStartContestAsContestant(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contest.getId()));
        }

        contestContestantService.startContestAsContestant(contest.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(UrielActivityKeys.START.construct(CONTEST, contest.getJid(), contest.getName()));

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToAnnouncements(contest.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createContest() {
        Form<ContestUpsertForm> contestUpsertForm = Form.form(ContestUpsertForm.class);

        return showCreateContest(contestUpsertForm);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    @RequireCSRFCheck
    public Result postCreateContest() {
        Form<ContestUpsertForm> contestUpsertForm = Form.form(ContestUpsertForm.class).bindFromRequest();

        if (formHasErrors(contestUpsertForm)) {
            return showCreateContest(contestUpsertForm);
        }

        ContestUpsertForm contestUpsertData = contestUpsertForm.get();
        Contest contest = contestService.createContest(
                contestUpsertData.name,
                JudgelsPlayUtils.toSafeHtml(contestUpsertData.description),
                ContestStyle.valueOf(contestUpsertData.style),
                new Date(JudgelsPlayUtils.parseDateTime(contestUpsertData.beginTime)),
                contestUpsertData.duration,
                IdentityUtils.getUserJid(),
                IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.CREATE.construct(CONTEST, contest.getJid(), contest.getName()));

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.editContestSpecificConfig(contest.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editContestGeneralConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestUpsertForm contestUpsertData = new ContestUpsertForm();
        contestUpsertData.name = contest.getName();
        contestUpsertData.description = contest.getDescription();
        contestUpsertData.style = contest.getStyle().name();
        contestUpsertData.beginTime = JudgelsPlayUtils.formatDateTime(contest.getBeginTime().getTime());
        contestUpsertData.duration = contest.getDuration();
        Form<ContestUpsertForm> contestUpsertForm = Form.form(ContestUpsertForm.class).fill(contestUpsertData);

        return showEditContestGeneralConfig(contestUpsertForm, contest);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postEditContestGeneralConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (contest.isLocked() || !ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.editContestGeneralConfig(contestId));
        }

        Form<ContestUpsertForm> contestUpsertForm = Form.form(ContestUpsertForm.class).bindFromRequest();
        if (formHasErrors(contestUpsertForm)) {
            return showEditContestGeneralConfig(contestUpsertForm, contest);
        }

        ContestUpsertForm contestUpsertData = contestUpsertForm.get();
        contestService.updateContest(
                contest.getJid(),
                contestUpsertData.name,
                JudgelsPlayUtils.toSafeHtml(contestUpsertData.description),
                ContestStyle.valueOf(contestUpsertData.style),
                new Date(JudgelsPlayUtils.parseDateTime(contestUpsertData.beginTime)),
                contestUpsertData.duration,
                IdentityUtils.getUserJid(),
                IdentityUtils.getIpAddress());

        if (!contest.getName().equals(contestUpsertData.name)) {
            UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.RENAME.construct(CONTEST, contest.getJid(), contest.getName(), contestUpsertData.name));
        }
        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.EDIT.construct(CONTEST, contest.getJid(), contestUpsertData.name));

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.editContestGeneralConfig(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result editContestModuleConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        LazyHtml content = new LazyHtml(listModulesView.render(contest));
        appendConfigSubtabLayout(content, contest);
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("commons.update"), org.iatoki.judgels.uriel.contest.routes.ContestController.editContestModuleConfig(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Update Module");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result enableModule(long contestId, String contestModule) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !EnumUtils.isValidEnum(ContestModules.class, contestModule) || !ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.editContestModuleConfig(contest.getId()));
        }

        ContestModules contestModuleType = ContestModules.valueOf(contestModule);
        if (!ContestModuleUtils.getModuleContradiction(contestModuleType).isEmpty() && contest.getModulesSet().containsAll(ContestModuleUtils.getModuleContradiction(contestModuleType))) {
            flashError(Messages.get("contest.module.enable.error.contradiction", ContestModuleUtils.getModuleContradiction(contestModuleType).toString()));
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.editContestModuleConfig(contest.getId()));
        }

        if (!contest.getModulesSet().containsAll(ContestModuleUtils.getModuleDependencies(contestModuleType))) {
            flashError(Messages.get("contest.module.enable.error.dependencies", ContestModuleUtils.getModuleDependencies(contestModuleType).toString()));
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.editContestModuleConfig(contest.getId()));
        }

        contestModuleService.enableModule(contest.getJid(), contestModuleType, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.editContestModuleConfig(contest.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result disableModule(long contestId, String contestModule) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !EnumUtils.isValidEnum(ContestModules.class, contestModule) || !ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.editContestModuleConfig(contest.getId()));
        }

        ContestModules contestModuleType = ContestModules.valueOf(contestModule);
        if (contest.getModulesSet().containsAll(ContestModuleUtils.getDependedModules(contestModuleType)) && !ContestModuleUtils.getDependedModules(contestModuleType).isEmpty()) {
            flashError(Messages.get("contest.module.disable.error.dependencies", ContestModuleUtils.getDependedModules(contestModuleType).toString()));
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.editContestModuleConfig(contest.getId()));
        }

        contestModuleService.disableModule(contest.getJid(), contestModuleType, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.editContestModuleConfig(contest.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editContestSpecificConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form styleConfigForm = null;

        if (contest.isICPC()) {
            ICPCContestStyleConfig icpcContestStyleConfig = (ICPCContestStyleConfig) contest.getStyleConfig();
            Form<ICPCContestStyleConfigForm> icpcStyleConfigForm = Form.form(ICPCContestStyleConfigForm.class);

            ICPCContestStyleConfigForm formData = new ICPCContestStyleConfigForm();
            formData.wrongSubmissionPenalty = icpcContestStyleConfig.getWrongSubmissionPenalty();
            formData.isAllowedAll = LanguageRestrictionAdapter.getFormIsAllowedAllFromLanguageRestriction(icpcContestStyleConfig.getLanguageRestriction());
            formData.allowedLanguageNames = LanguageRestrictionAdapter.getFormAllowedLanguageNamesFromLanguageRestriction(icpcContestStyleConfig.getLanguageRestriction());
            icpcStyleConfigForm = icpcStyleConfigForm.fill(formData);
            styleConfigForm = icpcStyleConfigForm;
        } else if (contest.isIOI()) {
            IOIContestStyleConfig ioiContestStyleConfig = (IOIContestStyleConfig) contest.getStyleConfig();
            Form<IOIContestStyleConfigForm> ioiStyleConfigForm = Form.form(IOIContestStyleConfigForm.class);

            IOIContestStyleConfigForm formData = new IOIContestStyleConfigForm();
            formData.usingLastAffectingPenalty = ioiContestStyleConfig.usingLastAffectingPenalty();
            formData.isAllowedAll = LanguageRestrictionAdapter.getFormIsAllowedAllFromLanguageRestriction(ioiContestStyleConfig.getLanguageRestriction());
            formData.allowedLanguageNames = LanguageRestrictionAdapter.getFormAllowedLanguageNamesFromLanguageRestriction(ioiContestStyleConfig.getLanguageRestriction());

            ioiStyleConfigForm = ioiStyleConfigForm.fill(formData);
            styleConfigForm = ioiStyleConfigForm;
        }

        ImmutableMap.Builder<ContestModule, Form<?>> moduleFormMapBuilder = ImmutableMap.builder();
        for (ContestModule contestModule : contest.getModules()) {
            moduleFormMapBuilder.put(contestModule, contestModule.generateConfigForm());
        }

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.EDIT.construct(CONTEST, contest.getJid(), contest.getName()));

        return showEditContestSpecificConfig(contest, styleConfigForm, moduleFormMapBuilder.build());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postEditContestSpecificConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (contest.isLocked() || !ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.editContestSpecificConfig(contest.getId()));
        }

        Form styleConfigForm = null;
        if (contest.isICPC()) {
            styleConfigForm = Form.form(ICPCContestStyleConfigForm.class).bindFromRequest();
        } else if (contest.isIOI()) {
            styleConfigForm = Form.form(IOIContestStyleConfigForm.class).bindFromRequest();
        }

        boolean checkError = false;
        ImmutableMap.Builder<ContestModule, Form<?>> moduleFormMap = ImmutableMap.builder();
        ImmutableList.Builder<ContestModule> updatedContestModuleBuilder = ImmutableList.builder();
        for (ContestModule contestModule : contest.getModules()) {
            Form<?> moduleForm = contestModule.updateModuleByFormFromRequest(request());
            moduleFormMap.put(contestModule, moduleForm);
            updatedContestModuleBuilder.add(contestModule);
            if (formHasErrors(moduleForm)) {
                checkError = true;
            }
        }

        if (formHasErrors(styleConfigForm) || checkError) {
            return showEditContestSpecificConfig(contest, styleConfigForm, moduleFormMap.build());
        }

        ContestStyleConfig contestStyleConfig = null;
        if (contest.isICPC()) {
            ICPCContestStyleConfigForm formData = (ICPCContestStyleConfigForm) styleConfigForm.get();
            contestStyleConfig = new ICPCContestStyleConfig(formData.wrongSubmissionPenalty, LanguageRestrictionAdapter.createLanguageRestrictionFromForm(formData.allowedLanguageNames, formData.isAllowedAll));
        } else if (contest.isIOI()) {
            IOIContestStyleConfigForm formData = (IOIContestStyleConfigForm) styleConfigForm.get();
            contestStyleConfig = new IOIContestStyleConfig(formData.usingLastAffectingPenalty, LanguageRestrictionAdapter.createLanguageRestrictionFromForm(formData.allowedLanguageNames, formData.isAllowedAll));
        }

        contestService.updateContestStyleConfiguration(contest.getJid(), contestStyleConfig, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        contestService.updateContestModuleConfiguration(contest.getJid(), updatedContestModuleBuilder.build(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.EDIT.construct(CONTEST, contest.getJid(), contest.getName()));

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.editContestSpecificConfig(contest.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized("admin")
    @Transactional
    public Result lockContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (contest.isLocked() || !ContestControllerUtils.getInstance().hasContestEnded(contest)) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contest.getId()));
        }

        contestService.lockContest(contest.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(UrielActivityKeys.LOCK.construct(CONTEST, contest.getJid(), contest.getName()));

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contest.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized("admin")
    @Transactional
    public Result unlockContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contest.isLocked() || !ContestControllerUtils.getInstance().hasContestEnded(contest)) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contest.getId()));
        }

        contestService.unlockContest(contest.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(UrielActivityKeys.UNLOCK.construct(CONTEST, contest.getJid(), contest.getName()));

        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contest.getId()));
    }

    private LazyHtml getBasicViewContestContent(Contest contest) {
        LazyHtml content;
        if (ContestControllerUtils.getInstance().requiresPasswordToEnterContest(contest, IdentityUtils.getUserJid())) {
            Form<ContestEnterWithPasswordForm> passwordForm = Form.form(ContestEnterWithPasswordForm.class);
            content = new LazyHtml(viewContestWithPasswordView.render(contest, passwordForm));
        } else {
            content = new LazyHtml(viewContestView.render(contest));
        }
        if (contest.containsModule(ContestModules.VIRTUAL)) {
            content.appendLayout(c -> viewVirtualContestLayout.render(c));
        }

        return content;
    }

    private void addBasicActionsInViewContest(Contest contest, ImmutableList.Builder<InternalLink> actionsBuilder) {
        if (ContestControllerUtils.getInstance().isAllowedToStartContestAsContestant(contest, IdentityUtils.getUserJid())) {
            actionsBuilder.add(new InternalLink(Messages.get("contest.start"), org.iatoki.judgels.uriel.contest.routes.ContestController.startContest(contest.getId())));
        }

        if (ContestControllerUtils.getInstance().isAllowedToViewEnterContestButton(contest, IdentityUtils.getUserJid())) {
            actionsBuilder.add(new InternalLink(Messages.get("contest.enter"), org.iatoki.judgels.uriel.contest.routes.ContestController.enterContest(contest.getId())));
        }
    }

    private Result showCreateContest(Form<ContestUpsertForm> contestUpsertForm) {
        LazyHtml content = new LazyHtml(createContestView.render(contestUpsertForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.create"), c));
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), org.iatoki.judgels.uriel.contest.routes.ContestController.index()),
                new InternalLink(Messages.get("contest.create"), org.iatoki.judgels.uriel.contest.routes.ContestController.createContest())
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Create");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditContestGeneralConfig(Form<ContestUpsertForm> contestUpsertForm, Contest contest) {
        LazyHtml content = new LazyHtml(editContestView.render(contestUpsertForm, contest));
        appendConfigSubtabLayout(content, contest);
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("commons.update"), org.iatoki.judgels.uriel.contest.routes.ContestController.editContestGeneralConfig(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Update General");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditContestSpecificConfig(Contest contest, Form contestStyleForm, Map<ContestModule, Form<?>> moduleFormMap) {
        LazyHtml content = new LazyHtml(editContestSpecificView.render(contest, contestStyleForm, moduleFormMap));
        appendConfigSubtabLayout(content, contest);
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("contest.config.specific"), org.iatoki.judgels.uriel.contest.routes.ContestController.editContestSpecificConfig(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Update Specific");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendConfigSubtabLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.config.general"), org.iatoki.judgels.uriel.contest.routes.ContestController.editContestGeneralConfig(contest.getId())),
                new InternalLink(Messages.get("contest.config.module"), org.iatoki.judgels.uriel.contest.routes.ContestController.editContestModuleConfig(contest.getId())),
                new InternalLink(Messages.get("contest.config.specific"), org.iatoki.judgels.uriel.contest.routes.ContestController.editContestSpecificConfig(contest.getId()))
        ), c));
        if (contest.isLocked()) {
            content.appendLayout(c -> alertLayout.render(Messages.get("contest.isLocked"), c));
        }
        ImmutableList.Builder<InternalLink> actionsBuilder = ImmutableList.builder();
        actionsBuilder.add(new InternalLink(Messages.get("contest.enter"), org.iatoki.judgels.uriel.contest.routes.ContestController.enterContest(contest.getId())));
        if ((UrielControllerUtils.getInstance().isAdmin()) && (ContestControllerUtils.getInstance().hasContestEnded(contest))) {
            if (contest.isLocked()) {
                actionsBuilder.add(new InternalLink(Messages.get("contest.unlock"), org.iatoki.judgels.uriel.contest.routes.ContestController.unlockContest(contest.getId())));
            } else {
                actionsBuilder.add(new InternalLink(Messages.get("contest.lock"), org.iatoki.judgels.uriel.contest.routes.ContestController.lockContest(contest.getId())));
            }
        }
        content.appendLayout(c -> headingWithActionsLayout.render("#" + contest.getId() + ": " + contest.getName(), actionsBuilder.build(), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

}
