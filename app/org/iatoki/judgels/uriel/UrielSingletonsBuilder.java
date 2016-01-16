package org.iatoki.judgels.uriel;

import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.jophiel.controllers.JophielClientControllerUtils;
import org.iatoki.judgels.jophiel.services.impls.UserActivityMessageServiceImpl;
import org.iatoki.judgels.uriel.contest.ContestControllerUtils;
import org.iatoki.judgels.uriel.activity.ActivityLogDao;
import org.iatoki.judgels.uriel.avatar.AvatarCacheDao;
import org.iatoki.judgels.uriel.jid.JidCacheDao;
import org.iatoki.judgels.uriel.contest.contestant.password.ContestContestantPasswordService;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestantService;
import org.iatoki.judgels.uriel.contest.manager.ContestManagerService;
import org.iatoki.judgels.uriel.contest.supervisor.ContestSupervisorService;
import org.iatoki.judgels.uriel.contest.team.ContestTeamService;
import org.iatoki.judgels.uriel.activity.ActivityLogServiceImpl;
import org.iatoki.judgels.uriel.avatar.AvatarCacheServiceImpl;
import org.iatoki.judgels.uriel.jid.JidCacheServiceImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @deprecated Temporary class. Will be restructured when new module system has been finalized.
 */
@Singleton
@Deprecated
public final class UrielSingletonsBuilder {

    @Inject
    public UrielSingletonsBuilder(JidCacheDao jidCacheDao, AvatarCacheDao avatarCacheDao, ActivityLogDao activityLogDao, JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI, ContestContestantService contestContestantService, ContestSupervisorService contestSupervisorService, ContestManagerService contestManagerService, ContestTeamService contestTeamService, ContestContestantPasswordService contestContestantPasswordService) {
        JidCacheServiceImpl.buildInstance(jidCacheDao);
        AvatarCacheServiceImpl.buildInstance(avatarCacheDao);
        ActivityLogServiceImpl.buildInstance(activityLogDao);
        UserActivityMessageServiceImpl.buildInstance();

        JophielClientControllerUtils.buildInstance(UrielProperties.getInstance().getJophielBaseUrl());
        UrielControllerUtils.buildInstance(jophielClientAPI, jophielPublicAPI);
        ContestControllerUtils.buildInstance(contestContestantService, contestSupervisorService, contestManagerService, contestTeamService, contestContestantPasswordService);
    }
}
