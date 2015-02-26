package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestScoreboardModel;

public interface ContestScoreboardDao extends Dao<Long, ContestScoreboardModel> {
    ContestScoreboardModel findContestScoreboardByContestJidAndScoreboardType(String contestJid, String type);

    boolean isContestScoreboardExistByContestJidAndScoreboardType(String contestJid, String type);
}
