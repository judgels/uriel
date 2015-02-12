package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestContestantModel;

import java.util.List;

public interface ContestContestantDao extends Dao<Long, ContestContestantModel> {

    boolean existsByContestantJid(String contestJid, String contestantJid);

    ContestContestantModel findByContestantJid(String contestId, String contestantJid);
}
