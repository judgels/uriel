package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.play.Page;
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
    public boolean isContestantInContest(String contestJid, String contestContestantJid) {
        return contestContestantDao.existsInContestByContestantJid(contestJid, contestContestantJid);
    }

    @Override
    public boolean hasContestantStartContest(String contestJid, String contestContestantJid) {
        return contestContestantDao.hasContestantStarted(contestJid, contestContestantJid);
    }

    @Override
    public ContestContestant findContestantInContestById(long contestContestantId) throws ContestContestantNotFoundException {
        ContestContestantModel contestContestantModel = contestContestantDao.findById(contestContestantId);
        if (contestContestantModel == null) {
            throw new ContestContestantNotFoundException("Contest Contestant not found.");
        }

        return createContestContestantFromModel(contestContestantModel);
    }

    @Override
    public ContestContestant findContestantInContestAndJid(String contestJid, String contestContestantJid) {
        ContestContestantModel contestContestantModel = contestContestantDao.findInContestByContestantJid(contestJid, contestContestantJid);
        return createContestContestantFromModel(contestContestantModel);
    }

    @Override
    public List<ContestContestant> getContestantsInContest(String contestJid) {
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFilters("id", "asc", "", ImmutableMap.of(ContestContestantModel_.contestJid, contestJid), ImmutableMap.of(), 0, -1);
        return Lists.transform(contestContestantModels, m -> createContestContestantFromModel(m));
    }

    @Override
    public Page<ContestContestant> getPageOfContestantsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestContestantDao.countByFilters(filterString, ImmutableMap.of(ContestContestantModel_.contestJid, contestJid), ImmutableMap.of());
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(ContestContestantModel_.contestJid, contestJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<ContestContestant> contestContestants = Lists.transform(contestContestantModels, m -> createContestContestantFromModel(m));

        return new Page<>(contestContestants, totalPages, pageIndex, pageSize);
    }

    @Override
    public long countContestantsInContest(String contestJid) {
        return contestContestantDao.countInContest(contestJid);
    }

    @Override
    public void createContestContestant(String contestJid, String userJid, ContestContestantStatus status, String createUserJid, String createUserIpAddress) {
        ContestModel contestModel = contestDao.findByJid(contestJid);

        ContestContestantModel contestContestantModel = new ContestContestantModel();
        contestContestantModel.contestJid = contestModel.jid;
        contestContestantModel.userJid = userJid;
        contestContestantModel.status = status.name();

        contestContestantDao.persist(contestContestantModel, createUserJid, createUserIpAddress);

        contestDao.edit(contestModel, createUserJid, createUserIpAddress);
    }

    @Override
    public void updateContestContestant(long contestContestantId, ContestContestantStatus status, String updateUserJid, String updateUserIpAddress) {
        ContestContestantModel contestContestantModel = contestContestantDao.findById(contestContestantId);
        contestContestantModel.status = status.name();

        contestContestantDao.edit(contestContestantModel, updateUserJid, updateUserIpAddress);

        ContestModel contestModel = contestDao.findByJid(contestContestantModel.contestJid);

        contestDao.edit(contestModel, updateUserJid, updateUserIpAddress);
    }

    @Override
    public void deleteContestContestant(long contestContestantId) {
        ContestContestantModel contestContestantModel = contestContestantDao.findById(contestContestantId);
        contestContestantDao.remove(contestContestantModel);
    }

    @Override
    public void startContestAsContestant(String contestJid, String userJid, String starterUserJid, String starterUserIpAddress) {
        ContestContestantModel contestContestantModel = contestContestantDao.findInContestByContestantJid(contestJid, userJid);
        if (contestContestantModel.contestStartTime == 0) {
            contestContestantModel.contestStartTime = System.currentTimeMillis();

            contestContestantDao.edit(contestContestantModel, starterUserJid, starterUserIpAddress);
        }
    }

    private static ContestContestant createContestContestantFromModel(ContestContestantModel contestContestantModel) {
        return new ContestContestant(contestContestantModel.id, contestContestantModel.contestJid, contestContestantModel.userJid, ContestContestantStatus.valueOf(contestContestantModel.status), contestContestantModel.contestStartTime);
    }
}
