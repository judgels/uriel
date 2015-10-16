package org.iatoki.judgels.uriel.services.impls;

import org.apache.commons.lang3.RandomStringUtils;
import org.iatoki.judgels.uriel.models.daos.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.ContestContestantPasswordDao;
import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantPasswordModel;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.services.ContestContestantPasswordService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Singleton
@Named("contestContestantPasswordService")
public final class ContestContestantPasswordServiceImpl implements ContestContestantPasswordService {

    private final ContestDao contestDao;
    private final ContestContestantDao contestContestantDao;
    private final ContestContestantPasswordDao contestContestantPasswordDao;

    @Inject
    public ContestContestantPasswordServiceImpl(ContestDao contestDao, ContestContestantDao contestContestantDao, ContestContestantPasswordDao contestContestantPasswordDao) {
        this.contestDao = contestDao;
        this.contestContestantDao = contestContestantDao;
        this.contestContestantPasswordDao = contestContestantPasswordDao;
    }

    @Override
    public void generateContestantPasswordForAllContestants(String contestJid, String userJid, String userIpAddress) {
        List<ContestContestantModel> contestantModels = contestContestantDao.getAllInContest(contestJid);

        for (ContestContestantModel contestantModel : contestantModels) {
            generateNewPassword(contestJid, contestantModel, userJid, userIpAddress);
        }

        ContestModel contestModel = contestDao.findByJid(contestJid);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void generateContestantPassword(String contestJid, String contestantJid, String userJid, String userIpAddress) {
        ContestContestantModel contestantModel = contestContestantDao.findInContestByContestantJid(contestJid, contestantJid);
        generateNewPassword(contestJid, contestantModel, userJid, userIpAddress);

        ContestModel contestModel = contestDao.findByJid(contestJid);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public String getContestantPassword(String contestJid, String contestantJid) {
        if (!contestContestantPasswordDao.existsInContestByContestantJid(contestJid, contestantJid)) {
            return null;
        }

        return contestContestantPasswordDao.findInContestByContestantJid(contestJid, contestantJid).password;
    }

    @Override
    public Map<String, String> getContestantPasswordsMap(String contestJid, Collection<String> contestantJids) {
        return contestContestantPasswordDao.getAllMappedInContestByContestantJids(contestJid, contestantJids);
    }

    private void generateNewPassword(String contestJid, ContestContestantModel contestantModel, String userJid, String userIpAddress) {
        String newPassword = RandomStringUtils.random(6, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (contestContestantPasswordDao.existsInContestByContestantJid(contestJid, contestantModel.userJid)) {
            ContestContestantPasswordModel existingModel = contestContestantPasswordDao.findInContestByContestantJid(contestJid, contestantModel.userJid);
            existingModel.password = newPassword;
            contestContestantPasswordDao.edit(existingModel, userJid, userIpAddress);
        } else {
            ContestContestantPasswordModel newModel = new ContestContestantPasswordModel();
            newModel.contestJid = contestJid;
            newModel.contestantJid = contestantModel.userJid;
            newModel.password = newPassword;
            contestContestantPasswordDao.persist(newModel, userJid, userIpAddress);
        }
    }
}
