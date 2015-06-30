package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantNotFoundException;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.models.daos.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel_;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.services.ContestContestantService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named("contestContestantService")
public final class ContestContestantServiceImpl implements ContestContestantService {

    private final ContestDao contestDao;
    private final ContestContestantDao contestContestantDao;

    @Inject
    public ContestContestantServiceImpl(ContestDao contestDao, ContestContestantDao contestContestantDao) {
        this.contestDao = contestDao;
        this.contestContestantDao = contestContestantDao;
    }

    @Override
    public boolean isContestContestantInContestByUserJid(String contestJid, String contestContestantJid) {
        return contestContestantDao.existsByContestJidAndContestantJid(contestJid, contestContestantJid);
    }

    @Override
    public boolean isContestStarted(String contestJid, String contestContestantJid) {
        return contestContestantDao.isContestStarted(contestJid, contestContestantJid);
    }

    @Override
    public ContestContestant findContestContestantByContestContestantId(long contestContestantId) throws ContestContestantNotFoundException {
        ContestContestantModel contestContestantModel = contestContestantDao.findById(contestContestantId);
        if (contestContestantModel != null) {
            return createContestContestantFromModel(contestContestantModel);
        } else {
            throw new ContestContestantNotFoundException("Contest Contestant not found.");
        }
    }

    @Override
    public ContestContestant findContestContestantByContestJidAndContestContestantJid(String contestJid, String contestContestantJid) {
        ContestContestantModel contestContestantModel = contestContestantDao.findByContestJidAndContestantJid(contestJid, contestContestantJid);
        return createContestContestantFromModel(contestContestantModel);
    }

    @Override
    public List<ContestContestant> findAllContestContestantsByContestJid(String contestJid) {
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFilters("id", "asc", "", ImmutableMap.of(ContestContestantModel_.contestJid, contestJid), ImmutableMap.of(), 0, -1);
        return Lists.transform(contestContestantModels, m -> createContestContestantFromModel(m));
    }

    @Override
    public Page<ContestContestant> pageContestContestantsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestContestantDao.countByFilters(filterString, ImmutableMap.of(ContestContestantModel_.contestJid, contestJid), ImmutableMap.of());
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(ContestContestantModel_.contestJid, contestJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<ContestContestant> contestContestants = Lists.transform(contestContestantModels, m -> createContestContestantFromModel(m));

        return new Page<>(contestContestants, totalPages, pageIndex, pageSize);
    }

    @Override
    public long getContestContestantCount(String contestJid) {
        return contestContestantDao.countContestContestantByContestJid(contestJid);
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
    public void deleteContestContestant(long contestContestantId) {
        ContestContestantModel contestContestantModel = contestContestantDao.findById(contestContestantId);
        contestContestantDao.remove(contestContestantModel);
    }

    @Override
    public void startContestAsContestant(String contestJid, String userJid) {
        ContestContestantModel contestContestantModel = contestContestantDao.findByContestJidAndContestantJid(contestJid, userJid);
        if (contestContestantModel.contestStartTime == 0) {
            contestContestantModel.contestStartTime = System.currentTimeMillis();

            contestContestantDao.edit(contestContestantModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    private ContestContestant createContestContestantFromModel(ContestContestantModel contestContestantModel) {
        return new ContestContestant(contestContestantModel.id, contestContestantModel.contestJid, contestContestantModel.userJid, ContestContestantStatus.valueOf(contestContestantModel.status), contestContestantModel.contestStartTime);
    }
}
