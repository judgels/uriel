package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestScope;
import org.iatoki.judgels.uriel.ContestScopeConfig;
import org.iatoki.judgels.uriel.ContestScopeConfigPrivate;
import org.iatoki.judgels.uriel.ContestScopeConfigPublic;
import org.iatoki.judgels.uriel.ContestScoreState;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.ContestStyleConfig;
import org.iatoki.judgels.uriel.ContestStyleConfigICPC;
import org.iatoki.judgels.uriel.ContestStyleConfigIOI;
import org.iatoki.judgels.uriel.ContestType;
import org.iatoki.judgels.uriel.ContestTypeConfig;
import org.iatoki.judgels.uriel.ContestTypeConfigStandard;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.adapters.ScoreboardAdapter;
import org.iatoki.judgels.uriel.adapters.impls.ScoreboardAdapters;
import org.iatoki.judgels.uriel.Scoreboard;
import org.iatoki.judgels.uriel.ScoreboardContent;
import org.iatoki.judgels.uriel.models.daos.ContestConfigurationDao;
import org.iatoki.judgels.uriel.models.daos.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.daos.ContestManagerDao;
import org.iatoki.judgels.uriel.models.daos.ContestProblemDao;
import org.iatoki.judgels.uriel.models.daos.ContestScoreboardDao;
import org.iatoki.judgels.uriel.models.daos.ContestSupervisorDao;
import org.iatoki.judgels.uriel.models.daos.ContestTeamCoachDao;
import org.iatoki.judgels.uriel.models.daos.ContestTeamDao;
import org.iatoki.judgels.uriel.models.entities.ContestConfigurationModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel_;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.models.entities.ContestProblemModel;
import org.iatoki.judgels.uriel.models.entities.ContestScoreboardModel;
import org.iatoki.judgels.uriel.services.ContestService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Named("contestService")
public final class ContestServiceImpl implements ContestService {

    private final ContestDao contestDao;
    private final ContestProblemDao contestProblemDao;
    private final ContestContestantDao contestContestantDao;
    private final ContestTeamDao contestTeamDao;
    private final ContestTeamCoachDao contestTeamCoachDao;
    private final ContestSupervisorDao contestSupervisorDao;
    private final ContestManagerDao contestManagerDao;
    private final ContestScoreboardDao contestScoreboardDao;
    private final ContestConfigurationDao contestConfigurationDao;

    @Inject
    public ContestServiceImpl(ContestDao contestDao, ContestProblemDao contestProblemDao, ContestContestantDao contestContestantDao, ContestTeamDao contestTeamDao, ContestTeamCoachDao contestTeamCoachDao, ContestSupervisorDao contestSupervisorDao, ContestManagerDao contestManagerDao, ContestScoreboardDao contestScoreboardDao, ContestConfigurationDao contestConfigurationDao) {
        this.contestDao = contestDao;
        this.contestProblemDao = contestProblemDao;
        this.contestContestantDao = contestContestantDao;
        this.contestTeamDao = contestTeamDao;
        this.contestTeamCoachDao = contestTeamCoachDao;
        this.contestSupervisorDao = contestSupervisorDao;
        this.contestManagerDao = contestManagerDao;
        this.contestScoreboardDao = contestScoreboardDao;
        this.contestConfigurationDao = contestConfigurationDao;
    }

    @Override
    public Contest findContestById(long contestId) throws ContestNotFoundException {
        ContestModel contestModel = contestDao.findById(contestId);
        if (contestModel != null) {
            return createContestFromModel(contestModel);
        } else {
            throw new ContestNotFoundException("Contest not found.");
        }
    }

    @Override
    public Contest findContestByJid(String contestJid) {
        ContestModel contestModel = contestDao.findByJid(contestJid);
        return createContestFromModel(contestModel);
    }

    @Override
    public ContestScoreState getContestStateByJid(String contestJid) {
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findUsedByContestJidOrderedByAlias(contestJid);
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFilters("id", "asc", "", ImmutableMap.of(ContestContestantModel_.contestJid, contestJid, ContestContestantModel_.status, ContestContestantStatus.APPROVED.name()), ImmutableMap.of(), 0, -1);

        List<String> problemJids = Lists.transform(contestProblemModels, m -> m.problemJid);
        List<String> problemAliases = Lists.transform(contestProblemModels, m -> m.alias);
        List<String> contestantJids = Lists.transform(contestContestantModels, m -> m.userJid);

        return new ContestScoreState(problemJids, problemAliases, contestantJids);
    }

    @Override
    public ContestConfiguration findContestConfigurationByContestJid(String contestJid) {
        ContestConfigurationModel contestConfigurationModel = contestConfigurationDao.findByContestJid(contestJid);

        return new ContestConfiguration(contestConfigurationModel.id, contestConfigurationModel.contestJid, contestConfigurationModel.typeConfig, contestConfigurationModel.scopeConfig, contestConfigurationModel.styleConfig);
    }

    @Override
    public Page<Contest> pageAllowedContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String userJid, boolean isAdmin) {
        if (isAdmin) {
            long totalRowsCount = contestDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
            List<ContestModel> contestModels = contestDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);

            List<Contest> contests = Lists.transform(contestModels, m -> createContestFromModel(m));
            return new Page<>(contests, totalRowsCount, pageIndex, pageSize);
        } else {
            List<String> contestJidsWhereIsContestant = contestContestantDao.findContestJidsByContestantJid(IdentityUtils.getUserJid());
            List<ContestModel> contestModels = contestDao.getRunningContestsWithinContestJids(System.currentTimeMillis(), contestJidsWhereIsContestant);
            long totalRowsCount;

            boolean anyExclusiveContest = false;
            for (ContestModel contestModel : contestModels) {
                anyExclusiveContest = anyExclusiveContest || contestModel.isExclusive;
            }

            if (anyExclusiveContest) {
                // Disable past contest
                contestModels = contestModels.stream().filter(c -> c.name.contains(filterString)).collect(Collectors.toList());
                Collections.sort(contestModels, (c1, c2) -> Long.compare(c1.endTime, c2.endTime));

                totalRowsCount = contestModels.size();
                contestModels = contestModels.stream().skip(pageIndex * pageSize).limit(pageSize).collect(Collectors.toList());
            } else {
                // Enable any contest
                List<String> contestJidsWhereIsManager = contestManagerDao.findContestJidsByManagerJid(IdentityUtils.getUserJid());
                List<String> contestJidsWhereIsSupervisor = contestSupervisorDao.findContestJidsBySupervisorJid(IdentityUtils.getUserJid());
                List<String> contestTeamJids = contestTeamCoachDao.findContestTeamJidsByCoachJid(IdentityUtils.getUserJid());
                List<String> contestJidsWhereIsCoach = contestTeamDao.findContestJidsByTeamJids(contestTeamJids);

                Set<String> contestJids = new HashSet<>();
                contestJids.addAll(contestJidsWhereIsManager);
                contestJids.addAll(contestJidsWhereIsSupervisor);
                contestJids.addAll(contestJidsWhereIsContestant);
                contestJids.addAll(contestJidsWhereIsCoach);

                totalRowsCount = contestDao.countContestsWithinContestJidsOrIsRunningPublic(filterString, contestJids, System.currentTimeMillis());
                contestModels = contestDao.findSortedContestsWithinContestJidsOrIsRunningPublicByFilters(orderBy, orderDir, filterString, contestJids, pageIndex * pageSize, pageSize, System.currentTimeMillis());
            }

            List<Contest> contests = Lists.transform(contestModels, m -> createContestFromModel(m));
            return new Page<>(contests, totalRowsCount, pageIndex, pageSize);
        }
    }

    @Override
    public List<Contest> getRunningContests(Date timeNow) {
        List<ContestModel> contestModels = contestDao.getRunningContests(timeNow.getTime());
        return Lists.transform(contestModels, m -> createContestFromModel(m));
    }

    @Override
    public Contest createContest(String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isExclusive, boolean isUsingScoreboard, boolean isIncognitoScoreboard, boolean requiresPassword) {
        ContestModel contestModel = new ContestModel();
        contestModel.name = name;
        contestModel.description = description;
        contestModel.type = type.name();
        contestModel.scope = scope.name();
        contestModel.style = style.name();
        contestModel.startTime = startTime.getTime();
        contestModel.endTime = endTime.getTime();
        contestModel.clarificationEndTime = clarificationEndTime.getTime();
        contestModel.isExclusive = isExclusive;
        contestModel.isUsingScoreboard = isUsingScoreboard;
        contestModel.isIncognitoScoreboard = isIncognitoScoreboard;
        contestModel.requiresPassword = requiresPassword;

        contestDao.persist(contestModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        ContestScoreboardModel contestScoreboardModel = new ContestScoreboardModel();
        contestScoreboardModel.contestJid = contestModel.jid;
        contestScoreboardModel.type = ContestScoreboardType.OFFICIAL.name();

        ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(style);
        ContestScoreState config = getContestStateByJid(contestModel.jid);
        ScoreboardContent content = adapter.computeScoreboardContent(config, ImmutableList.of(), ImmutableMap.of());
        Scoreboard scoreboard = adapter.createScoreboard(config, content);

        contestScoreboardModel.scoreboard = new Gson().toJson(scoreboard);

        contestScoreboardDao.persist(contestScoreboardModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        Contest contest = createContestFromModel(contestModel);

        ContestConfigurationModel contestConfigurationModel = new ContestConfigurationModel();
        contestConfigurationModel.contestJid = contest.getJid();

        if (contestModel.type.equals(ContestType.STANDARD.name())) {
            contestConfigurationModel.typeConfig = new Gson().toJson(ContestTypeConfigStandard.defaultConfig(contest));
        } else if (contestModel.type.equals(ContestType.VIRTUAL.name())) {
            contestConfigurationModel.typeConfig = new Gson().toJson(ContestTypeConfigVirtual.defaultConfig(contest));
        }

        if (contestModel.scope.equals(ContestScope.PRIVATE.name())) {
            contestConfigurationModel.scopeConfig = new Gson().toJson(ContestScopeConfigPrivate.defaultConfig(contest));
        } else if (contestModel.scope.equals(ContestScope.PUBLIC.name())) {
            contestConfigurationModel.scopeConfig = new Gson().toJson(ContestScopeConfigPublic.defaultConfig(contest));
        }

        if (contestModel.style.equals(ContestStyle.ICPC.name())) {
            contestConfigurationModel.styleConfig = new Gson().toJson(ContestStyleConfigICPC.defaultConfig(contest));
        } else if (contestModel.style.equals(ContestStyle.IOI.name())) {
            contestConfigurationModel.styleConfig = new Gson().toJson(ContestStyleConfigIOI.defaultConfig(contest));
        }

        contestConfigurationDao.persist(contestConfigurationModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return contest;
    }

    @Override
    public void updateContest(long contestId, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isExclusive, boolean isUsingScoreboard, boolean isIncognitoScoreboard, boolean requiresPassword) {
        boolean isTypeChanged;
        boolean isStyleChanged;
        boolean isScopeChanged;

        ContestModel contestModel = contestDao.findById(contestId);

        contestModel.name = name;
        contestModel.description = description;
        isTypeChanged = !contestModel.type.equals(type.name());
        contestModel.type = type.name();
        isScopeChanged = !contestModel.scope.equals(scope.name());
        contestModel.scope = scope.name();
        isStyleChanged = !contestModel.style.equals(style.name());
        contestModel.style = style.name();
        contestModel.startTime = startTime.getTime();
        contestModel.endTime = endTime.getTime();
        contestModel.clarificationEndTime = clarificationEndTime.getTime();
        contestModel.isExclusive = isExclusive;
        contestModel.isUsingScoreboard = isUsingScoreboard;
        contestModel.isIncognitoScoreboard = isIncognitoScoreboard;
        contestModel.requiresPassword = requiresPassword;

        contestDao.edit(contestModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        ContestConfigurationModel contestConfigurationModel = contestConfigurationDao.findByContestJid(contestModel.jid);
        Contest contest = createContestFromModel(contestModel);

        if (isTypeChanged) {
            if (contestModel.type.equals(ContestType.STANDARD.name())) {
                contestConfigurationModel.typeConfig = new Gson().toJson(ContestTypeConfigStandard.defaultConfig(contest));
            } else if (contestModel.type.equals(ContestType.VIRTUAL.name())) {
                contestConfigurationModel.typeConfig = new Gson().toJson(ContestTypeConfigVirtual.defaultConfig(contest));
            }
        }
        if (isScopeChanged) {
            if (contestModel.scope.equals(ContestScope.PRIVATE.name())) {
                contestConfigurationModel.scopeConfig = new Gson().toJson(ContestScopeConfigPrivate.defaultConfig(contest));
            } else if (contestModel.scope.equals(ContestScope.PUBLIC.name())) {
                contestConfigurationModel.scopeConfig = new Gson().toJson(ContestScopeConfigPublic.defaultConfig(contest));
            }
        }
        if (isStyleChanged) {
            if (contestModel.style.equals(ContestStyle.ICPC.name())) {
                contestConfigurationModel.styleConfig = new Gson().toJson(ContestStyleConfigICPC.defaultConfig(contest));
            } else if (contestModel.style.equals(ContestStyle.IOI.name())) {
                contestConfigurationModel.styleConfig = new Gson().toJson(ContestStyleConfigIOI.defaultConfig(contest));
            }

            try {
                ContestScoreboardModel contestScoreboardModel = contestScoreboardDao.findContestScoreboardByContestJidAndScoreboardType(contestModel.jid, ContestScoreboardType.OFFICIAL.name());
                contestScoreboardDao.remove(contestScoreboardModel);

                contestScoreboardModel = contestScoreboardDao.findContestScoreboardByContestJidAndScoreboardType(contestModel.jid, ContestScoreboardType.FROZEN.name());
                contestScoreboardDao.remove(contestScoreboardModel);
            } catch (NoResultException e) {
                throw new RuntimeException(e);
            } finally {
                // TODO recompute everything in this fucking scoreboard include fronzen scoreboard

                ContestScoreboardModel contestScoreboardModel = new ContestScoreboardModel();
                contestScoreboardModel.contestJid = contestModel.jid;
                contestScoreboardModel.type = ContestScoreboardType.OFFICIAL.name();

                ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(style);
                ContestScoreState config = getContestStateByJid(contestModel.jid);
                ScoreboardContent content = adapter.computeScoreboardContent(config, ImmutableList.of(), ImmutableMap.of());
                Scoreboard scoreboard = adapter.createScoreboard(config, content);

                contestScoreboardModel.scoreboard = new Gson().toJson(scoreboard);

                contestScoreboardDao.persist(contestScoreboardModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            }
        }

        if (isTypeChanged || isScopeChanged || isStyleChanged) {
            contestConfigurationDao.edit(contestConfigurationModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public void updateContestConfigurationByContestJid(String contestJid, ContestTypeConfig typeConfig, ContestScopeConfig scopeConfig, ContestStyleConfig styleConfig) {
        ContestConfigurationModel contestConfigurationModel = contestConfigurationDao.findByContestJid(contestJid);
        ContestModel contestModel = contestDao.findByJid(contestJid);

        if (contestModel.type.equals(ContestType.STANDARD.name())) {
            contestConfigurationModel.typeConfig = new Gson().toJson(typeConfig);
        } else if (contestModel.type.equals(ContestType.VIRTUAL.name())) {
            contestConfigurationModel.typeConfig = new Gson().toJson(typeConfig);
        }

        if (contestModel.scope.equals(ContestScope.PRIVATE.name())) {
            contestConfigurationModel.scopeConfig = new Gson().toJson(scopeConfig);
        } else if (contestModel.scope.equals(ContestScope.PUBLIC.name())) {
            contestConfigurationModel.scopeConfig = new Gson().toJson(scopeConfig);
        }

        if (contestModel.style.equals(ContestStyle.ICPC.name())) {
            contestConfigurationModel.styleConfig = new Gson().toJson(styleConfig);
        } else if (contestModel.style.equals(ContestStyle.IOI.name())) {
            contestConfigurationModel.styleConfig = new Gson().toJson(styleConfig);
        }

        contestConfigurationDao.edit(contestConfigurationModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    private Contest createContestFromModel(ContestModel contestModel) {
        return new Contest(contestModel.id, contestModel.jid, contestModel.name, contestModel.description, ContestType.valueOf(contestModel.type), ContestScope.valueOf(contestModel.scope), ContestStyle.valueOf(contestModel.style), new Date(contestModel.startTime), new Date(contestModel.endTime), new Date(contestModel.clarificationEndTime), contestModel.isExclusive, contestModel.isUsingScoreboard, contestModel.isIncognitoScoreboard, contestModel.requiresPassword);
    }
}
