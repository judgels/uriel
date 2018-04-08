package org.iatoki.judgels.uriel.contest.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.iatoki.judgels.sandalphon.problem.programming.submission.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.problem.programming.submission.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.contest.ContestService;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestantService;
import org.iatoki.judgels.uriel.contest.module.ContestModules;
import org.iatoki.judgels.uriel.contest.module.frozenscoreboard.ContestFrozenScoreboardModule;
import play.db.jpa.JPA;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class ScoreboardUpdater implements Runnable {

    private final Contest contest;
    private final ContestService contestService;
    private final ContestScoreboardService contestScoreboardService;
    private final ContestContestantService contestContestantService;
    private final ProgrammingSubmissionService programmingSubmissionService;
    private final List<OnScoreboardUpdateFinishListener> onScoreboardUpdateFinishListeners;

    public ScoreboardUpdater(Contest contest, ContestService contestService, ContestScoreboardService contestScoreboardService, ContestContestantService contestContestantService, ProgrammingSubmissionService programmingSubmissionService) {
        this.contest = contest;
        this.contestService = contestService;
        this.contestScoreboardService = contestScoreboardService;
        this.contestContestantService = contestContestantService;
        this.programmingSubmissionService = programmingSubmissionService;
        this.onScoreboardUpdateFinishListeners = Lists.newArrayList();
    }

    @Override
    public void run() {
        try {
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
            long time = System.currentTimeMillis();

            updateScoreboard(contest, contestScoreboardService, ContestScoreboardType.OFFICIAL, submissions, contestantStartTimes, contestService, adapter, time, "scoreboardUpdater", "localhost");

            if (contest.containsModule(ContestModules.FROZEN_SCOREBOARD)) {
                ContestFrozenScoreboardModule contestFrozenScoreboardModule = (ContestFrozenScoreboardModule) contest.getModule(ContestModules.FROZEN_SCOREBOARD);
                long freezeTime = contest.getEndTime().getTime() - contestFrozenScoreboardModule.getScoreboardFreezeTime();
                if (System.currentTimeMillis() >= freezeTime) {
                    List<ProgrammingSubmission> frozenSubmissions = JPA.withTransaction("default", true, () -> programmingSubmissionService.getProgrammingSubmissionsWithGradingsByContainerJidBeforeTime(contest.getJid(), freezeTime));
                    updateScoreboard(contest, contestScoreboardService, ContestScoreboardType.FROZEN, frozenSubmissions, contestantStartTimes, contestService, adapter, time, "scoreboardUpdater", "localhost");
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            for (OnScoreboardUpdateFinishListener onScoreboardUpdateFinishListener : onScoreboardUpdateFinishListeners) {
                onScoreboardUpdateFinishListener.onFinish(contest.getJid());
            }
        }
    }

    public void addOnScoreboardUpdateFinishListener(OnScoreboardUpdateFinishListener onScoreboardUpdateFinishListener) {
        this.onScoreboardUpdateFinishListeners.add(onScoreboardUpdateFinishListener);
    }

    private static void updateScoreboard(Contest contest, ContestScoreboardService contestScoreboardService, ContestScoreboardType contestScoreboardType, List<ProgrammingSubmission> submissions, Map<String, Date> contestantStartTimes, ContestService contestService, ScoreboardAdapter adapter, long time, String userJid, String ipAddress) {
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
                contestScoreboardService.upsertContestScoreboard(contest.getJid(), contestScoreboardType, scoreboard, time, userJid, ipAddress);
            });
    }
}
