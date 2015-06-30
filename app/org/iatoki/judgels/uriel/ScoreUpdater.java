package org.iatoki.judgels.uriel;

import com.google.gson.Gson;
import org.iatoki.judgels.sandalphon.Submission;
import org.iatoki.judgels.sandalphon.services.SubmissionService;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;
import play.db.jpa.JPA;

import java.util.Date;
import java.util.List;

public final class ScoreUpdater implements Runnable {

    private final ContestService contestService;
    private final ContestScoreboardService contestScoreboardService;
    private final SubmissionService submissionService;

    public ScoreUpdater(ContestService contestService, ContestScoreboardService contestScoreboardService, SubmissionService submissionService) {
        this.contestService = contestService;
        this.contestScoreboardService = contestScoreboardService;
        this.submissionService = submissionService;
    }

    @Override
    public void run() {
        JPA.withTransaction(() -> {
            Date timeNow = new Date();
            for (Contest contest : contestService.getRunningContests(timeNow)) {
                if (contest.isUsingScoreboard()) {
                    ScoreAdapter adapter = ScoreAdapters.fromContestStyle(contest.getStyle());
                    ContestScoreboard contestScoreboard = contestScoreboardService.findContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL);
                    ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
                    if ((contest.isStandard()) && (!contestScoreboardService.isContestScoreboardExistByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.FROZEN)) && (System.currentTimeMillis() > ((ContestTypeConfigStandard) new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigStandard.class)).getScoreboardFreezeTime())) {
                        contestScoreboardService.upsertFrozenScoreboard(contestScoreboard.getId());
                    }
                    ContestScoreState state = contestService.getContestStateByJid(contest.getJid());

                    List<Submission> submissions = submissionService.findAllSubmissionsByContestJid(contest.getJid());

                    ScoreboardContent content = adapter.computeScoreboardContent(state, submissions, contestScoreboardService.getMapContestantJidToImageUrlInContest(contest.getJid()));
                    Scoreboard scoreboard = adapter.createScoreboard(state, content);
                    contestScoreboardService.updateContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL, scoreboard);
                }
            }
        });
    }
}