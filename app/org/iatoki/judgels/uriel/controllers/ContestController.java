package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.accessTypesLayout;
import org.iatoki.judgels.commons.views.html.layouts.baseLayout;
import org.iatoki.judgels.commons.views.html.layouts.breadcrumbsLayout;
import org.iatoki.judgels.commons.views.html.layouts.headerFooterLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.leftSidebarLayout;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.jophiel.commons.JophielUtils;
import org.iatoki.judgels.sandalphon.commons.SandalphonUtils;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestAnnouncement;
import org.iatoki.judgels.uriel.ContestAnnouncementStatus;
import org.iatoki.judgels.uriel.ContestAnnouncementUpsertForm;
import org.iatoki.judgels.uriel.ContestClarification;
import org.iatoki.judgels.uriel.ContestClarificationCreateForm;
import org.iatoki.judgels.uriel.ContestClarificationStatus;
import org.iatoki.judgels.uriel.ContestClarificationUpdateForm;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantCreateForm;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestContestantUpdateForm;
import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.ContestProblemCreateForm;
import org.iatoki.judgels.uriel.ContestProblemStatus;
import org.iatoki.judgels.uriel.ContestProblemUpdateForm;
import org.iatoki.judgels.uriel.ContestScope;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.ContestType;
import org.iatoki.judgels.uriel.ContestUpsertForm;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.UserRoleService;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.createClarificationView;
import org.iatoki.judgels.uriel.views.html.contest.createView;
import org.iatoki.judgels.uriel.views.html.contest.listAnnouncementView;
import org.iatoki.judgels.uriel.views.html.contest.listClarificationView;
import org.iatoki.judgels.uriel.views.html.contest.listProblemView;
import org.iatoki.judgels.uriel.views.html.contest.listView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.announcement.createSupervisorAnnouncementView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.announcement.listSupervisorAnnouncementView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.announcement.updateSupervisorAnnouncementView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.clarification.listSupervisorClarificationView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.clarification.updateSupervisorClarificationView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.contestant.createSupervisorContestantView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.contestant.listSupervisorContestantView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.contestant.updateSupervisorContestantView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.problem.createSupervisorProblemView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.problem.listSupervisorProblemView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.problem.updateSupervisorProblemView;
import org.iatoki.judgels.uriel.views.html.contest.updateView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class ContestController extends Controller {

    private static final long PAGE_SIZE = 20;
    private final ContestService contestService;
    private final UserRoleService userRoleService;

    public ContestController(ContestService contestService, UserRoleService userRoleService) {
        this.contestService = contestService;
        this.userRoleService = userRoleService;
    }

    public Result index() {
        return list(0, "id", "asc", "");
    }

    private Result showCreate(Form<ContestUpsertForm> form) {
        LazyHtml content = new LazyHtml(createView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.create"), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(Messages.get("contest.create"), routes.ContestController.create())
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    @AddCSRFToken
    public Result create() {
        Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class);

        return showCreate(form);
    }

    @RequireCSRFCheck
    public Result postCreate() {
        Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreate(form);
        } else {
            ContestUpsertForm contestUpsertForm = form.get();
            contestService.createContest(contestUpsertForm.name, contestUpsertForm.description, ContestType.valueOf(contestUpsertForm.type), ContestScope.valueOf(contestUpsertForm.scope), ContestStyle.valueOf(contestUpsertForm.style), UrielUtils.convertStringToDate(contestUpsertForm.startTime), UrielUtils.convertStringToDate(contestUpsertForm.endTime));

            return redirect(routes.ContestController.index());
        }
    }

    private Result showUpdate(Form<ContestUpsertForm> form, Contest contest) {
        LazyHtml content = new LazyHtml(updateView.render(form, contest.getId()));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.update"), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(Messages.get("contest.update"), routes.ContestController.update(contest.getId()))
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    @AddCSRFToken
    public Result update(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        ContestUpsertForm contestUpsertForm = new ContestUpsertForm(contest);
        Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).fill(contestUpsertForm);

        return showUpdate(form, contest);
    }

    @RequireCSRFCheck
    public Result postUpdate(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        Form<ContestUpsertForm> form = Form.form(ContestUpsertForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return showUpdate(form, contest);
        } else {
            ContestUpsertForm contestUpsertForm = form.get();
            contestService.updateContest(contest.getId(), contestUpsertForm.name, contestUpsertForm.description, ContestType.valueOf(contestUpsertForm.type), ContestScope.valueOf(contestUpsertForm.scope), ContestStyle.valueOf(contestUpsertForm.style), UrielUtils.convertStringToDate(contestUpsertForm.startTime), UrielUtils.convertStringToDate(contestUpsertForm.endTime));

            return redirect(routes.ContestController.index());
        }
    }

    public Result delete(long contestId) {
        contestService.deleteContest(contestId);

        return redirect(routes.ContestController.index());
    }

    public Result list(long page, String sortBy, String orderBy, String filterString) {
        Page<Contest> currentPage = contestService.pageContest(page, PAGE_SIZE, sortBy, orderBy, filterString);

        LazyHtml content = new LazyHtml(listView.render(currentPage, sortBy, orderBy, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("contest.list"), new InternalLink(Messages.get("contest.create"), routes.ContestController.create()), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index())
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    public Result viewAnnouncement(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contest")) {
            List<ContestAnnouncement> contestAnnouncementList = contestService.findPublishedContestAnnouncementByContestJid(contest.getJid());

            LazyHtml content = new LazyHtml(listAnnouncementView.render(contest.getId(), contestAnnouncementList));

            if (checkIfPermitted(contest, "announcement")) {
                content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewAnnouncement(contest.getId()), routes.ContestController.viewSupervisorAnnouncement(contest.getId()), c));
            }
            appendViewTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewAnnouncement(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return forbidden();
        }
    }

    public Result viewProblem(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contest")) {
            List<ContestProblem> contestProblemList = contestService.findOpenedContestProblemByContestJid(contest.getJid());

            LazyHtml content = new LazyHtml(listProblemView.render(contest.getId(), contestProblemList));

            if (checkIfPermitted(contest, "problem")) {
                content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewProblem(contest.getId()), routes.ContestController.viewSupervisorProblem(contest.getId()), c));
            }
            appendViewTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewProblem(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return forbidden();
        }
    }

    public Result viewSubmission(long contestId) {
        return TODO;
    }

    public Result viewClarification(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contest")) {
            List<ContestClarification> contestClarificationList = contestService.findContestClarificationByContestJidAndAskerJid(contest.getJid(), IdentityUtils.getUserJid());

            LazyHtml content = new LazyHtml(listClarificationView.render(contest.getId(), contestClarificationList));

            content.appendLayout(c -> headingWithActionLayout.render(Messages.get("contest.clarifications"), new InternalLink(Messages.get("contest.clarification.create"), routes.ContestController.createContestClarification(contest.getId())), c));
            if (checkIfPermitted(contest, "clarification")) {
                content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewClarification(contest.getId()), routes.ContestController.viewSupervisorClarification(contest.getId()), c));
            }
            appendViewTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewClarification(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return forbidden();
        }
    }

    public Result viewSupervisorAnnouncement(long contestId) {
        return listContestAnnouncement(contestId, "timeUpdate", "desc", "");
    }

    public Result viewSupervisorProblem(long contestId) {
        return listContestProblem(contestId, 0, "alias", "asc", "");
    }

    public Result viewSupervisorSubmission(long contestId) {
        return TODO;
    }

    public Result viewSupervisorClarification(long contestId) {
        return listContestClarification(contestId, 0, "timeCreate", "desc", "");
    }

    public Result viewSupervisorContestant(long contestId) {
        return listContestContestant(contestId, 0, "timeUpdate", "desc", "");
    }

    public Result viewManagerSupervisor(long contestId) {
        return TODO;
    }

    public Result viewAdminManager(long contestId) {
        return TODO;
    }

    /*
        Contest Announcement Section
     */

    public Result listContestAnnouncement(long contestId, String sortBy, String orderBy, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "announcement")) {
            List<ContestAnnouncement> contestAnnouncementList = contestService.findContestAnnouncementByContestJid(contest.getJid(), sortBy, orderBy, filterString);

            LazyHtml content = new LazyHtml(listSupervisorAnnouncementView.render(contest.getId(), contestAnnouncementList, sortBy, orderBy, filterString));

            content.appendLayout(c -> headingWithActionLayout.render(Messages.get("contest.announcements"), new InternalLink(Messages.get("contest.announcement.create"), routes.ContestController.createContestAnnouncement(contest.getId())), c));
            content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewAnnouncement(contest.getId()), routes.ContestController.viewSupervisorAnnouncement(contest.getId()), c));
            appendViewTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewAnnouncement(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    private Result showCreateContestAnnouncement(Form<ContestAnnouncementUpsertForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createSupervisorAnnouncementView.render(contest.getId(), form));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.announcement.create"), c));
        content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewAnnouncement(contest.getId()), routes.ContestController.viewSupervisorAnnouncement(contest.getId()), c));
        appendViewTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewAnnouncement(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    @AddCSRFToken
    public Result createContestAnnouncement(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "announcement")) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class);

            return showCreateContestAnnouncement(form, contest);
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postCreateContestAnnouncement(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "announcement")) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateContestAnnouncement(form, contest);
            } else {
                ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = form.get();
                contestService.createContestAnnouncement(contest.getId(), contestAnnouncementUpsertForm.title, contestAnnouncementUpsertForm.announcement, ContestAnnouncementStatus.valueOf(contestAnnouncementUpsertForm.status));

                return redirect(routes.ContestController.viewSupervisorAnnouncement(contest.getId()));
            }
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    private Result showUpdateContestAnnouncement(Form<ContestAnnouncementUpsertForm> form, Contest contest, ContestAnnouncement contestAnnouncement){
        LazyHtml content = new LazyHtml(updateSupervisorAnnouncementView.render(contest.getId(), contestAnnouncement.getId(), form));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.announcement.update"), c));
        content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewAnnouncement(contest.getId()), routes.ContestController.viewSupervisorAnnouncement(contest.getId()), c));
        appendViewTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewAnnouncement(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    @AddCSRFToken
    public Result updateContestAnnouncement(long contestId, long contestAnnouncementId) {
        Contest contest = contestService.findContestById(contestId);
        ContestAnnouncement contestAnnouncement = contestService.findContestAnnouncementByContestAnnouncementId(contestAnnouncementId);
        if ((checkIfPermitted(contest, "announcement")) && (contestAnnouncement.getContestJid().equals(contest.getJid()))) {
            ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = new ContestAnnouncementUpsertForm(contestAnnouncement);
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).fill(contestAnnouncementUpsertForm);

            return showUpdateContestAnnouncement(form, contest, contestAnnouncement);
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postUpdateContestAnnouncement(long contestId, long contestAnnouncementId) {
        Contest contest = contestService.findContestById(contestId);
        ContestAnnouncement contestAnnouncement = contestService.findContestAnnouncementByContestAnnouncementId(contestAnnouncementId);
        if ((checkIfPermitted(contest, "announcement")) && (contestAnnouncement.getContestJid().equals(contest.getJid()))) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateContestAnnouncement(form, contest, contestAnnouncement);
            } else {
                ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = form.get();
                contestService.updateContestAnnouncement(contestAnnouncement.getId(), contestAnnouncementUpsertForm.title, contestAnnouncementUpsertForm.announcement, ContestAnnouncementStatus.valueOf(contestAnnouncementUpsertForm.status));

                return redirect(routes.ContestController.viewSupervisorAnnouncement(contest.getId()));
            }
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    /*
        Contest Problem Section
     */

    public Result listContestProblem(long contestId, long page, String sortBy, String orderBy, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "problem")) {
            Page<ContestProblem> contestProblemPage = contestService.pageContestProblemByContestJid(contest.getJid(), page, PAGE_SIZE, sortBy, orderBy, filterString);

            LazyHtml content = new LazyHtml(listSupervisorProblemView.render(contest.getId(), contestProblemPage, page, sortBy, orderBy, filterString));

            content.appendLayout(c -> headingWithActionLayout.render(Messages.get("contest.problems"), new InternalLink(Messages.get("contest.problem.create"), routes.ContestController.createContestProblem(contestId)), c));
            content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewProblem(contest.getId()), routes.ContestController.viewSupervisorProblem(contest.getId()), c));
            appendViewTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewSupervisorProblem(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    private Result showCreateContestProblem(Form<ContestProblemCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createSupervisorProblemView.render(contest.getId(), form));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.problem.create"), c));
        content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewProblem(contest.getId()), routes.ContestController.viewSupervisorProblem(contest.getId()), c));
        appendViewTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewSupervisorProblem(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    @AddCSRFToken
    public Result createContestProblem(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "problem")) {
            Form<ContestProblemCreateForm> form = Form.form(ContestProblemCreateForm.class);

            return showCreateContestProblem(form, contest);
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postCreateContestProblem(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contestant")) {
            Form<ContestProblemCreateForm> form = Form.form(ContestProblemCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateContestProblem(form, contest);
            } else {
                ContestProblemCreateForm contestProblemCreateForm = form.get();
                if ((SandalphonUtils.verifyProblemJid(contestProblemCreateForm.problemJid)) && (!contestService.isContestProblemInContestByProblemJid(contest.getJid(), contestProblemCreateForm.problemJid))) {
                    contestService.createContestProblem(contest.getId(), contestProblemCreateForm.problemJid, contestProblemCreateForm.problemSecret, contestProblemCreateForm.alias, contestProblemCreateForm.name, contestProblemCreateForm.submissionLimit, ContestProblemStatus.valueOf(contestProblemCreateForm.status));

                    return redirect(routes.ContestController.viewSupervisorProblem(contest.getId()));
                } else {
                    return showCreateContestProblem(form, contest);
                }
            }
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    private Result showUpdateContestProblem(Form<ContestProblemUpdateForm> form, Contest contest, ContestProblem contestProblem){
        LazyHtml content = new LazyHtml(updateSupervisorProblemView.render(contest.getId(), contestProblem.getId(), form));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.problem.update"), c));
        content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewProblem(contest.getId()), routes.ContestController.viewSupervisorProblem(contest.getId()), c));
        appendViewTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewSupervisorProblem(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    @AddCSRFToken
    public Result updateContestProblem(long contestId, long contestProblemId) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);
        if ((checkIfPermitted(contest, "problem")) && (contestProblem.getContestJid().equals(contest.getJid()))) {
            ContestProblemUpdateForm contestProblemUpdateForm = new ContestProblemUpdateForm(contestProblem);
            Form<ContestProblemUpdateForm> form = Form.form(ContestProblemUpdateForm.class).fill(contestProblemUpdateForm);

            return showUpdateContestProblem(form, contest, contestProblem);
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postUpdateContestProblem(long contestId, long contestProblemId) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);
        if ((checkIfPermitted(contest, "problem")) && (contestProblem.getContestJid().equals(contest.getJid()))) {
            Form<ContestProblemUpdateForm> form = Form.form(ContestProblemUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateContestProblem(form, contest, contestProblem);
            } else {
                ContestProblemUpdateForm contestProblemUpdateForm = form.get();
                contestService.updateContestProblem(contestProblem.getId(), contestProblemUpdateForm.problemSecret, contestProblemUpdateForm.alias, contestProblemUpdateForm.name, contestProblemUpdateForm.submissionLimit, ContestProblemStatus.valueOf(contestProblemUpdateForm.status));

                return redirect(routes.ContestController.viewSupervisorProblem(contest.getId()));
            }
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    /*
        Contest Clarification Section
     */

    public Result listContestClarification(long contestId, long page, String sortBy, String orderBy, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "clarification")) {
            Page<ContestClarification> contestClarificationPage = contestService.pageContestClarificationByContestJid(contest.getJid(), page, PAGE_SIZE, sortBy, orderBy, filterString);

            LazyHtml content = new LazyHtml(listSupervisorClarificationView.render(contest.getId(), contestClarificationPage, page, sortBy, orderBy, filterString));

            content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewClarification(contest.getId()), routes.ContestController.viewSupervisorClarification(contest.getId()), c));
            content.appendLayout(c -> headingLayout.render(Messages.get("contest.clarifications"), c));
            appendViewTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewClarification(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return redirect(routes.ContestController.viewClarification(contest.getId()));
        }
    }

    private Result showCreateContestClarification(Form<ContestClarificationCreateForm> form, Contest contest){
        List<ContestProblem> contestProblemList = contestService.findOpenedContestProblemByContestJid(contest.getJid());

        LazyHtml content = new LazyHtml(createClarificationView.render(contest, form, contestProblemList));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.clarification.create"), c));
        if (checkIfPermitted(contest, "clarification")) {
            content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewClarification(contest.getId()), routes.ContestController.viewSupervisorClarification(contest.getId()), c));
        }
        appendViewTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewClarification(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    @AddCSRFToken
    public Result createContestClarification(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        Form<ContestClarificationCreateForm> form = Form.form(ContestClarificationCreateForm.class);

        return showCreateContestClarification(form, contest);
    }

    @RequireCSRFCheck
    public Result postCreateContestClarification(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        Form<ContestClarificationCreateForm> form = Form.form(ContestClarificationCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateContestClarification(form, contest);
        } else {
            ContestClarificationCreateForm contestClarificationCreateForm = form.get();
            contestService.createContestClarification(contest.getId(), contestClarificationCreateForm.question, contestClarificationCreateForm.topicJid);

            return redirect(routes.ContestController.viewClarification(contest.getId()));
        }
    }

    private Result showUpdateContestClarification(Form<ContestClarificationUpdateForm> form, Contest contest, ContestClarification contestClarification){
        LazyHtml content = new LazyHtml(updateSupervisorClarificationView.render(contest.getId(), contestClarification, form));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.clarification.update"), c));
        content.appendLayout(c -> accessTypesLayout.render(routes.ContestController.viewClarification(contest.getId()), routes.ContestController.viewSupervisorClarification(contest.getId()), c));
        appendViewTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewClarification(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    @AddCSRFToken
    public Result updateContestClarification(long contestId, long contestClarificationId) {
        Contest contest = contestService.findContestById(contestId);
        ContestClarification contestClarification = contestService.findContestClarificationByContestClarificationId(contestClarificationId);
        if ((checkIfPermitted(contest, "clarification")) && (contestClarification.getContestJid().equals(contest.getJid()))) {
            ContestClarificationUpdateForm contestClarificationUpsertForm = new ContestClarificationUpdateForm(contestClarification);
            Form<ContestClarificationUpdateForm> form = Form.form(ContestClarificationUpdateForm.class).fill(contestClarificationUpsertForm);

            return showUpdateContestClarification(form, contest, contestClarification);
        } else {
            return redirect(routes.ContestController.viewClarification(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postUpdateContestClarification(long contestId, long contestClarificationId) {
        Contest contest = contestService.findContestById(contestId);
        ContestClarification contestClarification = contestService.findContestClarificationByContestClarificationId(contestClarificationId);
        if ((checkIfPermitted(contest, "clarification")) && (contestClarification.getContestJid().equals(contest.getJid()))) {
            Form<ContestClarificationUpdateForm> form = Form.form(ContestClarificationUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateContestClarification(form, contest, contestClarification);
            } else {
                ContestClarificationUpdateForm contestClarificationUpdateForm = form.get();
                contestService.updateContestClarification(contestClarification.getId(), contestClarificationUpdateForm.answer, ContestClarificationStatus.valueOf(contestClarificationUpdateForm.status));

                return redirect(routes.ContestController.viewSupervisorClarification(contest.getId()));
            }
        } else {
            return redirect(routes.ContestController.viewClarification(contest.getId()));
        }
    }

    /*
        Contest Contestant Section
     */

    public Result listContestContestant(long contestId, long page, String sortBy, String orderBy, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contestant")) {
            Page<ContestContestant> contestContestantPage = contestService.pageContestContestantByContestJid(contest.getJid(), page, PAGE_SIZE, sortBy, orderBy, filterString);

            LazyHtml content = new LazyHtml(listSupervisorContestantView.render(contest.getId(), contestContestantPage, page, sortBy, orderBy, filterString));

            content.appendLayout(c -> headingWithActionLayout.render(Messages.get("contest.contestants"), new InternalLink(Messages.get("contest.contestant.create"), routes.ContestController.createContestContestant(contestId)), c));
            appendViewTabsLayout(content, contest);
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewAnnouncement(contest.getId()))
            ), c));
            appendTemplateLayout(content);

            return lazyOk(content);
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    private Result showCreateContestContestant(Form<ContestContestantCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createSupervisorContestantView.render(contest.getId(), form));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.contestant.create"), c));
        appendViewTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewSupervisorContestant(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    @AddCSRFToken
    public Result createContestContestant(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contestant")) {
            Form<ContestContestantCreateForm> form = Form.form(ContestContestantCreateForm.class);

            return showCreateContestContestant(form, contest);
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postCreateContestContestant(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (checkIfPermitted(contest, "contestant")) {
            Form<ContestContestantCreateForm> form = Form.form(ContestContestantCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateContestContestant(form, contest);
            } else {
                ContestContestantCreateForm contestContestantCreateForm = form.get();
                if ((JophielUtils.verifyUserJid(contestContestantCreateForm.userJid)) && (!contestService.isContestContestantInContestByUserJid(contest.getJid(), contestContestantCreateForm.userJid))) {
                    userRoleService.upsertUserRoleFromJophielUserJid(contestContestantCreateForm.userJid);
                    contestService.createContestContestant(contest.getId(), contestContestantCreateForm.userJid, ContestContestantStatus.valueOf(contestContestantCreateForm.status));

                    return redirect(routes.ContestController.viewSupervisorContestant(contest.getId()));
                } else {
                    return showCreateContestContestant(form, contest);
                }
            }
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    private Result showUpdateContestContestant(Form<ContestContestantUpdateForm> form, Contest contest, ContestContestant contestContestant){
        LazyHtml content = new LazyHtml(updateSupervisorContestantView.render(contest.getId(), contestContestant.getId(), form));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.contestant.update"), c));
        appendViewTabsLayout(content, contest);
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewSupervisorContestant(contest.getId()))
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    @AddCSRFToken
    public Result updateContestContestant(long contestId, long contestContestantId) {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestService.findContestContestantByContestContestantId(contestContestantId);
        if ((checkIfPermitted(contest, "contestant")) && (contestContestant.getContestJid().equals(contest.getJid()))) {
            ContestContestantUpdateForm contestContestantUpsertForm = new ContestContestantUpdateForm(contestContestant);
            Form<ContestContestantUpdateForm> form = Form.form(ContestContestantUpdateForm.class).fill(contestContestantUpsertForm);

            return showUpdateContestContestant(form, contest, contestContestant);
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    @RequireCSRFCheck
    public Result postUpdateContestContestant(long contestId, long contestContestantId) {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestContestant = contestService.findContestContestantByContestContestantId(contestContestantId);
        if ((checkIfPermitted(contest, "contestant")) && (contestContestant.getContestJid().equals(contest.getJid()))) {
            Form<ContestContestantUpdateForm> form = Form.form(ContestContestantUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateContestContestant(form, contest, contestContestant);
            } else {
                ContestContestantUpdateForm contestContestantUpdateForm = form.get();
                contestService.updateContestContestant(contestContestant.getId(), ContestContestantStatus.valueOf(contestContestantUpdateForm.status));

                return redirect(routes.ContestController.viewSupervisorContestant(contest.getId()));
            }
        } else {
            return redirect(routes.ContestController.viewAnnouncement(contest.getId()));
        }
    }

    private boolean checkIfPermitted(Contest contest, String permission) {
        // TODO check ACL
        return true;
    }

    private void appendViewTabsLayout(LazyHtml content, Contest contest) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.view.tab.announcement"), routes.ContestController.viewAnnouncement(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.view.tab.problem"), routes.ContestController.viewProblem(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.view.tab.submission"), routes.ContestController.viewSubmission(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.view.tab.clarification"), routes.ContestController.viewClarification(contest.getId())));

        if (checkIfPermitted(contest, "contestant")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("contest.view.tab.contestant"), routes.ContestController.viewSupervisorContestant(contest.getId())));
        }

        if (checkIfPermitted(contest, "supervisor")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("contest.view.tab.supervisor"), routes.ContestController.viewManagerSupervisor(contest.getId())));
        }

        if (checkIfPermitted(contest, "manager")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("contest.view.tab.manager"), routes.ContestController.viewAdminManager(contest.getId())));
        }

        content.appendLayout(c -> tabLayout.render(internalLinkBuilder.build(), c));
        content.appendLayout(c -> headingLayout.render(Messages.get("contest.contest") + " #" + contest.getId() + ": " + contest.getName(), c));
    }

    private void appendTemplateLayout(LazyHtml content) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()));

        if (UrielUtils.hasRole("admin")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("userRole.userRoles"), routes.UserRoleController.index()));
        }

        content.appendLayout(c -> leftSidebarLayout.render(
            IdentityUtils.getUsername(),
            IdentityUtils.getUserRealName(),
            "#",
            org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.logout(routes.ApplicationController.index().absoluteURL(request())).absoluteURL(request()),
            internalLinkBuilder.build(), c)
        );
        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("TODO", c));
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
}
