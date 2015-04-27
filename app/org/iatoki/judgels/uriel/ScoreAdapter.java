package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.AbstractJidCacheService;
import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.uriel.commons.ContestScoreState;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;
import play.twirl.api.Html;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ScoreAdapter {
    ScoreboardContent computeScoreboardContent(ContestScoreState state, List<Submission> submissions, Map<String, URL> userJidToImageMap);

    Scoreboard parseScoreboardFromJson(String json);

    Scoreboard createScoreboard(ContestScoreState state, ScoreboardContent content);

    Scoreboard filterOpenProblems(Scoreboard scoreboard, Set<String> openProblemJids);

    Html renderScoreboard(Scoreboard scoreboard, Date lastUpdateTime, AbstractJidCacheService<?> jidCacheService, String currentContestantJid, boolean hiddenRank, Set<String> filterContestantJids);
}
