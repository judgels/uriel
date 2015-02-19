package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.uriel.commons.IOIScoreboard;
import org.iatoki.judgels.uriel.commons.IOIScoreboardContent;
import org.iatoki.judgels.uriel.commons.ContestConfig;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;
import org.iatoki.judgels.uriel.commons.views.html.ioiScoreboardView;
import play.twirl.api.Html;

import java.util.List;

public final class IOIScoreboardAdapter implements ScoreboardAdapter {
    @Override
    public ScoreboardContent computeContent(ContestConfig config, List<Submission> submissions) {
        return new IOIScoreboardContent(ImmutableList.of());
    }

    @Override
    public Scoreboard parseScoreboardFromJson(String json) {
        return new Gson().fromJson(json, IOIScoreboard.class);
    }

    @Override
    public Scoreboard createScoreboard(ContestConfig config, ScoreboardContent content) {
        return new IOIScoreboard(config, (IOIScoreboardContent) content);
    }

    @Override
    public Html renderScoreboard(Scoreboard scoreboard) {
        IOIScoreboard castScoreboard = (IOIScoreboard) scoreboard;
        return ioiScoreboardView.render(castScoreboard.getConfig(), castScoreboard.getContent().getEntries());
    }
}
