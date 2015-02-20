package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.AbstractJidCacheService;
import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.uriel.commons.ContestConfig;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;
import play.twirl.api.Html;

import java.util.Date;
import java.util.List;

public interface ScoreboardAdapter {
    ScoreboardContent computeContent(ContestConfig config, List<Submission> submissions);

    Scoreboard parseScoreboardFromJson(String json);

    Scoreboard createScoreboard(ContestConfig config, ScoreboardContent content);

    Html renderScoreboard(Scoreboard scoreboard, Date lastUpdateTime, AbstractJidCacheService<?> jidCacheService, String currentContestantJid);
}
