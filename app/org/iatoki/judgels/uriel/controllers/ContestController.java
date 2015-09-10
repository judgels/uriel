package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
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
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.sandalphon.LanguageRestrictionAdapter;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.ContestStyleConfig;
import org.iatoki.judgels.uriel.ICPCContestStyleConfig;
import org.iatoki.judgels.uriel.IOIContestStyleConfig;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.Authorized;
import org.iatoki.judgels.uriel.controllers.securities.GuestView;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.forms.ContestEnterWithPasswordForm;
import org.iatoki.judgels.uriel.forms.ContestUpsertForm;
import org.iatoki.judgels.uriel.forms.ICPCContestStyleConfigForm;
import org.iatoki.judgels.uriel.forms.IOIContestStyleConfigForm;
import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.modules.ContestModuleComparator;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.services.ContestContestantPasswordService;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestModuleService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.views.html.contest.createContestView;
import org.iatoki.judgels.uriel.views.html.contest.listContestsView;
import org.iatoki.judgels.uriel.views.html.contest.modules.listModulesView;
import org.iatoki.judgels.uriel.views.html.contest.specific.updateContestSpecificView;
import org.iatoki.judgels.uriel.views.html.contest.updateContestView;
import org.iatoki.judgels.uriel.views.html.contest.viewContestView;
import org.iatoki.judgels.uriel.views.html.contest.viewContestWithPasswordView;
import org.iatoki.judgels.uriel.views.html.contest.viewRegistrantsLayoutView;
import org.iatoki.judgels.uriel.views.html.contest.viewVirtualContestLayout;
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
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;

@Singleton
@Named
public final class ContestController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

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
        return redirect(routes.ContestAnnouncementController.viewPublishedAnnouncements(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result jumpToProblems(long contestId) {
        return redirect(routes.ContestProblemController.viewUsedProblems(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result jumpToSubmissions(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest, IdentityUtils.getUserJid())) {
            return redirect(routes.ContestProgrammingSubmissionController.viewSubmissions(contestId));
        }

        return redirect(routes.ContestProgrammingSubmissionController.viewScreenedSubmissions(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result jumpToScoreboard(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest, IdentityUtils.getUserJid())) {
            return redirect(routes.ContestScoreboardController.viewOfficialScoreboard(contestId));
        }

        return redirect(routes.ContestScoreboardController.viewScoreboard(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result jumpToClarifications(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest, IdentityUtils.getUserJid())) {
            return redirect(routes.ContestClarificationController.viewClarifications(contestId));
        }

        return redirect(routes.ContestClarificationController.viewScreenedClarifications(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result jumpToContestants(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        return redirect(routes.ContestContestantController.viewContestants(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result jumpToTeams(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isCoach(contest, IdentityUtils.getUserJid())) {
            return redirect(routes.ContestTeamController.viewScreenedTeams(contestId));
        }

        return redirect(routes.ContestTeamController.viewTeams(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result jumpToPasswords(long contestId) throws ContestNotFoundException {
        return redirect(routes.ContestPasswordController.viewContestantPasswords(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result jumpToSupervisors(long contestId) {
        return redirect(routes.ContestSupervisorController.viewSupervisors(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result jumpToManagers(long contestId) {
        return redirect(routes.ContestManagerController.viewManagers(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result jumpToFiles(long contestId) {
        return redirect(routes.ContestFileController.viewFiles(contestId));
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
            content.appendLayout(c -> headingWithActionLayout.render(Messages.get("contest.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createContest()), c));
        } else {
            content.appendLayout(c -> headingLayout.render(Messages.get("contest.list"), c));
        }
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("contest.contests"), routes.ContestController.index())
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contests");

        UrielControllerUtils.getInstance().addActivityLog("Open list of allowed contests <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

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
            return redirect(routes.ContestController.index());
        }

        LazyHtml content = getBasicViewContestContent(contest);

        ImmutableList.Builder<InternalLink> actionsBuilder = ImmutableList.builder();
        addBasicActionsInViewContest(contest, actionsBuilder);
        content.appendLayout(c -> actionsLayout.render(actionsBuilder.build(), false, c));

        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            content.appendLayout(c -> headingWithActionLayout.render(contest.getName(), new InternalLink(Messages.get("commons.update"), routes.ContestController.updateContestGeneralConfig(contest.getId())), c));
        } else {
            content.appendLayout(c -> headingLayout.render(contest.getName(), c));
        }
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId()))
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - View");

        UrielControllerUtils.getInstance().addActivityLog("View contest " + contest.getName() + "  <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewContestAndListRegistrants(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToViewContest(contest, IdentityUtils.getUserJid())) {
            return redirect(routes.ContestController.index());
        }

        LazyHtml content = getBasicViewContestContent(contest);

        ImmutableList.Builder<InternalLink> actionsBuilder = ImmutableList.builder();

        if (ContestControllerUtils.getInstance().isAllowedToRegisterContest(contest, IdentityUtils.getUserJid())) {
            actionsBuilder.add(new InternalLink(Messages.get("contest.register"), routes.ContestController.registerToAContest(contest.getId())));
        }

        if (ContestControllerUtils.getInstance().isAllowedToUnregisterContest(contest, IdentityUtils.getUserJid())) {
            actionsBuilder.add(new InternalLink(Messages.get("contest.unregister"), routes.ContestController.unregisterFromAContest(contest.getId())));
        }
        addBasicActionsInViewContest(contest, actionsBuilder);

        content.appendLayout(c -> actionsLayout.render(actionsBuilder.build(), false, c));

        Page<ContestContestant> pageOfContestContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
        content.appendLayout(c -> viewRegistrantsLayoutView.render(contest, pageOfContestContestants, pageIndex, orderBy, orderDir, filterString, c));

        if (ContestControllerUtils.getInstance().isContestant(contest, IdentityUtils.getUserJid()) && !ContestControllerUtils.getInstance().hasContestEnded(contest)) {
            content.appendLayout(c -> alertLayout.render(Messages.get("contest.registeredAndNotStarted"), c));
        }

        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            content.appendLayout(c -> headingWithActionLayout.render(contest.getName(), new InternalLink(Messages.get("commons.update"), routes.ContestController.updateContestGeneralConfig(contest.getId())), c));
        } else {
            content.appendLayout(c -> headingLayout.render(contest.getName(), c));
        }
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
              new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId()))
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - View");

        UrielControllerUtils.getInstance().addActivityLog("View contest " + contest.getName() + "  <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result registerToAContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToRegisterContest(contest, IdentityUtils.getUserJid())) {
            return redirect(routes.ContestController.index());
        }

        contestContestantService.createContestContestant(contest.getJid(), IdentityUtils.getUserJid(), ContestContestantStatus.APPROVED, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog("Register to contest " + contest.getName() + "  <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.ContestController.viewContest(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result unregisterFromAContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToUnregisterContest(contest, IdentityUtils.getUserJid())) {
            return redirect(routes.ContestController.index());
        }

        ContestContestant contestContestant = contestContestantService.findContestantInContestAndJid(contest.getJid(), IdentityUtils.getUserJid());
        contestContestantService.deleteContestContestant(contestContestant.getId());

        UrielControllerUtils.getInstance().addActivityLog("Unregister from contest " + contest.getName() + "  <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.ContestController.viewContest(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result enterContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid())) {
            return redirect(routes.ContestController.index());
        }

        if (!ContestControllerUtils.getInstance().isCoachOrAbove(contest, IdentityUtils.getUserJid()) && ContestControllerUtils.getInstance().isLegitimateContestant(contest, IdentityUtils.getUserJid()) && !ContestControllerUtils.getInstance().isContestant(contest, IdentityUtils.getUserJid())) {
            contestContestantService.createContestContestant(contest.getJid(), IdentityUtils.getUserJid(), ContestContestantStatus.APPROVED, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        UrielControllerUtils.getInstance().addActivityLog("Enter contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.ContestController.jumpToAnnouncements(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result enterContestWithPassword(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToViewEnterContestButton(contest, IdentityUtils.getUserJid())) {
            return redirect(routes.ContestController.index());
        }

        Form<ContestEnterWithPasswordForm> contestEnterWithPasswordForm = Form.form(ContestEnterWithPasswordForm.class).bindFromRequest();

        if (formHasErrors(contestEnterWithPasswordForm)) {
            flash("password", Messages.get("contestant.password.incorrect"));
            return redirect(routes.ContestController.viewContest(contest.getId()));
        }

        String password = contestEnterWithPasswordForm.get().password;
        String correctPassword = contestContestantPasswordService.getContestantPassword(contest.getJid(), IdentityUtils.getUserJid());

        if (correctPassword == null) {
            flash("password", Messages.get("contestant.password.notAvailable"));
            return redirect(routes.ContestController.viewContest(contest.getId()));
        } else if (!correctPassword.equals(password)) {
            flash("password", Messages.get("contestant.password.incorrect"));
            return redirect(routes.ContestController.viewContest(contest.getId()));
        }

        ContestControllerUtils.getInstance().establishContestWithPasswordCookie(correctPassword);

        UrielControllerUtils.getInstance().addActivityLog("Enter contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.ContestController.jumpToAnnouncements(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result startContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToStartContestAsContestant(contest, IdentityUtils.getUserJid())) {
            return redirect(routes.ContestController.viewContest(contest.getId()));
        }

        contestContestantService.startContestAsContestant(contest.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog("Enter contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createContest() {
        Form<ContestUpsertForm> contestUpsertForm = Form.form(ContestUpsertForm.class);

        UrielControllerUtils.getInstance().addActivityLog("Try to create a contest.");

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
        Contest contest = contestService.createContest(contestUpsertData.name, JudgelsPlayUtils.toSafeHtml(contestUpsertData.description), ContestStyle.valueOf(contestUpsertData.style), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog("Created contest " + contestUpsertData.name + ".");

        return redirect(routes.ContestController.updateContestSpecificConfig(contest.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateContestGeneralConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestUpsertForm contestUpsertData = new ContestUpsertForm();
        contestUpsertData.name = contest.getName();
        contestUpsertData.description = contest.getDescription();
        contestUpsertData.style = contest.getStyle().name();
        Form<ContestUpsertForm> contestUpsertForm = Form.form(ContestUpsertForm.class).fill(contestUpsertData);

        UrielControllerUtils.getInstance().addActivityLog("Try to update general config of contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateContestGeneralConfig(contestUpsertForm, contest);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postUpdateContestGeneralConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestUpsertForm> contestUpsertForm = Form.form(ContestUpsertForm.class).bindFromRequest();
        if (formHasErrors(contestUpsertForm)) {
            return showUpdateContestGeneralConfig(contestUpsertForm, contest);
        }

        ContestUpsertForm contestUpsertData = contestUpsertForm.get();
        contestService.updateContest(contest.getJid(), contestUpsertData.name, JudgelsPlayUtils.toSafeHtml(contestUpsertData.description), ContestStyle.valueOf(contestUpsertData.style), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog("Update general config of contest " + contest.getName() + ".");

        return redirect(routes.ContestController.updateContestGeneralConfig(contestId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result updateContestModuleConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        LazyHtml content = new LazyHtml(listModulesView.render(contest));
        appendConfigSubtabLayout(content, contest);
        content.appendLayout(c -> headingWithActionLayout.render("#" + contest.getId() + ": " + contest.getName(), new InternalLink(Messages.get("contest.enter"), routes.ContestController.enterContest(contest.getId())), c));
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("commons.update"), routes.ContestController.updateContestModuleConfig(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Update Module");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result enableModule(long contestId, String contestModule) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        contestModuleService.enableModule(contest.getJid(), ContestModules.valueOf(contestModule), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.ContestController.updateContestModuleConfig(contest.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result disableModule(long contestId, String contestModule) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        contestModuleService.disableModule(contest.getJid(), ContestModules.valueOf(contestModule), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.ContestController.updateContestModuleConfig(contest.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateContestSpecificConfig(long contestId) throws ContestNotFoundException {
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
            formData.isAllowedAll = LanguageRestrictionAdapter.getFormIsAllowedAllFromLanguageRestriction(ioiContestStyleConfig.getLanguageRestriction());
            formData.allowedLanguageNames = LanguageRestrictionAdapter.getFormAllowedLanguageNamesFromLanguageRestriction(ioiContestStyleConfig.getLanguageRestriction());

            ioiStyleConfigForm = ioiStyleConfigForm.fill(formData);
            styleConfigForm = ioiStyleConfigForm;
        }

        Map<ContestModule, Form<?>> moduleFormMap = Maps.newHashMap();
        for (ContestModule contestModule : contest.getModules()) {
            moduleFormMap.put(contestModule, contestModule.generateConfigForm());
        }

        UrielControllerUtils.getInstance().addActivityLog("Try to update specific config of contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateContestSpecificConfig(contest, styleConfigForm, ImmutableSortedMap.copyOf(moduleFormMap, new ContestModuleComparator()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postUpdateContestSpecificConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form styleConfigForm = null;
        if (contest.isICPC()) {
            styleConfigForm = Form.form(ICPCContestStyleConfigForm.class).bindFromRequest();
        } else if (contest.isIOI()) {
            styleConfigForm = Form.form(IOIContestStyleConfigForm.class).bindFromRequest();
        }

        boolean checkError = false;
        Map<ContestModule, Form<?>> moduleFormMap = Maps.newHashMap();
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
            return showUpdateContestSpecificConfig(contest, styleConfigForm, ImmutableSortedMap.copyOf(moduleFormMap, new ContestModuleComparator()));
        }

        ContestStyleConfig contestStyleConfig = null;
        if (contest.isICPC()) {
            ICPCContestStyleConfigForm formData = (ICPCContestStyleConfigForm) styleConfigForm.get();
            contestStyleConfig = new ICPCContestStyleConfig(formData.wrongSubmissionPenalty, LanguageRestrictionAdapter.createLanguageRestrictionFromForm(formData.allowedLanguageNames, formData.isAllowedAll));
        } else if (contest.isIOI()) {
            IOIContestStyleConfigForm formData = (IOIContestStyleConfigForm) styleConfigForm.get();
            contestStyleConfig = new IOIContestStyleConfig(LanguageRestrictionAdapter.createLanguageRestrictionFromForm(formData.allowedLanguageNames, formData.isAllowedAll));
        }

        contestService.updateContestStyleConfiguration(contest.getJid(), contestStyleConfig, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        contestService.updateContestModuleConfiguration(contest.getJid(), updatedContestModuleBuilder.build(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog("Update specific config of contest " + contest.getName() + ".");

        return redirect(routes.ContestController.updateContestSpecificConfig(contest.getId()));
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
            actionsBuilder.add(new InternalLink(Messages.get("contest.start"), routes.ContestController.startContest(contest.getId())));
        }

        if (ContestControllerUtils.getInstance().isAllowedToViewEnterContestButton(contest, IdentityUtils.getUserJid())) {
            actionsBuilder.add(new InternalLink(Messages.get("contest.enter"), routes.ContestController.enterContest(contest.getId())));
        }
    }

    private Result showCreateContest(Form<ContestUpsertForm> contestUpsertForm) {
        LazyHtml content = new LazyHtml(createContestView.render(contestUpsertForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.create"), c));
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(Messages.get("contest.create"), routes.ContestController.createContest())
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Create");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateContestGeneralConfig(Form<ContestUpsertForm> contestUpsertForm, Contest contest) {
        LazyHtml content = new LazyHtml(updateContestView.render(contestUpsertForm, contest));
        appendConfigSubtabLayout(content, contest);
        content.appendLayout(c -> headingWithActionLayout.render("#" + contest.getId() + ": " + contest.getName(), new InternalLink(Messages.get("contest.enter"), routes.ContestController.enterContest(contest.getId())), c));
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("commons.update"), routes.ContestController.updateContestGeneralConfig(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Update General");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateContestSpecificConfig(Contest contest, Form contestStyleForm, SortedMap<ContestModule, Form<?>> moduleFormMap) {
        LazyHtml content = new LazyHtml(updateContestSpecificView.render(contest, contestStyleForm, moduleFormMap));
        appendConfigSubtabLayout(content, contest);
        content.appendLayout(c -> headingWithActionLayout.render("#" + contest.getId() + ": " + contest.getName(), new InternalLink(Messages.get("contest.enter"), routes.ContestController.enterContest(contest.getId())), c));
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("contest.config.specific"), routes.ContestController.updateContestSpecificConfig(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Update Specific");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendConfigSubtabLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.config.general"), routes.ContestController.updateContestGeneralConfig(contest.getId())),
                new InternalLink(Messages.get("contest.config.module"), routes.ContestController.updateContestModuleConfig(contest.getId())),
                new InternalLink(Messages.get("contest.config.specific"), routes.ContestController.updateContestSpecificConfig(contest.getId()))
        ), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

}
