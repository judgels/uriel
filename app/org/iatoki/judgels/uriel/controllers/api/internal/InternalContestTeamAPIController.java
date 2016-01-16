package org.iatoki.judgels.uriel.controllers.api.internal;

import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.contest.team.ContestTeamService;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class InternalContestTeamAPIController extends AbstractJudgelsAPIController {

    private final ContestTeamService contestTeamService;

    @Inject
    public InternalContestTeamAPIController(ContestTeamService contestTeamService) {
        this.contestTeamService = contestTeamService;
    }

    @Transactional(readOnly = true)
    public Result renderTeamAvatarImage(String imageName) {
        String avatarUrl = contestTeamService.getTeamAvatarImageURL(imageName);
        return okAsImage(avatarUrl);
    }
}
