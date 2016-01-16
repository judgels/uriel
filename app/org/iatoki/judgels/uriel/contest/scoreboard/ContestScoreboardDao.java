package org.iatoki.judgels.uriel.contest.scoreboard;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(ContestScoreboardHibernateDao.class)
public interface ContestScoreboardDao extends Dao<Long, ContestScoreboardModel> {

    ContestScoreboardModel findInContestByScoreboardType(String contestJid, String type);

    boolean isContestScoreboardExistByContestJidAndScoreboardType(String contestJid, String type);
}
