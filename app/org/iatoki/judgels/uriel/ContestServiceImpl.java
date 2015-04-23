package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JidService;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.uriel.commons.ContestScoreState;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestAnnouncementDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestClarificationDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestConfigurationDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestManagerDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestProblemDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestReadDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestScoreboardDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestSupervisorDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestTeamCoachDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestTeamDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestTeamMemberDao;
import org.iatoki.judgels.uriel.models.domains.ContestAnnouncementModel;
import org.iatoki.judgels.uriel.models.domains.ContestClarificationModel;
import org.iatoki.judgels.uriel.models.domains.ContestConfigurationModel;
import org.iatoki.judgels.uriel.models.domains.ContestContestantModel;
import org.iatoki.judgels.uriel.models.domains.ContestManagerModel;
import org.iatoki.judgels.uriel.models.domains.ContestModel;
import org.iatoki.judgels.uriel.models.domains.ContestProblemModel;
import org.iatoki.judgels.uriel.models.domains.ContestReadModel;
import org.iatoki.judgels.uriel.models.domains.ContestScoreboardModel;
import org.iatoki.judgels.uriel.models.domains.ContestSupervisorModel;
import org.iatoki.judgels.uriel.models.domains.ContestTeamCoachModel;
import org.iatoki.judgels.uriel.models.domains.ContestTeamCoachModel_;
import org.iatoki.judgels.uriel.models.domains.ContestTeamMemberModel;
import org.iatoki.judgels.uriel.models.domains.ContestTeamModel;
import org.iatoki.judgels.uriel.models.domains.ContestTeamModel_;
import play.Play;
import play.i18n.Messages;

import javax.persistence.NoResultException;
import javax.persistence.metamodel.SingularAttribute;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ContestServiceImpl implements ContestService {

    private final ContestDao contestDao;
    private final ContestAnnouncementDao contestAnnouncementDao;
    private final ContestProblemDao contestProblemDao;
    private final ContestClarificationDao contestClarificationDao;
    private final ContestContestantDao contestContestantDao;
    private final ContestTeamDao contestTeamDao;
    private final ContestTeamCoachDao contestTeamCoachDao;
    private final ContestTeamMemberDao contestTeamMemberDao;
    private final ContestSupervisorDao contestSupervisorDao;
    private final ContestManagerDao contestManagerDao;
    private final ContestScoreboardDao contestScoreboardDao;
    private final ContestConfigurationDao contestConfigurationDao;
    private final ContestReadDao contestReadDao;
    private final FileSystemProvider teamAvatarFileProvider;

    public ContestServiceImpl(ContestDao contestDao, ContestAnnouncementDao contestAnnouncementDao, ContestProblemDao contestProblemDao, ContestClarificationDao contestClarificationDao, ContestContestantDao contestContestantDao, ContestTeamDao contestTeamDao, ContestTeamCoachDao contestTeamCoachDao, ContestTeamMemberDao contestTeamMemberDao, ContestSupervisorDao contestSupervisorDao, ContestManagerDao contestManagerDao, ContestScoreboardDao contestScoreboardDao, ContestConfigurationDao contestConfigurationDao, ContestReadDao contestReadDao, FileSystemProvider teamAvatarFileProvider) {
        this.contestDao = contestDao;
        this.contestAnnouncementDao = contestAnnouncementDao;
        this.contestProblemDao = contestProblemDao;
        this.contestClarificationDao = contestClarificationDao;
        this.contestContestantDao = contestContestantDao;
        this.contestTeamDao = contestTeamDao;
        this.contestTeamCoachDao = contestTeamCoachDao;
        this.contestTeamMemberDao = contestTeamMemberDao;
        this.contestSupervisorDao = contestSupervisorDao;
        this.contestManagerDao = contestManagerDao;
        this.contestScoreboardDao = contestScoreboardDao;
        this.contestConfigurationDao = contestConfigurationDao;
        this.contestReadDao = contestReadDao;
        this.teamAvatarFileProvider = teamAvatarFileProvider;
        if (!teamAvatarFileProvider.fileExists(ImmutableList.of("team-default.png"))) {
            try {
                teamAvatarFileProvider.uploadFile(ImmutableList.of(), play.api.Play.getFile("default-assets/team-default.png", play.api.Play.current()), "team-default.png");
                teamAvatarFileProvider.makeFilePublic(ImmutableList.of("team-default.png"));
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create default avatar.");
            }
        }
    }

    @Override
    public Contest findContestById(long contestId) {
        ContestModel contestModel = contestDao.findById(contestId);
        return createContestFromModel(contestModel);
    }

    @Override
    public Contest findContestByJid(String contestJid) {
        ContestModel contestModel = contestDao.findByJid(contestJid);
        return createContestFromModel(contestModel);
    }

    @Override
    public Contest createContest(String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isExclusive, boolean isUsingScoreboard, boolean isIncognitoScoreboard) {
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

        contestDao.persist(contestModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        ContestScoreboardModel contestScoreboardModel = new ContestScoreboardModel();
        contestScoreboardModel.contestJid = contestModel.jid;
        contestScoreboardModel.type = ContestScoreboardType.OFFICIAL.name();

        ScoreAdapter adapter = ScoreAdapters.fromContestStyle(style);
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
    public void updateContest(long contestId, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isExclusive, boolean isUsingScoreboard, boolean isIncognitoScoreboard) {
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

            } finally {
                // TODO recompute everything in this fucking scoreboard include fronzen scoreboard

                ContestScoreboardModel contestScoreboardModel = new ContestScoreboardModel();
                contestScoreboardModel.contestJid = contestModel.jid;
                contestScoreboardModel.type = ContestScoreboardType.OFFICIAL.name();

                ScoreAdapter adapter = ScoreAdapters.fromContestStyle(style);
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
    public Page<Contest> pageAllowedContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String userJid, boolean isAdmin) {
        if (isAdmin) {
            long totalRowsCount = contestDao.countByFilters(filterString, ImmutableMap.of());
            List<ContestModel> contestModels = contestDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), pageIndex * pageSize, pageSize);

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

                List<Contest> contests = Lists.transform(contestModels, m -> createContestFromModel(m));
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
    public boolean isThereNewProblemsOrContestants(String contestJid, long lastTime) {
        return (contestProblemDao.isThereNewProblem(contestJid, lastTime)) || (contestContestantDao.isThereNewContestant(contestJid, lastTime));
    }

    @Override
    public ContestScoreState getContestStateByJid(String contestJid) {
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findOpenedByContestJidOrderedByAlias(contestJid);
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFilters("id", "asc", "", ImmutableMap.of("contestJid", contestJid, "status", ContestContestantStatus.APPROVED.name()), 0, -1);

        List<String> problemJids = Lists.transform(contestProblemModels, m -> m.problemJid);
        List<String> problemAliases = Lists.transform(contestProblemModels, m -> m.alias);
        List<String> contestantJids = Lists.transform(contestContestantModels, m -> m.userJid);

        return new ContestScoreState(problemJids, problemAliases, contestantJids);
    }

    @Override
    public List<Contest> getRunningContests(Date timeNow) {
        List<ContestModel> contestModels = contestDao.getRunningContests(timeNow.getTime());
        return Lists.transform(contestModels, m -> createContestFromModel(m));
    }

    @Override
    public Page<ContestAnnouncement> pageContestAnnouncementsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status) {
        ImmutableMap.Builder<String, String> filterColumnsBuilder = ImmutableMap.builder();
        filterColumnsBuilder.put("contestJid", contestJid);
        if (status != null) {
            filterColumnsBuilder.put("status", status);
        }
        Map<String, String> filterColumns = filterColumnsBuilder.build();

        long totalPages = contestAnnouncementDao.countByFilters(filterString, filterColumns);
        List<ContestAnnouncementModel> contestAnnouncementModels = contestAnnouncementDao.findSortedByFilters(orderBy, orderDir, filterString, filterColumns, pageIndex, pageIndex * pageSize);
        List<ContestAnnouncement> contestAnnouncements = Lists.transform(contestAnnouncementModels, m -> createContestAnnouncementFromModel(m));

        return new Page<>(contestAnnouncements, totalPages, pageIndex, pageSize);
    }

    @Override
    public ContestAnnouncement findContestAnnouncementByContestAnnouncementId(long contestAnnouncementId) {
        ContestAnnouncementModel contestAnnouncementModel = contestAnnouncementDao.findById(contestAnnouncementId);
        return createContestAnnouncementFromModel(contestAnnouncementModel);
    }

    @Override
    public void createContestAnnouncement(long contestId, String title, String content, ContestAnnouncementStatus status) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestAnnouncementModel contestAnnouncementModel = new ContestAnnouncementModel();
        contestAnnouncementModel.contestJid = contestModel.jid;
        contestAnnouncementModel.title = title;
        contestAnnouncementModel.content = content;
        contestAnnouncementModel.status = status.name();

        contestAnnouncementDao.persist(contestAnnouncementModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestAnnouncement(long contestAnnouncementId, String title, String content, ContestAnnouncementStatus status) {
        ContestAnnouncementModel contestAnnouncementModel = contestAnnouncementDao.findById(contestAnnouncementId);
        contestAnnouncementModel.title = title;
        contestAnnouncementModel.content = content;
        contestAnnouncementModel.status = status.name();

        contestAnnouncementDao.edit(contestAnnouncementModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public List<ContestProblem> findOpenedContestProblemByContestJid(String contestJid) {
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findOpenedByContestJidOrderedByAlias(contestJid);
        return Lists.transform(contestProblemModels, m -> createContestProblemFromModel(m));
    }

    @Override
    public Map<String, String> findProblemJidToAliasMapByContestJid(String contestJid) {
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findByContestJid(contestJid);

        Map<String, String> map = Maps.newLinkedHashMap();

        for (ContestProblemModel model : contestProblemModels) {
            map.put(model.problemJid, model.alias);
        }

        return map;
    }

    @Override
    public Page<ContestProblem> pageContestProblemsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status) {
        ImmutableMap.Builder<String, String> filterColumnsBuilder = ImmutableMap.builder();
        filterColumnsBuilder.put("contestJid", contestJid);
        if (status != null) {
            filterColumnsBuilder.put("status", status);
        }
        Map<String, String> filterColumns = filterColumnsBuilder.build();

        long totalPages = contestProblemDao.countByFilters(filterString, filterColumns);
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findSortedByFilters(orderBy, orderDir, filterString, filterColumns, pageIndex * pageSize, pageSize);
        List<ContestProblem> contestProblems = Lists.transform(contestProblemModels, m -> createContestProblemFromModel(m));

        return new Page<>(contestProblems, totalPages, pageIndex, pageSize);
    }

    @Override
    public ContestProblem findContestProblemByContestProblemId(long contestProblemId) {
        ContestProblemModel contestProblemModel = contestProblemDao.findById(contestProblemId);
        return createContestProblemFromModel(contestProblemModel);
    }

    @Override
    public ContestProblem findContestProblemByContestJidAndContestProblemJid(String contestJid, String contestProblemJid) {
        ContestProblemModel contestProblemModel = contestProblemDao.findByProblemJidOrderedByAlias(contestJid, contestProblemJid);
        return createContestProblemFromModel(contestProblemModel);
    }

    @Override
    public boolean isContestProblemInContestByProblemJidOrAlias(String contestJid, String contestProblemJid, String contestProblemAlias) {
        return ((contestProblemDao.existsByProblemJid(contestJid, contestProblemJid)) || (contestProblemDao.existsByProblemAlias(contestJid, contestProblemAlias)));
    }

    @Override
    public void createContestProblem(long contestId, String problemJid, String problemSecret, String alias, long submissionsLimit, ContestProblemStatus status) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestProblemModel contestProblemModel = new ContestProblemModel();
        contestProblemModel.contestJid = contestModel.jid;
        contestProblemModel.problemJid = problemJid;
        contestProblemModel.problemSecret = problemSecret;
        contestProblemModel.alias = alias;
        contestProblemModel.submissionsLimit = submissionsLimit;
        contestProblemModel.status = status.name();

        contestProblemDao.persist(contestProblemModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestProblem(long contestProblemId, String problemSecret, String alias, long submissionsLimit, ContestProblemStatus status) {
        ContestProblemModel contestProblemModel = contestProblemDao.findById(contestProblemId);
        contestProblemModel.problemSecret = problemSecret;
        contestProblemModel.alias = alias;
        contestProblemModel.submissionsLimit = submissionsLimit;
        contestProblemModel.status = status.name();

        contestProblemDao.edit(contestProblemModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public Page<ContestClarification> pageContestClarificationsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, List<String> askerJids) {
        ContestModel contestModel = contestDao.findByJid(contestJid);

        if (askerJids == null) {
            ImmutableMap.Builder<String, String> filterColumnsBuilder = ImmutableMap.builder();
            filterColumnsBuilder.put("contestJid", contestJid);
            Map<String, String> filterColumns = filterColumnsBuilder.build();

            long totalPages = contestClarificationDao.countByFilters(filterString, filterColumns);
            List<ContestClarificationModel> contestClarificationModels = contestClarificationDao.findSortedByFilters(orderBy, orderDir, filterString, filterColumns, pageIndex * pageSize, pageSize);
            List<ContestClarification> contestClarifications = Lists.transform(contestClarificationModels, m -> createContestClarificationFromModel(m, contestModel));

            return new Page<>(contestClarifications, totalPages, pageIndex, pageSize);
        } else {
            long totalPages = contestClarificationDao.countClarificationsByContestJidAskedByUserJids(contestModel.jid, askerJids);
            List<ContestClarificationModel> contestClarificationModels = contestClarificationDao.findClarificationsByContestJidAskedByUserJids(contestModel.jid, askerJids);
            List<ContestClarification> contestClarifications = Lists.transform(contestClarificationModels, m -> createContestClarificationFromModel(m, contestModel));

            return new Page<>(contestClarifications, totalPages, pageIndex, pageSize);
        }
    }

    @Override
    public ContestClarification findContestClarificationByContestClarificationId(long contestClarificationId) {
        ContestClarificationModel contestClarificationModel = contestClarificationDao.findById(contestClarificationId);
        ContestModel contestModel = contestDao.findByJid(contestClarificationModel.contestJid);

        return createContestClarificationFromModel(contestClarificationModel, contestModel);
    }

    @Override
    public void createContestClarification(long contestId, String title, String question, String topicJid) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestClarificationModel contestClarificationModel = new ContestClarificationModel();
        contestClarificationModel.contestJid = contestModel.jid;
        contestClarificationModel.title = title;
        contestClarificationModel.question = question;
        contestClarificationModel.topicJid = topicJid;
        contestClarificationModel.status = ContestClarificationStatus.ASKED.name();

        contestClarificationDao.persist(contestClarificationModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestClarification(long contestClarificationId, String title, String question) {
        ContestClarificationModel contestClarificationModel = contestClarificationDao.findById(contestClarificationId);
        contestClarificationModel.title = title;
        contestClarificationModel.question = question;

        contestClarificationDao.edit(contestClarificationModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestClarification(long contestClarificationId, String answer, ContestClarificationStatus status) {
        ContestClarificationModel contestClarificationModel = contestClarificationDao.findById(contestClarificationId);
        contestClarificationModel.answer = answer;
        contestClarificationModel.status = status.name();

        contestClarificationDao.edit(contestClarificationModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public long getUnansweredContestClarificationsCount(String contestJid) {
        return contestClarificationDao.countUnansweredClarificationByContestJid(contestJid);
    }

    @Override
    public List<ContestContestant> findAllContestContestantsByContestJid(String contestJid) {
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFilters("id", "asc", "", ImmutableMap.of("contestJid", contestJid), 0, -1);
        return Lists.transform(contestContestantModels, m -> createContestContestantFromModel(m));
    }

    @Override
    public Page<ContestContestant> pageContestContestantsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestContestantDao.countByFilters(filterString, ImmutableMap.of("contestJid", contestJid));
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of("contestJid", contestJid), pageIndex * pageSize, pageSize);

        List<ContestContestant> contestContestants = Lists.transform(contestContestantModels, m -> createContestContestantFromModel(m));

        return new Page<>(contestContestants, totalPages, pageIndex, pageSize);
    }

    @Override
    public ContestContestant findContestContestantByContestContestantId(long contestContestantId) {
        ContestContestantModel contestContestantModel = contestContestantDao.findById(contestContestantId);
        return createContestContestantFromModel(contestContestantModel);
    }

    @Override
    public ContestContestant findContestContestantByContestJidAndContestContestantJid(String contestJid, String contestContestantJid) {
        ContestContestantModel contestContestantModel = contestContestantDao.findByContestJidAndContestantJid(contestJid, contestContestantJid);
        return createContestContestantFromModel(contestContestantModel);
    }

    @Override
    public boolean isContestContestantInContestByUserJid(String contestJid, String contestContestantJid) {
        return contestContestantDao.existsByContestJidAndContestantJid(contestJid, contestContestantJid);
    }

    @Override
    public void createContestContestant(long contestId, String userJid, ContestContestantStatus status) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestContestantModel contestContestantModel = new ContestContestantModel();
        contestContestantModel.contestJid = contestModel.jid;
        contestContestantModel.userJid = userJid;
        contestContestantModel.status = status.name();

        contestContestantDao.persist(contestContestantModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestContestant(long contestContestantId, ContestContestantStatus status) {
        ContestContestantModel contestContestantModel = contestContestantDao.findById(contestContestantId);
        contestContestantModel.status = status.name();

        contestContestantDao.edit(contestContestantModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public long getContestContestantCount(String contestJid) {
        return contestContestantDao.countContestContestantByContestJid(contestJid);
    }

    @Override
    public void startContestAsContestant(String contestJid, String userJid) {
        ContestContestantModel contestContestantModel = contestContestantDao.findByContestJidAndContestantJid(contestJid, userJid);
        if (contestContestantModel.contestStartTime == 0) {
            contestContestantModel.contestStartTime = System.currentTimeMillis();

            contestContestantDao.edit(contestContestantModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public void startTeamAsCoach(String contestJid, String teamJid) {
        long now = System.currentTimeMillis();
        ContestTeamModel contestTeamModel = contestTeamDao.findByJid(teamJid);
        contestTeamModel.contestStartTime = now;
        contestTeamDao.edit(contestTeamModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.findContestTeamMembersInTeam(teamJid);

        for (ContestTeamMemberModel contestTeamMemberModel : contestTeamMemberModels) {
            ContestContestantModel contestContestantModel = contestContestantDao.findByContestJidAndContestantJid(contestJid, contestTeamMemberModel.memberJid);
            contestContestantModel.contestStartTime = now;

            contestContestantDao.edit(contestContestantModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public boolean isContestStarted(String contestJid, String contestContestantJid) {
        return contestContestantDao.isContestStarted(contestJid, contestContestantJid);
    }

    @Override
    public Page<ContestTeam> pageContestTeamsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestTeamDao.countByFilters(filterString, ImmutableMap.of(ContestTeamModel_.contestJid, contestJid), ImmutableMap.of());
        List<ContestTeamModel> contestTeamModels = contestTeamDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(ContestTeamModel_.contestJid, contestJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<ContestTeam> contestTeamBuilder = ImmutableList.builder();
        for (ContestTeamModel contestTeamModel : contestTeamModels) {
            List<ContestTeamCoachModel> contestTeamCoachesModel = contestTeamCoachDao.findContestTeamCoachesByTeamJid(contestTeamModel.jid);
            List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.findContestTeamMembersInTeam(contestTeamModel.jid);

            contestTeamBuilder.add(createContestTeamFromModel(contestTeamModel, contestTeamCoachesModel, contestTeamMemberModels));
        }
        return new Page<>(contestTeamBuilder.build(), totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<ContestTeam> pageContestTeamsByContestJidAndCoachJid(String contestJid, String coachJid, long pageIndex, long pageSize, String orderBy, String orderDir) {
        List<String> teamJidsInContest = contestTeamDao.findTeamJidsByContestJid(contestJid);

        Map<SingularAttribute<? super ContestTeamCoachModel, String>, String> filterColumns = ImmutableMap.of(ContestTeamCoachModel_.coachJid, coachJid);
        Map<SingularAttribute<? super ContestTeamCoachModel, String>, List<String>> filterColumnsIn = ImmutableMap.of(ContestTeamCoachModel_.teamJid, teamJidsInContest);

        long totalRows = contestTeamCoachDao.countByFilters("", filterColumns, filterColumnsIn);
        List<ContestTeamCoachModel> contestTeamCoachModels = contestTeamCoachDao.findSortedByFilters(orderBy, orderDir, "", filterColumns, filterColumnsIn, pageIndex * pageSize, pageSize);

        List<String> teamJids = Lists.transform(contestTeamCoachModels, m -> m.teamJid);
        List<ContestTeamModel> contestTeamModels = contestTeamDao.findByJids(teamJids);

        ImmutableList.Builder<ContestTeam> contestTeamBuilder = ImmutableList.builder();
        for (ContestTeamModel contestTeamModel : contestTeamModels) {
            List<ContestTeamCoachModel> contestTeamCoachesModel = contestTeamCoachDao.findContestTeamCoachesByTeamJid(contestTeamModel.jid);
            List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.findContestTeamMembersInTeam(contestTeamModel.jid);

            contestTeamBuilder.add(createContestTeamFromModel(contestTeamModel, contestTeamCoachesModel, contestTeamMemberModels));
        }
        return new Page<>(contestTeamBuilder.build(), totalRows, pageIndex, pageSize);
    }

    @Override
    public List<ContestTeam> findAllContestTeams(String contestJid) {
        List<ContestTeamModel> contestTeamModels = contestTeamDao.findSortedByFilters("id", "asc", "", ImmutableMap.of(ContestTeamModel_.contestJid, contestJid), ImmutableMap.of(), 0, -1);

        ImmutableList.Builder<ContestTeam> contestTeamBuilder = ImmutableList.builder();
        for (ContestTeamModel contestTeamModel : contestTeamModels) {
            List<ContestTeamCoachModel> contestTeamCoachesModel = contestTeamCoachDao.findContestTeamCoachesByTeamJid(contestTeamModel.jid);
            List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.findContestTeamMembersInTeam(contestTeamModel.jid);

            contestTeamBuilder.add(createContestTeamFromModel(contestTeamModel, contestTeamCoachesModel, contestTeamMemberModels));
        }

        return contestTeamBuilder.build();
    }

    @Override
    public ContestTeam findContestTeamByContestTeamId(long contestTeamId) {
        ContestTeamModel contestTeamModel = contestTeamDao.findById(contestTeamId);
        List<ContestTeamCoachModel> contestTeamCoachesModel = contestTeamCoachDao.findContestTeamCoachesByTeamJid(contestTeamModel.jid);
        List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.findContestTeamMembersInTeam(contestTeamModel.jid);

        return createContestTeamFromModel(contestTeamModel, contestTeamCoachesModel, contestTeamMemberModels);
    }

    @Override
    public void createContestTeam(long contestId, String name) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestTeamModel contestTeamModel = new ContestTeamModel();
        contestTeamModel.contestJid = contestModel.jid;
        contestTeamModel.name = name;
        contestTeamModel.teamImageName = "team-default.png";

        contestTeamDao.persist(contestTeamModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void createContestTeam(long contestId, String name, File teamImage, String extension) throws IOException {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestTeamModel contestTeamModel = new ContestTeamModel();
        contestTeamModel.contestJid = contestModel.jid;
        contestTeamModel.name = name;
        contestTeamModel.teamImageName = "team-default.png";

        contestTeamDao.persist(contestTeamModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        String newImageName = contestTeamModel.jid + "-" + JudgelsUtils.hashMD5(UUID.randomUUID().toString()) + "." + extension;
        teamAvatarFileProvider.uploadFile(ImmutableList.of(), teamImage, newImageName);

        contestTeamModel.teamImageName = newImageName;

        contestTeamDao.edit(contestTeamModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestTeam(long contestTeamId, String name) {
        ContestTeamModel contestTeamModel = contestTeamDao.findById(contestTeamId);
        contestTeamModel.name = name;

        contestTeamDao.edit(contestTeamModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestTeam(long contestTeamId, String name, File teamImage, String extension) throws IOException {
        ContestTeamModel contestTeamModel = contestTeamDao.findById(contestTeamId);
        String newImageName = contestTeamModel.jid + "-" + JudgelsUtils.hashMD5(UUID.randomUUID().toString()) + "." + extension;
        teamAvatarFileProvider.uploadFile(ImmutableList.of(), teamImage, newImageName);

        contestTeamModel.name = name;
        contestTeamModel.teamImageName = newImageName;

        contestTeamDao.edit(contestTeamModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public boolean isUserInAnyTeamByContestJid(String contestJid, String userJid) {
        List<String> teamJids = contestTeamDao.findTeamJidsByContestJid(contestJid);

        return ((contestTeamCoachDao.isUserRegisteredAsCoachInTeams(userJid, teamJids)) || (contestTeamMemberDao.isUserRegisteredAsMemberInAnyTeam(userJid, teamJids)));
    }

    @Override
    public boolean isUserCoachInAnyTeamByContestJid(String contestJid, String coachJid) {
        List<String> teamJids = contestTeamDao.findTeamJidsByContestJid(contestJid);

        return (contestTeamCoachDao.isUserRegisteredAsCoachInTeams(coachJid, teamJids));
    }

    @Override
    public boolean isUserCoachByUserJidAndTeamJid(String userJid, String teamJid) {
        return contestTeamCoachDao.isUserCoachByUserJidAndTeamJid(userJid, teamJid);
    }

    @Override
    public List<ContestTeam> findContestTeamsByContestJidAndCoachJid(String contestJid, String coachJid) {
        List<String> teamJidsInContest = contestTeamDao.findTeamJidsByContestJid(contestJid);
        List<ContestTeamCoachModel> coaches = contestTeamCoachDao.findContestTeamCoachesByCoachJidInTeams(coachJid, teamJidsInContest);

        List<ContestTeamModel> teamModels = contestTeamDao.findByJids(Lists.transform(coaches, m -> m.teamJid));

        ImmutableList.Builder<ContestTeam> teams = ImmutableList.builder();
        for (ContestTeamModel teamModel : teamModels) {
            List<ContestTeamCoachModel> contestTeamCoachesModel = contestTeamCoachDao.findContestTeamCoachesByTeamJid(teamModel.jid);
            List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.findContestTeamMembersInTeam(teamModel.jid);

            teams.add(createContestTeamFromModel(teamModel, contestTeamCoachesModel, contestTeamMemberModels));
        }

        return teams.build();
    }

    @Override
    public ContestTeamCoach findContestTeamCoachByContestTeamCoachId(long contestTeamCoachId) {
        return createContestTeamCoachFromModel(contestTeamCoachDao.findById(contestTeamCoachId));
    }

    @Override
    public List<ContestTeamCoach> findContestTeamCoachesByTeamJid(String contestTeamJid) {
        List<ContestTeamCoachModel> contestTeamCoachModels = contestTeamCoachDao.findContestTeamCoachesByTeamJid(contestTeamJid);

        return Lists.transform(contestTeamCoachModels, m -> createContestTeamCoachFromModel(m));
    }

    @Override
    public void createContestTeamCoach(String contestTeamJid, String coachJid) {
        ContestTeamCoachModel contestTeamCoachModel = new ContestTeamCoachModel();
        contestTeamCoachModel.teamJid = contestTeamJid;
        contestTeamCoachModel.coachJid = coachJid;

        contestTeamCoachDao.persist(contestTeamCoachModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void removeContestTeamCoachByContestTeamCoachId(long contestTeamCoachId) {
        ContestTeamCoachModel contestTeamCoachModel = contestTeamCoachDao.findById(contestTeamCoachId);
        contestTeamCoachDao.remove(contestTeamCoachModel);
    }

    @Override
    public ContestTeamMember findContestTeamMemberByContestTeamMemberId(long contestTeamMemberId) {
        return createContestTeamMemberFromModel(contestTeamMemberDao.findById(contestTeamMemberId));
    }

    @Override
    public List<ContestTeamMember> findContestTeamMembersByContestJidAndCoachJid(String contestJid, String coachJid) {
        List<String> teamJids = contestTeamDao.findTeamJidsByContestJid(contestJid);
        List<ContestTeamCoachModel> contestTeamCoachModels = contestTeamCoachDao.findContestTeamCoachesByCoachJidInTeams(coachJid, teamJids);

        List<ContestTeamModel> contestTeamModels = contestTeamDao.findByJids(Lists.transform(contestTeamCoachModels, m -> m.teamJid));

        List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.findContestTeamMembersInTeams(Lists.transform(contestTeamModels, m -> m.jid));

        return Lists.transform(contestTeamMemberModels, m -> createContestTeamMemberFromModel(m));
    }

    @Override
    public List<ContestTeamMember> findContestTeamMembersByTeamJid(String contestTeamJid) {
        List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.findContestTeamMembersInTeam(contestTeamJid);

        return Lists.transform(contestTeamMemberModels, m -> createContestTeamMemberFromModel(m));
    }

    @Override
    public void createContestTeamMember(String contestTeamJid, String memberJid) {
        ContestTeamMemberModel contestTeamMemberModel = new ContestTeamMemberModel();
        contestTeamMemberModel.teamJid = contestTeamJid;
        contestTeamMemberModel.memberJid = memberJid;

        contestTeamMemberDao.persist(contestTeamMemberModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void removeContestTeamMemberByContestTeamMemberId(long contestTeamMemberId) {
        ContestTeamMemberModel contestTeamMemberModel = contestTeamMemberDao.findById(contestTeamMemberId);
        contestTeamMemberDao.remove(contestTeamMemberModel);
    }

    @Override
    public ContestSupervisor findContestSupervisorByContestJidAndUserJid(String contestJid, String userJid) {
        ContestSupervisorModel contestSupervisorModel = contestSupervisorDao.findByContestJidAndSupervisorJid(contestJid, userJid);

        return createContestSupervisorFromModel(contestSupervisorModel);
    }

    @Override
    public Page<ContestSupervisor> pageContestSupervisorsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestSupervisorDao.countByFilters(filterString, ImmutableMap.of("contestJid", contestJid));
        List<ContestSupervisorModel> contestSupervisorModels = contestSupervisorDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of("contestJid", contestJid), pageIndex * pageSize, pageSize);
        List<ContestSupervisor> contestSupervisors = Lists.transform(contestSupervisorModels, m -> createContestSupervisorFromModel(m));
        return new Page<>(contestSupervisors, totalPages, pageIndex, pageSize);
    }

    @Override
    public ContestSupervisor findContestSupervisorByContestSupervisorId(long contestSupervisorId) {
        ContestSupervisorModel contestSupervisorModel = contestSupervisorDao.findById(contestSupervisorId);

        return createContestSupervisorFromModel(contestSupervisorModel);
    }

    @Override
    public boolean isContestSupervisorInContestByUserJid(String contestJid, String contestSupervisorJid) {
        return contestSupervisorDao.existsByContestJidAndSupervisorJid(contestJid, contestSupervisorJid);
    }

    @Override
    public void createContestSupervisor(long contestId, String userJid, boolean announcement, boolean problem, boolean submission, boolean clarification, boolean contestant) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestSupervisorModel contestSupervisorModel = new ContestSupervisorModel();
        contestSupervisorModel.contestJid = contestModel.jid;
        contestSupervisorModel.userJid = userJid;
        contestSupervisorModel.announcement = announcement;
        contestSupervisorModel.problem = problem;
        contestSupervisorModel.submission = submission;
        contestSupervisorModel.clarification = clarification;
        contestSupervisorModel.contestant = contestant;

        contestSupervisorDao.persist(contestSupervisorModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestSupervisor(long contestSupervisorId, boolean announcement, boolean problem, boolean submission, boolean clarification, boolean contestant) {
        ContestSupervisorModel contestSupervisorModel = contestSupervisorDao.findById(contestSupervisorId);
        contestSupervisorModel.announcement = announcement;
        contestSupervisorModel.problem = problem;
        contestSupervisorModel.submission = submission;
        contestSupervisorModel.clarification = clarification;
        contestSupervisorModel.contestant = contestant;

        contestSupervisorDao.edit(contestSupervisorModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public Page<ContestManager> pageContestManagersByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestManagerDao.countByFilters(filterString, ImmutableMap.of("contestJid", contestJid));
        List<ContestManagerModel> contestManagerModels = contestManagerDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of("contestJid", contestJid), pageIndex * pageSize, pageSize);
        List<ContestManager> contestManagers = Lists.transform(contestManagerModels, m -> createContestManagerFromModel(m));

        return new Page<>(contestManagers, totalPages, pageIndex, pageSize);
    }

    @Override
    public ContestManager findContestManagerByContestManagerId(long contestManagerId) {
        ContestManagerModel contestManagerModel = contestManagerDao.findById(contestManagerId);
        return createContestManagerFromModel(contestManagerModel);
    }

    @Override
    public boolean isContestManagerInContestByUserJid(String contestJid, String contestManagerJid) {
        return contestManagerDao.existsByContestJidAndManagerJid(contestJid, contestManagerJid);
    }

    @Override
    public void createContestManager(long contestId, String userJid) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestManagerModel contestManagerModel = new ContestManagerModel();
        contestManagerModel.contestJid = contestModel.jid;
        contestManagerModel.userJid = userJid;

        contestManagerDao.persist(contestManagerModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public long getUnreadContestAnnouncementsCount(String userJid, String contestJid) {
        List<Long> announcementIds = contestAnnouncementDao.findAllPublishedAnnouncementIdInContest(contestJid);
        if (!announcementIds.isEmpty()) {
            return (announcementIds.size() - contestReadDao.countReadByUserJidAndTypeAndIdList(userJid, ContestReadType.ANNOUNCEMENT.name(), announcementIds));
        } else {
            return 0;
        }
    }

    @Override
    public long getUnreadContestClarificationsCount(List<String> askerJids, String userJid, String contestJid, boolean answered) {
        List<Long> clarificationIds;

        if (answered) {
            clarificationIds = contestClarificationDao.findAllAnsweredClarificationIdsInContestByUserJids(contestJid, askerJids);
        } else {
            clarificationIds = contestClarificationDao.findClarificationIdsByContestJidAskedByUserJids(contestJid, askerJids);
        }

        if (!clarificationIds.isEmpty()) {
            return (clarificationIds.size() - contestReadDao.countReadByUserJidAndTypeAndIdList(userJid, ContestReadType.CLARIFICATION.name(), clarificationIds));
        } else {
            return 0;
        }
    }

    @Override
    public void readContestAnnouncements(String userJid, List<Long> contestAnnouncementIds) {
        for (Long contestAnnouncementId : contestAnnouncementIds) {
            if (!contestReadDao.existByUserJidAndTypeAndId(userJid, ContestReadType.ANNOUNCEMENT.name(), contestAnnouncementId)) {
                ContestReadModel contestReadModel = new ContestReadModel();
                contestReadModel.userJid = userJid;
                contestReadModel.type = ContestReadType.ANNOUNCEMENT.name();
                contestReadModel.readId = contestAnnouncementId;

                contestReadDao.persist(contestReadModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            }
        }
    }

    @Override
    public void readContestClarifications(String userJid, List<Long> contestClarificationIds) {
        for (Long contestClarificationId : contestClarificationIds) {
            if (!contestReadDao.existByUserJidAndTypeAndId(userJid, ContestReadType.CLARIFICATION.name(), contestClarificationId)) {
                ContestReadModel contestReadModel = new ContestReadModel();
                contestReadModel.userJid = userJid;
                contestReadModel.type = ContestReadType.CLARIFICATION.name();
                contestReadModel.readId = contestClarificationId;

                contestReadDao.persist(contestReadModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            }
        }
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
    public Map<String, URL> getMapContestantJidToImageUrlInContest(String contestJid) {
        ImmutableMap.Builder<String, URL> resultBuilder = ImmutableMap.builder();

        List<ContestTeamModel> contestTeamModels = contestTeamDao.findContestTeamModelsByContestJid(contestJid);
        ImmutableMap.Builder<String, ContestTeamModel> contestTeamModelBuilder = ImmutableMap.builder();
        for (ContestTeamModel contestTeamModel : contestTeamModels) {
            contestTeamModelBuilder.put(contestTeamModel.jid, contestTeamModel);
        }
        Map<String, ContestTeamModel> contestTeamModelMap = contestTeamModelBuilder.build();

        List<String> contestTeamJids = contestTeamModels.stream().map(ct -> ct.jid).collect(Collectors.toList());
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFilters("id", "asc", "", ImmutableMap.of("contestJid", contestJid), 0, -1);

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
    public void updateContestScoreboardByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type, Scoreboard scoreboard) {
        try {
            ContestScoreboardModel contestScoreboardModel = contestScoreboardDao.findContestScoreboardByContestJidAndScoreboardType(contestJid, type.name());
            contestScoreboardModel.scoreboard = new Gson().toJson(scoreboard);

            contestScoreboardDao.edit(contestScoreboardModel, "scoreboardUpdater", "localhost");
        } catch (NoResultException e) {
            // just do nothing
        }
    }

    @Override
    public ContestConfiguration findContestConfigurationByContestJid(String contestJid) {
        ContestConfigurationModel contestConfigurationModel = contestConfigurationDao.findByContestJid(contestJid);

        return new ContestConfiguration(contestConfigurationModel.id, contestConfigurationModel.contestJid, contestConfigurationModel.typeConfig, contestConfigurationModel.scopeConfig, contestConfigurationModel.styleConfig);
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

    @Override
    public String getTeamAvatarImageURL(String imageName) {
        return teamAvatarFileProvider.getURL(ImmutableList.of(imageName));
    }

    private Contest createContestFromModel(ContestModel contestModel) {
        return new Contest(contestModel.id, contestModel.jid, contestModel.name, contestModel.description, ContestType.valueOf(contestModel.type), ContestScope.valueOf(contestModel.scope), ContestStyle.valueOf(contestModel.style), new Date(contestModel.startTime), new Date(contestModel.endTime), new Date(contestModel.clarificationEndTime), contestModel.isExclusive, contestModel.isUsingScoreboard, contestModel.isIncognitoScoreboard);
    }

    private ContestAnnouncement createContestAnnouncementFromModel(ContestAnnouncementModel contestAnnouncementModel) {
        return new ContestAnnouncement(contestAnnouncementModel.id, contestAnnouncementModel.contestJid, contestAnnouncementModel.title, contestAnnouncementModel.content, contestAnnouncementModel.userCreate, ContestAnnouncementStatus.valueOf(contestAnnouncementModel.status), new Date(contestAnnouncementModel.timeUpdate));
    }

    private ContestProblem createContestProblemFromModel(ContestProblemModel contestProblemModel) {
        return new ContestProblem(contestProblemModel.id, contestProblemModel.contestJid, contestProblemModel.problemJid, contestProblemModel.problemSecret, contestProblemModel.alias, contestProblemModel.submissionsLimit, ContestProblemStatus.valueOf(contestProblemModel.status));
    }

    private ContestClarification createContestClarificationFromModel(ContestClarificationModel contestClarificationModel, ContestModel contestModel) {
        String topic;
        if ("CONT".equals(JidService.getInstance().parsePrefix(contestClarificationModel.topicJid))) {
            topic = "(" + Messages.get("clarification.general") + ")";
        } else {
            ContestProblemModel contestProblemModel = contestProblemDao.findByProblemJidOrderedByAlias(contestModel.jid, contestClarificationModel.topicJid);
            topic = contestProblemModel.alias + " - " + JidCacheService.getInstance().getDisplayName(contestProblemModel.problemJid);
        }
        return new ContestClarification(contestClarificationModel.id, contestClarificationModel.contestJid, topic, contestClarificationModel.title, contestClarificationModel.question, contestClarificationModel.answer, contestClarificationModel.userCreate, contestClarificationModel.userUpdate, ContestClarificationStatus.valueOf(contestClarificationModel.status), new Date(contestClarificationModel.timeCreate), new Date(contestClarificationModel.timeUpdate));
    }

    private ContestContestant createContestContestantFromModel(ContestContestantModel contestContestantModel) {
        return new ContestContestant(contestContestantModel.id, contestContestantModel.contestJid, contestContestantModel.userJid, ContestContestantStatus.valueOf(contestContestantModel.status), contestContestantModel.contestStartTime);
    }

    private ContestTeam createContestTeamFromModel(ContestTeamModel contestTeamModel, List<ContestTeamCoachModel> contestTeamCoachModels, List<ContestTeamMemberModel> contestTeamMemberModels) {
        return new ContestTeam(contestTeamModel.id, contestTeamModel.jid, contestTeamModel.contestJid, contestTeamModel.name, getTeamImageURLFromImageName(contestTeamModel.teamImageName), new Date(contestTeamModel.contestStartTime), contestTeamCoachModels.stream().map(ctc -> createContestTeamCoachFromModel(ctc)).collect(Collectors.toList()), contestTeamMemberModels.stream().map(ctm -> createContestTeamMemberFromModel(ctm)).collect(Collectors.toList()));
    }

    private ContestTeamCoach createContestTeamCoachFromModel(ContestTeamCoachModel contestTeamCoachModel) {
        return new ContestTeamCoach(contestTeamCoachModel.id, contestTeamCoachModel.teamJid, contestTeamCoachModel.coachJid);
    }

    private ContestTeamMember createContestTeamMemberFromModel(ContestTeamMemberModel contestTeamMemberModel) {
        return new ContestTeamMember(contestTeamMemberModel.id, contestTeamMemberModel.teamJid, contestTeamMemberModel.memberJid);
    }

    private ContestSupervisor createContestSupervisorFromModel(ContestSupervisorModel contestSupervisorModel) {
        return new ContestSupervisor(contestSupervisorModel.id, contestSupervisorModel.contestJid, contestSupervisorModel.userJid, contestSupervisorModel.announcement, contestSupervisorModel.problem, contestSupervisorModel.submission, contestSupervisorModel.clarification, contestSupervisorModel.contestant);
    }

    private ContestManager createContestManagerFromModel(ContestManagerModel contestManagerModel) {
        return new ContestManager(contestManagerModel.id, contestManagerModel.contestJid, contestManagerModel.userJid);
    }

    private ContestScoreboard createContestScoreboardFromModel(ContestScoreboardModel contestScoreboardModel, ContestStyle style) {
        Scoreboard scoreboard = ScoreAdapters.fromContestStyle(style).parseScoreboardFromJson(contestScoreboardModel.scoreboard);
        return new ContestScoreboard(contestScoreboardModel.id, contestScoreboardModel.contestJid, ContestScoreboardType.valueOf(contestScoreboardModel.type), scoreboard, new Date(contestScoreboardModel.timeUpdate));
    }

    private URL getTeamImageURLFromImageName(String imageName) {
        try {
            return new URL(Play.application().configuration().getString("uriel.baseUrl") + org.iatoki.judgels.uriel.controllers.apis.routes.ContestAPIController.renderTeamAvatarImage(imageName));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
