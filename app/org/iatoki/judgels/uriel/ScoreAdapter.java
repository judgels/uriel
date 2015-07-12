package org.iatoki.judgels.uriel;

import org.iatoki.judgels.play.services.impls.AbstractBaseJidCacheServiceImpl;
import org.iatoki.judgels.sandalphon.Submission;
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

    Html renderScoreboard(Scoreboard scoreboard, Date lastUpdateTime, AbstractBaseJidCacheServiceImpl<?> jidCacheService, String currentContestantJid, boolean hiddenRank, Set<String> filterContestantJids);
}
