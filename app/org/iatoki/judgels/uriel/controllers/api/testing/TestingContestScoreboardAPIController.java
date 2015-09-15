package org.iatoki.judgels.uriel.controllers.api.testing;

import com.google.gson.Gson;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public final class TestingContestScoreboardAPIController extends AbstractJudgelsAPIController {

    private final ContestScoreboardService contestScoreboardService;

    @Inject
    public TestingContestScoreboardAPIController(ContestScoreboardService contestScoreboardService) {
        this.contestScoreboardService = contestScoreboardService;
    }

    @Transactional(readOnly = true)
    public Result getScoreboard() {
        DynamicForm dForm = DynamicForm.form().bindFromRequest();

        String secret = dForm.get("secret");
        String contestJid = dForm.get("containerJid");
        String type = dForm.get("type");

        if (secret == null || contestJid == null || type == null) {
            return notFound();
        }

        if (!UrielProperties.getInstance().getUrielScoreboardSecret().equals(secret)) {
            return notFound();
        }

        ContestScoreboardType contestScoreboardType;
        try {
            contestScoreboardType = ContestScoreboardType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return notFound();
        }

        ContestScoreboard contestScoreboard;

        if (contestScoreboardService.scoreboardExistsInContestByType(contestJid, contestScoreboardType)) {
            contestScoreboard = contestScoreboardService.findScoreboardInContestByType(contestJid, contestScoreboardType);
        } else {
            // Resort to the official one.
            contestScoreboard = contestScoreboardService.findScoreboardInContestByType(contestJid, ContestScoreboardType.OFFICIAL);
        }

        return ok(new Gson().toJson(contestScoreboard));
    }
}
