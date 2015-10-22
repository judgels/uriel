package org.iatoki.judgels.uriel.runnables;

import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestScoreboardUtils;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;
import play.db.jpa.JPA;

import java.util.Date;

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
        JPA.withTransaction(() -> {
                Date timeNow = new Date();
                for (Contest contest : contestService.getRunningContestsWithScoreboardModule(timeNow)) {
                    ContestScoreboardUtils.updateScoreboards(contest, contestService, contestScoreboardService, contestContestantService, programmingSubmissionService, "scoreboardUpdater", "localhost");
                }
            });
    }
}
