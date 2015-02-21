package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.AbstractJidCacheService;
import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.uriel.commons.IOIScoreboard;
import org.iatoki.judgels.uriel.commons.IOIScoreboardContent;
import org.iatoki.judgels.uriel.commons.ContestConfig;
import org.iatoki.judgels.uriel.commons.IOIScoreboardEntry;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;
import org.iatoki.judgels.uriel.commons.views.html.ioiScoreboardView;
import play.twirl.api.Html;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class IOIScoreboardAdapter implements ScoreboardAdapter {
    @Override
    public ScoreboardContent computeContent(ContestConfig config, List<Submission> submissions) {

        Map<String, Map<String, Integer>> scores = Maps.newHashMap();

        for (String contestantJid : config.getContestantJids()) {
            Map<String, Integer> problemScores = Maps.newHashMap();
            for (String problemJid : config.getProblemJids()) {
                problemScores.put(problemJid, 0);
            }
            scores.put(contestantJid, problemScores);
        }

        for (Submission submission : submissions) {
            String contestantJid = submission.getAuthorJid();

            if (!scores.containsKey(contestantJid)) {
                continue;
            }

            String problemJid = submission.getProblemJid();
            int score = submission.getLatestScore();

            int newScore = Math.max(scores.get(contestantJid).get(problemJid), score);
            scores.get(contestantJid).put(problemJid, newScore);
        }

        List<IOIScoreboardEntry> entries = Lists.newArrayList();

        for (String contestantJid : config.getContestantJids()) {
            IOIScoreboardEntry entry = new IOIScoreboardEntry();

            entry.contestantJid = contestantJid;
            entry.scores = Lists.newArrayList();

            int totalScores = 0;
            for (String problemJid : config.getProblemJids()) {
                int score = scores.get(contestantJid).get(problemJid);
                entry.scores.add(score);
                totalScores += score;
            }

            entry.totalScores = totalScores;

            entries.add(entry);
        }

        Collections.sort(entries);

        int currentRank = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (i == 0 || entries.get(i).totalScores != entries.get(i - 1).totalScores) {
                currentRank++;
            }

            entries.get(i).rank = currentRank;
        }

        return new IOIScoreboardContent(entries);
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
    public Html renderScoreboard(Scoreboard scoreboard, Date lastUpdateTime, AbstractJidCacheService<?> jidCacheService, String currentContestantJid) {
        IOIScoreboard castScoreboard = (IOIScoreboard) scoreboard;
        return ioiScoreboardView.render(castScoreboard.getConfig(), castScoreboard.getContent().getEntries(), lastUpdateTime, jidCacheService, currentContestantJid);
    }
}
