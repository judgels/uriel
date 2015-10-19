package org.iatoki.judgels.uriel.runnables;

import com.google.gson.Gson;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.Scoreboard;
import org.iatoki.judgels.uriel.ScoreboardContent;
import org.iatoki.judgels.uriel.ScoreboardState;
import org.iatoki.judgels.uriel.adapters.ScoreboardAdapter;
import org.iatoki.judgels.uriel.adapters.impls.ScoreboardAdapters;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.modules.contest.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.modules.contest.frozenscoreboard.ContestFrozenScoreboardModule;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;
import play.db.jpa.JPA;

import java.util.Date;
import java.util.List;

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
            System.out.println("update");
                for (Contest contest : contestService.getRunningContestsWithScoreboardModule(timeNow)) {
                    System.out.println(contest.getName());
                    boolean frozeScoreboard = false;
                    if (contest.containsModule(ContestModules.FROZEN_SCOREBOARD)) {
                        ContestDurationModule contestDurationModule = (ContestDurationModule) contest.getModule(ContestModules.DURATION);
                        ContestFrozenScoreboardModule contestFrozenScoreboardModule = (ContestFrozenScoreboardModule) contest.getModule(ContestModules.FROZEN_SCOREBOARD);
                        frozeScoreboard = System.currentTimeMillis() > (contestDurationModule.getEndTime().getTime() - contestFrozenScoreboardModule.getScoreboardFreezeTime());
                    }

                    ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(contest.getStyle());
                    ContestScoreboard contestScoreboard = contestScoreboardService.findScoreboardInContestByType(contest.getJid(), ContestScoreboardType.OFFICIAL);
                    if ((contest.containsModule(ContestModules.FROZEN_SCOREBOARD)) && (!contestScoreboardService.scoreboardExistsInContestByType(contest.getJid(), ContestScoreboardType.FROZEN)) && frozeScoreboard) {
                        contestScoreboardService.upsertFrozenScoreboard(contestScoreboard.getId());
                    }
                    ScoreboardState state = contestService.getScoreboardStateInContest(contest.getJid());

                    List<ProgrammingSubmission> programmingSubmissions = programmingSubmissionService.getProgrammingSubmissionsWithGradingsByContainerJid(contest.getJid());

                    ScoreboardContent content = adapter.computeScoreboardContent(contest, new Gson().toJson(contest.getStyleConfig()), state, programmingSubmissions, contestScoreboardService.getMappedContestantJidToImageUrlInContest(contest.getJid()));
                    Scoreboard scoreboard = adapter.createScoreboard(state, content);
                    contestScoreboardService.updateContestScoreboardInContestByType(contest.getJid(), ContestScoreboardType.OFFICIAL, scoreboard);
                }
            });
    }
}
