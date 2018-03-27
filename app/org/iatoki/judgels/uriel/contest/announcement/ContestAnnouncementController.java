package org.iatoki.judgels.uriel.contest.announcement;

import org.iatoki.judgels.jophiel.activity.BasicActivityKeys;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.contest.ContestNotFoundException;
import org.iatoki.judgels.uriel.contest.supervisor.ContestPermissions;
import org.iatoki.judgels.uriel.UrielControllerUtils;
import org.iatoki.judgels.uriel.contest.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.contest.ContestService;
import org.iatoki.judgels.uriel.contest.announcement.html.createAnnouncementView;
import org.iatoki.judgels.uriel.contest.announcement.html.listAnnouncementsView;
import org.iatoki.judgels.uriel.contest.announcement.html.listPublishedAnnouncementsView;
import org.iatoki.judgels.uriel.contest.announcement.html.editAnnouncementView;
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
import java.util.stream.Collectors;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
public class ContestAnnouncementController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String ANNOUNCEMENT = "announcement";
    private static final String CONTEST = "contest";

    private final ContestAnnouncementService contestAnnouncementService;
    private final ContestService contestService;

    @Inject
    public ContestAnnouncementController(ContestAnnouncementService contestAnnouncementService, ContestService contestService) {
        this.contestAnnouncementService = contestAnnouncementService;
        this.contestService = contestService;
    }

    @Transactional
    public Result viewPublishedAnnouncements(long contestId) throws ContestNotFoundException {
        return listPublishedAnnouncements(contestId, 0, "updatedAt", "desc", "");
    }

    @Transactional
    public Result listPublishedAnnouncements(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid())) {
            return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.viewContest(contest.getId()));
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
        ContestAnnouncement contestAnnouncement = contestAnnouncementService.createContestAnnouncement(contest.getJid(), contestAnnouncementUpsertData.title, JudgelsPlayUtils.toSafeHtml(contestAnnouncementUpsertData.content), ContestAnnouncementStatus.valueOf(contestAnnouncementUpsertData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.ADD_IN.construct(CONTEST, contest.getJid(), contest.getName(), ANNOUNCEMENT, contestAnnouncement.getJid(), contestAnnouncement.getTitle()));

        return redirect(routes.ContestAnnouncementController.viewAnnouncements(contest.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editAnnouncement(long contestId, long contestAnnouncementId) throws ContestNotFoundException, ContestAnnouncementNotFoundException {
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

        return showEditAnnouncement(contestAnnouncementUpsertForm, contest, contestAnnouncement);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditAnnouncement(long contestId, long contestAnnouncementId) throws ContestNotFoundException, ContestAnnouncementNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestAnnouncement contestAnnouncement = contestAnnouncementService.findContestAnnouncementById(contestAnnouncementId);
        if (contest.isLocked() || !isAllowedToSuperviseAnnouncements(contest) || !contestAnnouncement.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestAnnouncementUpsertForm> contestAnnouncementUpsertForm = Form.form(ContestAnnouncementUpsertForm.class).bindFromRequest();

        if (formHasErrors(contestAnnouncementUpsertForm)) {
            return showEditAnnouncement(contestAnnouncementUpsertForm, contest, contestAnnouncement);
        }

        ContestAnnouncementUpsertForm contestAnnouncementUpsertData = contestAnnouncementUpsertForm.get();
        contestAnnouncementService.updateContestAnnouncement(contestAnnouncement.getJid(), contestAnnouncementUpsertData.title, JudgelsPlayUtils.toSafeHtml(contestAnnouncementUpsertData.content), ContestAnnouncementStatus.valueOf(contestAnnouncementUpsertData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        if (!contestAnnouncement.getTitle().equals(contestAnnouncementUpsertData.title)) {
            UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.RENAME_IN.construct(CONTEST, contest.getJid(), contest.getName(), ANNOUNCEMENT, contestAnnouncement.getJid(), contestAnnouncement.getTitle(), contestAnnouncementUpsertData.title));
        }
        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.EDIT_IN.construct(CONTEST, contest.getJid(), contest.getName(), ANNOUNCEMENT, contestAnnouncement.getJid(), contestAnnouncementUpsertData.title));

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

    private Result showEditAnnouncement(Form<ContestAnnouncementUpsertForm> contestAnnouncementUpsertForm, Contest contest, ContestAnnouncement contestAnnouncement) {
        LazyHtml content = new LazyHtml(editAnnouncementView.render(contest.getId(), contestAnnouncement.getId(), contestAnnouncementUpsertForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("announcement.update"), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId())),
                new InternalLink(Messages.get("announcement.update"), routes.ContestAnnouncementController.editAnnouncement(contest.getId(), contestAnnouncement.getId()))
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
                        .add(new InternalLink(Messages.get("announcement.announcements"), org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToAnnouncements(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseAnnouncements(Contest contest) {
        return ContestControllerUtils.getInstance().isPermittedToSupervise(contest, ContestPermissions.ANNOUNCEMENT, IdentityUtils.getUserJid());
    }
}
