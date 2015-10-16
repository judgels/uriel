package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestManager;
import org.iatoki.judgels.uriel.ContestManagerNotFoundException;
import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.daos.ContestManagerDao;
import org.iatoki.judgels.uriel.models.entities.ContestManagerModel;
import org.iatoki.judgels.uriel.models.entities.ContestManagerModel_;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.services.ContestManagerService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named("contestManagerService")
public final class ContestManagerServiceImpl implements ContestManagerService {

    private final ContestDao contestDao;
    private final ContestManagerDao contestManagerDao;

    @Inject
    public ContestManagerServiceImpl(ContestDao contestDao, ContestManagerDao contestManagerDao) {
        this.contestDao = contestDao;
        this.contestManagerDao = contestManagerDao;
    }

    @Override
    public boolean isManagerInContest(String contestJid, String contestManagerJid) {
        return contestManagerDao.existsInContestByJid(contestJid, contestManagerJid);
    }

    @Override
    public ContestManager findContestManagerById(long contestManagerId) throws ContestManagerNotFoundException {
        ContestManagerModel contestManagerModel = contestManagerDao.findById(contestManagerId);

        if (contestManagerModel == null) {
            throw new ContestManagerNotFoundException("Contest Manager Not Found.");
        }

        return createContestManagerFromModel(contestManagerModel);
    }

    @Override
    public Page<ContestManager> getPageOfManagersInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestManagerDao.countByFiltersEq(filterString, ImmutableMap.of(ContestManagerModel_.contestJid, contestJid));
        List<ContestManagerModel> contestManagerModels = contestManagerDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(ContestManagerModel_.contestJid, contestJid), pageIndex * pageSize, pageSize);
        List<ContestManager> contestManagers = Lists.transform(contestManagerModels, m -> createContestManagerFromModel(m));

        return new Page<>(contestManagers, totalPages, pageIndex, pageSize);
    }

    @Override
    public void createContestManager(String contestJid, String userJid, String createUserJid, String createUserIpAddress) {
        ContestModel contestModel = contestDao.findByJid(contestJid);

        ContestManagerModel contestManagerModel = new ContestManagerModel();
        contestManagerModel.contestJid = contestModel.jid;
        contestManagerModel.userJid = userJid;

        contestManagerDao.persist(contestManagerModel, createUserJid, createUserIpAddress);

        contestDao.edit(contestModel, createUserJid, createUserIpAddress);
    }

    @Override
    public void deleteContestManager(long contestManagerId) {
        ContestManagerModel contestManagerModel = contestManagerDao.findById(contestManagerId);

        contestManagerDao.remove(contestManagerModel);
    }

    private static ContestManager createContestManagerFromModel(ContestManagerModel contestManagerModel) {
        return new ContestManager(contestManagerModel.id, contestManagerModel.contestJid, contestManagerModel.userJid);
    }
}
