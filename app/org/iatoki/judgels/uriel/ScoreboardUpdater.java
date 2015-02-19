package org.iatoki.judgels.uriel;

import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.gabriel.commons.SubmissionService;
import org.iatoki.judgels.uriel.commons.ContestConfig;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;

import java.util.Date;
import java.util.List;

public final class ScoreboardUpdater implements Runnable {
    private final ContestService contestService;
    private final SubmissionService submissionService;

    public ScoreboardUpdater(ContestService contestService, SubmissionService submissionService) {
        this.contestService = contestService;
        this.submissionService = submissionService;
    }

    @Override
    public void run() {
        Date timeNow = new Date();
        for (Contest contest : contestService.getRunningContests(timeNow)) {
            ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(contest.getStyle());
            ContestConfig config = contestService.getContestConfigByJid(contest.getJid());
            List<Submission> submissions = submissionService.findAllSubmissionsByContestJid(contest.getJid());
            ScoreboardContent content = adapter.computeContent(config, submissions);

            Scoreboard scoreboard = adapter.createScoreboard(config, content);

            contestService.updateContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL, scoreboard);

            // TODO: update frozen scoreboard
        }
    }
}
