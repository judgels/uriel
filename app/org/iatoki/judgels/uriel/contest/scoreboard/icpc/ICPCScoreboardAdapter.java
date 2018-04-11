package org.iatoki.judgels.uriel.contest.scoreboard.icpc;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.iatoki.judgels.gabriel.Verdict;
import org.iatoki.judgels.play.jid.AbstractBaseJidCacheServiceImpl;
import org.iatoki.judgels.sandalphon.problem.programming.submission.ProgrammingSubmission;
import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.contest.style.icpc.ICPCContestStyleConfig;
import org.iatoki.judgels.uriel.contest.scoreboard.Scoreboard;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardContent;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardEntryComparator;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardState;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardAdapter;
import org.iatoki.judgels.uriel.contest.scoreboard.icpc.html.icpcScoreboardView;
import play.i18n.Messages;
import play.twirl.api.Html;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ICPCScoreboardAdapter implements ScoreboardAdapter {

    @Override
    public ScoreboardContent computeScoreboardContent(Contest contest, ScoreboardState state, List<ProgrammingSubmission> submissions, Map<String, Date> contestantStartTimes) {

        ICPCContestStyleConfig icpcStyleConfig = (ICPCContestStyleConfig) contest.getStyleConfig();
        ScoreboardEntryComparator<ICPCScoreboardEntry> comparator = new ICPCScoreboardEntryComparator();

        Map<String, Map<String, Integer>> attemptsMap = Maps.newHashMap();
        Map<String, Map<String, Long>> penaltyMap = Maps.newHashMap();
        Map<String, Map<String, Integer>> problemStateMap = Maps.newHashMap();
        Map<String, Long> lastAcceptedPenaltyMap = Maps.newHashMap();
        Set<String> acceptedProblemJids = Sets.newHashSet();

        for (String contestantJid : state.getContestantJids()) {
            attemptsMap.put(contestantJid, Maps.newHashMap());
            penaltyMap.put(contestantJid, Maps.newHashMap());
            problemStateMap.put(contestantJid, Maps.newHashMap());

            for (String problemJid : state.getProblemJids()) {
                attemptsMap.get(contestantJid).put(problemJid, 0);
                penaltyMap.get(contestantJid).put(problemJid, 0L);
                lastAcceptedPenaltyMap.put(contestantJid, 0L);
                problemStateMap.get(contestantJid).put(problemJid, ICPCScoreboardEntry.State.NOT_ACCEPTED.ordinal());
            }
        }

        for (ProgrammingSubmission submission : submissions) {
            String contestantJid = submission.getAuthorJid();
            String problemJid = submission.getProblemJid();

            if (!attemptsMap.containsKey(contestantJid)) {
                continue;
            }
            if (!attemptsMap.get(contestantJid).containsKey(problemJid)) {
                continue;
            }
            if (problemStateMap.get(contestantJid).get(problemJid) != ICPCScoreboardEntry.State.NOT_ACCEPTED.ordinal()) {
                continue;
            }

            Verdict verdict = submission.getLatestVerdict();

            if (verdict.getCode().equals("?")) {
                continue;
            }

            int attempts = attemptsMap.get(contestantJid).get(problemJid);
            attemptsMap.get(contestantJid).put(problemJid, attempts + 1);

            long penaltyInMilliseconds = computeSubmissionPenaltyInMilliseconds(contest, contestantStartTimes.get(contestantJid), submission.getTime());
            penaltyMap.get(contestantJid).put(problemJid, convertPenaltyToMinutes(penaltyInMilliseconds));

            if (verdict.getCode().equals("AC")) {
                if (acceptedProblemJids.contains(problemJid)) {
                    problemStateMap.get(contestantJid).put(problemJid, ICPCScoreboardEntry.State.ACCEPTED.ordinal());
                } else {
                    problemStateMap.get(contestantJid).put(problemJid, ICPCScoreboardEntry.State.FIRST_ACCEPTED.ordinal());
                    acceptedProblemJids.add(problemJid);
                }

                lastAcceptedPenaltyMap.put(contestantJid, penaltyInMilliseconds);
            }
        }

        List<ICPCScoreboardEntry> entries = Lists.newArrayList();

        for (String contestantJid : state.getContestantJids()) {
            ICPCScoreboardEntry entry = new ICPCScoreboardEntry();
            entry.contestantJid = contestantJid;

            for (String problemJid : state.getProblemJids()) {
                int attempts = attemptsMap.get(contestantJid).get(problemJid);
                long penalty = penaltyMap.get(contestantJid).get(problemJid);
                long lastAcceptedPenalty = lastAcceptedPenaltyMap.get(contestantJid);
                int problemState = problemStateMap.get(contestantJid).get(problemJid);

                entry.attemptsList.add(attempts);
                entry.penaltyList.add(penalty);
                entry.lastAcceptedPenalty = lastAcceptedPenalty;
                entry.problemStateList.add(problemState);

                if (problemState != ICPCScoreboardEntry.State.NOT_ACCEPTED.ordinal()) {
                    entry.totalAccepted++;
                    entry.totalPenalties += icpcStyleConfig.getWrongSubmissionPenalty() * (attempts - 1) + penalty;
                }
            }

            entries.add(entry);
        }

        sortEntriesAndAssignRanks(comparator, entries);

        return new ICPCScoreboardContent(entries);
    }

    @Override
    public Scoreboard parseScoreboardFromJson(String json) {
        return new Gson().fromJson(json, ICPCScoreboard.class);
    }

    @Override
    public Scoreboard createScoreboard(ScoreboardState state, ScoreboardContent content) {
        return new ICPCScoreboard(state, (ICPCScoreboardContent) content);
    }

    @Override
    public Scoreboard filterOpenProblems(Contest contest, Scoreboard scoreboard, Set<String> openProblemJids) {
        // in ICPC-style it does not make sense to differentiate contestant's shown problems
        // and supervisor's shown problems

        return scoreboard;
    }

    @Override
    public Html renderScoreboard(Scoreboard scoreboard, Date lastUpdateTime, AbstractBaseJidCacheServiceImpl<?> jidCacheService, String currentContestantJid, boolean hiddenRank, Set<String> filterContestantJids) {
        if (scoreboard == null) {
            return new Html(Messages.get("scoreboard.not_available"));
        }

        ICPCScoreboard castScoreboard = (ICPCScoreboard) scoreboard;
        return icpcScoreboardView.render(castScoreboard.getState(), ((ICPCScoreboardContent) (castScoreboard.getContent())).getEntries().stream().filter(e -> filterContestantJids.contains(e.contestantJid)).collect(Collectors.toList()), lastUpdateTime, jidCacheService, currentContestantJid, hiddenRank);
    }

    private void sortEntriesAndAssignRanks(ScoreboardEntryComparator<ICPCScoreboardEntry> comparator, List<ICPCScoreboardEntry> entries) {
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

    private long convertPenaltyToMinutes(long penaltyInMilliseconds) {
        long penaltyInMinutes = TimeUnit.MILLISECONDS.toMinutes(penaltyInMilliseconds);
        if (TimeUnit.MINUTES.toMillis(penaltyInMinutes) != penaltyInMilliseconds) {
            penaltyInMinutes++;
        }

        return penaltyInMinutes;
    }

    private long computeSubmissionPenaltyInMilliseconds(Contest contest, Date contestStartTime, Date submissionTime) {
        if (contestStartTime != null) {
            return submissionTime.getTime() - contestStartTime.getTime();
        }
        return submissionTime.getTime() - contest.getBeginTime().getTime();
    }
}
