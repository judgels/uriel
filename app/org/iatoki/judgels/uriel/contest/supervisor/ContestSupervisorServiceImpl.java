package org.iatoki.judgels.uriel.contest.supervisor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.contest.ContestDao;
import org.iatoki.judgels.uriel.contest.ContestModel;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public final class ContestSupervisorServiceImpl implements ContestSupervisorService {

    private final ContestDao contestDao;
    private final ContestSupervisorDao contestSupervisorDao;

    @Inject
    public ContestSupervisorServiceImpl(ContestDao contestDao, ContestSupervisorDao contestSupervisorDao) {
        this.contestDao = contestDao;
        this.contestSupervisorDao = contestSupervisorDao;
    }

    @Override
    public ContestSupervisor findContestSupervisorInContestByUserJid(String contestJid, String userJid) {
        ContestSupervisorModel contestSupervisorModel = contestSupervisorDao.findInContestByJid(contestJid, userJid);

        return createContestSupervisorFromModel(contestSupervisorModel);
    }

    @Override
    public Page<ContestSupervisor> getPageOfSupervisorsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestSupervisorDao.countByFiltersEq(filterString, ImmutableMap.of(ContestSupervisorModel_.contestJid, contestJid));
        List<ContestSupervisorModel> contestSupervisorModels = contestSupervisorDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(ContestSupervisorModel_.contestJid, contestJid), pageIndex * pageSize, pageSize);
        List<ContestSupervisor> contestSupervisors = Lists.transform(contestSupervisorModels, m -> createContestSupervisorFromModel(m));
        return new Page<>(contestSupervisors, totalPages, pageIndex, pageSize);
    }

    @Override
    public ContestSupervisor findContestSupervisorById(long contestSupervisorId) throws ContestSupervisorNotFoundException {
        ContestSupervisorModel contestSupervisorModel = contestSupervisorDao.findById(contestSupervisorId);
        if (contestSupervisorModel == null) {
            throw new ContestSupervisorNotFoundException("Contest Supervisor not found.");
        }

        return createContestSupervisorFromModel(contestSupervisorModel);
    }

    @Override
    public boolean isContestSupervisorInContest(String contestJid, String contestSupervisorJid) {
        return contestSupervisorDao.existsInContestByJid(contestJid, contestSupervisorJid);
    }

    @Override
    public void createContestSupervisor(String contestJid, String userJid, ContestPermission contestPermission, String createUserJid, String createUserIpAddress) {
        ContestModel contestModel = contestDao.findByJid(contestJid);

        ContestSupervisorModel contestSupervisorModel = new ContestSupervisorModel();
        contestSupervisorModel.contestJid = contestModel.jid;
        contestSupervisorModel.userJid = userJid;
        contestSupervisorModel.permission = contestPermission.toJSONString();

        contestSupervisorDao.persist(contestSupervisorModel, createUserJid, createUserIpAddress);

        contestDao.edit(contestModel, createUserJid, createUserIpAddress);
    }

    @Override
    public void updateContestSupervisor(long contestSupervisorId, ContestPermission contestPermission, String userJid, String userIpAddress) {
        ContestSupervisorModel contestSupervisorModel = contestSupervisorDao.findById(contestSupervisorId);
        contestSupervisorModel.permission = contestPermission.toJSONString();

        contestSupervisorDao.edit(contestSupervisorModel, userJid, userIpAddress);

        ContestModel contestModel = contestDao.findByJid(contestSupervisorModel.contestJid);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void deleteContestSupervisor(long contestSupervisorId) {
        ContestSupervisorModel contestSupervisorModel = contestSupervisorDao.findById(contestSupervisorId);

        contestSupervisorDao.remove(contestSupervisorModel);
    }

    private static ContestSupervisor createContestSupervisorFromModel(ContestSupervisorModel contestSupervisorModel) {
        return new ContestSupervisor(contestSupervisorModel.id, contestSupervisorModel.contestJid, contestSupervisorModel.userJid, ContestPermission.fromJSONString(contestSupervisorModel.permission));
    }
}
