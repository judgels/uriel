package org.iatoki.judgels.uriel.controllers;

import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.commons.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestAnnouncement;
import org.iatoki.judgels.uriel.ContestAnnouncementNotFoundException;
import org.iatoki.judgels.uriel.ContestAnnouncementStatus;
import org.iatoki.judgels.uriel.controllers.forms.ContestAnnouncementUpsertForm;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
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
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.stream.Collectors;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public class ContestAnnouncementController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;

    @Inject
    public ContestAnnouncementController(ContestService contestService) {
        this.contestService = contestService;
    }

    @Transactional(readOnly = true)
    public Result viewPublishedAnnouncements(long contestId) throws ContestNotFoundException {
        return listPublishedAnnouncements(contestId, 0, "timeUpdate", "desc", "");
    }

    @Transactional(readOnly = true)
    public Result listPublishedAnnouncements(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isAllowedToEnterContest(contest)) {
            Page<ContestAnnouncement> contestAnnouncements = contestService.pageContestAnnouncementsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, ContestAnnouncementStatus.PUBLISHED.name());
            contestService.readContestAnnouncements(IdentityUtils.getUserJid(), contestAnnouncements.getData().stream().map(c -> c.getId()).collect(Collectors.toList()));

            LazyHtml content = new LazyHtml(listPublishedAnnouncementsView.render(contest.getId(), contestAnnouncements, pageIndex, orderBy, orderDir, filterString));
            content.appendLayout(c -> heading3Layout.render(Messages.get("announcement.list"), c));
            if (isAllowedToSuperviseAnnouncements(contest)) {
                appendSubtabsLayout(content, contest);
            }
            ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, contest,
                    new InternalLink(Messages.get("announcement.list"), routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()))
            );
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Announcements");

            ControllerUtils.getInstance().addActivityLog("Open list of published announcements in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return redirect(routes.ContestController.viewContest(contest.getId()));
        }
    }

    @Transactional(readOnly = true)
    public Result viewAnnouncements(long contestId) throws ContestNotFoundException {
        return listAnnouncements(contestId, 0, "id", "desc", "");
    }

    @Transactional(readOnly = true)
    public Result listAnnouncements(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseAnnouncements(contest)) {
            Page<ContestAnnouncement> contestAnnouncements = contestService.pageContestAnnouncementsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, null);

            LazyHtml content = new LazyHtml(listAnnouncementsView.render(contest.getId(), contestAnnouncements, pageIndex, orderBy, orderDir, filterString));
            content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("announcement.list"), new InternalLink(Messages.get("commons.create"), routes.ContestAnnouncementController.createAnnouncement(contest.getId())), c));
            appendSubtabsLayout(content, contest);
            ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, contest,
                    new InternalLink(Messages.get("announcement.list"), routes.ContestAnnouncementController.viewAnnouncements(contest.getId()))
            );
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - All Announcements");

            ControllerUtils.getInstance().addActivityLog("Open all announcements in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createAnnouncement(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseAnnouncements(contest)) {
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class);

            ControllerUtils.getInstance().addActivityLog("Try to create announcement in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showCreateAnnouncement(form, contest);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateAnnouncement(long contestId) throws ContestNotFoundException {
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
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateAnnouncement(long contestId, long contestAnnouncementId) throws ContestNotFoundException, ContestAnnouncementNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestAnnouncement contestAnnouncement = contestService.findContestAnnouncementByContestAnnouncementId(contestAnnouncementId);
        if (isAllowedToSuperviseAnnouncements(contest) && contestAnnouncement.getContestJid().equals(contest.getJid())) {
            ContestAnnouncementUpsertForm contestAnnouncementUpsertForm = new ContestAnnouncementUpsertForm(contestAnnouncement);
            Form<ContestAnnouncementUpsertForm> form = Form.form(ContestAnnouncementUpsertForm.class).fill(contestAnnouncementUpsertForm);

            ControllerUtils.getInstance().addActivityLog("Try to update announcement  " + contestAnnouncement.getTitle() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateAnnouncement(form, contest, contestAnnouncement);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateAnnouncement(long contestId, long contestAnnouncementId) throws ContestNotFoundException, ContestAnnouncementNotFoundException {
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
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    private Result showCreateAnnouncement(Form<ContestAnnouncementUpsertForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createAnnouncementView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("announcement.create"), c));
        appendSubtabsLayout(content, contest);ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestAnnouncementController.viewAnnouncements(contest.getId())),
                new InternalLink(Messages.get("announcement.create"), routes.ContestAnnouncementController.createAnnouncement(contest.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Announcement - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateAnnouncement(Form<ContestAnnouncementUpsertForm> form, Contest contest, ContestAnnouncement contestAnnouncement){
        LazyHtml content = new LazyHtml(updateAnnouncementView.render(contest.getId(), contestAnnouncement.getId(), form));
        content.appendLayout(c -> headingLayout.render(Messages.get("announcement.update"), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId())),
                new InternalLink(Messages.get("announcement.update"), routes.ContestAnnouncementController.updateAnnouncement(contest.getId(), contestAnnouncement.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Announcement - Update");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendSubtabsLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()), routes.ContestAnnouncementController.viewAnnouncements(contest.getId()), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.jumpToAnnouncements(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseAnnouncements(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest) || (ContestControllerUtils.getInstance().isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isAnnouncement());
    }
}
