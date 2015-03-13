package org.iatoki.judgels.uriel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.AbstractJidCacheService;
import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.uriel.commons.ContestScoreState;
import org.iatoki.judgels.uriel.commons.IOIScoreboard;
import org.iatoki.judgels.uriel.commons.IOIScoreboardContent;
import org.iatoki.judgels.uriel.commons.IOIScoreboardEntry;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;
import org.iatoki.judgels.uriel.commons.views.html.ioiScoreboardView;
import play.i18n.Messages;
import play.twirl.api.Html;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class IOIScoreAdapter implements ScoreAdapter {
    @Override
    public ScoreboardContent computeScoreboardContent(ContestScoreState state, List<Submission> submissions, Map<String, URL> userJidToImageMap) {

        Map<String, Map<String, Integer>> scores = Maps.newHashMap();

        for (String contestantJid : state.getContestantJids()) {
            Map<String, Integer> problemScores = Maps.newHashMap();
            for (String problemJid : state.getProblemJids()) {
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

        for (String contestantJid : state.getContestantJids()) {
            IOIScoreboardEntry entry = new IOIScoreboardEntry();
            entry.contestantJid = contestantJid;
            entry.imageURL = userJidToImageMap.get(contestantJid);

            int totalScores = 0;
            for (String problemJid : state.getProblemJids()) {
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
            currentRank++;
            if (i == 0 || entries.get(i).totalScores != entries.get(i - 1).totalScores) {
                entries.get(i).rank = currentRank;
            } else {
                entries.get(i).rank = entries.get(i -1).rank;
            }
        }

        return new IOIScoreboardContent(entries);
    }

    @Override
    public Scoreboard parseScoreboardFromJson(String json) {
        return new Gson().fromJson(json, IOIScoreboard.class);
    }

    @Override
    public Scoreboard createScoreboard(ContestScoreState state, ScoreboardContent content) {
        return new IOIScoreboard(state, (IOIScoreboardContent) content);
    }

    @Override
    public Html renderScoreboard(Scoreboard scoreboard, Date lastUpdateTime, AbstractJidCacheService<?> jidCacheService, String currentContestantJid, boolean hiddenRank, Set<String> filterContestantJids) {
        if (scoreboard == null) {
            return new Html(Messages.get("scoreboard.not_available"));
        } else {
            IOIScoreboard castScoreboard = (IOIScoreboard) scoreboard;
            return ioiScoreboardView.render(castScoreboard.getState(), castScoreboard.getContent().getEntries().stream().filter(e -> filterContestantJids.contains(e.contestantJid)).collect(Collectors.toList()), lastUpdateTime, jidCacheService, currentContestantJid, hiddenRank);
        }
    }
}
