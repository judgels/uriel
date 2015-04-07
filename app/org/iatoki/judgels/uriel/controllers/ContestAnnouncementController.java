package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.commons.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestAnnouncement;
import org.iatoki.judgels.uriel.ContestAnnouncementStatus;
import org.iatoki.judgels.uriel.ContestAnnouncementUpsertForm;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.announcement.createAnnouncementView;
import org.iatoki.judgels.uriel.views.html.contest.announcement.listAnnouncementsView;
import org.iatoki.judgels.uriel.views.html.contest.announcement.updateAnnouncementView;
import org.iatoki.judgels.uriel.views.html.contest.announcement.listPublishedAnnouncementsView;
import org.iatoki.judgels.uriel.views.html.layouts.accessTypeByStatusLayout;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Date;
import java.util.stream.Collectors;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public class ContestAnnouncementController extends Controller {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;

    public ContestAnnouncementController(ContestService contestService) {
        this.contestService = contestService;
    }

    public Result viewPublishedAnnouncements(long contestId) {
        return listPublishedAnnouncements(contestId, 0, "timeUpdate", "desc", "");
    }

    public Result listPublishedAnnouncements(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            Page<ContestAnnouncement> contestAnnouncements = contestService.pageContestAnnouncementsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, ContestAnnouncementStatus.PUBLISHED.name());
            contestService.readContestAnnouncements(IdentityUtils.getUserJid(), contestAnnouncements.getData().stream().map(c -> c.getId()).collect(Collectors.toList()));

            LazyHtml content = new LazyHtml(listPublishedAnnouncementsView.render(contest.getId(), contestAnnouncements, pageIndex, orderBy, orderDir, filterString));
            content.appendLayout(c -> heading3Layout.render(Messages.get("announcement.announcements"), c));
            if (isAllowedToSuperviseAnnouncements(contest)) {
                content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()), routes.ContestAnnouncementController.viewAnnouncements(contest.getId()), c));
            }
            appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                  new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                  new InternalLink(Messages.get("announcement.announcements"), routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Announcements");

            ControllerUtils.getInstance().addActivityLog("Open list of published announcements in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return redirect(routes.ContestController.viewContest(contest.getId()));
        }
    }

    public Result viewAnnouncements(long contestId) {
        return listAnnouncements(contestId, 0, "id", "desc", "");
    }

    public Result listAnnouncements(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseAnnouncements(contest)) {
            Page<ContestAnnouncement> contestAnnouncements = contestService.pageContestAnnouncementsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, null);

            LazyHtml content = new LazyHtml(listAnnouncementsView.render(contest.getId(), contestAnnouncements, pageIndex, orderBy, orderDir, filterString));
            content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("announcement.list"), new InternalLink(Messages.get("commons.create"), routes.ContestAnnouncementController.createAnnouncement(contest.getId())), c));
            content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()), routes.ContestAnnouncementController.viewAnnouncements(contest.getId()), c));
            appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                  new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                  new InternalLink(Messages.get("announcement.announcements"), routes.ContestAnnouncementController.viewAnnouncements(contest.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - All Announcements");

            ControllerUtils.getInstance().addActivityLog("Open all announcements in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result createAnnouncement(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseAnnouncements(contest)) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class);

            ControllerUtils.getInstance().addActivityLog("Try to create announcement in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showCreateAnnouncement(form, contest);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateAnnouncement(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseAnnouncements(contest)) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateAnnouncement(form, contest);
            } else {
                ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = form.get();
                contestService.createContestAnnouncement(contest.getId(), contestAnnouncementUpsertForm.title, contestAnnouncementUpsertForm.content, ContestAnnouncementStatus.valueOf(contestAnnouncementUpsertForm.status));

                ControllerUtils.getInstance().addActivityLog("Create " + contestAnnouncementUpsertForm.status + " announcement with title " + contestAnnouncementUpsertForm.title + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestAnnouncementController.viewAnnouncements(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateAnnouncement(long contestId, long contestAnnouncementId) {
        Contest contest = contestService.findContestById(contestId);
        ContestAnnouncement contestAnnouncement = contestService.findContestAnnouncementByContestAnnouncementId(contestAnnouncementId);
        if (isAllowedToSuperviseAnnouncements(contest) && contestAnnouncement.getContestJid().equals(contest.getJid())) {
            ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = new ContestAnnouncementUpsertForm(contestAnnouncement);
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).fill(contestAnnouncementUpsertForm);

            ControllerUtils.getInstance().addActivityLog("Try to update announcement  " + contestAnnouncement.getTitle() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateAnnouncement(form, contest, contestAnnouncement);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateAnnouncement(long contestId, long contestAnnouncementId) {
        Contest contest = contestService.findContestById(contestId);
        ContestAnnouncement contestAnnouncement = contestService.findContestAnnouncementByContestAnnouncementId(contestAnnouncementId);
        if (isAllowedToSuperviseAnnouncements(contest) && contestAnnouncement.getContestJid().equals(contest.getJid())) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateAnnouncement(form, contest, contestAnnouncement);
            } else {
                ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = form.get();
                contestService.updateContestAnnouncement(contestAnnouncement.getId(), contestAnnouncementUpsertForm.title, contestAnnouncementUpsertForm.content, ContestAnnouncementStatus.valueOf(contestAnnouncementUpsertForm.status));

                ControllerUtils.getInstance().addActivityLog("Update announcement  " + contestAnnouncement.getTitle() + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestAnnouncementController.viewAnnouncements(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    private Result showCreateAnnouncement(Form<ContestAnnouncementUpsertForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createAnnouncementView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("announcement.create"), c));
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()), routes.ContestAnnouncementController.viewAnnouncements(contest.getId()), c));
        appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                new InternalLink(Messages.get("announcement.announcements"), routes.ContestAnnouncementController.viewAnnouncements(contest.getId())),
                new InternalLink(Messages.get("announcement.create"), routes.ContestAnnouncementController.createAnnouncement(contest.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Announcement - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateAnnouncement(Form<ContestAnnouncementUpsertForm> form, Contest contest, ContestAnnouncement contestAnnouncement){
        LazyHtml content = new LazyHtml(updateAnnouncementView.render(contest.getId(), contestAnnouncement.getId(), form));
        content.appendLayout(c -> headingLayout.render(Messages.get("announcement.update"), c));
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()), routes.ContestAnnouncementController.viewAnnouncements(contest.getId()), c));
        appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                new InternalLink(Messages.get("announcement.announcements"), routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId())),
                new InternalLink(Messages.get("announcement.update"), routes.ContestAnnouncementController.viewAnnouncements(contest.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Announcement - Update");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendTabsLayout(LazyHtml content, Contest contest) {
        Date contestEndTime = contest.getEndTime();
        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
            contestEndTime = new Date(contestContestant.getContestEnterTime() + contestTypeConfigVirtual.getContestDuration());
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, isSupervisorOrAbove(contest), contestEndTime);
    }

    private boolean isManager(Contest contest) {
        return contestService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isSupervisor(Contest contest) {
        return contestService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isCoach(Contest contest) {
        return contestService.isUserCoachInAnyTeamByContestJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isContestant(Contest contest) {
        return contestService.isContestContestantInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isContestStarted(Contest contest) {
        return (!new Date().before(contest.getStartTime()));
    }

    private boolean isContestEnded(Contest contest) {
        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);

            return (System.currentTimeMillis() > (contestContestant.getContestEnterTime() + contestTypeConfigVirtual.getContestDuration()));
        } else {
            return new Date().after(contest.getEndTime());
        }
    }

    private boolean isAllowedToEnterContest(Contest contest) {
        if (ControllerUtils.getInstance().isAdmin() || isManager(contest) || isSupervisor(contest)) {
            return true;
        }
        if (contest.isStandard()) {
            return ((isContestant(contest) && isContestStarted(contest)) || (isCoach(contest)));
        } else {
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
            if (contestTypeConfigVirtual.getStartTrigger().equals(ContestTypeConfigVirtualStartTrigger.CONTESTANT)) {
                return (isContestant(contest) && (isContestStarted(contest)));
            } else {
                return ((isContestStarted(contest)) && (isCoach(contest) || (isContestant(contest) && (contestService.isContestEntered(contest.getJid(), IdentityUtils.getUserJid())))));
            }
        }
    }

    private boolean isSupervisorOrAbove(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest) || (isSupervisor(contest));
    }

    private boolean isAllowedToSuperviseAnnouncements(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest) || (isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isAnnouncement());
    }

    private Result tryEnteringContest(Contest contest) {
        if (isAllowedToEnterContest(contest)) {
            return redirect(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()));
        } else {
            return redirect(routes.ContestController.viewContest(contest.getId()));
        }
    }
}
