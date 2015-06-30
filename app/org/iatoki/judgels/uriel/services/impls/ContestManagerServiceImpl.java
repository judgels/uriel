package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.uriel.ContestManager;
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
    public boolean isContestManagerInContestByUserJid(String contestJid, String contestManagerJid) {
        return contestManagerDao.existsByContestJidAndManagerJid(contestJid, contestManagerJid);
    }

    @Override
    public Page<ContestManager> pageContestManagersByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestManagerDao.countByFilters(filterString, ImmutableMap.of(ContestManagerModel_.contestJid, contestJid), ImmutableMap.of());
        List<ContestManagerModel> contestManagerModels = contestManagerDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(ContestManagerModel_.contestJid, contestJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);
        List<ContestManager> contestManagers = Lists.transform(contestManagerModels, m -> createContestManagerFromModel(m));

        return new Page<>(contestManagers, totalPages, pageIndex, pageSize);
    }

    @Override
    public void createContestManager(long contestId, String userJid) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestManagerModel contestManagerModel = new ContestManagerModel();
        contestManagerModel.contestJid = contestModel.jid;
        contestManagerModel.userJid = userJid;

        contestManagerDao.persist(contestManagerModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    private ContestManager createContestManagerFromModel(ContestManagerModel contestManagerModel) {
        return new ContestManager(contestManagerModel.id, contestManagerModel.contestJid, contestManagerModel.userJid);
    }
}
