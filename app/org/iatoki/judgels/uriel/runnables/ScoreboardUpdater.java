package org.iatoki.judgels.uriel.runnables;

import com.beust.jcommander.internal.Lists;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestScoreboardUtils;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;
import play.db.jpa.JPA;

import java.util.Date;
import java.util.List;

public final class ScoreboardUpdater implements Runnable {

    private final ContestService contestService;
    private final ContestScoreboardService contestScoreboardService;
    private final ContestContestantService contestContestantService;
    private final ProgrammingSubmissionService programmingSubmissionService;

    public ScoreboardUpdater(ContestService contestService, ContestScoreboardService contestScoreboardService, ContestContestantService contestContestantService, ProgrammingSubmissionService programmingSubmissionService) {
        this.contestService = contestService;
        this.contestScoreboardService = contestScoreboardService;
        this.contestContestantService = contestContestantService;
        this.programmingSubmissionService = programmingSubmissionService;
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
            ContestScoreboardUtils.updateScoreboards(contest, contestService, contestScoreboardService, contestContestantService, programmingSubmissionService, "scoreboardUpdater", "localhost");
        }
    }
}
