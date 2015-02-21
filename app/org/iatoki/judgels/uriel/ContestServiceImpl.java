package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JidService;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.uriel.commons.ContestConfig;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestAnnouncementDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestClarificationDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestManagerDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestReadDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestScoreboardDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestSupervisorDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestProblemDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.UserRoleDao;
import org.iatoki.judgels.uriel.models.domains.ContestAnnouncementModel;
import org.iatoki.judgels.uriel.models.domains.ContestClarificationModel;
import org.iatoki.judgels.uriel.models.domains.ContestContestantModel;
import org.iatoki.judgels.uriel.models.domains.ContestManagerModel;
import org.iatoki.judgels.uriel.models.domains.ContestModel;
import org.iatoki.judgels.uriel.models.domains.ContestReadModel;
import org.iatoki.judgels.uriel.models.domains.ContestScoreboardModel;
import org.iatoki.judgels.uriel.models.domains.ContestSupervisorModel;
import org.iatoki.judgels.uriel.models.domains.ContestProblemModel;
import play.i18n.Messages;

import javax.persistence.NoResultException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ContestServiceImpl implements ContestService {

    private final ContestDao contestDao;
    private final ContestAnnouncementDao contestAnnouncementDao;
    private final ContestProblemDao contestProblemDao;
    private final ContestClarificationDao contestClarificationDao;
    private final ContestContestantDao contestContestantDao;
    private final ContestSupervisorDao contestSupervisorDao;
    private final ContestManagerDao contestManagerDao;
    private final ContestScoreboardDao contestScoreboardDao;
    private final ContestReadDao contestReadDao;

    public ContestServiceImpl(ContestDao contestDao, ContestAnnouncementDao contestAnnouncementDao, ContestProblemDao contestProblemDao, ContestClarificationDao contestClarificationDao, ContestContestantDao contestContestantDao, ContestSupervisorDao contestSupervisorDao, ContestManagerDao contestManagerDao, ContestScoreboardDao contestScoreboardDao, ContestReadDao contestReadDao) {
        this.contestDao = contestDao;
        this.contestAnnouncementDao = contestAnnouncementDao;
        this.contestProblemDao = contestProblemDao;
        this.contestClarificationDao = contestClarificationDao;
        this.contestContestantDao = contestContestantDao;
        this.contestSupervisorDao = contestSupervisorDao;
        this.contestManagerDao = contestManagerDao;
        this.contestScoreboardDao = contestScoreboardDao;
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
    public void createContest(String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime) {
        ContestModel contestModel = new ContestModel();
        contestModel.name = name;
        contestModel.description = description;
        contestModel.type = type.name();
        contestModel.scope = scope.name();
        contestModel.style = style.name();
        contestModel.startTime = startTime.getTime();
        contestModel.endTime = endTime.getTime();

        contestDao.persist(contestModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        ContestScoreboardModel contestScoreboardModel = new ContestScoreboardModel();
        contestScoreboardModel.contestJid = contestModel.jid;
        contestScoreboardModel.type = ContestScoreboardType.OFFICIAL.name();

        ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(style);
        ContestConfig config = getContestConfigByJid(contestModel.jid);
        ScoreboardContent content = adapter.computeContent(config, ImmutableList.of());
        Scoreboard scoreboard = adapter.createScoreboard(config, content);

        contestScoreboardModel.scoreboard = new Gson().toJson(scoreboard);

        contestScoreboardDao.persist(contestScoreboardModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContest(long contestId, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime) {
        ContestModel contestModel = contestDao.findById(contestId);
        contestModel.name = name;
        contestModel.description = description;
        contestModel.type = type.name();
        contestModel.scope = scope.name();
        contestModel.style = style.name();
        contestModel.startTime = startTime.getTime();
        contestModel.endTime = endTime.getTime();

        contestDao.edit(contestModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public Page<Contest> pageContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestDao.countByFilters(filterString, ImmutableMap.of());
        List<ContestModel> contestModels = contestDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<Contest> contests = Lists.transform(contestModels, m -> createContestFromModel(m));
        return new Page<>(contests, totalPages, pageIndex, pageSize);
    }

    @Override
    public ContestConfig getContestConfigByJid(String contestJid) {
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findOpenedByContestJidOrderedByAlias(contestJid);
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFilters("id", "asc", "", ImmutableMap.of("contestJid", contestJid, "status", ContestContestantStatus.APPROVED.name()), 0, -1);

        Map<String, String> problemAliasesByJid = contestProblemModels.stream().collect(Collectors.toMap(m -> m.problemJid, m -> m.alias));
        List<String> contestantJids = Lists.transform(contestContestantModels, m -> m.userJid);

        return new ContestConfig(problemAliasesByJid, contestantJids);
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
        ContestProblemModel contestProblemModel = contestProblemDao.findByProblemJid(contestJid, contestProblemJid);
        return createContestProblemFromModel(contestProblemModel);
    }

    @Override
    public boolean isContestProblemInContestByProblemJid(String contestJid, String contestProblemJid) {
        return contestProblemDao.existsByProblemJid(contestJid, contestProblemJid);
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
    public ContestScoreboard findContestScoreboardByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type) {
        ContestModel contestModel = contestDao.findByJid(contestJid);
        ContestScoreboardModel contestScoreboardModel = contestScoreboardDao.findContestScoreboardByContestJidAndScoreboardType(contestJid, type.name());
        return createContestScoreboardFromModel(contestScoreboardModel, ContestStyle.valueOf(contestModel.style));
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

    private Contest createContestFromModel(ContestModel contestModel) {
        return new Contest(contestModel.id, contestModel.jid, contestModel.name, contestModel.description, ContestType.valueOf(contestModel.type), ContestScope.valueOf(contestModel.scope), ContestStyle.valueOf(contestModel.style), new Date(contestModel.startTime), new Date(contestModel.endTime));
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
            topic = contestProblemDao.findByProblemJid(contestModel.jid, contestClarificationModel.topicJid).alias;
        }
        return new ContestClarification(contestClarificationModel.id, contestClarificationModel.contestJid, topic, contestClarificationModel.title, contestClarificationModel.question, contestClarificationModel.answer, contestClarificationModel.userCreate, contestClarificationModel.userUpdate, ContestClarificationStatus.valueOf(contestClarificationModel.status), new Date(contestClarificationModel.timeCreate), new Date(contestClarificationModel.timeUpdate));
    }

    private ContestContestant createContestContestantFromModel(ContestContestantModel contestContestantModel) {
        return new ContestContestant(contestContestantModel.id, contestContestantModel.contestJid, contestContestantModel.userJid, ContestContestantStatus.valueOf(contestContestantModel.status));
    }

    private ContestSupervisor createContestSupervisorFromModel(ContestSupervisorModel contestSupervisorModel) {
        return new ContestSupervisor(contestSupervisorModel.id, contestSupervisorModel.contestJid, contestSupervisorModel.userJid, contestSupervisorModel.announcement, contestSupervisorModel.problem, contestSupervisorModel.submission, contestSupervisorModel.clarification, contestSupervisorModel.contestant);
    }

    private ContestManager createContestManagerFromModel(ContestManagerModel contestManagerModel) {
        return new ContestManager(contestManagerModel.id, contestManagerModel.contestJid, contestManagerModel.userJid);
    }

    private ContestScoreboard createContestScoreboardFromModel(ContestScoreboardModel contestScoreboardModel, ContestStyle style) {
        Scoreboard scoreboard = ScoreboardAdapters.fromContestStyle(style).parseScoreboardFromJson(contestScoreboardModel.scoreboard);
        return new ContestScoreboard(contestScoreboardModel.id, contestScoreboardModel.contestJid, ContestScoreboardType.valueOf(contestScoreboardModel.type), scoreboard, new Date(contestScoreboardModel.timeUpdate));
    }
}
