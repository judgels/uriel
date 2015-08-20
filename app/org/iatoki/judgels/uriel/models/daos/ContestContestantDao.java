package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel;

import java.util.List;

public interface ContestContestantDao extends Dao<Long, ContestContestantModel> {

    boolean existsInContestByContestantJid(String contestJid, String contestantJid);

    ContestContestantModel findInContestByJid(String contestJid, String contestantJid);

    long countInContest(String contestJid);

    boolean hasContestantStarted(String contestJid, String contestantJid);

    List<String> getContestJidsByJid(String contestantJid);

    List<ContestContestantModel> getAllInContest(String contestJid);
}
