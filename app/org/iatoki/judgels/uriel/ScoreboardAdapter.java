package org.iatoki.judgels.uriel;

import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.uriel.commons.ContestConfig;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;
import play.twirl.api.Html;

import java.util.List;

public interface ScoreboardAdapter {
    ScoreboardContent computeContent(ContestConfig config, List<Submission> submissions);

    Scoreboard parseScoreboardFromJson(String json);

    Scoreboard createScoreboard(ContestConfig config, ScoreboardContent content);

    Html renderScoreboard(Scoreboard scoreboard);
}
