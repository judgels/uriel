package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestScoreboardModel;

public interface ContestScoreboardDao extends Dao<Long, ContestScoreboardModel> {
    ContestScoreboardModel findContestScoreboardByContestJidAndScoreboardType(String contestJid, String type);

    boolean isContestScoreboardExistByContestJidAndScoreboardType(String contestJid, String type);
}
