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

    private final ContestService contestService;
    private final ContestModuleService contestModuleService;
    private final ContestContestantService contestContestantService;
    private final ContestContestantPasswordService contestContestantPasswordService;

    @Inject
    public ContestController(ContestService contestService, ContestModuleService contestModuleService, ContestContestantService contestContestantService, ContestContestantPasswordService contestContestantPasswordService) {
        this.contestService = contestService;
        this.contestModuleService = contestModuleService;
        this.contestContestantService = contestContestantService;
        this.contestContestantPasswordService = contestContestantPasswordService;
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
            return redirect(routes.ContestSubmissionController.viewSubmissions(contestId));
        } else {
            return redirect(routes.ContestSubmissionController.viewScreenedSubmissions(contestId));
        }
    }

    @Transactional(readOnly = true)
    public Result jumpToScoreboard(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            return redirect(routes.ContestScoreboardController.viewOfficialScoreboard(contestId));
        } else {
            return redirect(routes.ContestScoreboardController.viewScoreboard(contestId));
        }
    }

    @Transactional(readOnly = true)
    public Result jumpToClarifications(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            return redirect(routes.ContestClarificationController.viewClarifications(contestId));
        } else {
            return redirect(routes.ContestClarificationController.viewScreenedClarifications(contestId));
        }
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
        Page<Contest> contests = contestService.pageAllowedContests(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, IdentityUtils.getUserJid(), ControllerUtils.getInstance().isAdmin());

        LazyHtml content = new LazyHtml(listContestsView.render(contests, pageIndex, orderBy, orderDir, filterString));
        if (ControllerUtils.getInstance().isAdmin()) {
            content.appendLayout(c -> headingWithActionLayout.render(Messages.get("contest.list"), new InternalLink(Messages.get("commons.create"), routes.ContestController.createContest()), c));
        } else {
            content.appendLayout(c -> headingLayout.render(Messages.get("contest.list"), c));
        }
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("contest.contests"), routes.ContestController.index())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contests");

        ControllerUtils.getInstance().addActivityLog("Open list of allowed contests <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewContest(long contestId) throws ContestNotFoundException {
        return viewContestAndListRegistrants(contestId, 0, "id", "desc", "");
    }

    @Transactional(readOnly = true)
    public Result viewContestAndListRegistrants(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToViewContest(contest)) {
            Page<ContestContestant> contestContestants = contestContestantService.pageContestContestantsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            Form<ContestEnterWithPasswordForm> passwordForm;
            if (ContestControllerUtils.getInstance().requiresPasswordToEnterContest(contest)) {
                passwordForm = Form.form(ContestEnterWithPasswordForm.class);
            } else {
                passwordForm = null;
            }

            LazyHtml content = new LazyHtml(viewContestView.render(contest, contestModuleService.findContestModulesByContestJid(contest.getJid()).stream().collect(Collectors.toMap(m -> m.getType(), m -> m)), contestContestants, pageIndex, orderBy, orderDir, filterString, ContestControllerUtils.getInstance().isAllowedToRegisterContest(contest), ContestControllerUtils.getInstance().isAllowedToUnregisterContest(contest), ContestControllerUtils.getInstance().isContestant(contest) && !ContestControllerUtils.getInstance().hasContestEnded(contest), ContestControllerUtils.getInstance().isAllowedToStartContestAsContestant(contest), ContestControllerUtils.getInstance().isAllowedToViewEnterContestButton(contest), passwordForm, ContestControllerUtils.getInstance().isAllowedToManageContest(contest)));
            content.appendLayout(c -> headingLayout.render(contest.getName(), c));
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                  new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - View");

            ControllerUtils.getInstance().addActivityLog("View contest " + contest.getName() + "  <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return redirect(routes.ContestController.index());
        }
    }

    @Transactional
    public Result registerToAContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToRegisterContest(contest)) {
            contestContestantService.createContestContestant(contest.getId(), IdentityUtils.getUserJid(), ContestContestantStatus.APPROVED);

            ControllerUtils.getInstance().addActivityLog("Register to contest " + contest.getName() + "  <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return redirect(routes.ContestController.viewContest(contestId));
        } else {
            return redirect(routes.ContestController.index());
        }
    }

    @Transactional
    public Result unregisterFromAContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToUnregisterContest(contest)) {
            ContestContestant contestContestant = contestContestantService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            contestContestantService.deleteContestContestant(contestContestant.getId());

            ControllerUtils.getInstance().addActivityLog("Unregister from contest " + contest.getName() + "  <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return redirect(routes.ContestController.viewContest(contestId));
        } else {
            return redirect(routes.ContestController.index());
        }
    }

    @Transactional
    public Result enterContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToEnterContest(contest)) {
            ControllerUtils.getInstance().addActivityLog("Enter contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");
            return redirect(routes.ContestController.jumpToAnnouncements(contestId));
        } else {
            return redirect(routes.ContestController.index());
        }
    }

    @Transactional
    public Result enterContestWithPassword(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToViewEnterContestButton(contest)) {
            Form<ContestEnterWithPasswordForm> form = Form.form(ContestEnterWithPasswordForm.class).bindFromRequest();

            String password = form.get().password;
            String correctPassword = contestContestantPasswordService.getContestantPassword(contest.getJid(), IdentityUtils.getUserJid());

            if (correctPassword == null) {
                flash("password", Messages.get("contestant.password.notAvailable"));
                return redirect(routes.ContestController.viewContest(contest.getId()));
            } else if (!correctPassword.equals(password)) {
                flash("password", Messages.get("contestant.password.incorrect"));
                return redirect(routes.ContestController.viewContest(contest.getId()));
            }

            ContestControllerUtils.getInstance().establishContestWithPasswordCookie(correctPassword);

            ControllerUtils.getInstance().addActivityLog("Enter contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");
            return redirect(routes.ContestController.jumpToAnnouncements(contestId));
        } else {
            return redirect(routes.ContestController.index());
        }
    }

    @Transactional
    public Result startContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToStartContestAsContestant(contest)) {
            contestContestantService.startContestAsContestant(contest.getJid(), IdentityUtils.getUserJid());
            ControllerUtils.getInstance().addActivityLog("Enter contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");
            return redirect(routes.ContestController.jumpToAnnouncements(contestId));
        } else {
            return redirect(routes.ContestController.index());
        }
    }

    @Authorized(value = {"admin"})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createContest() {
        Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class);

        ControllerUtils.getInstance().addActivityLog("Try to create a contest.");

        return showCreateContest(form);
    }

    @Authorized(value = {"admin"})
    @Transactional
    @RequireCSRFCheck
    public Result postCreateContest() {
        Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateContest(form);
        } else {
            boolean check = true;
            ContestUpsertForm contestUpsertForm = form.get();
            if (check) {
                Contest contest = contestService.createContest(contestUpsertForm.name, contestUpsertForm.description, ContestStyle.valueOf(contestUpsertForm.style));

                ControllerUtils.getInstance().addActivityLog("Created contest " + contestUpsertForm.name + ".");

                return redirect(routes.ContestController.updateContestSpecificConfig(contest.getId()));
            } else {
                return showCreateContest(form);
            }
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateContestGeneralConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            ContestUpsertForm contestUpsertForm = new ContestUpsertForm(contest);
            Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).fill(contestUpsertForm);

            ControllerUtils.getInstance().addActivityLog("Try to update general config of contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateContestGeneralConfig(form, contest);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateContestGeneralConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).bindFromRequest();
            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateContestGeneralConfig(form, contest);
            } else {
                boolean check = true;
                ContestUpsertForm contestUpsertForm = form.get();
                if (check) {
                    contestService.updateContest(contest.getId(), contestUpsertForm.name, contestUpsertForm.description, ContestStyle.valueOf(contestUpsertForm.style));

                    ControllerUtils.getInstance().addActivityLog("Update general config of contest " + contest.getName() + ".");

                    return redirect(routes.ContestController.updateContestGeneralConfig(contestId));
                } else {
                    return showUpdateContestGeneralConfig(form, contest);
                }
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    public Result updateContestModuleConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            LazyHtml content = new LazyHtml(listModulesView.render(contest, contestModuleService.findContestModulesByContestJid(contest.getJid()).stream().collect(Collectors.toMap(m -> m.getType(), m -> m))));
            appendConfigSubtabLayout(content, contest);
            content.appendLayout(c -> headingWithActionLayout.render("#" + contest.getId() + ": " + contest.getName(), new InternalLink(Messages.get("contest.enter"), routes.ContestController.enterContest(contest.getId())), c));
            ControllerUtils.getInstance().appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, contest,
                    new InternalLink(Messages.get("commons.update"), routes.ContestController.updateContestModuleConfig(contest.getId()))
            );

            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Update Module");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    public Result enableModule(long contestId, String contestModule) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            contestModuleService.enableModule(contest.getJid(), ContestModules.valueOf(contestModule));
            return redirect(routes.ContestController.updateContestModuleConfig(contest.getId()));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    public Result disableModule(long contestId, String contestModule) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            contestModuleService.disableModule(contest.getJid(), ContestModules.valueOf(contestModule));
            return redirect(routes.ContestController.updateContestModuleConfig(contest.getId()));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateContestSpecificConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            Form form1 = null;

            if (contest.isICPC()) {
                ICPCContestStyleConfig icpcContestStyleConfig = (ICPCContestStyleConfig)contest.getStyleConfig();
                Form<ICPCContestStyleConfigForm> form = Form.form(ICPCContestStyleConfigForm.class);

                ICPCContestStyleConfigForm formData = new ICPCContestStyleConfigForm();
                formData.wrongSubmissionPenalty = icpcContestStyleConfig.getWrongSubmissionPenalty();
                formData.isAllowedAll = LanguageRestrictionAdapter.getFormIsAllowedAllFromLanguageRestriction(icpcContestStyleConfig.getLanguageRestriction());
                formData.allowedLanguageNames = LanguageRestrictionAdapter.getFormAllowedLanguageNamesFromLanguageRestriction(icpcContestStyleConfig.getLanguageRestriction());
                form = form.fill(formData);
                form1 = form;
            } else if (contest.isIOI()) {
                IOIContestStyleConfig ioiContestStyleConfig = (IOIContestStyleConfig)contest.getStyleConfig();
                Form<IOIContestStyleConfigForm> form = Form.form(IOIContestStyleConfigForm.class);

                IOIContestStyleConfigForm formData = new IOIContestStyleConfigForm();
                formData.isAllowedAll = LanguageRestrictionAdapter.getFormIsAllowedAllFromLanguageRestriction(ioiContestStyleConfig.getLanguageRestriction());
                formData.allowedLanguageNames = LanguageRestrictionAdapter.getFormAllowedLanguageNamesFromLanguageRestriction(ioiContestStyleConfig.getLanguageRestriction());

                form = form.fill(formData);
                form1 = form;
            }

            Map<ContestModule, Form<?>> map = Maps.newHashMap();
            for (ContestModule contestModule : contestModuleService.findContestModulesByContestJid(contest.getJid())) {
                map.put(contestModule, contestModule.generateConfigForm());
            }

            ControllerUtils.getInstance().addActivityLog("Try to update specific config of contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateContestSpecificConfig(contest, form1, ImmutableSortedMap.copyOf(map, new ContestModuleComparator()));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateContestSpecificConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            Form form1 = null;
            if (contest.isICPC()) {
                form1 = Form.form(ICPCContestStyleConfigForm.class).bindFromRequest();
            } else if (contest.isIOI()) {
                form1 = Form.form(IOIContestStyleConfigForm.class).bindFromRequest();
            }

            boolean checkError = false;
            Map<ContestModule, Form<?>> map = Maps.newHashMap();
            ImmutableList.Builder<ContestModule> updatedContestModuleBuilder = ImmutableList.builder();
            for (ContestModule contestModule : contestModuleService.findContestModulesByContestJid(contest.getJid())) {
                Form<?> tempForm = contestModule.updateModuleByFormFromRequest(request());
                map.put(contestModule, tempForm);
                updatedContestModuleBuilder.add(contestModule);
                if ((tempForm.hasErrors()) || (tempForm.hasGlobalErrors())) {
                    checkError = true;
                }
            }

            if ((form1.hasErrors() || form1.hasGlobalErrors() || checkError)) {
                return showUpdateContestSpecificConfig(contest, form1, ImmutableSortedMap.copyOf(map, new ContestModuleComparator()));
            } else {
                ContestStyleConfig contestStyleConfig = null;
                if (contest.isICPC()) {
                    ICPCContestStyleConfigForm formData = (ICPCContestStyleConfigForm) form1.get();
                    contestStyleConfig = new ICPCContestStyleConfig(formData.wrongSubmissionPenalty, LanguageRestrictionAdapter.createLanguageRestrictionFromForm(formData.allowedLanguageNames, formData.isAllowedAll));
                } else if (contest.isIOI()) {
                    IOIContestStyleConfigForm formData = (IOIContestStyleConfigForm) form1.get();
                    contestStyleConfig = new IOIContestStyleConfig(LanguageRestrictionAdapter.createLanguageRestrictionFromForm(formData.allowedLanguageNames, formData.isAllowedAll));
                }

                contestService.updateContestStyleConfigurationByContestJid(contest.getJid(), contestStyleConfig);
                contestService.updateContestModuleConfigurationByContestJid(contest.getJid(), updatedContestModuleBuilder.build());

                ControllerUtils.getInstance().addActivityLog("Update specific config of contest " + contest.getName() + ".");

                return redirect(routes.ContestController.updateContestSpecificConfig(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    private Result showCreateContest(Form<ContestUpsertForm> form) {
        LazyHtml content = new LazyHtml(createContestView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.create"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(Messages.get("contest.create"), routes.ContestController.createContest())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateContestGeneralConfig(Form<ContestUpsertForm> form, Contest contest) {
        LazyHtml content = new LazyHtml(updateContestView.render(form, contest));
        appendConfigSubtabLayout(content, contest);
        content.appendLayout(c -> headingWithActionLayout.render("#" + contest.getId() + ": " + contest.getName(), new InternalLink(Messages.get("contest.enter"), routes.ContestController.enterContest(contest.getId())), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("commons.update"), routes.ContestController.updateContestGeneralConfig(contest.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Update General");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateContestSpecificConfig(Contest contest, Form form1, SortedMap<ContestModule, Form<?>> moduleFormMap) {
        LazyHtml content = new LazyHtml(updateContestSpecificView.render(contest, form1, moduleFormMap));
        appendConfigSubtabLayout(content, contest);
        content.appendLayout(c -> headingWithActionLayout.render("#" + contest.getId() + ": " + contest.getName(), new InternalLink(Messages.get("contest.enter"), routes.ContestController.enterContest(contest.getId())), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("contest.config.specific"), routes.ContestController.updateContestSpecificConfig(contest.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Update Specific");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendConfigSubtabLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.config.general"), routes.ContestController.updateContestGeneralConfig(contest.getId())),
                new InternalLink(Messages.get("contest.config.module"), routes.ContestController.updateContestModuleConfig(contest.getId())),
                new InternalLink(Messages.get("contest.config.specific"), routes.ContestController.updateContestSpecificConfig(contest.getId()))
        ), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

}
