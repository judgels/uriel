package org.iatoki.judgels.uriel.contest;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestantStatus;
import org.iatoki.judgels.uriel.contest.style.ContestStyle;
import org.iatoki.judgels.uriel.contest.style.ContestStyleConfig;
import org.iatoki.judgels.uriel.contest.style.icpc.ICPCContestStyleConfig;
import org.iatoki.judgels.uriel.contest.scoreboard.ioi.IOIContestStyleConfig;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardState;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestantDao;
import org.iatoki.judgels.uriel.contest.manager.ContestManagerDao;
import org.iatoki.judgels.uriel.contest.module.ContestModuleDao;
import org.iatoki.judgels.uriel.contest.problem.ContestProblemDao;
import org.iatoki.judgels.uriel.contest.scoreboard.ContestScoreboardDao;
import org.iatoki.judgels.uriel.contest.style.ContestStyleDao;
import org.iatoki.judgels.uriel.contest.supervisor.ContestSupervisorDao;
import org.iatoki.judgels.uriel.contest.team.coach.ContestTeamCoachDao;
import org.iatoki.judgels.uriel.contest.team.ContestTeamDao;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestantModel;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestantModel_;
import org.iatoki.judgels.uriel.contest.module.ContestModuleModel;
import org.iatoki.judgels.uriel.contest.problem.ContestProblemModel;
import org.iatoki.judgels.uriel.contest.scoreboard.ContestScoreboardModel;
import org.iatoki.judgels.uriel.contest.scoreboard.ContestScoreboardModel_;
import org.iatoki.judgels.uriel.contest.style.ContestStyleModel;
import org.iatoki.judgels.uriel.contest.module.ContestModule;
import org.iatoki.judgels.uriel.contest.module.ContestModuleFactory;
import org.iatoki.judgels.uriel.contest.module.ContestModules;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
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
            Date currentDate = new Date();
            if (currentDate.after(contestModel.beginTime) && currentDate.before(new Date(contestModel.beginTime.getTime() + contestModel.duration))) {
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
                Date currentDate = new Date();
                if (currentDate.after(contestModel.beginTime) && currentDate.before(new Date(contestModel.beginTime.getTime() + contestModel.duration))) {
                    runningContestModelsBuilder.add(contestModel);
                }
            }
        }

        return Lists.transform(runningContestModelsBuilder.build(), m -> createContestFromModel(contestStyleDao, m, contestModuleDao.getEnabledInContest(m.jid)));
    }

    @Override
    public Contest createContest(String name, String description, ContestStyle style, Date beginTime, long duration, String userJid, String userIpAddress) {
        ContestModel contestModel = new ContestModel();
        contestModel.name = name;
        contestModel.description = description;
        contestModel.style = style.name();
        contestModel.beginTime = beginTime;
        contestModel.duration = duration;

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
    public void updateContest(String contestJid, String name, String description, ContestStyle style, Date beginTime, long duration, String userJid, String userIpAddress) {
        boolean isStyleChanged;

        ContestModel contestModel = contestDao.findByJid(contestJid);

        contestModel.name = name;
        contestModel.description = description;
        isStyleChanged = !contestModel.style.equals(style.name());
        contestModel.style = style.name();
        contestModel.beginTime = beginTime;
        contestModel.duration = duration;

        contestDao.edit(contestModel, userJid, userIpAddress);

        ContestStyleModel contestStyleModel = contestStyleDao.findInContest(contestModel.jid);

        if (isStyleChanged) {
            if (contestModel.style.equals(ContestStyle.ICPC.name())) {
                contestStyleModel.config = new Gson().toJson(ICPCContestStyleConfig.defaultConfig());
            } else if (contestModel.style.equals(ContestStyle.IOI.name())) {
                contestStyleModel.config = new Gson().toJson(IOIContestStyleConfig.defaultConfig());
            }

            contestStyleDao.edit(contestStyleModel, userJid, userIpAddress);

            for (ContestScoreboardModel contestScoreboardModel : contestScoreboardDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ContestScoreboardModel_.contestJid, contestModel.jid), 0, -1)) {
                contestScoreboardDao.remove(contestScoreboardModel);
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

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

        contestModuleModel = new ContestModuleModel();
        contestModuleModel.contestJid = contestJid;
        contestModuleModel.enabled = true;
        contestModuleModel.name = ContestModules.FILE.name();
        contestModuleModel.config = ContestModuleFactory.createDefaultContestModule(ContestModules.FILE).toJSONString();

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

        contestModuleModel = new ContestModuleModel();
        contestModuleModel.contestJid = contestJid;
        contestModuleModel.enabled = true;
        contestModuleModel.name = ContestModules.LIMITED.name();
        contestModuleModel.config = ContestModuleFactory.createDefaultContestModule(ContestModules.LIMITED).toJSONString();

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

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

        return new Contest(
                contestModel.id,
                contestModel.jid,
                contestModel.name,
                contestModel.description,
                contestModel.locked,
                ContestStyle.valueOf(contestModel.style),
                contestModel.beginTime,
                contestModel.duration,
                contestStyleConfig,
                ImmutableMap.copyOf(contestModules));
    }
}
