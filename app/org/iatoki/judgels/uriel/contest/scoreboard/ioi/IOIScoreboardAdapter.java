package org.iatoki.judgels.uriel.contest.scoreboard.ioi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.iatoki.judgels.play.jid.AbstractBaseJidCacheServiceImpl;
import org.iatoki.judgels.sandalphon.problem.programming.submission.ProgrammingSubmission;
import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.contest.scoreboard.Scoreboard;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardContent;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardEntryComparator;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardState;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardAdapter;
import org.iatoki.judgels.uriel.contest.scoreboard.ioi.html.ioiScoreboardView;
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
    public ScoreboardContent computeScoreboardContent(Contest contest, ScoreboardState state, List<ProgrammingSubmission> submissions, Map<String, Date> contestantStartTimes, Map<String, URL> userJidToImageMap) {

        IOIContestStyleConfig styleConfig = (IOIContestStyleConfig) contest.getStyleConfig();

        ScoreboardEntryComparator<IOIScoreboardEntry> comparator;
        if (styleConfig.usingLastAffectingPenalty()) {
            comparator = new UsingLastAffectingPenaltyIOIScoreboardEntryComparator();
        } else {
            comparator = new StandardIOIScoreboardEntryComparator();
        }

        Map<String, Map<String, Integer>> scores = Maps.newHashMap();
        Map<String, Long> lastAffectingPenalties = Maps.newHashMap();

        for (String contestantJid : state.getContestantJids()) {
            scores.put(contestantJid, Maps.newHashMap());
            lastAffectingPenalties.put(contestantJid, 0L);
        }

        for (ProgrammingSubmission submission : submissions) {
            String contestantJid = submission.getAuthorJid();

            if (!scores.containsKey(contestantJid)) {
                continue;
            }

            String problemJid = submission.getProblemJid();
            int score = submission.getLatestScore();

            boolean updateLastAffectingPenalty = false;

            if (scores.get(contestantJid).containsKey(problemJid)) {
                int oldScore = scores.get(contestantJid).get(problemJid);

                if (score > oldScore) {
                    updateLastAffectingPenalty = true;
                } else {
                    score = oldScore;
                }
            } else if (score > 0) {
                updateLastAffectingPenalty = true;
            }

            scores.get(contestantJid).put(problemJid, score);

            if (updateLastAffectingPenalty) {
                long lastAffectingPenalty = computeLastAffectingPenalty(contest, contestantStartTimes.get(contestantJid), submission.getTime());
                lastAffectingPenalties.put(contestantJid, lastAffectingPenalty);
            }
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
            entry.lastAffectingPenalty = lastAffectingPenalties.get(contestantJid);

            entries.add(entry);
        }

        sortEntriesAndAssignRanks(comparator, entries);

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
    public Scoreboard filterOpenProblems(Contest contest, Scoreboard scoreboard, Set<String> openProblemJids) {
        IOIContestStyleConfig styleConfig = (IOIContestStyleConfig) contest.getStyleConfig();

        ScoreboardEntryComparator<IOIScoreboardEntry> comparator;
        if (styleConfig.usingLastAffectingPenalty()) {
            comparator = new UsingLastAffectingPenaltyIOIScoreboardEntryComparator();
        } else {
            comparator = new StandardIOIScoreboardEntryComparator();
        }

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

        sortEntriesAndAssignRanks(comparator, newEntries);

        return new IOIScoreboard(newState, new IOIScoreboardContent(newEntries));
    }

    @Override
    public Html renderScoreboard(Scoreboard scoreboard, Date lastUpdateTime, AbstractBaseJidCacheServiceImpl<?> jidCacheService, String currentContestantJid, boolean hiddenRank, Set<String> filterContestantJids) {
        if (scoreboard == null) {
            return new Html(Messages.get("scoreboard.not_available"));
        }

        IOIScoreboard castScoreboard = (IOIScoreboard) scoreboard;
        return ioiScoreboardView.render(castScoreboard.getState(), castScoreboard.getContent().getEntries().stream().filter(e -> filterContestantJids.contains(e.contestantJid)).collect(Collectors.toList()), lastUpdateTime, jidCacheService, currentContestantJid, hiddenRank);
    }

    private <T> List<T> filterIndices(List<T> list, List<Integer> indices) {
        return indices.stream().map(i -> list.get(i)).collect(Collectors.toList());
    }

    private void sortEntriesAndAssignRanks(ScoreboardEntryComparator<IOIScoreboardEntry> comparator, List<IOIScoreboardEntry> entries) {
        Collections.sort(entries, comparator);

        int currentRank = 0;
        for (int i = 0; i < entries.size(); i++) {
            currentRank++;
            if (i == 0 || comparator.compareWithoutTieBreakerForEqualRanks(entries.get(i), entries.get(i - 1)) != 0) {
                entries.get(i).rank = currentRank;
            } else {
                entries.get(i).rank = entries.get(i - 1).rank;
            }
        }
    }


    private long computeLastAffectingPenalty(Contest contest, Date contestStartTime, Date submissionTime) {
        if (contestStartTime != null) {
            return submissionTime.getTime() - contestStartTime.getTime();
        }
        return submissionTime.getTime() - contest.getBeginTime().getTime();
    }
}
