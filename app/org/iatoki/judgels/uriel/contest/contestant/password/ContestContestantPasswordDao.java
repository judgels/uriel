package org.iatoki.judgels.uriel.contest.contestant.password;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.Collection;
import java.util.Map;

@ImplementedBy(ContestContestantPasswordHibernateDao.class)
public interface ContestContestantPasswordDao extends Dao<Long, ContestContestantPasswordModel> {

    boolean existsInContestByContestantJid(String contestJid, String contestantJid);

    ContestContestantPasswordModel findInContestByContestantJid(String contestJid, String contestantJid);

    Map<String, String> getAllMappedInContestByContestantJids(String contestJid, Collection<String> contestantJids);
}
