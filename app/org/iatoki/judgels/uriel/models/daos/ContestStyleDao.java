package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestStyleModel;

public interface ContestStyleDao extends Dao<Long, ContestStyleModel> {

    ContestStyleModel findInContest(String contestJid);
}
