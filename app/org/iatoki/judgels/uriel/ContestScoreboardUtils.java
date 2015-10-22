package org.iatoki.judgels.uriel;

import com.google.gson.Gson;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.adapters.ScoreboardAdapter;
import org.iatoki.judgels.uriel.adapters.impls.ScoreboardAdapters;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.modules.contest.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.modules.contest.frozenscoreboard.ContestFrozenScoreboardModule;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;

import java.util.Date;
import java.util.List;
import java.util.Map;

public final class ContestScoreboardUtils {

    private ContestScoreboardUtils() {
        // prevent instantiation
    }

    public static void updateScoreboards(Contest contest, ContestService contestService, ContestScoreboardService contestScoreboardService, ContestContestantService contestContestantService, ProgrammingSubmissionService programmingSubmissionService, String userJid, String ipAddress) {
        ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(contest.getStyle());
        ScoreboardState state = contestService.getScoreboardStateInContest(contest.getJid());
        Map<String, Date> contestantStartTimes =  contestContestantService.getContestantStartTimes(contest.getJid());

        List<ProgrammingSubmission> submissions = programmingSubmissionService.getProgrammingSubmissionsWithGradingsByContainerJid(contest.getJid());
        updateScoreboard(contest, contestScoreboardService, ContestScoreboardType.OFFICIAL, submissions, contestantStartTimes, adapter, state, userJid, ipAddress);

        if (contest.containsModule(ContestModules.FROZEN_SCOREBOARD)) {
            ContestDurationModule contestDurationModule = (ContestDurationModule) contest.getModule(ContestModules.DURATION);
            ContestFrozenScoreboardModule contestFrozenScoreboardModule = (ContestFrozenScoreboardModule) contest.getModule(ContestModules.FROZEN_SCOREBOARD);
            long freezeTime = contestDurationModule.getEndTime().getTime() - contestFrozenScoreboardModule.getScoreboardFreezeTime();
            if (System.currentTimeMillis() >= freezeTime) {
                List<ProgrammingSubmission> frozenSubmissions = programmingSubmissionService.getProgrammingSubmissionsWithGradingsByContainerJidBeforeTime(contest.getJid(), freezeTime);
                updateScoreboard(contest, contestScoreboardService, ContestScoreboardType.FROZEN, frozenSubmissions, contestantStartTimes, adapter, state, userJid, ipAddress);
            }
        }
    }

    private static void updateScoreboard(Contest contest, ContestScoreboardService contestScoreboardService, ContestScoreboardType contestScoreboardType, List<ProgrammingSubmission> submissions, Map<String, Date> contestantStartTimes, ScoreboardAdapter adapter, ScoreboardState state, String userJid, String ipAddress) {
        ScoreboardContent content = adapter.computeScoreboardContent(contest, new Gson().toJson(contest.getStyleConfig()), state, submissions, contestantStartTimes, contestScoreboardService.getMappedContestantJidToImageUrlInContest(contest.getJid()));
        Scoreboard scoreboard = adapter.createScoreboard(state, content);
        contestScoreboardService.upsertContestScoreboard(contest.getJid(), contestScoreboardType, scoreboard, userJid, ipAddress);
    }
}
