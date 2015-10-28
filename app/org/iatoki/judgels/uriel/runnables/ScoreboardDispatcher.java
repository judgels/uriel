package org.iatoki.judgels.uriel.runnables;

import akka.actor.Scheduler;
import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ScoreboardUpdater;
import org.iatoki.judgels.uriel.OnScoreboardUpdateFinishListener;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;
import play.db.jpa.JPA;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class ScoreboardDispatcher implements Runnable {

    private static final Set<String> UPDATER_JIDS = Sets.newHashSet();

    private final Scheduler scheduler;
    private final ExecutionContext executor;
    private final ContestService contestService;
    private final ContestScoreboardService contestScoreboardService;
    private final ContestContestantService contestContestantService;
    private final ProgrammingSubmissionService programmingSubmissionService;

    public ScoreboardDispatcher(Scheduler scheduler, ExecutionContext executor, ContestService contestService, ContestScoreboardService contestScoreboardService, ContestContestantService contestContestantService, ProgrammingSubmissionService programmingSubmissionService) {
        this.scheduler = scheduler;
        this.executor = executor;
        this.contestService = contestService;
        this.contestScoreboardService = contestScoreboardService;
        this.contestContestantService = contestContestantService;
        this.programmingSubmissionService = programmingSubmissionService;
    }

    public static boolean updaterExists(String contestJid) {
        return UPDATER_JIDS.contains(contestJid);
    }

    public static synchronized void updateScoreboard(Scheduler scheduler, ExecutionContext executor, Contest contest, ContestService contestService, ContestScoreboardService contestScoreboardService, ContestContestantService contestContestantService, ProgrammingSubmissionService programmingSubmissionService) {
        if (ScoreboardDispatcher.updaterExists(contest.getJid())) {
            return;
        }

        ScoreboardUpdater scoreboardUpdater = new ScoreboardUpdater(contest, contestService, contestScoreboardService, contestContestantService, programmingSubmissionService);
        scoreboardUpdater.addOnScoreboardUpdateFinishListener(new OnScoreboardUpdateFinishListener() {
            @Override
            public void onFinish(String contestJid) {
                UPDATER_JIDS.remove(contestJid);
            }
        });
        UPDATER_JIDS.add(contest.getJid());
        scheduler.scheduleOnce(Duration.create(10, TimeUnit.MILLISECONDS), scoreboardUpdater, executor);
    }

    @Override
    public void run() {
        Date timeNow = new Date();
        List<Contest> runningContests = Lists.newArrayList();
        try {
            JPA.withTransaction("default", true, () -> {
                    runningContests.addAll(contestService.getRunningContestsWithScoreboardModule(timeNow));
                    return null;
                });
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        for (Contest contest : runningContests) {
            if (!ScoreboardDispatcher.updaterExists(contest.getJid())) {
                ScoreboardDispatcher.updateScoreboard(scheduler, executor, contest, contestService, contestScoreboardService, contestContestantService, programmingSubmissionService);
            }
        }
    }
}
