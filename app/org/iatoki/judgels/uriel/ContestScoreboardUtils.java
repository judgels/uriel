package org.iatoki.judgels.uriel;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
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
import play.db.jpa.JPA;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class ContestScoreboardUtils {

    private ContestScoreboardUtils() {
        // prevent instantiation
    }

    public static void updateScoreboards(Contest contest, ContestService contestService, ContestScoreboardService contestScoreboardService, ContestContestantService contestContestantService, ProgrammingSubmissionService programmingSubmissionService, String userJid, String ipAddress) {
        ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(contest.getStyle());
        Map<String, Date> contestantStartTimes = Maps.newHashMap();
        List<ProgrammingSubmission> submissions = Lists.newArrayList();
        try {
            JPA.withTransaction("default", true, () -> {
                    contestantStartTimes.putAll(contestContestantService.getContestantStartTimes(contest.getJid()));
                    submissions.addAll(programmingSubmissionService.getProgrammingSubmissionsWithGradingsByContainerJid(contest.getJid()));
                    return null;
                });
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        updateScoreboard(contest, contestScoreboardService, ContestScoreboardType.OFFICIAL, submissions, contestantStartTimes, contestService, adapter, userJid, ipAddress);

        if (contest.containsModule(ContestModules.FROZEN_SCOREBOARD)) {
            ContestDurationModule contestDurationModule = (ContestDurationModule) contest.getModule(ContestModules.DURATION);
            ContestFrozenScoreboardModule contestFrozenScoreboardModule = (ContestFrozenScoreboardModule) contest.getModule(ContestModules.FROZEN_SCOREBOARD);
            long freezeTime = contestDurationModule.getEndTime().getTime() - contestFrozenScoreboardModule.getScoreboardFreezeTime();
            if (System.currentTimeMillis() >= freezeTime) {
                List<ProgrammingSubmission> frozenSubmissions = programmingSubmissionService.getProgrammingSubmissionsWithGradingsByContainerJidBeforeTime(contest.getJid(), freezeTime);
                updateScoreboard(contest, contestScoreboardService, ContestScoreboardType.FROZEN, frozenSubmissions, contestantStartTimes, contestService, adapter, userJid, ipAddress);
            }
        }
    }

    private static void updateScoreboard(Contest contest, ContestScoreboardService contestScoreboardService, ContestScoreboardType contestScoreboardType, List<ProgrammingSubmission> submissions, Map<String, Date> contestantStartTimes, ContestService contestService, ScoreboardAdapter adapter, String userJid, String ipAddress) {
        ScoreboardState state;
        Map<String, URL> mapContestantToAvatar = Maps.newHashMap();
        try {
            state = JPA.withTransaction("default", true, () -> {
                    mapContestantToAvatar.putAll(contestScoreboardService.getMappedContestantJidToImageUrlInContest(contest.getJid()));
                    return contestService.getScoreboardStateInContest(contest.getJid());
                });
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        ScoreboardContent content = adapter.computeScoreboardContent(contest, state, submissions, contestantStartTimes, mapContestantToAvatar);
        Scoreboard scoreboard = adapter.createScoreboard(state, content);
        JPA.withTransaction(() -> {
                contestScoreboardService.upsertContestScoreboard(contest.getJid(), contestScoreboardType, scoreboard, userJid, ipAddress);
            });
    }
}
