package org.iatoki.judgels.uriel.contest.contestant;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.List;

@ImplementedBy(ContestContestantHibernateDao.class)
public interface ContestContestantDao extends Dao<Long, ContestContestantModel> {

    boolean existsInContestByContestantJid(String contestJid, String contestantJid);

    boolean existsInContestByContestantJid(String contestJid, String contestantJid, String status);

    ContestContestantModel findInContestByContestantJid(String contestJid, String contestantJid);

    long countInContest(String contestJid);

    boolean hasContestantStarted(String contestJid, String contestantJid);

    List<String> getContestJidsByJid(String contestantJid);

    List<ContestContestantModel> getAllInContest(String contestJid);
}
