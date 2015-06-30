package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.ScoreAdapters;
import org.iatoki.judgels.uriel.Scoreboard;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.models.daos.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.daos.ContestScoreboardDao;
import org.iatoki.judgels.uriel.models.daos.ContestTeamDao;
import org.iatoki.judgels.uriel.models.daos.ContestTeamMemberDao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel_;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.models.entities.ContestScoreboardModel;
import org.iatoki.judgels.uriel.models.entities.ContestTeamMemberModel;
import org.iatoki.judgels.uriel.models.entities.ContestTeamModel;
import org.iatoki.judgels.uriel.services.AvatarCacheService;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Named("contestScoreboardService")
public final class ContestScoreboardServiceImpl implements ContestScoreboardService {

    private final ContestDao contestDao;
    private final ContestContestantDao contestContestantDao;
    private final ContestTeamDao contestTeamDao;
    private final ContestTeamMemberDao contestTeamMemberDao;
    private final ContestScoreboardDao contestScoreboardDao;
    private final FileSystemProvider teamAvatarFileSystemProvider;

    @Inject
    public ContestScoreboardServiceImpl(ContestDao contestDao, ContestContestantDao contestContestantDao, ContestTeamDao contestTeamDao, ContestTeamMemberDao contestTeamMemberDao, ContestScoreboardDao contestScoreboardDao, FileSystemProvider teamAvatarFileSystemProvider) {
        this.contestDao = contestDao;
        this.contestContestantDao = contestContestantDao;
        this.contestTeamDao = contestTeamDao;
        this.contestTeamMemberDao = contestTeamMemberDao;
        this.contestScoreboardDao = contestScoreboardDao;
        this.teamAvatarFileSystemProvider = teamAvatarFileSystemProvider;
    }

    @Override
    public boolean isContestScoreboardExistByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type) {
        return contestScoreboardDao.isContestScoreboardExistByContestJidAndScoreboardType(contestJid, type.name());
    }

    @Override
    public ContestScoreboard findContestScoreboardByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type) {
        ContestModel contestModel = contestDao.findByJid(contestJid);
        ContestScoreboardModel contestScoreboardModel = contestScoreboardDao.findContestScoreboardByContestJidAndScoreboardType(contestJid, type.name());
        return createContestScoreboardFromModel(contestScoreboardModel, ContestStyle.valueOf(contestModel.style));
    }

    @Override
    public Map<String, URL> getMapContestantJidToImageUrlInContest(String contestJid) {
        ImmutableMap.Builder<String, URL> resultBuilder = ImmutableMap.builder();

        List<ContestTeamModel> contestTeamModels = contestTeamDao.findContestTeamModelsByContestJid(contestJid);
        ImmutableMap.Builder<String, ContestTeamModel> contestTeamModelBuilder = ImmutableMap.builder();
        for (ContestTeamModel contestTeamModel : contestTeamModels) {
            contestTeamModelBuilder.put(contestTeamModel.jid, contestTeamModel);
        }
        Map<String, ContestTeamModel> contestTeamModelMap = contestTeamModelBuilder.build();

        List<String> contestTeamJids = contestTeamModels.stream().map(ct -> ct.jid).collect(Collectors.toList());
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFilters("id", "asc", "", ImmutableMap.of(ContestContestantModel_.contestJid, contestJid), ImmutableMap.of(), 0, -1);

        for (ContestContestantModel contestContestantModel : contestContestantModels) {
            if (contestTeamMemberDao.isUserRegisteredAsMemberInAnyTeam(contestContestantModel.userJid, contestTeamJids)) {
                ContestTeamMemberModel contestTeamMemberModel = contestTeamMemberDao.findContestTeamMemberByMemberJidInAnyTeam(contestContestantModel.userJid, contestTeamJids);
                resultBuilder.put(contestContestantModel.userJid, getTeamImageURLFromImageName(contestTeamModelMap.get(contestTeamMemberModel.teamJid).teamImageName));
            } else {
                resultBuilder.put(contestContestantModel.userJid, AvatarCacheService.getInstance().getAvatarUrl(contestContestantModel.userJid));
            }
        }

        return resultBuilder.build();
    }

    @Override
    public void upsertFrozenScoreboard(long contestScoreboardId) {
        ContestScoreboardModel contestScoreboardModel = contestScoreboardDao.findById(contestScoreboardId);
        ContestScoreboardModel frozenContestScoreboardModel;
        if (contestScoreboardDao.isContestScoreboardExistByContestJidAndScoreboardType(contestScoreboardModel.contestJid, ContestScoreboardType.FROZEN.name())) {
            frozenContestScoreboardModel = contestScoreboardDao.findContestScoreboardByContestJidAndScoreboardType(contestScoreboardModel.contestJid, ContestScoreboardType.FROZEN.name());
        } else {
            frozenContestScoreboardModel = new ContestScoreboardModel();
        }
        frozenContestScoreboardModel.contestJid = contestScoreboardModel.contestJid;
        frozenContestScoreboardModel.scoreboard = contestScoreboardModel.scoreboard;
        frozenContestScoreboardModel.type = ContestScoreboardType.FROZEN.name();

        contestScoreboardDao.edit(frozenContestScoreboardModel, "scoreUpdater", "localhost");
    }

    @Override
    public void updateContestScoreboardByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type, Scoreboard scoreboard) {
        try {
            ContestScoreboardModel contestScoreboardModel = contestScoreboardDao.findContestScoreboardByContestJidAndScoreboardType(contestJid, type.name());
            contestScoreboardModel.scoreboard = new Gson().toJson(scoreboard);

            contestScoreboardDao.edit(contestScoreboardModel, "scoreboardUpdater", "localhost");
        } catch (NoResultException e) {
            // just do nothing
        }
    }

    private ContestScoreboard createContestScoreboardFromModel(ContestScoreboardModel contestScoreboardModel, ContestStyle style) {
        Scoreboard scoreboard = ScoreAdapters.fromContestStyle(style).parseScoreboardFromJson(contestScoreboardModel.scoreboard);
        return new ContestScoreboard(contestScoreboardModel.id, contestScoreboardModel.contestJid, ContestScoreboardType.valueOf(contestScoreboardModel.type), scoreboard, new Date(contestScoreboardModel.timeUpdate));
    }

    private URL getTeamImageURLFromImageName(String imageName) {
        try {
            return new URL(UrielProperties.getInstance().getUrielBaseUrl() + org.iatoki.judgels.uriel.controllers.apis.routes.ContestAPIController.renderTeamAvatarImage(imageName));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
