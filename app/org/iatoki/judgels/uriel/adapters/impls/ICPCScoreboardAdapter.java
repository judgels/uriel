package org.iatoki.judgels.uriel.adapters.impls;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.iatoki.judgels.gabriel.Verdict;
import org.iatoki.judgels.play.services.impls.AbstractBaseJidCacheServiceImpl;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ICPCContestStyleConfig;
import org.iatoki.judgels.uriel.ICPCScoreboard;
import org.iatoki.judgels.uriel.ICPCScoreboardContent;
import org.iatoki.judgels.uriel.ICPCScoreboardEntry;
import org.iatoki.judgels.uriel.Scoreboard;
import org.iatoki.judgels.uriel.ScoreboardContent;
import org.iatoki.judgels.uriel.ScoreboardState;
import org.iatoki.judgels.uriel.adapters.ScoreboardAdapter;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.modules.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.views.html.contest.scoreboard.icpcScoreboardView;
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
    public ScoreboardContent computeScoreboardContent(Contest contest, String styleConfig, ScoreboardState state, List<ProgrammingSubmission> submissions, Map<String, URL> userJidToImageMap) {

        ICPCContestStyleConfig icpcStyleConfig = new Gson().fromJson(styleConfig, ICPCContestStyleConfig.class);

        Map<String, Map<String, Integer>> attemptsMap = Maps.newHashMap();
        Map<String, Map<String, Long>> penaltyMap = Maps.newHashMap();
        Map<String, Map<String, Boolean>> isAcceptedMap = Maps.newHashMap();

        for (String contestantJid : state.getContestantJids()) {
            attemptsMap.put(contestantJid, Maps.newHashMap());
            penaltyMap.put(contestantJid, Maps.newHashMap());
            isAcceptedMap.put(contestantJid, Maps.newHashMap());

            for (String problemJid : state.getProblemJids()) {
                attemptsMap.get(contestantJid).put(problemJid, 0);
                penaltyMap.get(contestantJid).put(problemJid, 0L);
                isAcceptedMap.get(contestantJid).put(problemJid, false);
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

            if (isAcceptedMap.get(contestantJid).get(problemJid)) {
                continue;
            }

            Verdict verdict = submission.getLatestVerdict();

            int attempts = attemptsMap.get(contestantJid).get(problemJid);
            attemptsMap.get(contestantJid).put(problemJid, attempts + 1);

            penaltyMap.get(contestantJid).put(problemJid, computeSubmissionPenalty(((ContestDurationModule) contest.getModule(ContestModules.DURATION)).getBeginTime(), submission.getTime()));

            isAcceptedMap.get(contestantJid).put(problemJid, verdict.getCode().equals("AC"));
        }

        List<ICPCScoreboardEntry> entries = Lists.newArrayList();

        for (String contestantJid : state.getContestantJids()) {
            ICPCScoreboardEntry entry = new ICPCScoreboardEntry();
            entry.contestantJid = contestantJid;
            entry.imageURL = userJidToImageMap.get(contestantJid);

            for (String problemJid : state.getProblemJids()) {
                int attempts = attemptsMap.get(contestantJid).get(problemJid);
                long penalty = penaltyMap.get(contestantJid).get(problemJid);
                boolean isAccepted = isAcceptedMap.get(contestantJid).get(problemJid);

                entry.attemptsList.add(attempts);
                entry.penaltyList.add(penalty);
                entry.isAcceptedList.add(isAccepted);

                if (isAccepted) {
                    entry.totalAccepted++;
                    entry.totalPenalties += icpcStyleConfig.getWrongSubmissionPenalty() * (attempts - 1) + penalty;
                }
            }

            entries.add(entry);
        }

        sortEntriesAndAssignRanks(entries);

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
    public Scoreboard filterOpenProblems(Scoreboard scoreboard, Set<String> openProblemJids) {
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

    private void sortEntriesAndAssignRanks(List<ICPCScoreboardEntry> entries) {
        Collections.sort(entries);

        int currentRank = 0;
        for (int i = 0; i < entries.size(); i++) {
            currentRank++;
            if (i == 0 || entries.get(i).compareToIgnoringTieBreaker(entries.get(i - 1)) != 0) {
                entries.get(i).rank = currentRank;
            } else {
                entries.get(i).rank = entries.get(i - 1).rank;
            }
        }
    }

    private long computeSubmissionPenalty(Date contestBeginTime, Date submissionTime) {
        long contestBeginMillis = contestBeginTime.getTime();
        long submissionMillis = submissionTime.getTime();
        long millisElapsed = submissionMillis - contestBeginMillis;

        long penalty = TimeUnit.MILLISECONDS.toMinutes(millisElapsed);
        if (TimeUnit.MINUTES.toMillis(penalty) != millisElapsed) {
            penalty++;
        }

        return penalty;
    }
}
