package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
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
import java.util.stream.Collectors;

@Authenticated(value = {LoggedIn.class, HasRole.class})
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

    @Transactional(readOnly = true)
    public Result index() {
        return listAllowedContests(0, "id", "desc", "");
    }

    public Result jumpToAnnouncements(long contestId) {
        return redirect(routes.ContestAnnouncementController.viewPublishedAnnouncements(contestId));
    }

    public Result jumpToProblems(long contestId) {
        return redirect(routes.ContestProblemController.viewUsedProblems(contestId));
    }

    @Transactional(readOnly = true)
    public Result jumpToSubmissions(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            return redirect(routes.ContestProgrammingSubmissionController.viewSubmissions(contestId));
        }

        return redirect(routes.ContestProgrammingSubmissionController.viewScreenedSubmissions(contestId));
    }

    @Transactional(readOnly = true)
    public Result jumpToScoreboard(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            return redirect(routes.ContestScoreboardController.viewOfficialScoreboard(contestId));
        }

        return redirect(routes.ContestScoreboardController.viewScoreboard(contestId));
    }

    @Transactional(readOnly = true)
    public Result jumpToClarifications(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            return redirect(routes.ContestClarificationController.viewClarifications(contestId));
        }

        return redirect(routes.ContestClarificationController.viewScreenedClarifications(contestId));
    }

    @Transactional(readOnly = true)
    public Result jumpToContestants(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isCoach(contest)) {
            return redirect(routes.ContestTeamController.viewScreenedTeams(contestId));
        }

        return redirect(routes.ContestContestantController.viewContestants(contestId));
    }

    @Transactional(readOnly = true)
    public Result jumpToTeams(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            return redirect(routes.ContestTeamController.viewTeams(contestId));
        }
        return redirect(routes.ContestContestantController.viewContestants(contestId));
    }

    @Transactional(readOnly = true)
    public Result jumpToPasswords(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            return redirect(routes.ContestPasswordController.viewContestantPasswords(contestId));
        }

        return redirect(routes.ContestContestantController.viewContestants(contestId));
    }

    public Result jumpToSupervisors(long contestId) {
        return redirect(routes.ContestSupervisorController.viewSupervisors(contestId));
    }

    public Result jumpToManagers(long contestId) {
        return redirect(routes.ContestManagerController.viewManagers(contestId));
    }

    public Result jumpToFiles(long contestId) {
        return redirect(routes.ContestFileController.viewFiles(contestId));
    }

    @Transactional(readOnly = true)
    public Result listAllowedContests(long pageIndex, String orderBy, String orderDir, String filterString) {
        Page<Contest> pageOfContests = contestService.getPageOfAllowedContests(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, IdentityUtils.getUserJid(), UrielControllerUtils.getInstance().isAdmin());

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

    @Transactional(readOnly = true)
    public Result viewContest(long contestId) throws ContestNotFoundException {
        return viewContestAndListRegistrants(contestId, 0, "id", "desc", "");
    }

    @Transactional(readOnly = true)
    public Result viewContestAndListRegistrants(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToViewContest(contest)) {
            return redirect(routes.ContestController.index());
        }

        Page<ContestContestant> pageOfContestContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        Form<ContestEnterWithPasswordForm> passwordForm;
        if (ContestControllerUtils.getInstance().requiresPasswordToEnterContest(contest)) {
            passwordForm = Form.form(ContestEnterWithPasswordForm.class);
        } else {
            passwordForm = null;
        }

        LazyHtml content = new LazyHtml(viewContestView.render(contest, contestModuleService.getModulesInContest(contest.getJid()).stream().collect(Collectors.toMap(m -> m.getType(), m -> m)), pageOfContestContestants, pageIndex, orderBy, orderDir, filterString, ContestControllerUtils.getInstance().isAllowedToRegisterContest(contest), ContestControllerUtils.getInstance().isAllowedToUnregisterContest(contest), ContestControllerUtils.getInstance().isContestant(contest) && !ContestControllerUtils.getInstance().hasContestEnded(contest), ContestControllerUtils.getInstance().isAllowedToStartContestAsContestant(contest), ContestControllerUtils.getInstance().isAllowedToViewEnterContestButton(contest), passwordForm, ContestControllerUtils.getInstance().isAllowedToManageContest(contest)));
        content.appendLayout(c -> headingLayout.render(contest.getName(), c));
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
              new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId()))
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - View");

        UrielControllerUtils.getInstance().addActivityLog("View contest " + contest.getName() + "  <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional
    public Result registerToAContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToRegisterContest(contest)) {
            return redirect(routes.ContestController.index());
        }

        contestContestantService.createContestContestant(contest.getId(), IdentityUtils.getUserJid(), ContestContestantStatus.APPROVED);

        UrielControllerUtils.getInstance().addActivityLog("Register to contest " + contest.getName() + "  <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.ContestController.viewContest(contestId));
    }

    @Transactional
    public Result unregisterFromAContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToUnregisterContest(contest)) {
            return redirect(routes.ContestController.index());
        }

        ContestContestant contestContestant = contestContestantService.findContestantInContestAndJid(contest.getJid(), IdentityUtils.getUserJid());
        contestContestantService.deleteContestContestant(contestContestant.getId());

        UrielControllerUtils.getInstance().addActivityLog("Unregister from contest " + contest.getName() + "  <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.ContestController.viewContest(contestId));
    }

    @Transactional
    public Result enterContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest)) {
            return redirect(routes.ContestController.index());
        }

        UrielControllerUtils.getInstance().addActivityLog("Enter contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.ContestController.jumpToAnnouncements(contestId));
    }

    @Transactional
    public Result enterContestWithPassword(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToViewEnterContestButton(contest)) {
            return redirect(routes.ContestController.index());
        }

        Form<ContestEnterWithPasswordForm> contestEnterWithPasswordForm = Form.form(ContestEnterWithPasswordForm.class).bindFromRequest();

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

    @Transactional
    public Result startContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToStartContestAsContestant(contest)) {
            return redirect(routes.ContestController.index());
        }

        contestContestantService.startContestAsContestant(contest.getJid(), IdentityUtils.getUserJid());

        UrielControllerUtils.getInstance().addActivityLog("Enter contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.ContestController.jumpToAnnouncements(contestId));
    }

    @Authorized(value = "admin")
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createContest() {
        Form<ContestUpsertForm> contestUpsertForm = Form.form(ContestUpsertForm.class);

        UrielControllerUtils.getInstance().addActivityLog("Try to create a contest.");

        return showCreateContest(contestUpsertForm);
    }

    @Authorized(value = "admin")
    @Transactional
    @RequireCSRFCheck
    public Result postCreateContest() {
        Form<ContestUpsertForm> contestUpsertForm = Form.form(ContestUpsertForm.class).bindFromRequest();

        if (formHasErrors(contestUpsertForm)) {
            return showCreateContest(contestUpsertForm);
        }

        ContestUpsertForm contestUpsertData = contestUpsertForm.get();
        Contest contest = contestService.createContest(contestUpsertData.name, contestUpsertData.description, ContestStyle.valueOf(contestUpsertData.style));

        UrielControllerUtils.getInstance().addActivityLog("Created contest " + contestUpsertData.name + ".");

        return redirect(routes.ContestController.updateContestSpecificConfig(contest.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateContestGeneralConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        ContestUpsertForm contestUpsertData = new ContestUpsertForm();
        contestUpsertData.name = contest.getName();
        contestUpsertData.description = contest.getDescription();
        contestUpsertData.style = contest.getStyle().name();
        Form<ContestUpsertForm> contestUpsertForm = Form.form(ContestUpsertForm.class).fill(contestUpsertData);

        UrielControllerUtils.getInstance().addActivityLog("Try to update general config of contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateContestGeneralConfig(contestUpsertForm, contest);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateContestGeneralConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Form<ContestUpsertForm> contestUpsertForm = Form.form(ContestUpsertForm.class).bindFromRequest();
        if (formHasErrors(contestUpsertForm)) {
            return showUpdateContestGeneralConfig(contestUpsertForm, contest);
        }

        ContestUpsertForm contestUpsertData = contestUpsertForm.get();
        contestService.updateContest(contest.getId(), contestUpsertData.name, contestUpsertData.description, ContestStyle.valueOf(contestUpsertData.style));

        UrielControllerUtils.getInstance().addActivityLog("Update general config of contest " + contest.getName() + ".");

        return redirect(routes.ContestController.updateContestGeneralConfig(contestId));
    }

    @Transactional(readOnly = true)
    public Result updateContestModuleConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        LazyHtml content = new LazyHtml(listModulesView.render(contest, contestModuleService.getModulesInContest(contest.getJid()).stream().collect(Collectors.toMap(m -> m.getType(), m -> m))));
        appendConfigSubtabLayout(content, contest);
        content.appendLayout(c -> headingWithActionLayout.render("#" + contest.getId() + ": " + contest.getName(), new InternalLink(Messages.get("contest.enter"), routes.ContestController.enterContest(contest.getId())), c));
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("commons.update"), routes.ContestController.updateContestModuleConfig(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Update Module");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional
    public Result enableModule(long contestId, String contestModule) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        contestModuleService.enableModule(contest.getJid(), ContestModules.valueOf(contestModule));

        return redirect(routes.ContestController.updateContestModuleConfig(contest.getId()));
    }

    @Transactional
    public Result disableModule(long contestId, String contestModule) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        contestModuleService.disableModule(contest.getJid(), ContestModules.valueOf(contestModule));

        return redirect(routes.ContestController.updateContestModuleConfig(contest.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateContestSpecificConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
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
        for (ContestModule contestModule : contestModuleService.getModulesInContest(contest.getJid())) {
            moduleFormMap.put(contestModule, contestModule.generateConfigForm());
        }

        UrielControllerUtils.getInstance().addActivityLog("Try to update specific config of contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateContestSpecificConfig(contest, styleConfigForm, ImmutableSortedMap.copyOf(moduleFormMap, new ContestModuleComparator()));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateContestSpecificConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
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
        for (ContestModule contestModule : contestModuleService.getModulesInContest(contest.getJid())) {
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

        contestService.updateContestStyleConfiguration(contest.getJid(), contestStyleConfig);
        contestService.updateContestModuleConfiguration(contest.getJid(), updatedContestModuleBuilder.build());

        UrielControllerUtils.getInstance().addActivityLog("Update specific config of contest " + contest.getName() + ".");

        return redirect(routes.ContestController.updateContestSpecificConfig(contest.getId()));
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
