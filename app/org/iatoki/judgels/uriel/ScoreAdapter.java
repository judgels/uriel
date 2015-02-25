package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.AbstractJidCacheService;
import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.uriel.commons.ContestScoreState;
import org.iatoki.judgels.uriel.commons.ScoreEntry;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;
import play.twirl.api.Html;

import java.util.Date;
import java.util.List;

public interface ScoreAdapter {
    ScoreboardContent computeScoreboardContent(ContestScoreState state, List<ContestScore> contestScores);

    Scoreboard parseScoreboardFromJson(String json);

    Scoreboard createScoreboard(ContestScoreState state, ScoreboardContent content);

    ScoreEntry createEmptyScoreEntry(List<String> problemJids);

    ScoreEntry parseScoreFromJson(String json);

    ScoreEntry updateScoreEntry(ScoreEntry scoreEntry, List<String> problemJids, List<Submission> submissions);

    Html renderScoreboard(Scoreboard scoreboard, Date lastUpdateTime, AbstractJidCacheService<?> jidCacheService, String currentContestantJid);
}
