package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestReadModel;

import java.util.List;

public interface ContestReadDao extends Dao<Long, ContestReadModel> {

    boolean existByUserJidAndTypeAndId(String userJid, String type, long id);

    long countReadByUserJidAndTypeAndIdList(String userJid, String type, List<Long> IdList);

}
