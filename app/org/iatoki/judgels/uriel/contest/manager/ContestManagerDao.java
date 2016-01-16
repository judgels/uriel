package org.iatoki.judgels.uriel.contest.manager;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.List;

@ImplementedBy(ContestManagerHibernateDao.class)
public interface ContestManagerDao extends Dao<Long, ContestManagerModel> {

    boolean existsInContestByJid(String contestJid, String managerJid);

    List<String> getContestJidsByJid(String managerJid);
}
