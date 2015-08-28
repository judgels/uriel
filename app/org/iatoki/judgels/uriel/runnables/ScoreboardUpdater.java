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
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.modules.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.modules.scoreboard.ContestScoreboardModule;
import org.iatoki.judgels.uriel.services.ContestModuleService;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;
import play.db.jpa.JPA;

import java.util.Date;
import java.util.List;

public final class ScoreboardUpdater implements Runnable {

    private final ContestService contestService;
    private final ContestModuleService contestModuleService;
    private final ContestScoreboardService contestScoreboardService;
    private final ProgrammingSubmissionService submissionService;

    public ScoreboardUpdater(ContestService contestService, ContestModuleService contestModuleService, ContestScoreboardService contestScoreboardService, ProgrammingSubmissionService submissionService) {
        this.contestService = contestService;
        this.contestModuleService = contestModuleService;
        this.contestScoreboardService = contestScoreboardService;
        this.submissionService = submissionService;
    }

    @Override
    public void run() {
        JPA.withTransaction(() -> {
                Date timeNow = new Date();
                for (Contest contest : contestService.getRunningContests(timeNow)) {
                    if (contest.containsModule(ContestModules.SCOREBOARD)) {
                        ContestScoreboardModule contestScoreboardModule = (ContestScoreboardModule) contest.getModule(ContestModules.SCOREBOARD);
                        boolean frozeScoreboard = false;
                        if (contest.containsModule(ContestModules.DURATION)) {
                            ContestDurationModule contestDurationModule = (ContestDurationModule) contest.getModule(ContestModules.DURATION);
                            frozeScoreboard = System.currentTimeMillis() > (contestDurationModule.getEndTime().getTime() - contestScoreboardModule.getScoreboardFreezeTime());
                        }

                        ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(contest.getStyle());
                        ContestScoreboard contestScoreboard = contestScoreboardService.findScoreboardInContestByType(contest.getJid(), ContestScoreboardType.OFFICIAL);
                        if ((!contest.containsModule(ContestModules.VIRTUAL)) && (!contestScoreboardService.scoreboardExistsInContestByType(contest.getJid(), ContestScoreboardType.FROZEN)) && frozeScoreboard) {
                            contestScoreboardService.upsertFrozenScoreboard(contestScoreboard.getId());
                        }
                        ScoreboardState state = contestService.getScoreboardStateInContest(contest.getJid());

                        List<ProgrammingSubmission> submissions = submissionService.getProgrammingSubmissionsWithGradingsByContainerJid(contest.getJid());

                        ScoreboardContent content = adapter.computeScoreboardContent(contest, new Gson().toJson(contest.getStyleConfig()), state, submissions, contestScoreboardService.getMappedContestantJidToImageUrlInContest(contest.getJid()));
                        Scoreboard scoreboard = adapter.createScoreboard(state, content);
                        contestScoreboardService.updateContestScoreboardInContestByType(contest.getJid(), ContestScoreboardType.OFFICIAL, scoreboard);
                    }
                }
            });
    }
}
