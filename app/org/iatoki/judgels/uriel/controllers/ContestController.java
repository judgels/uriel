package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.accessTypesLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.sandalphon.commons.programming.LanguageRestrictionAdapter;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestScope;
import org.iatoki.judgels.uriel.ContestScopeConfig;
import org.iatoki.judgels.uriel.ContestScopeConfigPrivate;
import org.iatoki.judgels.uriel.ContestScopeConfigPrivateForm;
import org.iatoki.judgels.uriel.ContestScopeConfigPublic;
import org.iatoki.judgels.uriel.ContestScopeConfigPublicForm;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.ContestStyleConfig;
import org.iatoki.judgels.uriel.ContestStyleConfigICPC;
import org.iatoki.judgels.uriel.ContestStyleConfigICPCForm;
import org.iatoki.judgels.uriel.ContestStyleConfigIOI;
import org.iatoki.judgels.uriel.ContestStyleConfigIOIForm;
import org.iatoki.judgels.uriel.ContestType;
import org.iatoki.judgels.uriel.ContestTypeConfig;
import org.iatoki.judgels.uriel.ContestTypeConfigStandard;
import org.iatoki.judgels.uriel.ContestTypeConfigStandardForm;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualForm;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.ContestUpsertForm;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.Authorized;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.createContestView;
import org.iatoki.judgels.uriel.views.html.contest.listContestsView;
import org.iatoki.judgels.uriel.views.html.contest.updateContestView;
import org.iatoki.judgels.uriel.views.html.contest.specific.updateContestSpecificView;
import org.iatoki.judgels.uriel.views.html.contest.viewContestView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Arrays;
import java.util.Date;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class ContestController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;

    public ContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    public Result index() {
        return listAllowedContests(0, "id", "desc", "");
    }

    public Result jumpToAnnouncements(long contestId) {
        return redirect(routes.ContestAnnouncementController.viewPublishedAnnouncements(contestId));
    }

    public Result jumpToProblems(long contestId) {
        return redirect(routes.ContestProblemController.viewOpenedProblems(contestId));
    }

    public Result jumpToSubmissions(long contestId) {
        return redirect(routes.ContestSubmissionController.viewScreenedSubmissions(contestId));
    }

    public Result jumpToScoreboard(long contestId) {
        return redirect(routes.ContestScoreboardController.viewScoreboard(contestId));
    }

    public Result jumpToClarifications(long contestId) {
        return redirect(routes.ContestClarificationController.viewScreenedClarifications(contestId));
    }

    public Result jumpToContestants(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isCoach(contest)) {
            return redirect(routes.ContestTeamController.viewScreenedTeams(contestId));
        }
        return redirect(routes.ContestContestantController.viewContestants(contestId));
    }

    public Result jumpToSupervisors(long contestId) {
        return redirect(routes.ContestSupervisorController.viewSupervisors(contestId));
    }

    public Result jumpToManagers(long contestId) {
        return redirect(routes.ContestManagerController.viewManagers(contestId));
    }

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

    public Result viewContest(long contestId) throws ContestNotFoundException {
        return viewContestAndListRegistrants(contestId, 0, "id", "asc", "");
    }

    public Result viewContestAndListRegistrants(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToViewContest(contest)) {
            Page<ContestContestant> contestContestants = contestService.pageContestContestantsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            LazyHtml content = new LazyHtml(viewContestView.render(contest, contestContestants, pageIndex, orderBy, orderDir, filterString, ContestControllerUtils.getInstance().isAllowedToRegisterContest(contest), ContestControllerUtils.getInstance().isContestant(contest) && !ContestControllerUtils.getInstance().hasContestEnded(contest), ContestControllerUtils.getInstance().isAllowedToStartContestAsContestant(contest), ContestControllerUtils.getInstance().isAllowedToEnterContest(contest), ContestControllerUtils.getInstance().isAllowedToManageContest(contest)));
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

    public Result registerToAContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToRegisterContest(contest)) {
            contestService.createContestContestant(contest.getId(), IdentityUtils.getUserJid(), ContestContestantStatus.APPROVED);

            ControllerUtils.getInstance().addActivityLog("Register to contest " + contest.getName() + "  <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return redirect(routes.ContestController.viewContest(contestId));
        } else {
            return redirect(routes.ContestController.index());
        }
    }

    public Result enterContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToEnterContest(contest)) {
            ControllerUtils.getInstance().addActivityLog("Enter contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");
            return redirect(routes.ContestController.jumpToAnnouncements(contestId));
        } else {
            return redirect(routes.ContestController.index());
        }
    }

    public Result startContest(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToStartContestAsContestant(contest)) {
            contestService.startContestAsContestant(contest.getJid(), IdentityUtils.getUserJid());
            ControllerUtils.getInstance().addActivityLog("Enter contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");
            return redirect(routes.ContestController.jumpToAnnouncements(contestId));
        } else {
            return redirect(routes.ContestController.index());
        }
    }

    @Authorized(value = {"admin"})
    @AddCSRFToken
    public Result createContest() {
        Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class);

        ControllerUtils.getInstance().addActivityLog("Try to create a contest.");

        return showCreateContest(form);
    }

    @Authorized(value = {"admin"})
    @RequireCSRFCheck
    public Result postCreateContest() {
        Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateContest(form);
        } else {
            ContestUpsertForm contestUpsertForm = form.get();
            Contest contest = contestService.createContest(contestUpsertForm.name, contestUpsertForm.description, ContestType.valueOf(contestUpsertForm.type), ContestScope.valueOf(contestUpsertForm.scope), ContestStyle.valueOf(contestUpsertForm.style), UrielUtils.convertStringToDate(contestUpsertForm.startTime), UrielUtils.convertStringToDate(contestUpsertForm.endTime), UrielUtils.convertStringToDate(contestUpsertForm.clarificationEndTime), contestUpsertForm.isExclusive, contestUpsertForm.isUsingScoreboard, contestUpsertForm.isIncognitoScoreboard);

            ControllerUtils.getInstance().addActivityLog("Created contest " + contestUpsertForm.name + ".");

            return redirect(routes.ContestController.updateContestSpecificConfig(contest.getId()));
        }
    }

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

    @RequireCSRFCheck
    public Result postUpdateContestGeneralConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
            Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).bindFromRequest();
            if (form.hasErrors()) {
                return showUpdateContestGeneralConfig(form, contest);
            } else {
                ContestUpsertForm contestUpsertForm = form.get();
                contestService.updateContest(contest.getId(), contestUpsertForm.name, contestUpsertForm.description, ContestType.valueOf(contestUpsertForm.type), ContestScope.valueOf(contestUpsertForm.scope), ContestStyle.valueOf(contestUpsertForm.style), UrielUtils.convertStringToDate(contestUpsertForm.startTime), UrielUtils.convertStringToDate(contestUpsertForm.endTime), UrielUtils.convertStringToDate(contestUpsertForm.clarificationEndTime), contestUpsertForm.isExclusive, contestUpsertForm.isUsingScoreboard, contestUpsertForm.isIncognitoScoreboard);

                ControllerUtils.getInstance().addActivityLog("Update general config of contest " + contest.getName() + ".");

                return redirect(routes.ContestController.updateContestGeneralConfig(contestId));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateContestSpecificConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
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

            ControllerUtils.getInstance().addActivityLog("Try to update specific config of contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateContestSpecificConfig(form1, form2, form3, contest);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateContestSpecificConfig(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToManageContest(contest)) {
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

                    ControllerUtils.getInstance().addActivityLog("Update specific config of contest " + contest.getName() + ".");

                    return redirect(routes.ContestController.updateContestSpecificConfig(contest.getId()));
                } else {
                    return showUpdateContestSpecificConfig(form1, form2, form3, contest);
                }
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
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contest.config.general"), routes.ContestController.updateContestGeneralConfig(contest.getId())), new InternalLink(Messages.get("contest.config.specific"), routes.ContestController.updateContestSpecificConfig(contest.getId()))), c));
        content.appendLayout(c -> headingWithActionLayout.render("#" + contest.getId() + ": " + contest.getName(), new InternalLink(Messages.get("contest.enter"), routes.ContestController.enterContest(contest.getId())), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("commons.update"), routes.ContestController.updateContestGeneralConfig(contest.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Update General");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateContestSpecificConfig(Form form1, Form form2, Form form3, Contest contest) {
        LazyHtml content = new LazyHtml(updateContestSpecificView.render(contest, form1, form2, form3));
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contest.config.general"), routes.ContestController.updateContestGeneralConfig(contest.getId())), new InternalLink(Messages.get("contest.config.specific"), routes.ContestController.updateContestSpecificConfig(contest.getId()))), c));
        content.appendLayout(c -> headingWithActionLayout.render("#" + contest.getId() + ": " + contest.getName(), new InternalLink(Messages.get("contest.enter"), routes.ContestController.enterContest(contest.getId())), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("contest.config.specific"), routes.ContestController.updateContestSpecificConfig(contest.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Update Specific");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

}
