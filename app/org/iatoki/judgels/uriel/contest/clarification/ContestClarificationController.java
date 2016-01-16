package org.iatoki.judgels.uriel.contest.clarification;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.jophiel.BasicActivityKeys;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.alertLayout;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.contest.ContestNotFoundException;
import org.iatoki.judgels.uriel.contest.supervisor.ContestPermissions;
import org.iatoki.judgels.uriel.contest.problem.ContestProblem;
import org.iatoki.judgels.uriel.contest.team.ContestTeam;
import org.iatoki.judgels.uriel.contest.team.member.ContestTeamMember;
import org.iatoki.judgels.uriel.activity.UrielActivityKeys;
import org.iatoki.judgels.uriel.UrielControllerUtils;
import org.iatoki.judgels.uriel.contest.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.contest.module.ContestModules;
import org.iatoki.judgels.uriel.contest.module.clarificationtimelimit.ContestClarificationTimeLimitModule;
import org.iatoki.judgels.uriel.contest.module.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.contest.problem.ContestProblemService;
import org.iatoki.judgels.uriel.contest.ContestService;
import org.iatoki.judgels.uriel.contest.team.ContestTeamService;
import org.iatoki.judgels.uriel.jid.JidCacheServiceImpl;
import org.iatoki.judgels.uriel.contest.clarification.html.createClarificationView;
import org.iatoki.judgels.uriel.contest.clarification.html.listClarificationsView;
import org.iatoki.judgels.uriel.contest.clarification.html.listScreenedClarificationsView;
import org.iatoki.judgels.uriel.contest.clarification.html.editClarificationAnswerView;
import org.iatoki.judgels.uriel.contest.clarification.html.editClarificationContentView;
import org.iatoki.judgels.uriel.contest.html.accessTypeByStatusLayout;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
public class ContestClarificationController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String CLARIFICATION = "clarification";
    private static final String CONTEST = "contest";

    private final ContestClarificationService contestClarificationService;
    private final ContestProblemService contestProblemService;
    private final ContestService contestService;
    private final ContestTeamService contestTeamService;

    @Inject
    public ContestClarificationController(ContestClarificationService contestClarificationService, ContestProblemService contestProblemService, ContestService contestService, ContestTeamService contestTeamService) {
        this.contestClarificationService = contestClarificationService;
        this.contestProblemService = contestProblemService;
        this.contestService = contestService;
        this.contestTeamService = contestTeamService;
    }

    @Transactional
    public Result viewScreenedClarifications(long contestId) throws ContestNotFoundException {
        return listScreenedClarifications(contestId, 0, "id", "desc", "");
    }

    @Transactional
    public Result listScreenedClarifications(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        if (!contest.containsModule(ContestModules.CLARIFICATION)) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        Page<ContestClarification> pageOfContestClarifications;
        boolean coach = ContestControllerUtils.getInstance().isCoach(contest, IdentityUtils.getUserJid());
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
        contestClarificationService.readContestClarifications(IdentityUtils.getUserJid(), pageOfContestClarifications.getData().stream().filter(c -> c.isAnswered()).map(c -> c.getJid()).collect(Collectors.toList()), IdentityUtils.getIpAddress());

        LazyHtml content = new LazyHtml(listScreenedClarificationsView.render(contest, pageOfContestClarifications, pageIndex, orderBy, orderDir, filterString, coach));
        if (coach) {
            content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.list"), c));
        } else if (contest.containsModule(ContestModules.CLARIFICATION_TIME_LIMIT)) {
            ContestDurationModule contestDurationModule = (ContestDurationModule) contest.getModule(ContestModules.DURATION);
            ContestClarificationTimeLimitModule contestClarificationTimeLimitModule = (ContestClarificationTimeLimitModule) contest.getModule(ContestModules.CLARIFICATION_TIME_LIMIT);
            if (new Date().before(new Date(contestDurationModule.getBeginTime().getTime() + contestClarificationTimeLimitModule.getClarificationDuration()))) {
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
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);

        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("clarification.clarifications"), routes.ContestClarificationController.viewScreenedClarifications(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Clarifications");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createClarification(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToDoContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        if (!contest.containsModule(ContestModules.CLARIFICATION)) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        Form<ContestClarificationCreateForm> contestClarificationCreateForm = Form.form(ContestClarificationCreateForm.class);
        if (!contest.containsModule(ContestModules.CLARIFICATION_TIME_LIMIT)) {
            return showCreateClarification(contestClarificationCreateForm, contest);
        }

        ContestDurationModule contestDurationModule = (ContestDurationModule) contest.getModule(ContestModules.DURATION);
        ContestClarificationTimeLimitModule contestClarificationTimeLimitModule = (ContestClarificationTimeLimitModule) contest.getModule(ContestModules.CLARIFICATION_TIME_LIMIT);
        if (!new Date().before(new Date(contestDurationModule.getBeginTime().getTime() + contestClarificationTimeLimitModule.getClarificationDuration()))) {
            return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
        }

        return showCreateClarification(contestClarificationCreateForm, contest);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateClarification(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToDoContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        if (!contest.containsModule(ContestModules.CLARIFICATION)) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        if (!contest.containsModule(ContestModules.CLARIFICATION_TIME_LIMIT)) {
            return processCreateClarification(contest);
        }

        ContestDurationModule contestDurationModule = (ContestDurationModule) contest.getModule(ContestModules.DURATION);
        ContestClarificationTimeLimitModule contestClarificationTimeLimitModule = (ContestClarificationTimeLimitModule) contest.getModule(ContestModules.CLARIFICATION_TIME_LIMIT);
        if (!new Date().before(new Date(contestDurationModule.getBeginTime().getTime() + contestClarificationTimeLimitModule.getClarificationDuration()))) {
            return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
        }

        return processCreateClarification(contest);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editClarificationContent(long contestId, long contestClarificationId) throws ContestNotFoundException, ContestClarificationNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contest.containsModule(ContestModules.CLARIFICATION)) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        ContestClarification contestClarification = contestClarificationService.findContestClarificationById(contestClarificationId);
        if (contest.isLocked() || !ContestControllerUtils.getInstance().isCoach(contest, IdentityUtils.getUserJid()) || contestClarification.isAnswered() || !contestClarification.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestClarificationChangeForm contestClarificationChangeData = new ContestClarificationChangeForm();
        contestClarificationChangeData.title = contestClarification.getTitle();
        contestClarificationChangeData.question = contestClarification.getQuestion();
        Form<ContestClarificationChangeForm> contestClarificationChangeForm = Form.form(ContestClarificationChangeForm.class).fill(contestClarificationChangeData);
        contestClarificationChangeForm = contestClarificationChangeForm.fill(contestClarificationChangeData);

        return showEditClarificationContent(contestClarificationChangeForm, contest, contestClarification);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditClarificationContent(long contestId, long contestClarificationId) throws ContestNotFoundException, ContestClarificationNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contest.containsModule(ContestModules.CLARIFICATION)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestClarification contestClarification = contestClarificationService.findContestClarificationById(contestClarificationId);
        if (contest.isLocked() || !ContestControllerUtils.getInstance().isCoach(contest, IdentityUtils.getUserJid()) || contestClarification.isAnswered() || !contestClarification.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestClarificationChangeForm> contestClarificationChangeForm = Form.form(ContestClarificationChangeForm.class).bindFromRequest();

        if (formHasErrors(contestClarificationChangeForm)) {
            return showEditClarificationContent(contestClarificationChangeForm, contest, contestClarification);
        }

        ContestClarificationChangeForm contestClarificationChangeData = contestClarificationChangeForm.get();
        contestClarificationService.updateContestClarification(contestClarification.getJid(), contestClarificationChangeData.title, contestClarificationChangeData.question, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        if (!contestClarification.getTitle().equals(contestClarificationChangeData.title)) {
            UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.RENAME_IN.construct(CONTEST, contest.getJid(), contest.getName(), CLARIFICATION, contestClarification.getJid(), contestClarification.getTitle(), contestClarificationChangeData.title));
        }
        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.EDIT_IN.construct(CONTEST, contest.getJid(), contest.getName(), CLARIFICATION, contestClarification.getJid(), contestClarificationChangeData.title));

        return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
    }

    @Transactional(readOnly = true)
    public Result viewClarifications(long contestId) throws ContestNotFoundException {
        return listClarifications(contestId, 0, "id", "desc", "");
    }

    @Transactional(readOnly = true)
    public Result listClarifications(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contest.containsModule(ContestModules.CLARIFICATION)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        if (!isAllowedToSuperviseClarifications(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Page<ContestClarification> pageOfContestClarifications = contestClarificationService.getPageOfClarificationsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, null);

        LazyHtml content = new LazyHtml(listClarificationsView.render(contest.getId(), pageOfContestClarifications, pageIndex, orderBy, orderDir, filterString));

        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.list"), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestClarificationController.viewClarifications(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - All Clarifications");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editClarificationAnswer(long contestId, long contestClarificationId) throws ContestNotFoundException, ContestClarificationNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contest.containsModule(ContestModules.CLARIFICATION)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestClarification contestClarification = contestClarificationService.findContestClarificationById(contestClarificationId);
        if (contest.isLocked() || !isAllowedToSuperviseClarifications(contest) || !contestClarification.getContestJid().equals(contest.getJid())) {
            return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
        }

        ContestClarificationEditForm contestClarificationUpsertData = new ContestClarificationEditForm();
        contestClarificationUpsertData.answer = contestClarification.getAnswer();
        contestClarificationUpsertData.status = contestClarification.getStatus().name();
        Form<ContestClarificationEditForm> contestClarificationUpsertForm = Form.form(ContestClarificationEditForm.class).fill(contestClarificationUpsertData);

        return showEditClarificationAnswer(contestClarificationUpsertForm, contest, contestClarification);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditClarificationAnswer(long contestId, long contestClarificationId) throws ContestNotFoundException, ContestClarificationNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contest.containsModule(ContestModules.CLARIFICATION)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestClarification contestClarification = contestClarificationService.findContestClarificationById(contestClarificationId);
        if (contest.isLocked() || !isAllowedToSuperviseClarifications(contest) || !contestClarification.getContestJid().equals(contest.getJid())) {
            return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
        }

        Form<ContestClarificationEditForm> contestClarificationUpsertForm = Form.form(ContestClarificationEditForm.class).bindFromRequest();

        if (formHasErrors(contestClarificationUpsertForm)) {
            return showEditClarificationAnswer(contestClarificationUpsertForm, contest, contestClarification);
        }

        ContestClarificationEditForm contestClarificationEditData = contestClarificationUpsertForm.get();
        contestClarificationService.updateContestClarification(contestClarification.getJid(), contestClarificationEditData.answer, ContestClarificationStatus.valueOf(contestClarificationEditData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(UrielActivityKeys.ANSWER.construct(CONTEST, contest.getJid(), contest.getName(), CLARIFICATION, contestClarification.getJid(), contestClarification.getTitle()));

        return redirect(routes.ContestClarificationController.viewClarifications(contest.getId()));
    }

    private Result processCreateClarification(Contest contest) {
        Form<ContestClarificationCreateForm> contestClarificationCreateForm = Form.form(ContestClarificationCreateForm.class).bindFromRequest();

        if (formHasErrors(contestClarificationCreateForm)) {
            return showCreateClarification(contestClarificationCreateForm, contest);
        }

        ContestClarificationCreateForm contestClarificationCreateData = contestClarificationCreateForm.get();
        ContestClarification contestClarification = contestClarificationService.createContestClarification(contest.getJid(), contestClarificationCreateData.title, contestClarificationCreateData.question, contestClarificationCreateData.topicJid, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.ADD_IN.construct(CONTEST, contest.getJid(), contest.getName(), CLARIFICATION, contestClarification.getJid(), contestClarificationCreateData.title));

        return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
    }

    private Result showCreateClarification(Form<ContestClarificationCreateForm> contestClarificationCreateForm, Contest contest) {
        List<ContestProblem> contestProblems = contestProblemService.getOpenedProblemsInContest(contest.getJid());

        List<String> problemJids = contestProblemService.getOpenedProblemsInContest(contest.getJid())
                .stream()
                .map(p -> p.getProblemJid())
                .collect(Collectors.toList());

        Map<String, String> problemTitlesMap = SandalphonResourceDisplayNameUtils.buildTitlesMap(JidCacheServiceImpl.getInstance().getDisplayNames(problemJids), ContestControllerUtils.getInstance().getCurrentStatementLanguage());

        LazyHtml content = new LazyHtml(createClarificationView.render(contest, contestClarificationCreateForm, contestProblems, problemTitlesMap));
        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.create"), c));
        if (isAllowedToSuperviseClarifications(contest)) {
            appendSubtabsLayout(content, contest);
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.contestant"), routes.ContestClarificationController.viewScreenedClarifications(contest.getId())),
                new InternalLink(Messages.get("clarification.create"), routes.ContestClarificationController.createClarification(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Clarification - Create");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditClarificationContent(Form<ContestClarificationChangeForm> contestClarificationChangeForm, Contest contest, ContestClarification contestClarification) {
        LazyHtml content = new LazyHtml(editClarificationContentView.render(contest, contestClarification, contestClarificationChangeForm));
        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.update"), c));
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest, IdentityUtils.getUserJid())) {
            appendSubtabsLayout(content, contest);
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.contestant"), routes.ContestClarificationController.viewScreenedClarifications(contest.getId())),
                new InternalLink(Messages.get("clarification.update"), routes.ContestClarificationController.editClarificationContent(contest.getId(), contestClarification.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Clarification - Update Content");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditClarificationAnswer(Form<ContestClarificationEditForm> contestClarificationEditForm, Contest contest, ContestClarification contestClarification) {
        LazyHtml content = new LazyHtml(editClarificationAnswerView.render(contest.getId(), contestClarification, contestClarificationEditForm));
        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.update"), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestClarificationController.viewClarifications(contest.getId())),
                new InternalLink(Messages.get("clarification.update"), routes.ContestClarificationController.editClarificationAnswer(contest.getId(), contestClarification.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Clarification - Update Answer");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendSubtabsLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()), routes.ContestClarificationController.viewClarifications(contest.getId()), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("clarification.clarifications"), org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToClarifications(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseClarifications(Contest contest) {
        return ContestControllerUtils.getInstance().isPermittedToSupervise(contest, ContestPermissions.CLARIFICATION, IdentityUtils.getUserJid());
    }
}
