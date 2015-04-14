package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.alertLayout;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.commons.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestClarification;
import org.iatoki.judgels.uriel.ContestClarificationChangeForm;
import org.iatoki.judgels.uriel.ContestClarificationCreateForm;
import org.iatoki.judgels.uriel.ContestClarificationStatus;
import org.iatoki.judgels.uriel.ContestClarificationUpdateForm;
import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
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
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public class ContestClarificationController extends Controller {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;

    public ContestClarificationController(ContestService contestService) {
        this.contestService = contestService;
    }

    public Result viewScreenedClarifications(long contestId) {
        return listScreenedClarifications(contestId, 0, "id", "desc", "");
    }

    public Result listScreenedClarifications(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isAllowedToEnterContest(contest)) {
            Page<ContestClarification> contestClarifications;
            boolean coach = ContestControllerUtils.getInstance().isCoach(contest);
            if (coach) {
                List<ContestTeam> contestTeams = contestService.findContestTeamsByContestJidAndCoachJid(contest.getJid(), IdentityUtils.getUserJid());
                ImmutableList.Builder<ContestTeamMember> contestTeamMembersBuilder = ImmutableList.builder();
                for (ContestTeam team : contestTeams) {
                    contestTeamMembersBuilder.addAll(team.getMembers());
                }
                List<ContestTeamMember> contestTeamMembers = contestTeamMembersBuilder.build();
                contestClarifications = contestService.pageContestClarificationsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, contestTeamMembers.stream().map(ct -> ct.getMemberJid()).collect(Collectors.toList()));
            } else {
                contestClarifications = contestService.pageContestClarificationsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, ImmutableList.of(IdentityUtils.getUserJid()));
            }
            contestService.readContestClarifications(IdentityUtils.getUserJid(), contestClarifications.getData().stream().map(c -> c.getId()).collect(Collectors.toList()));

            LazyHtml content = new LazyHtml(listScreenedClarificationsView.render(contest, contestClarifications, pageIndex, orderBy, orderDir, filterString, coach));
            if (coach) {
                content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.list"), c));
            } else if (contest.isClarificationTimeValid()) {
                content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("clarification.list"), new InternalLink(Messages.get("commons.create"), routes.ContestClarificationController.createClarification(contest.getId())), c));
            } else {
                content.appendLayout(c -> alertLayout.render(Messages.get("clarification.time_ended"), c));
                content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.list"), c));
            }
            if (isAllowedToSuperviseClarifications(contest)) {
                content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()), routes.ContestClarificationController.viewClarifications(contest.getId()), c));
            }
            ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                  new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                  new InternalLink(Messages.get("clarification.clarifications"), routes.ContestClarificationController.viewScreenedClarifications(contest.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Clarifications");

            ControllerUtils.getInstance().addActivityLog("Open list of own clarifications in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result createClarification(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isAllowedToDoContest(contest)) {
            if (contest.isClarificationTimeValid()) {
                Form<ContestClarificationCreateForm> form = Form.form(ContestClarificationCreateForm.class);

                ControllerUtils.getInstance().addActivityLog("Try to create clarification in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

                return showCreateClarification(form, contest);
            } else {
                return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateClarification(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isAllowedToDoContest(contest)) {
            if (contest.isClarificationTimeValid()) {
                Form<ContestClarificationCreateForm> form = Form.form(ContestClarificationCreateForm.class).bindFromRequest();

                if (form.hasErrors() || form.hasGlobalErrors()) {
                    return showCreateClarification(form, contest);
                } else {
                    ContestClarificationCreateForm contestClarificationCreateForm = form.get();
                    contestService.createContestClarification(contest.getId(), contestClarificationCreateForm.title, contestClarificationCreateForm.question, contestClarificationCreateForm.topicJid);

                    ControllerUtils.getInstance().addActivityLog("Create clarification " + contestClarificationCreateForm.title + " in contest " + contest.getName() + ".");

                    return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
                }
            } else {
                return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateClarificationContent(long contestId, long contestClarificationId) {
        Contest contest = contestService.findContestById(contestId);
        ContestClarification contestClarification = contestService.findContestClarificationByContestClarificationId(contestClarificationId);
        if (ContestControllerUtils.getInstance().isCoach(contest) && !contestClarification.isAnswered() && contestClarification.getContestJid().equals(contest.getJid())) {
            ContestClarificationChangeForm contestClarificationChangeForm = new ContestClarificationChangeForm(contestClarification);
            Form<ContestClarificationChangeForm> form = Form.form(ContestClarificationChangeForm.class).fill(contestClarificationChangeForm);
            form = form.fill(contestClarificationChangeForm);

            ControllerUtils.getInstance().addActivityLog("Try to update clarification " + contestClarification.getTitle() + " content in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateClarificationContent(form, contest, contestClarification);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateClarificationContent(long contestId, long contestClarificationId) {
        Contest contest = contestService.findContestById(contestId);
        ContestClarification contestClarification = contestService.findContestClarificationByContestClarificationId(contestClarificationId);
        if (ContestControllerUtils.getInstance().isCoach(contest) && !contestClarification.isAnswered() && contestClarification.getContestJid().equals(contest.getJid())) {
            Form<ContestClarificationChangeForm> form = Form.form(ContestClarificationChangeForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateClarificationContent(form, contest, contestClarification);
            } else {
                ContestClarificationChangeForm contestClarificationChangeForm = form.get();
                contestService.updateContestClarification(contestClarification.getId(), contestClarificationChangeForm.title, contestClarificationChangeForm.question);

                ControllerUtils.getInstance().addActivityLog("Update clarification " + contestClarification.getTitle() + " content in contest " + contest.getName() + ".");

                return redirect(routes.ContestClarificationController.viewScreenedClarifications(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    public Result viewClarifications(long contestId) {
        return listClarifications(contestId, 0, "id", "desc", "");
    }

    public Result listClarifications(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseClarifications(contest)) {
            Page<ContestClarification> contestClarifications = contestService.pageContestClarificationsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, null);

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

    @AddCSRFToken
    public Result updateClarificationAnswer(long contestId, long contestClarificationId) {
        Contest contest = contestService.findContestById(contestId);
        ContestClarification contestClarification = contestService.findContestClarificationByContestClarificationId(contestClarificationId);
        if (isAllowedToSuperviseClarifications(contest) && contestClarification.getContestJid().equals(contest.getJid())) {
            ContestClarificationUpdateForm contestClarificationUpsertForm = new ContestClarificationUpdateForm(contestClarification);
            Form<ContestClarificationUpdateForm> form = Form.form(ContestClarificationUpdateForm.class).fill(contestClarificationUpsertForm);

            ControllerUtils.getInstance().addActivityLog("Try to answer clarification " + contestClarification.getTitle() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateClarificationAnswer(form, contest, contestClarification);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateClarificationAnswer(long contestId, long contestClarificationId) {
        Contest contest = contestService.findContestById(contestId);
        ContestClarification contestClarification = contestService.findContestClarificationByContestClarificationId(contestClarificationId);
        if (isAllowedToSuperviseClarifications(contest) && contestClarification.getContestJid().equals(contest.getJid())) {
            Form<ContestClarificationUpdateForm> form = Form.form(ContestClarificationUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateClarificationAnswer(form, contest, contestClarification);
            } else {
                ContestClarificationUpdateForm contestClarificationUpdateForm = form.get();
                contestService.updateContestClarification(contestClarification.getId(), contestClarificationUpdateForm.answer, ContestClarificationStatus.valueOf(contestClarificationUpdateForm.status));

                ControllerUtils.getInstance().addActivityLog("Answer clarification " + contestClarification.getTitle() + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestClarificationController.viewClarifications(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    private Result showCreateClarification(Form<ContestClarificationCreateForm> form, Contest contest){
        List<ContestProblem> contestProblemList = contestService.findOpenedContestProblemByContestJid(contest.getJid());

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

    private Result showUpdateClarificationContent(Form<ContestClarificationChangeForm> form, Contest contest, ContestClarification contestClarification){
        LazyHtml content = new LazyHtml(updateClarificationContentView.render(contest, contestClarification, form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("clarification.update"), c));
        if(ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
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

    private Result showUpdateClarificationAnswer(Form<ContestClarificationUpdateForm> form, Contest contest, ContestClarification contestClarification){
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
        return ControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest) || (ContestControllerUtils.getInstance().isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isClarification());
    }
}
