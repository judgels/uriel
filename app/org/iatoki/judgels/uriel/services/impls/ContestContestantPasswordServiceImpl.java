package org.iatoki.judgels.uriel.services.impls;

import org.apache.commons.lang3.RandomStringUtils;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.uriel.models.daos.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.ContestContestantPasswordDao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantPasswordModel;
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

    private final ContestContestantDao contestContestantDao;
    private final ContestContestantPasswordDao contestContestantPasswordDao;

    @Inject
    public ContestContestantPasswordServiceImpl(ContestContestantDao contestContestantDao, ContestContestantPasswordDao contestContestantPasswordDao) {
        this.contestContestantDao = contestContestantDao;
        this.contestContestantPasswordDao = contestContestantPasswordDao;
    }

    @Override
    public void generateContestantPasswordForAllContestants(String contestJid) {
        List<ContestContestantModel> contestantModels = contestContestantDao.getAllInContest(contestJid);

        for (ContestContestantModel contestantModel : contestantModels) {
            generateNewPassword(contestJid, contestantModel);
        }
    }

    @Override
    public void generateContestantPassword(String contestJid, String contestantJid) {
        ContestContestantModel contestantModel = contestContestantDao.findInContestByJid(contestJid, contestantJid);
        generateNewPassword(contestJid, contestantModel);
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

    private void generateNewPassword(String contestJid, ContestContestantModel contestantModel) {
        String newPassword = RandomStringUtils.random(6, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (contestContestantPasswordDao.existsInContestByContestantJid(contestJid, contestantModel.userJid)) {
            ContestContestantPasswordModel existingModel = contestContestantPasswordDao.findInContestByContestantJid(contestJid, contestantModel.userJid);
            existingModel.password = newPassword;
            contestContestantPasswordDao.edit(existingModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } else {
            ContestContestantPasswordModel newModel = new ContestContestantPasswordModel();
            newModel.contestJid = contestJid;
            newModel.contestantJid = contestantModel.userJid;
            newModel.password = newPassword;
            contestContestantPasswordDao.persist(newModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }
}
