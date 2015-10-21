package org.iatoki.judgels.uriel.runnables;

import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestScoreboardUtils;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;
import play.db.jpa.JPA;

import java.util.Date;

public final class ScoreboardUpdater implements Runnable {

    private final ContestService contestService;
    private final ContestScoreboardService contestScoreboardService;
    private final ProgrammingSubmissionService programmingSubmissionService;

    public ScoreboardUpdater(ContestService contestService, ContestScoreboardService contestScoreboardService, ProgrammingSubmissionService programmingSubmissionService) {
        this.contestService = contestService;
        this.contestScoreboardService = contestScoreboardService;
        this.programmingSubmissionService = programmingSubmissionService;
    }

    @Override
    public void run() {
        JPA.withTransaction(() -> {
                Date timeNow = new Date();
                for (Contest contest : contestService.getRunningContestsWithScoreboardModule(timeNow)) {
                    ContestScoreboardUtils.updateScoreboards(contest, contestService, contestScoreboardService, programmingSubmissionService, "scoreboardUpdater", "localhost");
                }
            });
    }
}
