package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestContestantModel;

public interface ContestContestantDao extends Dao<Long, ContestContestantModel> {

    boolean existsByContestantJid(String contestJid, String contestantJid);

    ContestContestantModel findByContestantJid(String contestId, String contestantJid);

    long countContestContestantByContestJid(String contestJid);

    boolean isContestEntered(String contestJid, String contestantJid);

    boolean isThereNewContestant(String contestJid, long lastTime);
}
