package org.iatoki.judgels.uriel.contest.style;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(ContestStyleHibernateDao.class)
public interface ContestStyleDao extends Dao<Long, ContestStyleModel> {

    ContestStyleModel findInContest(String contestJid);
}
