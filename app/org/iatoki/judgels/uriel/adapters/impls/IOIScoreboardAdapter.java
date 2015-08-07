package org.iatoki.judgels.uriel.adapters.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.iatoki.judgels.play.services.impls.AbstractBaseJidCacheServiceImpl;
import org.iatoki.judgels.sandalphon.Submission;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ScoreboardState;
import org.iatoki.judgels.uriel.IOIScoreboard;
import org.iatoki.judgels.uriel.IOIScoreboardContent;
import org.iatoki.judgels.uriel.IOIScoreboardEntry;
import org.iatoki.judgels.uriel.Scoreboard;
import org.iatoki.judgels.uriel.ScoreboardContent;
import org.iatoki.judgels.uriel.adapters.ScoreboardAdapter;
import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.views.html.contest.scoreboard.ioiScoreboardView;
import play.i18n.Messages;
import play.twirl.api.Html;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class IOIScoreboardAdapter implements ScoreboardAdapter {

    @Override
    public ScoreboardContent computeScoreboardContent(Contest contest, List<ContestModule> contestModules, String styleConfig, ScoreboardState state, List<Submission> submissions, Map<String, URL> userJidToImageMap) {

        Map<String, Map<String, Integer>> scores = Maps.newHashMap();

        for (String contestantJid : state.getContestantJids()) {
            scores.put(contestantJid, Maps.newHashMap());
        }

        for (Submission submission : submissions) {
            String contestantJid = submission.getAuthorJid();

            if (!scores.containsKey(contestantJid)) {
                continue;
            }

            String problemJid = submission.getProblemJid();
            int score = submission.getLatestScore();

            if (scores.get(contestantJid).containsKey(problemJid)) {
                score = Math.max(scores.get(contestantJid).get(problemJid), score);
            }

            scores.get(contestantJid).put(problemJid, score);
        }

        List<IOIScoreboardEntry> entries = Lists.newArrayList();

        for (String contestantJid : state.getContestantJids()) {
            IOIScoreboardEntry entry = new IOIScoreboardEntry();
            entry.contestantJid = contestantJid;
            entry.imageURL = userJidToImageMap.get(contestantJid);

            int totalScores = 0;
            for (String problemJid : state.getProblemJids()) {
                Integer score = scores.get(contestantJid).get(problemJid);
                entry.scores.add(score);

                if (score != null) {
                    totalScores += score;
                }
            }

            entry.totalScores = totalScores;

            entries.add(entry);
        }

        sortEntriesAndAssignRanks(entries);

        return new IOIScoreboardContent(entries);
    }

    @Override
    public Scoreboard parseScoreboardFromJson(String json) {
        return new Gson().fromJson(json, IOIScoreboard.class);
    }

    @Override
    public Scoreboard createScoreboard(ScoreboardState state, ScoreboardContent content) {
        return new IOIScoreboard(state, (IOIScoreboardContent) content);
    }

    @Override
    public Scoreboard filterOpenProblems(Scoreboard scoreboard, Set<String> openProblemJids) {
        ScoreboardState state = scoreboard.getState();
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

        ScoreboardState newState = new ScoreboardState(
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
            newEntry.totalScores = newEntry.scores.stream().filter(s -> s != null).mapToInt(s -> s).sum();
            newEntries.add(newEntry);
        }

        sortEntriesAndAssignRanks(newEntries);

        return new IOIScoreboard(newState, new IOIScoreboardContent(newEntries));
    }

    @Override
    public Html renderScoreboard(Scoreboard scoreboard, Date lastUpdateTime, AbstractBaseJidCacheServiceImpl<?> jidCacheService, String currentContestantJid, boolean hiddenRank, Set<String> filterContestantJids) {
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

    private void sortEntriesAndAssignRanks(List<IOIScoreboardEntry> entries) {
        Collections.sort(entries);

        int currentRank = 0;
        for (int i = 0; i < entries.size(); i++) {
            currentRank++;
            if (i == 0 || entries.get(i).totalScores != entries.get(i - 1).totalScores) {
                entries.get(i).rank = currentRank;
            } else {
                entries.get(i).rank = entries.get(i - 1).rank;
            }
        }
    }
}
