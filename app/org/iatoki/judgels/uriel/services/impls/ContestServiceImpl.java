package org.iatoki.judgels.uriel.services.impls;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.ContestStyleConfig;
import org.iatoki.judgels.uriel.ICPCContestStyleConfig;
import org.iatoki.judgels.uriel.IOIContestStyleConfig;
import org.iatoki.judgels.uriel.Scoreboard;
import org.iatoki.judgels.uriel.ScoreboardContent;
import org.iatoki.judgels.uriel.ScoreboardState;
import org.iatoki.judgels.uriel.adapters.ScoreboardAdapter;
import org.iatoki.judgels.uriel.adapters.impls.ScoreboardAdapters;
import org.iatoki.judgels.uriel.models.daos.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.daos.ContestManagerDao;
import org.iatoki.judgels.uriel.models.daos.ContestModuleDao;
import org.iatoki.judgels.uriel.models.daos.ContestProblemDao;
import org.iatoki.judgels.uriel.models.daos.ContestScoreboardDao;
import org.iatoki.judgels.uriel.models.daos.ContestStyleDao;
import org.iatoki.judgels.uriel.models.daos.ContestSupervisorDao;
import org.iatoki.judgels.uriel.models.daos.ContestTeamCoachDao;
import org.iatoki.judgels.uriel.models.daos.ContestTeamDao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel_;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.models.entities.ContestModel_;
import org.iatoki.judgels.uriel.models.entities.ContestModuleModel;
import org.iatoki.judgels.uriel.models.entities.ContestProblemModel;
import org.iatoki.judgels.uriel.models.entities.ContestScoreboardModel;
import org.iatoki.judgels.uriel.models.entities.ContestStyleModel;
import org.iatoki.judgels.uriel.modules.contest.ContestModule;
import org.iatoki.judgels.uriel.modules.contest.ContestModuleFactory;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.modules.contest.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.services.ContestService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Named("contestService")
public final class ContestServiceImpl implements ContestService {

    private final ContestContestantDao contestContestantDao;
    private final ContestDao contestDao;
    private final ContestManagerDao contestManagerDao;
    private final ContestModuleDao contestModuleDao;
    private final ContestProblemDao contestProblemDao;
    private final ContestScoreboardDao contestScoreboardDao;
    private final ContestStyleDao contestStyleDao;
    private final ContestSupervisorDao contestSupervisorDao;
    private final ContestTeamCoachDao contestTeamCoachDao;
    private final ContestTeamDao contestTeamDao;

    @Inject
    public ContestServiceImpl(ContestContestantDao contestContestantDao, ContestDao contestDao, ContestManagerDao contestManagerDao, ContestModuleDao contestModuleDao, ContestProblemDao contestProblemDao, ContestScoreboardDao contestScoreboardDao, ContestStyleDao contestStyleDao, ContestSupervisorDao contestSupervisorDao, ContestTeamCoachDao contestTeamCoachDao, ContestTeamDao contestTeamDao) {
        this.contestContestantDao = contestContestantDao;
        this.contestDao = contestDao;
        this.contestManagerDao = contestManagerDao;
        this.contestModuleDao = contestModuleDao;
        this.contestProblemDao = contestProblemDao;
        this.contestScoreboardDao = contestScoreboardDao;
        this.contestStyleDao = contestStyleDao;
        this.contestSupervisorDao = contestSupervisorDao;
        this.contestTeamCoachDao = contestTeamCoachDao;
        this.contestTeamDao = contestTeamDao;
    }

    @Override
    public Contest findContestById(long contestId) throws ContestNotFoundException {
        ContestModel contestModel = contestDao.findById(contestId);
        if (contestModel == null) {
            throw new ContestNotFoundException("Contest not found.");
        }

        return createContestFromModel(contestStyleDao, contestModel, contestModuleDao.getEnabledInContest(contestModel.jid));
    }

    @Override
    public Contest findContestByJid(String contestJid) {
        ContestModel contestModel = contestDao.findByJid(contestJid);

        return createContestFromModel(contestStyleDao, contestModel, contestModuleDao.getEnabledInContest(contestModel.jid));
    }

    @Override
    public ScoreboardState getScoreboardStateInContest(String contestJid) {
        List<ContestProblemModel> contestProblemModels = contestProblemDao.getUsedInContest(contestJid);
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ContestContestantModel_.contestJid, contestJid, ContestContestantModel_.status, ContestContestantStatus.APPROVED.name()), 0, -1);

        List<String> problemJids = Lists.transform(contestProblemModels, m -> m.problemJid);
        List<String> problemAliases = Lists.transform(contestProblemModels, m -> m.alias);
        List<String> contestantJids = Lists.transform(contestContestantModels, m -> m.userJid);

        return new ScoreboardState(problemJids, problemAliases, contestantJids);
    }

    @Override
    public Page<Contest> getPageOfContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalRowsCount = contestDao.countByFilters(filterString);
        List<ContestModel> contestModels = contestDao.findSortedByFilters(orderBy, orderDir, filterString, pageIndex * pageSize, pageSize);

        List<Contest> contests = Lists.transform(contestModels, m -> createContestFromModel(contestStyleDao, m, contestModuleDao.getEnabledInContest(m.jid)));
        return new Page<>(contests, totalRowsCount, pageIndex, pageSize);
    }

    @Override
    public Page<Contest> getPageOfAllowedContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String userJid) {
        List<String> contestJidsWhereIsContestant = contestContestantDao.getContestJidsByJid(userJid);
        List<ContestModel> contestModels = contestDao.findSortedByFiltersIn("id", "desc", "", ImmutableMap.of(ContestModel_.jid, contestJidsWhereIsContestant), 0, -1);
        ImmutableList.Builder<ContestModel> runningContestModelsBuilder = ImmutableList.builder();

        for (ContestModel contestModel : contestModels) {
            if (contestModuleDao.existsEnabledInContestByName(contestModel.jid, ContestModules.DURATION.name())) {
                ContestModuleModel contestModuleModel = contestModuleDao.findInContestByName(contestModel.jid, ContestModules.DURATION.name());
                ContestDurationModule contestDurationModule = (ContestDurationModule) ContestModuleFactory.parseFromConfig(ContestModules.DURATION, contestModuleModel.config);
                Date currentDate = new Date();
                if (currentDate.after(contestDurationModule.getBeginTime()) && currentDate.before(contestDurationModule.getEndTime())) {
                    runningContestModelsBuilder.add(contestModel);
                }
            } else {
                runningContestModelsBuilder.add(contestModel);
            }
        }
        contestModels = runningContestModelsBuilder.build();

        long totalRowsCount;
        boolean anyExclusiveContest = false;
        for (ContestModel contestModel : contestModels) {
            anyExclusiveContest = anyExclusiveContest || contestModuleDao.existsEnabledInContestByName(contestModel.jid, ContestModules.EXCLUSIVE.name());
        }

        if (anyExclusiveContest) {
            // Disable past contest
            contestModels = contestModels.stream().filter(c -> c.name.contains(filterString)).collect(Collectors.toList());

            totalRowsCount = contestModels.size();
            contestModels = contestModels.stream().skip(pageIndex * pageSize).limit(pageSize).collect(Collectors.toList());
        } else {
            // Enable any contest
            List<String> contestJidsWhereIsManager = contestManagerDao.getContestJidsByJid(userJid);
            List<String> contestJidsWhereIsSupervisor = contestSupervisorDao.getContestJidsByJid(userJid);
            List<String> contestTeamJids = contestTeamCoachDao.getTeamJidsByJid(userJid);
            List<String> contestJidsWhereIsCoach = contestTeamDao.getContestJidsByJids(contestTeamJids);
            List<ContestModuleModel> contestRegistrationModules = contestModuleDao.getEnabledByName(ContestModules.REGISTRATION.name());

            Set<String> contestJids = new HashSet<>();
            contestJids.addAll(contestJidsWhereIsManager);
            contestJids.addAll(contestJidsWhereIsSupervisor);
            contestJids.addAll(contestJidsWhereIsContestant);
            contestJids.addAll(contestJidsWhereIsCoach);
            contestJids.addAll(contestRegistrationModules.stream().map(m -> m.contestJid).collect(Collectors.toList()));

            totalRowsCount = contestDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of(ContestModel_.jid, contestJids));
            contestModels = contestDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(ContestModel_.jid, contestJids), pageIndex * pageSize, pageSize);
        }

        List<Contest> contests = Lists.transform(contestModels, m -> createContestFromModel(contestStyleDao, m, contestModuleDao.getEnabledInContest(m.jid)));
        return new Page<>(contests, totalRowsCount, pageIndex, pageSize);
    }

    @Override
    public List<Contest> getRunningContestsWithScoreboardModule(Date timeNow) {
        List<ContestModel> contestModels = contestDao.findSortedByFilters("id", "desc", "", 0, -1);
        ImmutableList.Builder<ContestModel> runningContestModelsBuilder = ImmutableList.builder();

        for (ContestModel contestModel : contestModels) {
            if (contestModuleDao.existsEnabledInContestByName(contestModel.jid, ContestModules.SCOREBOARD.name())) {
                if (contestModuleDao.existsEnabledInContestByName(contestModel.jid, ContestModules.DURATION.name())) {
                    ContestModuleModel contestModuleModel = contestModuleDao.findInContestByName(contestModel.jid, ContestModules.DURATION.name());
                    ContestDurationModule contestDurationModule = (ContestDurationModule) ContestModuleFactory.parseFromConfig(ContestModules.DURATION, contestModuleModel.config);
                    Date currentDate = new Date();
                    if (currentDate.after(contestDurationModule.getBeginTime()) && currentDate.before(contestDurationModule.getEndTime())) {
                        runningContestModelsBuilder.add(contestModel);
                    }
                } else {
                    runningContestModelsBuilder.add(contestModel);
                }
            }
        }

        return Lists.transform(runningContestModelsBuilder.build(), m -> createContestFromModel(contestStyleDao, m, contestModuleDao.getEnabledInContest(m.jid)));
    }

    @Override
    public Contest createContest(String name, String description, ContestStyle style, String userJid, String userIpAddress) {
        ContestModel contestModel = new ContestModel();
        contestModel.name = name;
        contestModel.description = description;
        contestModel.style = style.name();

        contestDao.persist(contestModel, userJid, userIpAddress);

        ContestStyleModel contestStyleModel = new ContestStyleModel();
        contestStyleModel.contestJid = contestModel.jid;

        if (contestModel.style.equals(ContestStyle.ICPC.name())) {
            contestStyleModel.config = new Gson().toJson(ICPCContestStyleConfig.defaultConfig());
        } else if (contestModel.style.equals(ContestStyle.IOI.name())) {
            contestStyleModel.config = new Gson().toJson(IOIContestStyleConfig.defaultConfig());
        }

        contestStyleDao.persist(contestStyleModel, userJid, userIpAddress);

        createDefaultContestModules(contestModel, style, userJid, userIpAddress);

        Contest contest = createContestFromModel(contestStyleDao, contestModel, contestModuleDao.getEnabledInContest(contestModel.jid));

        return contest;
    }

    @Override
    public void updateContest(String contestJid, String name, String description, ContestStyle style, String userJid, String userIpAddress) {
        boolean isStyleChanged;

        ContestModel contestModel = contestDao.findByJid(contestJid);

        contestModel.name = name;
        contestModel.description = description;
        isStyleChanged = !contestModel.style.equals(style.name());
        contestModel.style = style.name();

        contestDao.edit(contestModel, userJid, userIpAddress);

        ContestStyleModel contestStyleModel = contestStyleDao.findInContest(contestModel.jid);
        Contest contest = createContestFromModel(contestStyleDao, contestModel, contestModuleDao.getEnabledInContest(contestModel.jid));

        if (isStyleChanged) {
            if (contestModel.style.equals(ContestStyle.ICPC.name())) {
                contestStyleModel.config = new Gson().toJson(ICPCContestStyleConfig.defaultConfig());
            } else if (contestModel.style.equals(ContestStyle.IOI.name())) {
                contestStyleModel.config = new Gson().toJson(IOIContestStyleConfig.defaultConfig());
            }

            contestStyleDao.edit(contestStyleModel, userJid, userIpAddress);

            if (contestModuleDao.existsEnabledInContestByName(contestJid, ContestModules.SCOREBOARD.name())) {
                ContestScoreboardModel contestScoreboardModel = contestScoreboardDao.findInContestByScoreboardType(contestModel.jid, ContestScoreboardType.OFFICIAL.name());
                contestScoreboardDao.remove(contestScoreboardModel);

                contestScoreboardModel = new ContestScoreboardModel();
                contestScoreboardModel.contestJid = contestModel.jid;
                contestScoreboardModel.type = ContestScoreboardType.OFFICIAL.name();

                ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(style);
                ScoreboardState state = getScoreboardStateInContest(contestModel.jid);
                ScoreboardContent content = adapter.computeScoreboardContent(contest, contestStyleModel.config, state, ImmutableList.of(), ImmutableMap.of());
                Scoreboard scoreboard = adapter.createScoreboard(state, content);

                contestScoreboardModel.scoreboard = new Gson().toJson(scoreboard);

                contestScoreboardDao.persist(contestScoreboardModel, userJid, userIpAddress);
            }

            if (contestModuleDao.existsEnabledInContestByName(contestJid, ContestModules.FROZEN_SCOREBOARD.name())) {
                ContestScoreboardModel contestScoreboardModel = contestScoreboardDao.findInContestByScoreboardType(contestModel.jid, ContestScoreboardType.FROZEN.name());
                contestScoreboardDao.remove(contestScoreboardModel);

                // TODO recompute frozen scoreboard
            }
        }
    }

    @Override
    public void updateContestStyleConfiguration(String contestJid, ContestStyleConfig styleConfig, String userJid, String userIpAddress) {
        ContestModel contestModel = contestDao.findByJid(contestJid);
        ContestStyleModel contestStyleModel = contestStyleDao.findInContest(contestJid);

        if (contestModel.style.equals(ContestStyle.ICPC.name())) {
            contestStyleModel.config = new Gson().toJson(styleConfig);
        } else if (contestModel.style.equals(ContestStyle.IOI.name())) {
            contestStyleModel.config = new Gson().toJson(styleConfig);
        }

        contestStyleDao.edit(contestStyleModel, userJid, userIpAddress);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void updateContestModuleConfiguration(String contestJid, Collection<ContestModule> contestModules, String userJid, String userIpAddress) {
        for (ContestModule contestModule : contestModules) {
            ContestModuleModel contestModuleModel = contestModuleDao.findInContestByName(contestJid, contestModule.getType().name());
            contestModuleModel.config = contestModule.toJSONString();

            contestModuleDao.edit(contestModuleModel, userJid, userIpAddress);
        }

        ContestModel contestModel = contestDao.findByJid(contestJid);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void lockContest(String contestJid, String userJid, String userIpAddress) {
        ContestModel contestModel = contestDao.findByJid(contestJid);
        contestModel.locked = true;

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void unlockContest(String contestJid, String userJid, String userIpAddress) {
        ContestModel contestModel = contestDao.findByJid(contestJid);
        contestModel.locked = false;

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    private List<ContestModuleModel> createDefaultContestModules(ContestModel contestModel, ContestStyle style, String userJid, String userIpAddress) {
        String contestJid = contestModel.jid;

        ImmutableList.Builder<ContestModuleModel> moduleModelBuilder = ImmutableList.builder();

        ContestModuleModel contestModuleModel = new ContestModuleModel();
        contestModuleModel.contestJid = contestJid;
        contestModuleModel.enabled = true;
        contestModuleModel.name = ContestModules.CLARIFICATION.name();
        contestModuleModel.config = ContestModuleFactory.createDefaultContestModule(ContestModules.CLARIFICATION).toJSONString();

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

        contestModuleModel = new ContestModuleModel();
        contestModuleModel.contestJid = contestJid;
        contestModuleModel.enabled = true;
        contestModuleModel.name = ContestModules.SUPERVISOR.name();
        contestModuleModel.config = ContestModuleFactory.createDefaultContestModule(ContestModules.SUPERVISOR).toJSONString();

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

        contestModuleModel = new ContestModuleModel();
        contestModuleModel.contestJid = contestJid;
        contestModuleModel.enabled = true;
        contestModuleModel.name = ContestModules.SCOREBOARD.name();
        contestModuleModel.config = ContestModuleFactory.createDefaultContestModule(ContestModules.SCOREBOARD).toJSONString();

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

        contestModuleModel = new ContestModuleModel();
        contestModuleModel.contestJid = contestJid;
        contestModuleModel.enabled = true;
        contestModuleModel.name = ContestModules.DURATION.name();
        contestModuleModel.config = ContestModuleFactory.createDefaultContestModule(ContestModules.DURATION).toJSONString();

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

        contestModuleModel = new ContestModuleModel();
        contestModuleModel.contestJid = contestJid;
        contestModuleModel.enabled = true;
        contestModuleModel.name = ContestModules.FILE.name();
        contestModuleModel.config = ContestModuleFactory.createDefaultContestModule(ContestModules.FILE).toJSONString();

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

        ContestScoreboardModel contestScoreboardModel = new ContestScoreboardModel();
        contestScoreboardModel.contestJid = contestModel.jid;
        contestScoreboardModel.type = ContestScoreboardType.OFFICIAL.name();

        ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(style);
        ScoreboardState config = getScoreboardStateInContest(contestModel.jid);
        ScoreboardContent content = adapter.computeScoreboardContent(createContestFromModel(contestStyleDao, contestModel, contestModuleDao.getEnabledInContest(contestModel.jid)), contestStyleDao.findInContest(contestJid).config, config, ImmutableList.of(), ImmutableMap.of());
        Scoreboard scoreboard = adapter.createScoreboard(config, content);

        contestScoreboardModel.scoreboard = new Gson().toJson(scoreboard);

        contestScoreboardDao.persist(contestScoreboardModel, userJid, userIpAddress);

        return moduleModelBuilder.build();
    }

    private static Contest createContestFromModel(ContestStyleDao contestStyleDao, ContestModel contestModel, List<ContestModuleModel> contestModuleModels) {
        ContestStyleConfig contestStyleConfig = null;
        ContestStyleModel contestStyleModel = contestStyleDao.findInContest(contestModel.jid);
        if (contestModel.style.equals(ContestStyle.ICPC.name())) {
            contestStyleConfig = new Gson().fromJson(contestStyleModel.config, ICPCContestStyleConfig.class);
        } else if (contestModel.style.equals(ContestStyle.IOI.name())) {
            contestStyleConfig = new Gson().fromJson(contestStyleModel.config, IOIContestStyleConfig.class);
        }

        Map<ContestModules, ContestModule> contestModules = Maps.newTreeMap();

        for (ContestModuleModel contestModuleModel : contestModuleModels) {
            ContestModules contestModule = ContestModules.valueOf(contestModuleModel.name);
            contestModules.put(contestModule, ContestModuleFactory.parseFromConfig(contestModule, contestModuleModel.config));
        }

        return new Contest(contestModel.id, contestModel.jid, contestModel.name, contestModel.description, contestModel.locked, ContestStyle.valueOf(contestModel.style), contestStyleConfig, ImmutableMap.copyOf(contestModules));
    }
}
