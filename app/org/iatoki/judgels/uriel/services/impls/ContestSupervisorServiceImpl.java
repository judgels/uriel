package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestSupervisor;
import org.iatoki.judgels.uriel.ContestSupervisorNotFoundException;
import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.daos.ContestSupervisorDao;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.models.entities.ContestSupervisorModel;
import org.iatoki.judgels.uriel.models.entities.ContestSupervisorModel_;
import org.iatoki.judgels.uriel.services.ContestSupervisorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named("contestSupervisorService")
public final class ContestSupervisorServiceImpl implements ContestSupervisorService {

    private final ContestDao contestDao;
    private final ContestSupervisorDao contestSupervisorDao;

    @Inject
    public ContestSupervisorServiceImpl(ContestDao contestDao, ContestSupervisorDao contestSupervisorDao) {
        this.contestDao = contestDao;
        this.contestSupervisorDao = contestSupervisorDao;
    }

    @Override
    public ContestSupervisor findContestSupervisorByContestJidAndUserJid(String contestJid, String userJid) {
        ContestSupervisorModel contestSupervisorModel = contestSupervisorDao.findByContestJidAndSupervisorJid(contestJid, userJid);

        return createContestSupervisorFromModel(contestSupervisorModel);
    }

    @Override
    public Page<ContestSupervisor> pageContestSupervisorsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestSupervisorDao.countByFilters(filterString, ImmutableMap.of(ContestSupervisorModel_.contestJid, contestJid), ImmutableMap.of());
        List<ContestSupervisorModel> contestSupervisorModels = contestSupervisorDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(ContestSupervisorModel_.contestJid, contestJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);
        List<ContestSupervisor> contestSupervisors = Lists.transform(contestSupervisorModels, m -> createContestSupervisorFromModel(m));
        return new Page<>(contestSupervisors, totalPages, pageIndex, pageSize);
    }

    @Override
    public ContestSupervisor findContestSupervisorByContestSupervisorId(long contestSupervisorId) throws ContestSupervisorNotFoundException {
        ContestSupervisorModel contestSupervisorModel = contestSupervisorDao.findById(contestSupervisorId);
        if (contestSupervisorModel != null) {
            return createContestSupervisorFromModel(contestSupervisorModel);
        } else {
            throw new ContestSupervisorNotFoundException("Contest Supervisor not found.");
        }
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

    private ContestSupervisor createContestSupervisorFromModel(ContestSupervisorModel contestSupervisorModel) {
        return new ContestSupervisor(contestSupervisorModel.id, contestSupervisorModel.contestJid, contestSupervisorModel.userJid, contestSupervisorModel.announcement, contestSupervisorModel.problem, contestSupervisorModel.submission, contestSupervisorModel.clarification, contestSupervisorModel.contestant);
    }
}
