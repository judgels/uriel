package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestReadModel;

import java.util.List;

public interface ContestReadDao extends Dao<Long, ContestReadModel> {

    boolean existByUserJidAndTypeAndId(String userJid, String type, long id);

    long countReadByUserJidAndTypeAndIdList(String userJid, String type, List<Long> IdList);

}
