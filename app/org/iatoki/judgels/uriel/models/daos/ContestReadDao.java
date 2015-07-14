package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestReadModel;

import java.util.List;

public interface ContestReadDao extends Dao<Long, ContestReadModel> {

    boolean existByUserJidAndTypeAndJid(String userJid, String type, String jid);

    long countReadByUserJidAndTypeAndJidList(String userJid, String type, List<String> JidList);

}
