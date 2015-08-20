package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.alertLayout;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestClarification;
import org.iatoki.judgels.uriel.ContestClarificationNotFoundException;
import org.iatoki.judgels.uriel.ContestClarificationStatus;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.forms.ContestClarificationChangeForm;
import org.iatoki.judgels.uriel.forms.ContestClarificationCreateForm;
import org.iatoki.judgels.uriel.forms.ContestClarificationUpdateForm;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.modules.clarification.ContestClarificationModule;
import org.iatoki.judgels.uriel.modules.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.services.ContestClarificationService;
import org.iatoki.judgels.uriel.services.ContestModuleService;
import org.iatoki.judgels.uriel.services.ContestProblemService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.ContestSupervisorService;
import org.iatoki.judgels.uriel.services.ContestTeamService;
import org.iatoki.judgels.uriel.views.html.contest.clarification.createClarificationView;
import org.iatoki.judgels.uriel.views.html.contest.clarification.listClarificationsView;
import org.iatoki.judgels.uriel.views.html.contest.clarification.listScreenedClarificationsView;
import org.iatoki.judgels.uriel.views.html.contest.clarification.updateClarificationAnswerView;
import org.iatoki.judgels.uriel.views.html.contest.clarification.updateClarificationContentView;
import org.iatoki.judgels.uriel.views.html.layouts.accessTypeByStatusLayout;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public class ContestClarificationController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final ContestClarificationService contestClarificationService;
    private final ContestModuleService contestModuleService;
    private final ContestProblemService contestProblemService;
    private final ContestService contestService;
    private final ContestSupervisorService contestSupervisorService;
    private final ContestTeamService contestTeamService;

    @Inject
    public ContestClarificationController(ContestClarificationService contestClarificationService, ContestModuleService contestModuleService, ContestProblemService contestProblemService, ContestService contestService, ContestSupervisorService contestSupervisorService, ContestTeamService contestTeamService) {
        this.contestClarificationService = contestClarificationService;
        this.contestModuleService = contestModuleService;
        this.contestProblemService = contestProblemService;
        this.contestService = contestService;
        this.contestSupervisorService = contestSupervisorService;
        this.contestTeamService = contestTeamService;
    }

    @Transactional
    public Result viewScreenedClarifications(long contestId) throws ContestNotFoundException {
        return listScreenedClarifications(contestId, 0, "id", "desc", "");
    }

    @Transactional
    public Result listScreenedClarifications(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        ContestClarificationModule contestClarificationModule = (ContestClarificationModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.CLARIFICATION);

        Page<ContestClarification> pageOfContestClarifications;
        boolean coach = ContestControllerUtils.getInstance().isCoach(contest);
        if (coach) {
            List<ContestTeam> contestTeams = contestTeamService.getTeamsInContestByCoachJid(contest.getJid(), IdentityUtils.getUserJid());
            ImmutableList.Builder<ContestTeamMember> contestTeamMembersBuilder = ImmutableList.builder();
            for (ContestTeam team : contestTeams) {
                contestTeamMembersBuilder.addAll(team.getMembers());
            }
            List<ContestTeamMember> contestTeamMembers = contestTeamMembersBuilder.build();
            pageOfContestClarifications = contestClarificationService.getPageOfClarificationsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, contestTeamMembers.stream().map(ct -> ct.getMemberJid()).collect(Collectors.toList()));
        } else {
            pageOfContestClarifications = contestClarificationService.getPageOfClarificationsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, ImmutableList.of(IdentityUtils.getUserJid()));
        }
        contestClarificationService.readContestClarifications(IdentityUtils.getUserJid(), pageOfContestClarifications.getData().stream().filter(c -> c.isAnswered()).map(c -> c.getJid()).collect(Collectors.toList()));

        LazyHtml content = new LazyHtml(listScreenedClarificationsView.render(contest, pageOfContestClarifications, pageIndex, orderBy, orderDir, filterString, coach));
        if (coach) {
            content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.list"), c));
        } else if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.DURATION)) {
            ContestDurationModule contestDurationModule = (ContestDurationModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.DURATION);
            if (new Date().before(new Date(contestDurationModule.getBeginTime().getTime() + contestClarificationModule.getClarificationDuration()))) {
                content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("clarification.list"), new InternalLink(Messages.get("commons.create"), routes.ContestClarificationController.createClarification(contest.getId())), c));
            } else {
                content.appendLayout(c -> alertLayout.render(Messages.get("clarification.time_ended"), c));
                content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.list"), c));
            }
        } else {
            content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("clarification.list"), new InternalLink(Messages.get("commons.create"), routes.ContestClarificationController.createClarification(contest.getId())), c));

        }

        if (isAllowedToSuperviseClarifications(contest)) {
            content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()), routes.ContestClarificationController.viewClarifications(contest.getId()), c));
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);

        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("clarification.clarifications"), routes.ContestClarificationController.viewScreenedClarifications(contest.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Clarifications");

        ControllerUtils.getInstance().addActivityLog("Open list of own clarifications in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createClarification(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToDoContest(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        ContestClarificationModule contestClarificationModule = (ContestClarificationModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.CLARIFICATION);

        Form<ContestClarificationCreateForm> contestClarificationCreateForm = Form.form(ContestClarificationCreateForm.class);
        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.DURATION)) {
            ControllerUtils.getInstance().addActivityLog("Try to create clarification in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showCreateClarification(contestClarificationCreateForm, contest);
        }

        ContestDurationModule contestDurationModule = (ContestDurationModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.DURATION);
        if (!new Date().before(new Date(contestDurationModule.getBeginTime().getTime() + contestClarificationModule.getClarificationDuration()))) {
            return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
        }

        ControllerUtils.getInstance().addActivityLog("Try to create clarification in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showCreateClarification(contestClarificationCreateForm, contest);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateClarification(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToDoContest(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        ContestClarificationModule contestClarificationModule = (ContestClarificationModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.CLARIFICATION);

        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.DURATION)) {
            return processCreateClarification(contest);
        }

        ContestDurationModule contestDurationModule = (ContestDurationModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.DURATION);
        if (!new Date().before(new Date(contestDurationModule.getBeginTime().getTime() + contestClarificationModule.getClarificationDuration()))) {
            return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
        }

        return processCreateClarification(contest);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateClarificationContent(long contestId, long contestClarificationId) throws ContestNotFoundException, ContestClarificationNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        ContestClarification contestClarification = contestClarificationService.findContestClarificationById(contestClarificationId);
        if (!ContestControllerUtils.getInstance().isCoach(contest) || contestClarification.isAnswered() || !contestClarification.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        ContestClarificationChangeForm contestClarificationChangeData = new ContestClarificationChangeForm();
        contestClarificationChangeData.title = contestClarification.getTitle();
        contestClarificationChangeData.question = contestClarification.getQuestion();
        Form<ContestClarificationChangeForm> contestClarificationChangeForm = Form.form(ContestClarificationChangeForm.class).fill(contestClarificationChangeData);
        contestClarificationChangeForm = contestClarificationChangeForm.fill(contestClarificationChangeData);

        ControllerUtils.getInstance().addActivityLog("Try to update clarification " + contestClarification.getTitle() + " content in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateClarificationContent(contestClarificationChangeForm, contest, contestClarification);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateClarificationContent(long contestId, long contestClarificationId) throws ContestNotFoundException, ContestClarificationNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        ContestClarification contestClarification = contestClarificationService.findContestClarificationById(contestClarificationId);
        if (!ContestControllerUtils.getInstance().isCoach(contest) || contestClarification.isAnswered() || !contestClarification.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Form<ContestClarificationChangeForm> contestClarificationChangeForm = Form.form(ContestClarificationChangeForm.class).bindFromRequest();

        if (formHasErrors(contestClarificationChangeForm)) {
            return showUpdateClarificationContent(contestClarificationChangeForm, contest, contestClarification);
        }

        ContestClarificationChangeForm contestClarificationChangeData = contestClarificationChangeForm.get();
        contestClarificationService.updateContestClarification(contestClarification.getId(), contestClarificationChangeData.title, contestClarificationChangeData.question);

        ControllerUtils.getInstance().addActivityLog("Update clarification " + contestClarification.getTitle() + " content in contest " + contest.getName() + ".");

        return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
    }

    @Transactional(readOnly = true)
    public Result viewClarifications(long contestId) throws ContestNotFoundException {
        return listClarifications(contestId, 0, "id", "desc", "");
    }

    @Transactional(readOnly = true)
    public Result listClarifications(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        if (!isAllowedToSuperviseClarifications(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Page<ContestClarification> pageOfContestClarifications = contestClarificationService.getPageOfClarificationsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, null);

        LazyHtml content = new LazyHtml(listClarificationsView.render(contest.getId(), pageOfContestClarifications, pageIndex, orderBy, orderDir, filterString));

        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.list"), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestClarificationController.viewClarifications(contest.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - All Clarifications");

        ControllerUtils.getInstance().addActivityLog("Open all clarifications in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateClarificationAnswer(long contestId, long contestClarificationId) throws ContestNotFoundException, ContestClarificationNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        ContestClarification contestClarification = contestClarificationService.findContestClarificationById(contestClarificationId);
        if (!isAllowedToSuperviseClarifications(contest) || !contestClarification.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        ContestClarificationUpdateForm contestClarificationUpsertData = new ContestClarificationUpdateForm();
        contestClarificationUpsertData.answer = contestClarification.getAnswer();
        contestClarificationUpsertData.status = contestClarification.getStatus().name();
        Form<ContestClarificationUpdateForm> contestClarificationUpsertForm = Form.form(ContestClarificationUpdateForm.class).fill(contestClarificationUpsertData);

        ControllerUtils.getInstance().addActivityLog("Try to answer clarification " + contestClarification.getTitle() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateClarificationAnswer(contestClarificationUpsertForm, contest, contestClarification);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateClarificationAnswer(long contestId, long contestClarificationId) throws ContestNotFoundException, ContestClarificationNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        ContestClarification contestClarification = contestClarificationService.findContestClarificationById(contestClarificationId);
        if (!isAllowedToSuperviseClarifications(contest) || !contestClarification.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Form<ContestClarificationUpdateForm> contestClarificationUpsertForm = Form.form(ContestClarificationUpdateForm.class).bindFromRequest();

        if (formHasErrors(contestClarificationUpsertForm)) {
            return showUpdateClarificationAnswer(contestClarificationUpsertForm, contest, contestClarification);
        }

        ContestClarificationUpdateForm contestClarificationUpdateData = contestClarificationUpsertForm.get();
        contestClarificationService.updateContestClarification(contestClarification.getId(), contestClarificationUpdateData.answer, ContestClarificationStatus.valueOf(contestClarificationUpdateData.status));

        ControllerUtils.getInstance().addActivityLog("Answer clarification " + contestClarification.getTitle() + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestClarificationController.viewClarifications(contest.getId()));
    }

    private Result processCreateClarification(Contest contest) {
        Form<ContestClarificationCreateForm> contestClarificationCreateForm = Form.form(ContestClarificationCreateForm.class).bindFromRequest();

        if (formHasErrors(contestClarificationCreateForm)) {
            return showCreateClarification(contestClarificationCreateForm, contest);
        }

        ContestClarificationCreateForm contestClarificationCreateData = contestClarificationCreateForm.get();
        contestClarificationService.createContestClarification(contest.getId(), contestClarificationCreateData.title, contestClarificationCreateData.question, contestClarificationCreateData.topicJid);

        ControllerUtils.getInstance().addActivityLog("Create clarification " + contestClarificationCreateData.title + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
    }

    private Result showCreateClarification(Form<ContestClarificationCreateForm> contestClarificationCreateForm, Contest contest) {
        List<ContestProblem> contestProblemList = contestProblemService.getOpenedProblemsInContest(contest.getJid());

        LazyHtml content = new LazyHtml(createClarificationView.render(contest, contestClarificationCreateForm, contestProblemList));
        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.create"), c));
        if (isAllowedToSuperviseClarifications(contest)) {
            appendSubtabsLayout(content, contest);
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.contestant"), routes.ContestClarificationController.viewScreenedClarifications(contest.getId())),
                new InternalLink(Messages.get("clarification.create"), routes.ContestClarificationController.createClarification(contest.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Clarification - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateClarificationContent(Form<ContestClarificationChangeForm> contestClarificationChangeForm, Contest contest, ContestClarification contestClarification) {
        LazyHtml content = new LazyHtml(updateClarificationContentView.render(contest, contestClarification, contestClarificationChangeForm));
        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.update"), c));
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            appendSubtabsLayout(content, contest);
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.contestant"), routes.ContestClarificationController.viewScreenedClarifications(contest.getId())),
                new InternalLink(Messages.get("clarification.update"), routes.ContestClarificationController.updateClarificationContent(contest.getId(), contestClarification.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Clarification - Update Content");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateClarificationAnswer(Form<ContestClarificationUpdateForm> contestClarificationUpdateForm, Contest contest, ContestClarification contestClarification) {
        LazyHtml content = new LazyHtml(updateClarificationAnswerView.render(contest.getId(), contestClarification, contestClarificationUpdateForm));
        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.update"), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestClarificationController.viewClarifications(contest.getId())),
                new InternalLink(Messages.get("clarification.update"), routes.ContestClarificationController.updateClarificationAnswer(contest.getId(), contestClarification.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Clarification - Update Answer");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendSubtabsLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()), routes.ContestClarificationController.viewClarifications(contest.getId()), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("clarification.clarifications"), routes.ContestController.jumpToClarifications(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseClarifications(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest) || (ContestControllerUtils.getInstance().isSupervisor(contest) && contestSupervisorService.findContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid()).getContestPermission().isAllowed(ContestPermissions.CLARIFICATION));
    }
}
