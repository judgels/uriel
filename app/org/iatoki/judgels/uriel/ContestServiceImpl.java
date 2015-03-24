package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JidService;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.sandalphon.commons.programming.LanguageRestriction;
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
import org.iatoki.judgels.uriel.models.domains.ContestTeamMemberModel;
import org.iatoki.judgels.uriel.models.domains.ContestTeamModel;
import play.Play;
import play.i18n.Messages;

import javax.persistence.NoResultException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    public ContestServiceImpl(ContestDao contestDao, ContestAnnouncementDao contestAnnouncementDao, ContestProblemDao contestProblemDao, ContestClarificationDao contestClarificationDao, ContestContestantDao contestContestantDao, ContestTeamDao contestTeamDao, ContestTeamCoachDao contestTeamCoachDao, ContestTeamMemberDao contestTeamMemberDao, ContestSupervisorDao contestSupervisorDao, ContestManagerDao contestManagerDao, ContestScoreboardDao contestScoreboardDao, ContestConfigurationDao contestConfigurationDao, ContestReadDao contestReadDao) {
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
    public void createContest(String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isUsingScoreboard, boolean isIncognitoScoreboard) {
        ContestModel contestModel = new ContestModel();
        contestModel.name = name;
        contestModel.description = description;
        contestModel.type = type.name();
        contestModel.scope = scope.name();
        contestModel.style = style.name();
        contestModel.startTime = startTime.getTime();
        contestModel.endTime = endTime.getTime();
        contestModel.clarificationEndTime = clarificationEndTime.getTime();
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
    }

    @Override
    public void updateContest(long contestId, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isUsingScoreboard, boolean isIncognitoScoreboard) {
        boolean resetScoreboard = false;

        ContestModel contestModel = contestDao.findById(contestId);
        contestModel.name = name;
        contestModel.description = description;
        contestModel.type = type.name();
        contestModel.scope = scope.name();
        if (!contestModel.style.equals(style.name())) {
            resetScoreboard = true;
        }
        contestModel.style = style.name();
        contestModel.startTime = startTime.getTime();
        contestModel.endTime = endTime.getTime();
        contestModel.clarificationEndTime = clarificationEndTime.getTime();
        contestModel.isUsingScoreboard = isUsingScoreboard;
        contestModel.isIncognitoScoreboard = isIncognitoScoreboard;

        contestDao.edit(contestModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        if (resetScoreboard) {
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
    }

    @Override
    public Page<Contest> pageAllowedContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String userJid, boolean isAdmin) {
        long totalRowsCount = contestDao.countAllowedContests(filterString, userJid, isAdmin);
        List<ContestModel> contestModels = contestDao.findSortedAllowedContestsByFilters(orderBy, orderDir, filterString, userJid, isAdmin, pageIndex * pageSize, pageSize);

        List<Contest> contests = Lists.transform(contestModels, m -> createContestFromModel(m));
        return new Page<>(contests, totalRowsCount, pageIndex, pageSize);
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
    public Page<ContestClarification> pageContestClarificationsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String askerJid) {
        ContestModel contestModel = contestDao.findByJid(contestJid);

        ImmutableMap.Builder<String, String> filterColumnsBuilder = ImmutableMap.builder();
        filterColumnsBuilder.put("contestJid", contestJid);
        if (askerJid != null) {
            filterColumnsBuilder.put("userCreate", askerJid);
        }
        Map<String, String> filterColumns = filterColumnsBuilder.build();

        long totalPages = contestClarificationDao.countByFilters(filterString, filterColumns);
        List<ContestClarificationModel> contestClarificationModels = contestClarificationDao.findSortedByFilters(orderBy, orderDir, filterString, filterColumns, pageIndex * pageSize, pageSize);
        List<ContestClarification> contestClarifications = Lists.transform(contestClarificationModels, m -> createContestClarificationFromModel(m, contestModel));

        return new Page<>(contestClarifications, totalPages, pageIndex, pageSize);
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
        ContestContestantModel contestContestantModel = contestContestantDao.findByContestantJid(contestJid, contestContestantJid);
        return createContestContestantFromModel(contestContestantModel);
    }

    @Override
    public boolean isContestContestantInContestByUserJid(String contestJid, String contestContestantJid) {
        return contestContestantDao.existsByContestantJid(contestJid, contestContestantJid);
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
    public void enterContestAsContestant(String contestJid, String userJid) {
        ContestContestantModel contestContestantModel = contestContestantDao.findByContestantJid(contestJid, userJid);
        if (contestContestantModel.contestEnterTime == 0) {
            contestContestantModel.contestEnterTime = System.currentTimeMillis();

            contestContestantDao.edit(contestContestantModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public void enterContestAsCoach(String contestJid, String coachJid) {
        List<String> contestTeamJids = contestTeamDao.findAllTeamJidsInContest(contestJid);

        ContestTeamCoachModel contestTeamCoachModel = contestTeamCoachDao.findContestTeamCoachByCoachJidInAnyTeam(coachJid, contestTeamJids);
        List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.findContestTeamMembersInTeam(contestTeamCoachModel.teamJid);

        for (ContestTeamMemberModel contestTeamMemberModel : contestTeamMemberModels) {
            ContestContestantModel contestContestantModel = contestContestantDao.findByContestantJid(contestJid, contestTeamMemberModel.memberJid);
            contestContestantModel.contestEnterTime = System.currentTimeMillis();

            contestContestantDao.edit(contestContestantModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public boolean isContestEntered(String contestJid, String contestContestantJid) {
        return contestContestantDao.isContestEntered(contestJid, contestContestantJid);
    }

    @Override
    public Page<ContestTeam> pageContestTeamsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestTeamDao.countByFilters(filterString, ImmutableMap.of("contestJid", contestJid));
        List<ContestTeamModel> contestTeamModels = contestTeamDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of("contestJid", contestJid), pageIndex * pageSize, pageSize);

        List<ContestTeam> contestTeams = Lists.transform(contestTeamModels, m -> createContestTeamFromModel(m));

        return new Page<>(contestTeams, totalPages, pageIndex, pageSize);
    }

    @Override
    public List<ContestTeam> findAllContestTeams(String contestJid) {
        List<ContestTeamModel> contestTeamModels = contestTeamDao.findSortedByFilters("id", "asc", "", ImmutableMap.of("contestJid", contestJid), 0, -1);

        List<ContestTeam> contestTeams = Lists.transform(contestTeamModels, m -> createContestTeamFromModel(m));

        return contestTeams;
    }

    @Override
    public ContestTeam findContestTeamByContestTeamId(long contestTeamId) {
        return createContestTeamFromModel(contestTeamDao.findById(contestTeamId));
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
    public void createContestTeam(long contestId, String name, File teamImage, String extension) {
        try {
            ContestModel contestModel = contestDao.findById(contestId);

            ContestTeamModel contestTeamModel = new ContestTeamModel();
            contestTeamModel.contestJid = contestModel.jid;
            contestTeamModel.name = name;
            contestTeamModel.teamImageName = "team-default.png";

            contestTeamDao.persist(contestTeamModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            String newImageName = contestTeamModel.jid + "-" + JudgelsUtils.hashMD5(UUID.randomUUID().toString()) + "." + extension;
            FileUtils.copyFile(teamImage, new File(UrielProperties.getInstance().getTeamAvatarDir(), newImageName));

            contestTeamModel.teamImageName = newImageName;

            contestTeamDao.edit(contestTeamModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateContestTeam(long contestTeamId, String name) {
        ContestTeamModel contestTeamModel = contestTeamDao.findById(contestTeamId);
        contestTeamModel.name = name;

        contestTeamDao.edit(contestTeamModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestTeam(long contestTeamId, String name, File teamImage, String extension) {
        try {
            ContestTeamModel contestTeamModel = contestTeamDao.findById(contestTeamId);
            String newImageName = contestTeamModel.jid + "-" + JudgelsUtils.hashMD5(UUID.randomUUID().toString()) + "." + extension;
            FileUtils.copyFile(teamImage, new File(UrielProperties.getInstance().getTeamAvatarDir(), newImageName));

            contestTeamModel.name = name;
            contestTeamModel.teamImageName = newImageName;

            contestTeamDao.edit(contestTeamModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isUserInAnyTeam(String contestJid, String userJid) {
        List<String> teamJids = contestTeamDao.findAllTeamJidsInContest(contestJid);

        return ((contestTeamCoachDao.isUserRegisteredAsCoachInAnyTeam(userJid, teamJids)) || (contestTeamMemberDao.isUserRegisteredAsMemberInAnyTeam(userJid, teamJids)));
    }

    @Override
    public boolean isUserCoachInAnyTeam(String contestJid, String userJid) {
        List<String> teamJids = contestTeamDao.findAllTeamJidsInContest(contestJid);

        return (contestTeamCoachDao.isUserRegisteredAsCoachInAnyTeam(userJid, teamJids));
    }

    @Override
    public ContestTeamCoach findContestTeamCoachByContestTeamCoachId(long contestTeamCoachId) {
        return createContestTeamCoachFromModel(contestTeamCoachDao.findById(contestTeamCoachId));
    }

    @Override
    public List<ContestTeamCoach> findContestTeamCoachesByTeamJid(String contestTeamJid) {
        List<ContestTeamCoachModel> contestTeamCoachModels = contestTeamCoachDao.findContestTeamCoachesInTeam(contestTeamJid);

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
        ContestSupervisorModel contestSupervisorModel = contestSupervisorDao.findBySupervisorJid(contestJid, userJid);

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
        return contestSupervisorDao.existsBySupervisorJid(contestJid, contestSupervisorJid);
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
        return contestManagerDao.existsByManagerJid(contestJid, contestManagerJid);
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
    public long getUnreadContestClarificationsCount(String userJid, String contestJid) {
        List<Long> clarificationIds = contestClarificationDao.findAllAnsweredClarificationIdInContestByUserJid(contestJid, userJid);
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

        List<ContestTeamModel> contestTeamModels = contestTeamDao.findAllContestTeamModelInContest(contestJid);
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
        ContestConfigurationModel contestConfigurationModel;
        if (contestConfigurationDao.isExistByContestJid(contestJid)) {
            contestConfigurationModel = contestConfigurationDao.findByContestJid(contestJid);

            ContestModel contestModel = contestDao.findByJid(contestJid);
            Contest contest = createContestFromModel(contestModel);

            if ("{}".equals(contestConfigurationModel.typeConfig)) {
                if (contestModel.type.equals(ContestType.STANDARD.name())) {
                    contestConfigurationModel.typeConfig = new Gson().toJson(ContestTypeConfigStandard.defaultConfig(contest));
                } else if (contestModel.type.equals(ContestType.VIRTUAL.name())) {
                    contestConfigurationModel.typeConfig = new Gson().toJson(ContestTypeConfigVirtual.defaultConfig(contest));
                }
            }

            if ("{}".equals(contestConfigurationModel.scopeConfig)) {
                if (contestModel.scope.equals(ContestScope.PRIVATE.name())) {
                    contestConfigurationModel.scopeConfig = new Gson().toJson(ContestScopeConfigPrivate.defaultConfig(contest));
                } else if (contestModel.scope.equals(ContestScope.PUBLIC.name())) {
                    contestConfigurationModel.scopeConfig = new Gson().toJson(ContestScopeConfigPublic.defaultConfig(contest));
                }
            }

            if ("{}".equals(contestConfigurationModel.styleConfig)) {
                if (contestModel.style.equals(ContestStyle.ICPC.name())) {
                    contestConfigurationModel.styleConfig = new Gson().toJson(ContestStyleConfigICPC.defaultConfig(contest));
                } else if (contestModel.style.equals(ContestStyle.IOI.name())) {
                    contestConfigurationModel.styleConfig = new Gson().toJson(ContestStyleConfigIOI.defaultConfig(contest));
                }
            }

            contestConfigurationDao.edit(contestConfigurationModel, "urielConfigHandler", "localhost");
        } else {
            contestConfigurationModel = new ContestConfigurationModel();
            contestConfigurationModel.contestJid = contestJid;

            ContestModel contestModel = contestDao.findByJid(contestJid);
            Contest contest = createContestFromModel(contestModel);
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

            contestConfigurationDao.persist(contestConfigurationModel, "urielConfigHandler", "localhost");
        }

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
    public File getTeamAvatarImageFile(String imageName) {
        return FileUtils.getFile(UrielProperties.getInstance().getTeamAvatarDir(), imageName);
    }

    private Contest createContestFromModel(ContestModel contestModel) {
        return new Contest(contestModel.id, contestModel.jid, contestModel.name, contestModel.description, ContestType.valueOf(contestModel.type), ContestScope.valueOf(contestModel.scope), ContestStyle.valueOf(contestModel.style), new Date(contestModel.startTime), new Date(contestModel.endTime), new Date(contestModel.clarificationEndTime), contestModel.isUsingScoreboard, contestModel.isIncognitoScoreboard);
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
        return new ContestContestant(contestContestantModel.id, contestContestantModel.contestJid, contestContestantModel.userJid, ContestContestantStatus.valueOf(contestContestantModel.status), contestContestantModel.contestEnterTime);
    }

    private ContestTeam createContestTeamFromModel(ContestTeamModel contestTeamModel) {
        return new ContestTeam(contestTeamModel.id, contestTeamModel.jid, contestTeamModel.contestJid, contestTeamModel.name, getTeamImageURLFromImageName(contestTeamModel.teamImageName));
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
            return new URL(Play.application().configuration().getString("uriel.baseUrl") + org.iatoki.judgels.uriel.controllers.routes.ContestController.renderTeamAvatarImage(imageName));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
