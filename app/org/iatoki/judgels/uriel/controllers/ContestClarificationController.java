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

    private final ContestService contestService;
    private final ContestClarificationService contestClarificationService;
    private final ContestModuleService contestModuleService;
    private final ContestProblemService contestProblemService;
    private final ContestSupervisorService contestSupervisorService;
    private final ContestTeamService contestTeamService;

    @Inject
    public ContestClarificationController(ContestService contestService, ContestClarificationService contestClarificationService, ContestModuleService contestModuleService, ContestProblemService contestProblemService, ContestSupervisorService contestSupervisorService, ContestTeamService contestTeamService) {
        this.contestService = contestService;
        this.contestClarificationService = contestClarificationService;
        this.contestModuleService = contestModuleService;
        this.contestProblemService = contestProblemService;
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

        if (ContestControllerUtils.getInstance().isAllowedToEnterContest(contest)) {
            if (!contestModuleService.containEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
                return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
            }
            ContestClarificationModule contestClarificationModule = (ContestClarificationModule) contestModuleService.getModule(contest.getJid(), ContestModules.CLARIFICATION);

            Page<ContestClarification> contestClarifications;
            boolean coach = ContestControllerUtils.getInstance().isCoach(contest);
            if (coach) {
                List<ContestTeam> contestTeams = contestTeamService.findContestTeamsByContestJidAndCoachJid(contest.getJid(), IdentityUtils.getUserJid());
                ImmutableList.Builder<ContestTeamMember> contestTeamMembersBuilder = ImmutableList.builder();
                for (ContestTeam team : contestTeams) {
                    contestTeamMembersBuilder.addAll(team.getMembers());
                }
                List<ContestTeamMember> contestTeamMembers = contestTeamMembersBuilder.build();
                contestClarifications = contestClarificationService.pageContestClarificationsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, contestTeamMembers.stream().map(ct -> ct.getMemberJid()).collect(Collectors.toList()));
            } else {
                contestClarifications = contestClarificationService.pageContestClarificationsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, ImmutableList.of(IdentityUtils.getUserJid()));
            }
            contestClarificationService.readContestClarifications(IdentityUtils.getUserJid(), contestClarifications.getData().stream().filter(c -> c.isAnswered()).map(c -> c.getJid()).collect(Collectors.toList()));

            LazyHtml content = new LazyHtml(listScreenedClarificationsView.render(contest, contestClarifications, pageIndex, orderBy, orderDir, filterString, coach));
            if (coach) {
                content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.list"), c));
            } else if (contestModuleService.containEnabledModule(contest.getJid(), ContestModules.DURATION)) {
                ContestDurationModule contestDurationModule = (ContestDurationModule) contestModuleService.getModule(contest.getJid(), ContestModules.DURATION);
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
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createClarification(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isAllowedToDoContest(contest)) {
            if (!contestModuleService.containEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
                return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
            }
            ContestClarificationModule contestClarificationModule = (ContestClarificationModule) contestModuleService.getModule(contest.getJid(), ContestModules.CLARIFICATION);

            Form<ContestClarificationCreateForm> form = Form.form(ContestClarificationCreateForm.class);
            if (contestModuleService.containEnabledModule(contest.getJid(), ContestModules.DURATION)) {
                ContestDurationModule contestDurationModule = (ContestDurationModule) contestModuleService.getModule(contest.getJid(), ContestModules.DURATION);
                if (new Date().before(new Date(contestDurationModule.getBeginTime().getTime() + contestClarificationModule.getClarificationDuration()))) {
                    ControllerUtils.getInstance().addActivityLog("Try to create clarification in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

                    return showCreateClarification(form, contest);
                } else {
                    return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
                }
            } else {
                ControllerUtils.getInstance().addActivityLog("Try to create clarification in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

                return showCreateClarification(form, contest);
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateClarification(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isAllowedToDoContest(contest)) {
            if (!contestModuleService.containEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
                return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
            }
            ContestClarificationModule contestClarificationModule = (ContestClarificationModule) contestModuleService.getModule(contest.getJid(), ContestModules.CLARIFICATION);

            if (contestModuleService.containEnabledModule(contest.getJid(), ContestModules.DURATION)) {
                ContestDurationModule contestDurationModule = (ContestDurationModule) contestModuleService.getModule(contest.getJid(), ContestModules.DURATION);
                if (new Date().before(new Date(contestDurationModule.getBeginTime().getTime() + contestClarificationModule.getClarificationDuration()))) {
                    Form<ContestClarificationCreateForm> form = Form.form(ContestClarificationCreateForm.class).bindFromRequest();

                    if (form.hasErrors() || form.hasGlobalErrors()) {
                        return showCreateClarification(form, contest);
                    } else {
                        ContestClarificationCreateForm contestClarificationCreateForm = form.get();
                        contestClarificationService.createContestClarification(contest.getId(), contestClarificationCreateForm.title, contestClarificationCreateForm.question, contestClarificationCreateForm.topicJid);

                        ControllerUtils.getInstance().addActivityLog("Create clarification " + contestClarificationCreateForm.title + " in contest " + contest.getName() + ".");

                        return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
                    }
                } else {
                    return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
                }
            } else {
                Form<ContestClarificationCreateForm> form = Form.form(ContestClarificationCreateForm.class).bindFromRequest();

                if (form.hasErrors() || form.hasGlobalErrors()) {
                    return showCreateClarification(form, contest);
                } else {
                    ContestClarificationCreateForm contestClarificationCreateForm = form.get();
                    contestClarificationService.createContestClarification(contest.getId(), contestClarificationCreateForm.title, contestClarificationCreateForm.question, contestClarificationCreateForm.topicJid);

                    ControllerUtils.getInstance().addActivityLog("Create clarification " + contestClarificationCreateForm.title + " in contest " + contest.getName() + ".");

                    return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
                }
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateClarificationContent(long contestId, long contestClarificationId) throws ContestNotFoundException, ContestClarificationNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contestModuleService.containEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        ContestClarification contestClarification = contestClarificationService.findContestClarificationByContestClarificationId(contestClarificationId);
        if (ContestControllerUtils.getInstance().isCoach(contest) && !contestClarification.isAnswered() && contestClarification.getContestJid().equals(contest.getJid())) {
            ContestClarificationChangeForm contestClarificationChangeForm = new ContestClarificationChangeForm();
            contestClarificationChangeForm.title = contestClarification.getTitle();
            contestClarificationChangeForm.question = contestClarification.getQuestion();
            Form<ContestClarificationChangeForm> form = Form.form(ContestClarificationChangeForm.class).fill(contestClarificationChangeForm);
            form = form.fill(contestClarificationChangeForm);

            ControllerUtils.getInstance().addActivityLog("Try to update clarification " + contestClarification.getTitle() + " content in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateClarificationContent(form, contest, contestClarification);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateClarificationContent(long contestId, long contestClarificationId) throws ContestNotFoundException, ContestClarificationNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contestModuleService.containEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        ContestClarification contestClarification = contestClarificationService.findContestClarificationByContestClarificationId(contestClarificationId);
        if (ContestControllerUtils.getInstance().isCoach(contest) && !contestClarification.isAnswered() && contestClarification.getContestJid().equals(contest.getJid())) {
            Form<ContestClarificationChangeForm> form = Form.form(ContestClarificationChangeForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateClarificationContent(form, contest, contestClarification);
            } else {
                ContestClarificationChangeForm contestClarificationChangeForm = form.get();
                contestClarificationService.updateContestClarification(contestClarification.getId(), contestClarificationChangeForm.title, contestClarificationChangeForm.question);

                ControllerUtils.getInstance().addActivityLog("Update clarification " + contestClarification.getTitle() + " content in contest " + contest.getName() + ".");

                return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    public Result viewClarifications(long contestId) throws ContestNotFoundException {
        return listClarifications(contestId, 0, "id", "desc", "");
    }

    @Transactional(readOnly = true)
    public Result listClarifications(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contestModuleService.containEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        if (isAllowedToSuperviseClarifications(contest)) {
            Page<ContestClarification> contestClarifications = contestClarificationService.pageContestClarificationsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, null);

            LazyHtml content = new LazyHtml(listClarificationsView.render(contest.getId(), contestClarifications, pageIndex, orderBy, orderDir, filterString));

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
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateClarificationAnswer(long contestId, long contestClarificationId) throws ContestNotFoundException, ContestClarificationNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contestModuleService.containEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        ContestClarification contestClarification = contestClarificationService.findContestClarificationByContestClarificationId(contestClarificationId);
        if (isAllowedToSuperviseClarifications(contest) && contestClarification.getContestJid().equals(contest.getJid())) {
            ContestClarificationUpdateForm contestClarificationUpsertForm = new ContestClarificationUpdateForm();
            contestClarificationUpsertForm.answer = contestClarification.getAnswer();
            contestClarificationUpsertForm.status = contestClarification.getStatus().name();
            Form<ContestClarificationUpdateForm> form = Form.form(ContestClarificationUpdateForm.class).fill(contestClarificationUpsertForm);

            ControllerUtils.getInstance().addActivityLog("Try to answer clarification " + contestClarification.getTitle() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateClarificationAnswer(form, contest, contestClarification);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateClarificationAnswer(long contestId, long contestClarificationId) throws ContestNotFoundException, ContestClarificationNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contestModuleService.containEnabledModule(contest.getJid(), ContestModules.CLARIFICATION)) {
            return redirect(routes.ContestController.jumpToAnnouncements(contest.getId()));
        }

        ContestClarification contestClarification = contestClarificationService.findContestClarificationByContestClarificationId(contestClarificationId);
        if (isAllowedToSuperviseClarifications(contest) && contestClarification.getContestJid().equals(contest.getJid())) {
            Form<ContestClarificationUpdateForm> form = Form.form(ContestClarificationUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateClarificationAnswer(form, contest, contestClarification);
            } else {
                ContestClarificationUpdateForm contestClarificationUpdateForm = form.get();
                contestClarificationService.updateContestClarification(contestClarification.getId(), contestClarificationUpdateForm.answer, ContestClarificationStatus.valueOf(contestClarificationUpdateForm.status));

                ControllerUtils.getInstance().addActivityLog("Answer clarification " + contestClarification.getTitle() + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestClarificationController.viewClarifications(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    private Result showCreateClarification(Form<ContestClarificationCreateForm> form, Contest contest) {
        List<ContestProblem> contestProblemList = contestProblemService.findOpenedContestProblemByContestJid(contest.getJid());

        LazyHtml content = new LazyHtml(createClarificationView.render(contest, form, contestProblemList));
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

    private Result showUpdateClarificationContent(Form<ContestClarificationChangeForm> form, Contest contest, ContestClarification contestClarification) {
        LazyHtml content = new LazyHtml(updateClarificationContentView.render(contest, contestClarification, form));
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

    private Result showUpdateClarificationAnswer(Form<ContestClarificationUpdateForm> form, Contest contest, ContestClarification contestClarification) {
        LazyHtml content = new LazyHtml(updateClarificationAnswerView.render(contest.getId(), contestClarification, form));
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
        return ControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest) || (ContestControllerUtils.getInstance().isSupervisor(contest) && contestSupervisorService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).getContestPermission().isAllowed(ContestPermissions.CLARIFICATION));
    }
}
