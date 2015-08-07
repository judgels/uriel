package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JidService;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestClarification;
import org.iatoki.judgels.uriel.ContestClarificationNotFoundException;
import org.iatoki.judgels.uriel.ContestClarificationStatus;
import org.iatoki.judgels.uriel.ContestReadType;
import org.iatoki.judgels.uriel.models.daos.ContestClarificationDao;
import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.daos.ContestProblemDao;
import org.iatoki.judgels.uriel.models.daos.ContestReadDao;
import org.iatoki.judgels.uriel.models.entities.ContestClarificationModel;
import org.iatoki.judgels.uriel.models.entities.ContestClarificationModel_;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.models.entities.ContestProblemModel;
import org.iatoki.judgels.uriel.models.entities.ContestReadModel;
import org.iatoki.judgels.uriel.services.ContestClarificationService;
import play.i18n.Messages;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Singleton
@Named("contestClarificationService")
public final class ContestClarificationServiceImpl implements ContestClarificationService {

    private final ContestDao contestDao;
    private final ContestProblemDao contestProblemDao;
    private final ContestClarificationDao contestClarificationDao;
    private final ContestReadDao contestReadDao;

    @Inject
    public ContestClarificationServiceImpl(ContestDao contestDao, ContestProblemDao contestProblemDao, ContestClarificationDao contestClarificationDao, ContestReadDao contestReadDao) {
        this.contestDao = contestDao;
        this.contestProblemDao = contestProblemDao;
        this.contestClarificationDao = contestClarificationDao;
        this.contestReadDao = contestReadDao;
    }

    @Override
    public ContestClarification findContestClarificationByContestClarificationId(long contestClarificationId) throws ContestClarificationNotFoundException {
        ContestClarificationModel contestClarificationModel = contestClarificationDao.findById(contestClarificationId);
        if (contestClarificationModel != null) {
            ContestModel contestModel = contestDao.findByJid(contestClarificationModel.contestJid);

            return createContestClarificationFromModel(contestClarificationModel, contestModel);
        } else {
            throw new ContestClarificationNotFoundException("Contest Clarification not found.");
        }
    }

    @Override
    public Page<ContestClarification> pageContestClarificationsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, List<String> askerJids) {
        ContestModel contestModel = contestDao.findByJid(contestJid);

        if (askerJids == null) {
            ImmutableMap.Builder<SingularAttribute<? super ContestClarificationModel, String>, String> filterColumnsBuilder = ImmutableMap.builder();
            filterColumnsBuilder.put(ContestClarificationModel_.contestJid, contestJid);
            Map<SingularAttribute<? super ContestClarificationModel, String>, String> filterColumns = filterColumnsBuilder.build();

            long totalPages = contestClarificationDao.countByFilters(filterString, filterColumns, ImmutableMap.of());
            List<ContestClarificationModel> contestClarificationModels = contestClarificationDao.findSortedByFilters(orderBy, orderDir, filterString, filterColumns, ImmutableMap.of(), pageIndex * pageSize, pageSize);
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
    public long getUnansweredContestClarificationsCount(String contestJid) {
        return contestClarificationDao.countUnansweredClarificationByContestJid(contestJid);
    }

    @Override
    public long getUnreadContestClarificationsCount(List<String> askerJids, String userJid, String contestJid, boolean answered) {
        List<String> clarificationJids;

        if (answered) {
            clarificationJids = contestClarificationDao.findAllAnsweredClarificationJidsInContestByUserJids(contestJid, askerJids);
        } else {
            clarificationJids = contestClarificationDao.findClarificationJidsByContestJidAskedByUserJids(contestJid, askerJids);
        }

        if (!clarificationJids.isEmpty()) {
            return (clarificationJids.size() - contestReadDao.countReadByUserJidAndTypeAndJidList(userJid, ContestReadType.CLARIFICATION.name(), clarificationJids));
        } else {
            return 0;
        }
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
    public void readContestClarifications(String userJid, List<String> contestClarificationJids) {
        for (String contestClarificationJid : contestClarificationJids) {
            if (!contestReadDao.existByUserJidAndTypeAndJid(userJid, ContestReadType.CLARIFICATION.name(), contestClarificationJid)) {
                ContestReadModel contestReadModel = new ContestReadModel();
                contestReadModel.userJid = userJid;
                contestReadModel.type = ContestReadType.CLARIFICATION.name();
                contestReadModel.readJid = contestClarificationJid;

                contestReadDao.persist(contestReadModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            }
        }
    }

    private ContestClarification createContestClarificationFromModel(ContestClarificationModel contestClarificationModel, ContestModel contestModel) {
        String topic;
        if ("CONT".equals(JidService.getInstance().parsePrefix(contestClarificationModel.topicJid))) {
            topic = "(" + Messages.get("clarification.general") + ")";
        } else {
            ContestProblemModel contestProblemModel = contestProblemDao.findByProblemJidOrderedByAlias(contestModel.jid, contestClarificationModel.topicJid);
            topic = contestProblemModel.alias + " - " + JidCacheServiceImpl.getInstance().getDisplayName(contestProblemModel.problemJid);
        }
        return new ContestClarification(contestClarificationModel.id, contestClarificationModel.jid, contestClarificationModel.contestJid, topic, contestClarificationModel.title, contestClarificationModel.question, contestClarificationModel.answer, contestClarificationModel.userCreate, contestClarificationModel.userUpdate, ContestClarificationStatus.valueOf(contestClarificationModel.status), new Date(contestClarificationModel.timeCreate), new Date(contestClarificationModel.timeUpdate));
    }
}
