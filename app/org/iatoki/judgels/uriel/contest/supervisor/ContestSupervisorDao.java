package org.iatoki.judgels.uriel.contest.supervisor;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.List;

@ImplementedBy(ContestSupervisorHibernateDao.class)
public interface ContestSupervisorDao extends Dao<Long, ContestSupervisorModel> {

    boolean existsInContestByJid(String contestJid, String supervisorJid);

    ContestSupervisorModel findInContestByJid(String contestJid, String supervisorJid);

    List<String> getContestJidsByJid(String supervisorJid);
}
