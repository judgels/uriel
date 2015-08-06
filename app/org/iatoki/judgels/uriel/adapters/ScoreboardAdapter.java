package org.iatoki.judgels.uriel.adapters;

import org.iatoki.judgels.play.services.impls.AbstractBaseJidCacheServiceImpl;
import org.iatoki.judgels.sandalphon.Submission;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.Scoreboard;
import org.iatoki.judgels.uriel.ScoreboardContent;
import org.iatoki.judgels.uriel.ScoreboardState;
import org.iatoki.judgels.uriel.modules.ContestModule;
import play.twirl.api.Html;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ScoreboardAdapter {

    ScoreboardContent computeScoreboardContent(Contest contest, List<ContestModule> contestModules, String styleConfig, ScoreboardState state, List<Submission> submissions, Map<String, URL> userJidToImageMap);

    Scoreboard parseScoreboardFromJson(String json);

    Scoreboard createScoreboard(ScoreboardState state, ScoreboardContent content);

    Scoreboard filterOpenProblems(Scoreboard scoreboard, Set<String> openProblemJids);

    Html renderScoreboard(Scoreboard scoreboard, Date lastUpdateTime, AbstractBaseJidCacheServiceImpl<?> jidCacheService, String currentContestantJid, boolean hiddenRank, Set<String> filterContestantJids);
}
