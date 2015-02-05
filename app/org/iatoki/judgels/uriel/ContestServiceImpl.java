package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JidType;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestAnnouncementDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestClarificationDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestProblemDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.UserRoleDao;
import org.iatoki.judgels.uriel.models.domains.ContestAnnouncementModel;
import org.iatoki.judgels.uriel.models.domains.ContestClarificationModel;
import org.iatoki.judgels.uriel.models.domains.ContestContestantModel;
import org.iatoki.judgels.uriel.models.domains.ContestModel;
import org.iatoki.judgels.uriel.models.domains.ContestProblemModel;
import org.iatoki.judgels.uriel.models.domains.UserRoleModel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ContestServiceImpl implements ContestService {

    private ContestDao contestDao;
    private ContestAnnouncementDao contestAnnouncementDao;
    private ContestProblemDao contestProblemDao;
    private ContestClarificationDao contestClarificationDao;
    private ContestContestantDao contestContestantDao;
    private UserRoleDao userRoleDao;

    public ContestServiceImpl(ContestDao contestDao, ContestAnnouncementDao contestAnnouncementDao, ContestProblemDao contestProblemDao, ContestClarificationDao contestClarificationDao, ContestContestantDao contestContestantDao, UserRoleDao userRoleDao) {
        this.contestDao = contestDao;
        this.contestAnnouncementDao = contestAnnouncementDao;
        this.contestProblemDao = contestProblemDao;
        this.contestClarificationDao = contestClarificationDao;
        this.contestContestantDao = contestContestantDao;
        this.userRoleDao = userRoleDao;
    }

    @Override
    public Contest findContestById(long contestId) {
        ContestModel contestModel = contestDao.findById(contestId);
        Contest contest = new Contest(contestModel.id, contestModel.jid, contestModel.name, contestModel.description, ContestType.valueOf(contestModel.type), ContestScope.valueOf(contestModel.scope), ContestStyle.valueOf(contestModel.style), new Date(contestModel.startTime), new Date(contestModel.endTime));

        return contest;
    }

    @Override
    public Contest findContestByJid(String contestJid) {
        ContestModel contestModel = contestDao.findByJid(contestJid);
        Contest contest = new Contest(contestModel.id, contestModel.jid, contestModel.name, contestModel.description, ContestType.valueOf(contestModel.type), ContestScope.valueOf(contestModel.scope), ContestStyle.valueOf(contestModel.style), new Date(contestModel.startTime), new Date(contestModel.endTime));

        return contest;
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
    public void deleteContest(long contestId) {
        ContestModel contestModel = contestDao.findById(contestId);
        contestDao.remove(contestModel);
    }

    @Override
    public Page<Contest> pageContest(long page, long pageSize, String sortBy, String order, String filterString) {
        long totalPage = contestDao.countByFilter(filterString);
        List<ContestModel> contestModels = contestDao.findByFilterAndSort(filterString, sortBy, order, page * pageSize, pageSize);
        ImmutableList.Builder<Contest> listBuilder = ImmutableList.builder();

        for (ContestModel contestModel : contestModels) {
            listBuilder.add(new Contest(contestModel.id, contestModel.jid, contestModel.name, contestModel.description, ContestType.valueOf(contestModel.type), ContestScope.valueOf(contestModel.scope), ContestStyle.valueOf(contestModel.style), new Date(contestModel.startTime), new Date(contestModel.endTime)));
        }

        Page<Contest> ret = new Page<>(listBuilder.build(), totalPage, page, pageSize);
        return ret;
    }

    @Override
    public List<ContestAnnouncement> findPublishedContestAnnouncementByContestJid(String contestJid) {
        List<ContestAnnouncementModel> contestAnnouncementModels = contestAnnouncementDao.findPublishedByContestJidOrderedByUpdateTime(contestJid);
        if (contestAnnouncementModels == null) {
            contestAnnouncementModels = ImmutableList.of();
        }

        ImmutableList.Builder<ContestAnnouncement> listBuilder = ImmutableList.builder();

        for (ContestAnnouncementModel contestAnnouncementModel : contestAnnouncementModels) {
            UserRoleModel userRoleModel = userRoleDao.findByUserJid(contestAnnouncementModel.userUpdate);
            listBuilder.add(new ContestAnnouncement(contestAnnouncementModel.id, contestAnnouncementModel.contestJid, contestAnnouncementModel.title, contestAnnouncementModel.announcement, userRoleModel.username, ContestAnnouncementStatus.valueOf(contestAnnouncementModel.status), new Date(contestAnnouncementModel.timeUpdate)));
        }

        return listBuilder.build();
    }

    @Override
    public List<ContestAnnouncement> findContestAnnouncementByContestJid(String contestJid, String sortBy, String order, String filterString) {
        List<String> userJids = userRoleDao.findUserJidByFilter(filterString);
        List<ContestAnnouncementModel> contestAnnouncementModels = contestAnnouncementDao.findByContestJidFilterAndSort(contestJid, filterString, userJids, sortBy, order);
        if (contestAnnouncementModels == null) {
            contestAnnouncementModels = ImmutableList.of();
        }

        ImmutableList.Builder<ContestAnnouncement> listBuilder = ImmutableList.builder();

        for (ContestAnnouncementModel contestAnnouncementModel : contestAnnouncementModels) {
            UserRoleModel userRoleModel = userRoleDao.findByUserJid(contestAnnouncementModel.userUpdate);
            listBuilder.add(new ContestAnnouncement(contestAnnouncementModel.id, contestAnnouncementModel.contestJid, contestAnnouncementModel.title, contestAnnouncementModel.announcement, userRoleModel.username, ContestAnnouncementStatus.valueOf(contestAnnouncementModel.status), new Date(contestAnnouncementModel.timeUpdate)));
        }

        return listBuilder.build();
    }

    @Override
    public ContestAnnouncement findContestAnnouncementByContestAnnouncementId(long contestAnnouncementId) {
        ContestAnnouncementModel contestAnnouncementModel = contestAnnouncementDao.findById(contestAnnouncementId);
        UserRoleModel userRoleModel = userRoleDao.findByUserJid(contestAnnouncementModel.userUpdate);

        return new ContestAnnouncement(contestAnnouncementModel.id, contestAnnouncementModel.contestJid, contestAnnouncementModel.title, contestAnnouncementModel.announcement, userRoleModel.username, ContestAnnouncementStatus.valueOf(contestAnnouncementModel.status), new Date(contestAnnouncementModel.timeUpdate));
    }

    @Override
    public void createContestAnnouncement(long contestId, String title, String announcement, ContestAnnouncementStatus status) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestAnnouncementModel contestAnnouncementModel = new ContestAnnouncementModel();
        contestAnnouncementModel.contestJid = contestModel.jid;
        contestAnnouncementModel.title = title;
        contestAnnouncementModel.announcement = announcement;
        contestAnnouncementModel.status = status.name();

        contestAnnouncementDao.persist(contestAnnouncementModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestAnnouncement(long contestAnnouncementId, String title, String announcement, ContestAnnouncementStatus status) {
        ContestAnnouncementModel contestAnnouncementModel = contestAnnouncementDao.findById(contestAnnouncementId);
        contestAnnouncementModel.title = title;
        contestAnnouncementModel.announcement = announcement;
        contestAnnouncementModel.status = status.name();

        contestAnnouncementDao.edit(contestAnnouncementModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public List<ContestProblem> findOpenedContestProblemByContestJid(String contestJid) {
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findOpenedByContestJidOrderedByAlias(contestJid);
        if (contestProblemModels == null) {
            contestProblemModels = ImmutableList.of();
        }

        ImmutableList.Builder<ContestProblem> listBuilder = ImmutableList.builder();

        for (ContestProblemModel contestProblemModel : contestProblemModels) {
            listBuilder.add(new ContestProblem(contestProblemModel.id, contestProblemModel.contestJid, contestProblemModel.problemJid, contestProblemModel.problemSecret, contestProblemModel.alias, contestProblemModel.name, contestProblemModel.submissionLimit, ContestProblemStatus.valueOf(contestProblemModel.status)));
        }

        return listBuilder.build();
    }

    @Override
    public Page<ContestProblem> pageContestProblemByContestJid(String contestJid, long page, long pageSize, String sortBy, String order, String filterString) {
        long totalPage = contestProblemDao.countByFilter(contestJid, filterString);
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findByContestJidFilterAndSort(contestJid, filterString, sortBy, order, page * pageSize, pageSize);
        ImmutableList.Builder<ContestProblem> listBuilder = ImmutableList.builder();

        for (ContestProblemModel contestProblemModel : contestProblemModels) {
            listBuilder.add(new ContestProblem(contestProblemModel.id, contestProblemModel.contestJid, contestProblemModel.problemJid, contestProblemModel.problemSecret, contestProblemModel.alias, contestProblemModel.name, contestProblemModel.submissionLimit, ContestProblemStatus.valueOf(contestProblemModel.status)));
        }

        Page<ContestProblem> ret = new Page<>(listBuilder.build(), totalPage, page, pageSize);
        return ret;
    }

    @Override
    public ContestProblem findContestProblemByContestProblemId(long contestProblemId) {
        ContestProblemModel contestProblemModel = contestProblemDao.findById(contestProblemId);

        return new ContestProblem(contestProblemModel.id, contestProblemModel.contestJid, contestProblemModel.problemJid, contestProblemModel.problemSecret, contestProblemModel.alias, contestProblemModel.name, contestProblemModel.submissionLimit, ContestProblemStatus.valueOf(contestProblemModel.status));
    }

    @Override
    public ContestProblem findContestProblemByContestJidAndContestProblemJid(String contestJid, String contestProblemJid) {
        ContestProblemModel contestProblemModel = contestProblemDao.findByProblemJid(contestJid, contestProblemJid);

        return new ContestProblem(contestProblemModel.id, contestProblemModel.contestJid, contestProblemModel.problemJid, contestProblemModel.problemSecret, contestProblemModel.alias, contestProblemModel.name, contestProblemModel.submissionLimit, ContestProblemStatus.valueOf(contestProblemModel.status));
    }

    @Override
    public boolean isContestProblemInContestByProblemJid(String contestJid, String contestProblemJid) {
        return contestProblemDao.isExistByProblemJid(contestJid, contestProblemJid);
    }

    @Override
    public void createContestProblem(long contestId, String problemJid, String problemSecret, String alias, String name, long submissionLimit, ContestProblemStatus status) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestProblemModel contestProblemModel = new ContestProblemModel();
        contestProblemModel.contestJid = contestModel.jid;
        contestProblemModel.problemJid = problemJid;
        contestProblemModel.problemSecret = problemSecret;
        contestProblemModel.alias = alias;
        contestProblemModel.name = name;
        contestProblemModel.submissionLimit = submissionLimit;
        contestProblemModel.status = status.name();

        contestProblemDao.persist(contestProblemModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestProblem(long contestProblemId, String problemSecret, String alias, String name, long submissionLimit, ContestProblemStatus status) {
        ContestProblemModel contestProblemModel = contestProblemDao.findById(contestProblemId);
        contestProblemModel.problemSecret = problemSecret;
        contestProblemModel.alias = alias;
        contestProblemModel.name = name;
        contestProblemModel.submissionLimit = submissionLimit;
        contestProblemModel.status = status.name();

        contestProblemDao.edit(contestProblemModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public List<ContestClarification> findContestClarificationByContestJidAndAskerJid(String contestJid, String askerJid) {
        ContestModel contestModel = contestDao.findByJid(contestJid);
        List<ContestClarificationModel> contestClarificationModels = contestClarificationDao.findByContestJidAndAskerJid(contestJid, askerJid);
        ImmutableList.Builder<ContestClarification> contestClarificationBuilder = ImmutableList.builder();

        for (ContestClarificationModel contestClarificationModel : contestClarificationModels) {
            String topic = "";
            if (JidType.CONTEST.equals(IdentityUtils.getJidType(contestClarificationModel.topicJid))) {
                topic = contestModel.name;
            } else if (JidType.PROBLEM.equals(IdentityUtils.getJidType(contestClarificationModel.topicJid))) {
                topic = contestProblemDao.findByProblemJid(contestModel.jid, contestClarificationModel.topicJid).alias;
            }

            if (contestClarificationModel.status.equals(ContestClarificationStatus.ASKED.name())) {
                UserRoleModel asker = userRoleDao.findByUserJid(contestClarificationModel.userCreate);
                contestClarificationBuilder.add(new ContestClarification(contestClarificationModel.id, contestClarificationModel.contestJid, topic, contestClarificationModel.question, contestClarificationModel.answer, asker.username + "(" + asker.alias + ")", ContestClarificationStatus.valueOf(contestClarificationModel.status), new Date(contestClarificationModel.timeCreate)));
            } else {
                UserRoleModel asker = userRoleDao.findByUserJid(contestClarificationModel.userCreate);
                UserRoleModel answerer = userRoleDao.findByUserJid(contestClarificationModel.userUpdate);
                contestClarificationBuilder.add(new ContestClarification(contestClarificationModel.id, contestClarificationModel.contestJid, topic, contestClarificationModel.question, contestClarificationModel.answer, asker.username + "(" + asker.alias + ")", answerer.username + "(" + answerer.alias + ")", ContestClarificationStatus.valueOf(contestClarificationModel.status), new Date(contestClarificationModel.timeCreate), new Date(contestClarificationModel.timeUpdate)));
            }
        }

        return contestClarificationBuilder.build();
    }

    @Override
    public Page<ContestClarification> pageContestClarificationByContestJid(String contestJid, long page, long pageSize, String sortBy, String order, String filterString) {
        ContestModel contestModel = contestDao.findByJid(contestJid);
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findByContestJid(contestJid);
        List<String> userJids = userRoleDao.findUserJidByFilter(filterString);

        Map<String, ContestProblemModel> problemJidMap = new HashMap<>();
        for (ContestProblemModel contestProblemModel : contestProblemModels) {
            problemJidMap.put(contestProblemModel.problemJid, contestProblemModel);
        }

        long totalPage = contestClarificationDao.countByFilter(contestJid, filterString, userJids);
        List<ContestClarificationModel> contestClarificationModels = contestClarificationDao.findByContestJidFilterAndSort(contestJid, filterString, userJids, sortBy, order, page * pageSize, pageSize);
        ImmutableList.Builder<ContestClarification> contestClarificationBuilder = ImmutableList.builder();

        for (ContestClarificationModel contestClarificationModel : contestClarificationModels) {
            String topic = "";
            if (JidType.CONTEST.equals(IdentityUtils.getJidType(contestClarificationModel.topicJid))) {
                topic = contestModel.name;
            } else if (JidType.PROBLEM.equals(IdentityUtils.getJidType(contestClarificationModel.topicJid))) {
                topic = problemJidMap.get(contestClarificationModel.topicJid).alias;
            }

            if (contestClarificationModel.status.equals(ContestClarificationStatus.ASKED.name())) {
                UserRoleModel asker = userRoleDao.findByUserJid(contestClarificationModel.userCreate);
                contestClarificationBuilder.add(new ContestClarification(contestClarificationModel.id, contestClarificationModel.contestJid, topic, contestClarificationModel.question, contestClarificationModel.answer, asker.username + "(" + asker.alias + ")", ContestClarificationStatus.valueOf(contestClarificationModel.status), new Date(contestClarificationModel.timeCreate)));
            } else {
                UserRoleModel asker = userRoleDao.findByUserJid(contestClarificationModel.userCreate);
                UserRoleModel answerer = userRoleDao.findByUserJid(contestClarificationModel.userUpdate);
                contestClarificationBuilder.add(new ContestClarification(contestClarificationModel.id, contestClarificationModel.contestJid, topic, contestClarificationModel.question, contestClarificationModel.answer, asker.username + "(" + asker.alias + ")", answerer.username + "(" + answerer.alias + ")", ContestClarificationStatus.valueOf(contestClarificationModel.status), new Date(contestClarificationModel.timeCreate), new Date(contestClarificationModel.timeUpdate)));
            }
        }

        Page<ContestClarification> ret = new Page<>(contestClarificationBuilder.build(), totalPage, page, pageSize);
        return ret;
    }

    @Override
    public ContestClarification findContestClarificationByContestClarificationId(long contestClarificationId) {
        ContestClarificationModel contestClarificationModel = contestClarificationDao.findById(contestClarificationId);
        ContestModel contestModel = contestDao.findByJid(contestClarificationModel.contestJid);

        UserRoleModel asker = userRoleDao.findByUserJid(contestClarificationModel.userCreate);
        UserRoleModel answerer = userRoleDao.findByUserJid(contestClarificationModel.userUpdate);
        String topic = "";
        if (JidType.CONTEST.equals(IdentityUtils.getJidType(contestClarificationModel.topicJid))) {
            topic = contestModel.name;
        } else if (JidType.PROBLEM.equals(IdentityUtils.getJidType(contestClarificationModel.topicJid))) {
            topic = contestProblemDao.findByProblemJid(contestModel.jid, contestClarificationModel.topicJid).alias;
        }

        return new ContestClarification(contestClarificationModel.id, contestClarificationModel.contestJid, topic, contestClarificationModel.question, contestClarificationModel.answer, asker.username + "(" + asker.alias + ")", answerer.username + "(" + answerer.alias + ")", ContestClarificationStatus.valueOf(contestClarificationModel.status), new Date(contestClarificationModel.timeCreate), new Date(contestClarificationModel.timeUpdate));
    }

    @Override
    public void createContestClarification(long contestId, String question, String topicJid) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestClarificationModel contestClarificationModel = new ContestClarificationModel();
        contestClarificationModel.contestJid = contestModel.jid;
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
    public Page<ContestContestant> pageContestContestantByContestJid(String contestJid, long page, long pageSize, String sortBy, String order, String filterString) {
        List<String> userJids = userRoleDao.findUserJidByFilter(filterString);

        long totalPage = contestContestantDao.countByFilter(contestJid, filterString, userJids);
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findByContestJidFilterAndSort(contestJid, filterString, userJids, sortBy, order, page * pageSize, pageSize);
        ImmutableList.Builder<ContestContestant> listBuilder = ImmutableList.builder();

        for (ContestContestantModel contestContestantModel : contestContestantModels) {
            UserRoleModel userRoleModel = userRoleDao.findByUserJid(contestContestantModel.userJid);
            listBuilder.add(new ContestContestant(contestContestantModel.id, contestContestantModel.contestJid, userRoleModel.username, userRoleModel.alias, ContestContestantStatus.valueOf(contestContestantModel.status)));
        }

        Page<ContestContestant> ret = new Page<>(listBuilder.build(), totalPage, page, pageSize);
        return ret;
    }

    @Override
    public ContestContestant findContestContestantByContestContestantId(long contestContestantId) {
        ContestContestantModel contestContestantModel = contestContestantDao.findById(contestContestantId);
        UserRoleModel userRoleModel = userRoleDao.findByUserJid(contestContestantModel.userJid);

        return new ContestContestant(contestContestantModel.id, contestContestantModel.contestJid, userRoleModel.username, userRoleModel.alias, ContestContestantStatus.valueOf(contestContestantModel.status));
    }

    @Override
    public boolean isContestContestantInContestByUserJid(String contestJid, String contestContestantJid) {
        return contestContestantDao.isExistByContestantJid(contestJid, contestContestantJid);
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
}
