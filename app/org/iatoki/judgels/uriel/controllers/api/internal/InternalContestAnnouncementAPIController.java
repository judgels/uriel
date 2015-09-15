package org.iatoki.judgels.uriel.controllers.api.internal;

import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.apis.JudgelsAPIForbiddenException;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.services.ContestAnnouncementService;
import org.iatoki.judgels.uriel.services.ContestService;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class InternalContestAnnouncementAPIController extends AbstractJudgelsAPIController {

    private final ContestService contestService;
    private final ContestAnnouncementService contestAnnouncementService;

    @Inject
    public InternalContestAnnouncementAPIController(ContestService contestService, ContestAnnouncementService contestAnnouncementService) {
        this.contestService = contestService;
        this.contestAnnouncementService = contestAnnouncementService;
    }

    @Transactional(readOnly = true)
    public Result getUnreadAnnouncementsCount(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid())) {
            throw new JudgelsAPIForbiddenException("Not allowed to enter contest");
        }

        long unreadCount = contestAnnouncementService.countUnreadAnnouncementsInContest(IdentityUtils.getUserJid(), contest.getJid());
        return okAsJson(unreadCount);
    }
}
