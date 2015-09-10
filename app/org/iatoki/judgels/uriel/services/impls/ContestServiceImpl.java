package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.Contest;
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
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.models.entities.ContestModel_;
import org.iatoki.judgels.uriel.models.entities.ContestModuleModel;
import org.iatoki.judgels.uriel.models.entities.ContestScoreboardModel;
import org.iatoki.judgels.uriel.models.entities.ContestStyleModel;
import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.modules.ContestModuleFactory;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.modules.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.services.ContestService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Named("contestService")
public final class ContestServiceImpl implements ContestService {

    private final ContestContestantDao contestContestantDao;
    private final ContestDao contestDao;
    private final ContestManagerDao contestManagerDao;
    private final ContestModuleDao contestModuleDao;
    private final ContestModuleFactory contestModuleFactory;
    private final ContestProblemDao contestProblemDao;
    private final ContestScoreboardDao contestScoreboardDao;
    private final ContestStyleDao contestStyleDao;
    private final ContestSupervisorDao contestSupervisorDao;
    private final ContestTeamCoachDao contestTeamCoachDao;
    private final ContestTeamDao contestTeamDao;

    @Inject
    public ContestServiceImpl(ContestContestantDao contestContestantDao, ContestDao contestDao, ContestManagerDao contestManagerDao, ContestModuleDao contestModuleDao, ContestModuleFactory contestModuleFactory, ContestProblemDao contestProblemDao, ContestScoreboardDao contestScoreboardDao, ContestStyleDao contestStyleDao, ContestSupervisorDao contestSupervisorDao, ContestTeamCoachDao contestTeamCoachDao, ContestTeamDao contestTeamDao) {
        this.contestContestantDao = contestContestantDao;
        this.contestDao = contestDao;
        this.contestManagerDao = contestManagerDao;
        this.contestModuleDao = contestModuleDao;
        this.contestModuleFactory = contestModuleFactory;
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

        return ContestServiceUtils.createContestFromModel(contestStyleDao, contestModuleFactory, contestModel, contestModuleDao.getEnabledInContest(contestModel.jid));
    }

    @Override
    public Contest findContestByJid(String contestJid) {
        ContestModel contestModel = contestDao.findByJid(contestJid);

        return ContestServiceUtils.createContestFromModel(contestStyleDao, contestModuleFactory, contestModel, contestModuleDao.getEnabledInContest(contestModel.jid));
    }

    @Override
    public ScoreboardState getScoreboardStateInContest(String contestJid) {
        return ContestScoreboardServiceUtils.getScoreboardStateInContest(contestProblemDao, contestContestantDao, contestJid);
    }

    @Override
    public Page<Contest> getPageOfContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalRowsCount = contestDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
        List<ContestModel> contestModels = contestDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<Contest> contests = Lists.transform(contestModels, m -> ContestServiceUtils.createContestFromModel(contestStyleDao, contestModuleFactory, m, contestModuleDao.getEnabledInContest(m.jid)));
        return new Page<>(contests, totalRowsCount, pageIndex, pageSize);
    }

    @Override
    public Page<Contest> getPageOfAllowedContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String userJid) {
        List<String> contestJidsWhereIsContestant = contestContestantDao.getContestJidsByJid(userJid);
        List<ContestModel> contestModels = contestDao.findSortedByFilters("id", "desc", "", ImmutableMap.of(), ImmutableMap.of(ContestModel_.jid, contestJidsWhereIsContestant), 0, -1);
        ImmutableList.Builder<ContestModel> runningContestModelsBuilder = ImmutableList.builder();

        for (ContestModel contestModel : contestModels) {
            if (contestModuleDao.existsInContestByName(contestModel.jid, ContestModules.DURATION.name())) {
                ContestModuleModel contestModuleModel = contestModuleDao.findInContestByName(contestModel.jid, ContestModules.DURATION.name());
                ContestDurationModule contestDurationModule = (ContestDurationModule) contestModuleFactory.parseFromConfig(ContestModules.DURATION, contestModuleModel.config);
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
            anyExclusiveContest = anyExclusiveContest || contestModuleDao.existsInContestByName(contestModel.jid, ContestModules.EXCLUSIVE.name());
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

        List<Contest> contests = Lists.transform(contestModels, m -> ContestServiceUtils.createContestFromModel(contestStyleDao, contestModuleFactory, m, contestModuleDao.getEnabledInContest(m.jid)));
        return new Page<>(contests, totalRowsCount, pageIndex, pageSize);
    }

    @Override
    public List<Contest> getRunningContests(Date timeNow) {
        List<ContestModel> contestModels = contestDao.findSortedByFilters("id", "desc", "", ImmutableMap.of(), ImmutableMap.of(), 0, -1);
        ImmutableList.Builder<ContestModel> runningContestModelsBuilder = ImmutableList.builder();

        for (ContestModel contestModel : contestModels) {
            if (contestModuleDao.existsInContestByName(contestModel.jid, ContestModules.DURATION.name())) {
                ContestModuleModel contestModuleModel = contestModuleDao.findInContestByName(contestModel.jid, ContestModules.DURATION.name());
                ContestDurationModule contestDurationModule = (ContestDurationModule) contestModuleFactory.parseFromConfig(ContestModules.DURATION, contestModuleModel.config);
                Date currentDate = new Date();
                if (currentDate.after(contestDurationModule.getBeginTime()) && currentDate.before(contestDurationModule.getEndTime())) {
                    runningContestModelsBuilder.add(contestModel);
                }
            } else {
                runningContestModelsBuilder.add(contestModel);
            }
        }

        return Lists.transform(runningContestModelsBuilder.build(), m -> ContestServiceUtils.createContestFromModel(contestStyleDao, contestModuleFactory, m, contestModuleDao.getEnabledInContest(m.jid)));
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

        Contest contest = ContestServiceUtils.createContestFromModel(contestStyleDao, contestModuleFactory, contestModel, contestModuleDao.getEnabledInContest(contestModel.jid));

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
        Contest contest = ContestServiceUtils.createContestFromModel(contestStyleDao, contestModuleFactory, contestModel, contestModuleDao.getEnabledInContest(contestModel.jid));

        if (isStyleChanged) {
            if (contestModel.style.equals(ContestStyle.ICPC.name())) {
                contestStyleModel.config = new Gson().toJson(ICPCContestStyleConfig.defaultConfig());
            } else if (contestModel.style.equals(ContestStyle.IOI.name())) {
                contestStyleModel.config = new Gson().toJson(IOIContestStyleConfig.defaultConfig());
            }

            contestStyleDao.edit(contestStyleModel, userJid, userIpAddress);

            try {
                ContestScoreboardModel contestScoreboardModel = contestScoreboardDao.findInContestByScoreboardType(contestModel.jid, ContestScoreboardType.OFFICIAL.name());
                contestScoreboardDao.remove(contestScoreboardModel);

                contestScoreboardModel = contestScoreboardDao.findInContestByScoreboardType(contestModel.jid, ContestScoreboardType.FROZEN.name());
                contestScoreboardDao.remove(contestScoreboardModel);
            } catch (NoResultException e) {
                throw new RuntimeException(e);
            } finally {
                // TODO recompute everything in this scoreboard include fronzen scoreboard

                ContestScoreboardModel contestScoreboardModel = new ContestScoreboardModel();
                contestScoreboardModel.contestJid = contestModel.jid;
                contestScoreboardModel.type = ContestScoreboardType.OFFICIAL.name();

                ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(style);
                ScoreboardState state = getScoreboardStateInContest(contestModel.jid);
                ScoreboardContent content = adapter.computeScoreboardContent(contest, contestStyleModel.config, state, ImmutableList.of(), ImmutableMap.of());
                Scoreboard scoreboard = adapter.createScoreboard(state, content);

                contestScoreboardModel.scoreboard = new Gson().toJson(scoreboard);

                contestScoreboardDao.persist(contestScoreboardModel, userJid, userIpAddress);
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

    private List<ContestModuleModel> createDefaultContestModules(ContestModel contestModel, ContestStyle style, String userJid, String userIpAddress) {
        String contestJid = contestModel.jid;

        ImmutableList.Builder<ContestModuleModel> moduleModelBuilder = ImmutableList.builder();

        ContestModuleModel contestModuleModel = new ContestModuleModel();
        contestModuleModel.contestJid = contestJid;
        contestModuleModel.enabled = true;
        contestModuleModel.name = ContestModules.CLARIFICATION.name();
        contestModuleModel.config = contestModuleFactory.createDefaultContestModule(ContestModules.CLARIFICATION).toJSONString();

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

        contestModuleModel = new ContestModuleModel();
        contestModuleModel.contestJid = contestJid;
        contestModuleModel.enabled = true;
        contestModuleModel.name = ContestModules.SUPERVISOR.name();
        contestModuleModel.config = contestModuleFactory.createDefaultContestModule(ContestModules.SUPERVISOR).toJSONString();

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

        contestModuleModel = new ContestModuleModel();
        contestModuleModel.contestJid = contestJid;
        contestModuleModel.enabled = true;
        contestModuleModel.name = ContestModules.SCOREBOARD.name();
        contestModuleModel.config = contestModuleFactory.createDefaultContestModule(ContestModules.SCOREBOARD).toJSONString();

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

        contestModuleModel = new ContestModuleModel();
        contestModuleModel.contestJid = contestJid;
        contestModuleModel.enabled = true;
        contestModuleModel.name = ContestModules.DURATION.name();
        contestModuleModel.config = contestModuleFactory.createDefaultContestModule(ContestModules.DURATION).toJSONString();

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

        contestModuleModel = new ContestModuleModel();
        contestModuleModel.contestJid = contestJid;
        contestModuleModel.enabled = true;
        contestModuleModel.name = ContestModules.FILE.name();
        contestModuleModel.config = contestModuleFactory.createDefaultContestModule(ContestModules.FILE).toJSONString();

        contestModuleDao.persist(contestModuleModel, userJid, userIpAddress);
        moduleModelBuilder.add(contestModuleModel);

        ContestScoreboardModel contestScoreboardModel = new ContestScoreboardModel();
        contestScoreboardModel.contestJid = contestModel.jid;
        contestScoreboardModel.type = ContestScoreboardType.OFFICIAL.name();

        ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(style);
        ScoreboardState config = getScoreboardStateInContest(contestModel.jid);
        ScoreboardContent content = adapter.computeScoreboardContent(ContestServiceUtils.createContestFromModel(contestStyleDao, contestModuleFactory, contestModel, contestModuleDao.getEnabledInContest(contestModel.jid)), contestStyleDao.findInContest(contestJid).config, config, ImmutableList.of(), ImmutableMap.of());
        Scoreboard scoreboard = adapter.createScoreboard(config, content);

        contestScoreboardModel.scoreboard = new Gson().toJson(scoreboard);

        contestScoreboardDao.persist(contestScoreboardModel, userJid, userIpAddress);

        return moduleModelBuilder.build();
    }
}
