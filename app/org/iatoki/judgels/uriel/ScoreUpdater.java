package org.iatoki.judgels.uriel;

import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.gabriel.commons.SubmissionService;
import org.iatoki.judgels.uriel.commons.ContestScoreState;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;
import play.db.jpa.JPA;

import java.util.Date;
import java.util.List;

public final class ScoreUpdater implements Runnable {
    private final ContestService contestService;
    private final SubmissionService submissionService;

    public ScoreUpdater(ContestService contestService, SubmissionService submissionService) {
        this.contestService = contestService;
        this.submissionService = submissionService;
    }

    @Override
    public void run() {
        JPA.withTransaction(() -> {
            Date timeNow = new Date();
            for (Contest contest : contestService.getRunningContests(timeNow)) {
                ScoreAdapter adapter = ScoreAdapters.fromContestStyle(contest.getStyle());
                ContestScoreboard contestScoreboard = contestService.findContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL);
                ContestScoreState state;
                if (contestService.isThereNewProblemsOrContestants(contest.getJid(), contestScoreboard.getLastUpdateTime().getTime())) {
                    state = contestService.getContestConfigByJid(contest.getJid());
                } else {
                    state = contestScoreboard.getScoreboard().getState();
                }
                List<Submission> submissions = submissionService.findNewSubmissionsByContestJidByContestants(contest.getJid(), state.getProblemJids(), state.getContestantJids(), contestScoreboard.getLastUpdateTime().getTime());

                contestService.updateContestScoreBySubmissions(contest.getJid(), submissions, adapter, state);

                ScoreboardContent content = adapter.computeScoreboardContent(state, contestService.findContestScoresInContest(contest.getJid(), adapter));

                Scoreboard scoreboard = adapter.createScoreboard(state, content);

                contestService.updateContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL, scoreboard);

                // TODO: update frozen scoreboard
            }
        });
    }
}