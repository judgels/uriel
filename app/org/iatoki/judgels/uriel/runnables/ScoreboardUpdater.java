package org.iatoki.judgels.uriel.runnables;

import com.google.gson.Gson;
import org.iatoki.judgels.sandalphon.Submission;
import org.iatoki.judgels.sandalphon.services.SubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ScoreboardState;
import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.StandardContestTypeConfig;
import org.iatoki.judgels.uriel.adapters.ScoreboardAdapter;
import org.iatoki.judgels.uriel.adapters.impls.ScoreboardAdapters;
import org.iatoki.judgels.uriel.Scoreboard;
import org.iatoki.judgels.uriel.ScoreboardContent;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;
import play.db.jpa.JPA;

import java.util.Date;
import java.util.List;

public final class ScoreboardUpdater implements Runnable {

    private final ContestService contestService;
    private final ContestScoreboardService contestScoreboardService;
    private final SubmissionService submissionService;

    public ScoreboardUpdater(ContestService contestService, ContestScoreboardService contestScoreboardService, SubmissionService submissionService) {
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
                    ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(contest.getStyle());
                    ContestScoreboard contestScoreboard = contestScoreboardService.findContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL);
                    ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
                    if ((contest.isStandard()) && (!contestScoreboardService.isContestScoreboardExistByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.FROZEN)) && (System.currentTimeMillis() > ((StandardContestTypeConfig) new Gson().fromJson(contestConfiguration.getTypeConfig(), StandardContestTypeConfig.class)).getScoreboardFreezeTime())) {
                        contestScoreboardService.upsertFrozenScoreboard(contestScoreboard.getId());
                    }
                    ScoreboardState state = contestService.getContestStateByJid(contest.getJid());

                    List<Submission> submissions = submissionService.findAllSubmissionsByContestJid(contest.getJid());

                    ScoreboardContent content = adapter.computeScoreboardContent(contest, contestConfiguration.getStyleConfig(), state, submissions, contestScoreboardService.getMapContestantJidToImageUrlInContest(contest.getJid()));
                    Scoreboard scoreboard = adapter.createScoreboard(state, content);
                    contestScoreboardService.updateContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL, scoreboard);
                }
            }
        });
    }
}