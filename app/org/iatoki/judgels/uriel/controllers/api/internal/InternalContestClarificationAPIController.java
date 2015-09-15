package org.iatoki.judgels.uriel.controllers.api.internal;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.apis.JudgelsAPIForbiddenException;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.services.ContestClarificationService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.ContestTeamService;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named
@Authenticated(value = {LoggedIn.class, HasRole.class})

public final class InternalContestClarificationAPIController extends AbstractJudgelsAPIController {

    private final ContestService contestService;
    private final ContestTeamService contestTeamService;
    private final ContestClarificationService contestClarificationService;

    @Inject
    public InternalContestClarificationAPIController(ContestService contestService, ContestTeamService contestTeamService, ContestClarificationService contestClarificationService) {
        this.contestService = contestService;
        this.contestTeamService = contestTeamService;
        this.contestClarificationService = contestClarificationService;
    }

    @Transactional(readOnly = true)
    public Result getUnreadClarificationsCount(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid())) {
            throw new JudgelsAPIForbiddenException("Not allowed to enter contest");
        }

        long unreadCount;
        if (ContestControllerUtils.getInstance().isCoach(contest, IdentityUtils.getUserJid())) {
            List<ContestTeamMember> contestTeamMemberList = contestTeamService.getCoachedMembersInContest(contest.getJid(), IdentityUtils.getUserJid());
            unreadCount = contestClarificationService.countUnreadClarificationsInContest(contestTeamMemberList.stream().map(ContestTeamMember::getMemberJid).collect(Collectors.toList()), IdentityUtils.getUserJid(), contest.getJid(), false);
        } else {
            unreadCount = contestClarificationService.countUnreadClarificationsInContest(ImmutableList.of(IdentityUtils.getUserJid()), IdentityUtils.getUserJid(), contest.getJid(), true);
        }
        return okAsJson(unreadCount);
    }

    @Transactional(readOnly = true)
    public Result getUnansweredClarificationsCount(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isPermittedToSupervise(contest, ContestPermissions.CLARIFICATION, IdentityUtils.getUserJid())) {
            throw new JudgelsAPIForbiddenException("Not permitted to supervise clarification");
        }

        long unreadCount = contestClarificationService.countUnansweredClarificationsInContest(contest.getJid());
        return okAsJson(unreadCount);
    }
}
