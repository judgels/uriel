package org.iatoki.judgels.uriel.controllers;

import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestAnnouncement;
import org.iatoki.judgels.uriel.ContestAnnouncementNotFoundException;
import org.iatoki.judgels.uriel.ContestAnnouncementStatus;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.forms.ContestAnnouncementUpsertForm;
import org.iatoki.judgels.uriel.services.ContestAnnouncementService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.views.html.contest.announcement.createAnnouncementView;
import org.iatoki.judgels.uriel.views.html.contest.announcement.listAnnouncementsView;
import org.iatoki.judgels.uriel.views.html.contest.announcement.listPublishedAnnouncementsView;
import org.iatoki.judgels.uriel.views.html.contest.announcement.updateAnnouncementView;
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
public class ContestAnnouncementController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final ContestAnnouncementService contestAnnouncementService;
    private final ContestService contestService;

    @Inject
    public ContestAnnouncementController(ContestAnnouncementService contestAnnouncementService, ContestService contestService) {
        this.contestAnnouncementService = contestAnnouncementService;
        this.contestService = contestService;
    }

    @Transactional
    public Result viewPublishedAnnouncements(long contestId) throws ContestNotFoundException {
        return listPublishedAnnouncements(contestId, 0, "timeUpdate", "desc", "");
    }

    @Transactional
    public Result listPublishedAnnouncements(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid())) {
            return redirect(routes.ContestController.viewContest(contest.getId()));
        }

        Page<ContestAnnouncement> pageOfContestAnnouncements = contestAnnouncementService.getPageOfAnnouncementsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, ContestAnnouncementStatus.PUBLISHED.name());
        contestAnnouncementService.readContestAnnouncements(IdentityUtils.getUserJid(), pageOfContestAnnouncements.getData().stream().map(c -> c.getJid()).collect(Collectors.toList()), IdentityUtils.getIpAddress());

        LazyHtml content = new LazyHtml(listPublishedAnnouncementsView.render(contest.getId(), pageOfContestAnnouncements, pageIndex, orderBy, orderDir, filterString));
        content.appendLayout(c -> heading3Layout.render(Messages.get("announcement.list"), c));
        if (isAllowedToSuperviseAnnouncements(contest)) {
            appendSubtabsLayout(content, contest);
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("announcement.list"), routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Announcements");

        UrielControllerUtils.getInstance().addActivityLog("Open list of published announcements in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewAnnouncements(long contestId) throws ContestNotFoundException {
        return listAnnouncements(contestId, 0, "id", "desc", "");
    }

    @Transactional(readOnly = true)
    public Result listAnnouncements(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!isAllowedToSuperviseAnnouncements(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Page<ContestAnnouncement> pageOfContestAnnouncements = contestAnnouncementService.getPageOfAnnouncementsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, null);

        LazyHtml content = new LazyHtml(listAnnouncementsView.render(contest.getId(), pageOfContestAnnouncements, pageIndex, orderBy, orderDir, filterString));
        content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("announcement.list"), new InternalLink(Messages.get("commons.create"), routes.ContestAnnouncementController.createAnnouncement(contest.getId())), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("announcement.list"), routes.ContestAnnouncementController.viewAnnouncements(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - All Announcements");

        UrielControllerUtils.getInstance().addActivityLog("Open all announcements in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createAnnouncement(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !isAllowedToSuperviseAnnouncements(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestAnnouncementUpsertForm> contestAnnouncementUpsertForm = Form.form(ContestAnnouncementUpsertForm.class);

        UrielControllerUtils.getInstance().addActivityLog("Try to create announcement in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showCreateAnnouncement(contestAnnouncementUpsertForm, contest);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateAnnouncement(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !isAllowedToSuperviseAnnouncements(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestAnnouncementUpsertForm> contestAnnouncementUpsertForm = Form.form(ContestAnnouncementUpsertForm.class).bindFromRequest();

        if (formHasErrors(contestAnnouncementUpsertForm)) {
            return showCreateAnnouncement(contestAnnouncementUpsertForm, contest);
        }

        ContestAnnouncementUpsertForm contestAnnouncementUpsertData = contestAnnouncementUpsertForm.get();
        contestAnnouncementService.createContestAnnouncement(contest.getJid(), contestAnnouncementUpsertData.title, JudgelsPlayUtils.toSafeHtml(contestAnnouncementUpsertData.content), ContestAnnouncementStatus.valueOf(contestAnnouncementUpsertData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog("Create " + contestAnnouncementUpsertData.status + " announcement with title " + contestAnnouncementUpsertData.title + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestAnnouncementController.viewAnnouncements(contest.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateAnnouncement(long contestId, long contestAnnouncementId) throws ContestNotFoundException, ContestAnnouncementNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestAnnouncement contestAnnouncement = contestAnnouncementService.findContestAnnouncementById(contestAnnouncementId);
        if (contest.isLocked() || !isAllowedToSuperviseAnnouncements(contest) || !contestAnnouncement.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestAnnouncementUpsertForm contestAnnouncementUpsertData = new ContestAnnouncementUpsertForm();
        contestAnnouncementUpsertData.title = contestAnnouncement.getTitle();
        contestAnnouncementUpsertData.content = contestAnnouncement.getContent();
        contestAnnouncementUpsertData.status = contestAnnouncement.getStatus().name();
        Form<ContestAnnouncementUpsertForm> contestAnnouncementUpsertForm = Form.form(ContestAnnouncementUpsertForm.class).fill(contestAnnouncementUpsertData);

        UrielControllerUtils.getInstance().addActivityLog("Try to update announcement  " + contestAnnouncement.getTitle() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateAnnouncement(contestAnnouncementUpsertForm, contest, contestAnnouncement);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateAnnouncement(long contestId, long contestAnnouncementId) throws ContestNotFoundException, ContestAnnouncementNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestAnnouncement contestAnnouncement = contestAnnouncementService.findContestAnnouncementById(contestAnnouncementId);
        if (contest.isLocked() || !isAllowedToSuperviseAnnouncements(contest) || !contestAnnouncement.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestAnnouncementUpsertForm> contestAnnouncementUpsertForm = Form.form(ContestAnnouncementUpsertForm.class).bindFromRequest();

        if (formHasErrors(contestAnnouncementUpsertForm)) {
            return showUpdateAnnouncement(contestAnnouncementUpsertForm, contest, contestAnnouncement);
        }

        ContestAnnouncementUpsertForm contestAnnouncementUpsertData = contestAnnouncementUpsertForm.get();
        contestAnnouncementService.updateContestAnnouncement(contestAnnouncement.getJid(), contestAnnouncementUpsertData.title, JudgelsPlayUtils.toSafeHtml(contestAnnouncementUpsertData.content), ContestAnnouncementStatus.valueOf(contestAnnouncementUpsertData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog("Update announcement  " + contestAnnouncement.getTitle() + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestAnnouncementController.viewAnnouncements(contest.getId()));
    }

    private Result showCreateAnnouncement(Form<ContestAnnouncementUpsertForm> contestAnnouncementUpsertForm, Contest contest) {
        LazyHtml content = new LazyHtml(createAnnouncementView.render(contest.getId(), contestAnnouncementUpsertForm));
        content.appendLayout(c -> heading3Layout.render(Messages.get("announcement.create"), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestAnnouncementController.viewAnnouncements(contest.getId())),
                new InternalLink(Messages.get("announcement.create"), routes.ContestAnnouncementController.createAnnouncement(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Announcement - Create");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateAnnouncement(Form<ContestAnnouncementUpsertForm> contestAnnouncementUpsertForm, Contest contest, ContestAnnouncement contestAnnouncement) {
        LazyHtml content = new LazyHtml(updateAnnouncementView.render(contest.getId(), contestAnnouncement.getId(), contestAnnouncementUpsertForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("announcement.update"), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId())),
                new InternalLink(Messages.get("announcement.update"), routes.ContestAnnouncementController.updateAnnouncement(contest.getId(), contestAnnouncement.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Announcement - Update");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendSubtabsLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()), routes.ContestAnnouncementController.viewAnnouncements(contest.getId()), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.jumpToAnnouncements(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseAnnouncements(Contest contest) {
        return ContestControllerUtils.getInstance().isPermittedToSupervise(contest, ContestPermissions.ANNOUNCEMENT, IdentityUtils.getUserJid());
    }
}
