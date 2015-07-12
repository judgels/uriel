package org.iatoki.judgels.uriel.services.impls;

import org.apache.commons.lang3.RandomStringUtils;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.uriel.models.daos.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.ContestContestantPasswordDao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantPasswordModel;
import org.iatoki.judgels.uriel.services.ContestPasswordService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Singleton
@Named("contestPasswordService")
public final class ContestPasswordServiceImpl implements ContestPasswordService {

    private final ContestContestantDao contestContestantDao;
    private final ContestContestantPasswordDao contestContestantPasswordDao;

    @Inject
    public ContestPasswordServiceImpl(ContestContestantDao contestContestantDao, ContestContestantPasswordDao contestContestantPasswordDao) {
        this.contestContestantDao = contestContestantDao;
        this.contestContestantPasswordDao = contestContestantPasswordDao;
    }

    @Override
    public void generateContestantPasswordForAllContestants(String contestJid) {
        List<ContestContestantModel> contestantModels = contestContestantDao.findAllByContestJid(contestJid);

        for (ContestContestantModel contestantModel : contestantModels) {
            generateNewPassword(contestJid, contestantModel);
        }
    }

    @Override
    public void generateContestantPassword(String contestJid, String contestantJid) {
        ContestContestantModel contestantModel = contestContestantDao.findByContestJidAndContestantJid(contestJid, contestantJid);
        generateNewPassword(contestJid, contestantModel);
    }

    @Override
    public String getContestantPassword(String contestJid, String contestantJid) {
        if (contestContestantPasswordDao.existsByContestJidAndContestantJid(contestJid, contestantJid)) {
            return contestContestantPasswordDao.findByContestJidAndContestantJid(contestJid, contestantJid).password;
        } else {
            return null;
        }
    }

    @Override
    public Map<String, String> getContestantPasswordsMap(String contestJid, Collection<String> contestantJids) {
        return contestContestantPasswordDao.getContestantPasswordsByContestJidAndContestantJids(contestJid, contestantJids);
    }

    private void generateNewPassword(String contestJid, ContestContestantModel contestantModel) {
        String newPassword = RandomStringUtils.random(6, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (contestContestantPasswordDao.existsByContestJidAndContestantJid(contestJid, contestantModel.userJid)) {
            ContestContestantPasswordModel existingModel = contestContestantPasswordDao.findByContestJidAndContestantJid(contestJid, contestantModel.userJid);
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
