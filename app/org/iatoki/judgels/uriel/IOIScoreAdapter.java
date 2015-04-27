package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import java.util.stream.IntStream;

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
    public Scoreboard filterOpenProblems(Scoreboard scoreboard, Set<String> openProblemJids) {
        ContestScoreState state = scoreboard.getState();
        IOIScoreboardContent content = (IOIScoreboardContent) scoreboard.getContent();

        if (state.getProblemJids().size() == openProblemJids.size()) {
            return scoreboard;
        }

        ImmutableList.Builder<Integer> openProblemIndicesBuilder = ImmutableList.builder();

        for (int i = 0; i < state.getProblemJids().size(); i++) {
            if (openProblemJids.contains(state.getProblemJids().get(i))) {
                openProblemIndicesBuilder.add(i);
            }
        }

        List<Integer> openProblemIndices = openProblemIndicesBuilder.build();

        ContestScoreState newState = new ContestScoreState(
                filterIndices(state.getProblemJids(), openProblemIndices),
                filterIndices(state.getProblemAliases(), openProblemIndices),
                state.getContestantJids()
        );

        List<IOIScoreboardEntry> newEntries = Lists.newArrayList();

        for (IOIScoreboardEntry entry : content.getEntries()) {
            IOIScoreboardEntry newEntry = new IOIScoreboardEntry();
            newEntry.scores = filterIndices(entry.scores, openProblemIndices);
            newEntry.contestantJid = entry.contestantJid;
            newEntry.imageURL = entry.imageURL;
            newEntry.totalScores = newEntry.scores.stream().mapToInt(s -> s).sum();
            newEntries.add(newEntry);
        }

        Collections.sort(newEntries);

        int currentRank = 0;
        for (int i = 0; i < newEntries.size(); i++) {
            currentRank++;
            if (i == 0 || newEntries.get(i).totalScores != newEntries.get(i - 1).totalScores) {
                newEntries.get(i).rank = currentRank;
            } else {
                newEntries.get(i).rank = newEntries.get(i -1).rank;
            }
        }

        return new IOIScoreboard(newState, new IOIScoreboardContent(newEntries));
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

    private <T> List<T> filterIndices(List<T> list, List<Integer> indices) {
        return indices.stream().map(i -> list.get(i)).collect(Collectors.toList());
    }
}
