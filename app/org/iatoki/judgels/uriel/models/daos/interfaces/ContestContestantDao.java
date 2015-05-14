package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestContestantModel;

import java.util.List;

public interface ContestContestantDao extends Dao<Long, ContestContestantModel> {

    boolean existsByContestJidAndContestantJid(String contestJid, String contestantJid);

    ContestContestantModel findByContestJidAndContestantJid(String contestJid, String contestantJid);

    long countContestContestantByContestJid(String contestJid);

    boolean isContestStarted(String contestJid, String contestantJid);

    boolean isThereNewContestant(String contestJid, long lastTime);

    List<String> findContestJidsByContestantJid(String contestantJid);

    List<ContestContestantModel> findAllByContestJid(String contestJid);
}
