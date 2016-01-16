package org.iatoki.judgels.uriel.contest.scoreboard;

import org.iatoki.judgels.play.jid.AbstractBaseJidCacheServiceImpl;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.uriel.contest.Contest;
import play.twirl.api.Html;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ScoreboardAdapter {

    ScoreboardContent computeScoreboardContent(Contest contest, ScoreboardState state, List<ProgrammingSubmission> submissions, Map<String, Date> contestantStartTimesMap, Map<String, URL> userJidToImageMap);

    Scoreboard parseScoreboardFromJson(String json);

    Scoreboard createScoreboard(ScoreboardState state, ScoreboardContent content);

    Scoreboard filterOpenProblems(Contest contest, Scoreboard scoreboard, Set<String> openProblemJids);

    Html renderScoreboard(Scoreboard scoreboard, Date lastUpdateTime, AbstractBaseJidCacheServiceImpl<?> jidCacheService, String currentContestantJid, boolean hiddenRank, Set<String> filterContestantJids);
}
