package org.iatoki.judgels.uriel;

import akka.actor.ActorSystem;
import akka.actor.Scheduler;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.sealtiel.SealtielClientAPI;
import org.iatoki.judgels.jophiel.runnables.UserActivityMessagePusher;
import org.iatoki.judgels.jophiel.services.impls.UserActivityMessageServiceImpl;
import org.iatoki.judgels.sandalphon.runnables.GradingResponsePoller;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardUpdaterDispatcher;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestantService;
import org.iatoki.judgels.uriel.contest.scoreboard.ContestScoreboardService;
import org.iatoki.judgels.uriel.contest.ContestService;
import play.db.jpa.JPAApi;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * @deprecated Temporary class. Will be restructured when new module system has been finalized.
 */
@Singleton
@Deprecated
public final class UrielThreadsScheduler {

    @Inject
    public UrielThreadsScheduler(ActorSystem actorSystem, JPAApi jpaApi, ProgrammingSubmissionService programmingSubmissionService, SealtielClientAPI sealtielClientAPI, ContestService contestService, ContestScoreboardService contestScoreboardService, ContestContestantService contestContestantService, JophielClientAPI jophielClientAPI) {
        Scheduler scheduler = actorSystem.scheduler();
        ExecutionContextExecutor context = actorSystem.dispatcher();

        GradingResponsePoller poller = new GradingResponsePoller(scheduler, context, programmingSubmissionService, sealtielClientAPI, TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS));
        ScoreboardUpdaterDispatcher updater = new ScoreboardUpdaterDispatcher(scheduler, context, contestService, contestScoreboardService, contestContestantService, programmingSubmissionService);
        UserActivityMessagePusher userActivityMessagePusher = new UserActivityMessagePusher(jpaApi, jophielClientAPI, UserActivityMessageServiceImpl.getInstance());

        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(UrielProperties.getInstance().getUrielGradingPollerInterval(), TimeUnit.SECONDS), poller, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(UrielProperties.getInstance().getUrielScoreboardUpdateInterval(), TimeUnit.SECONDS), updater, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(UrielProperties.getInstance().getUrielLogPusherInterval(), TimeUnit.SECONDS), userActivityMessagePusher, context);
    }
}
