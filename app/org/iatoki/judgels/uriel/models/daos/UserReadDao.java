package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.UserReadModel;

import java.util.Collection;

public interface UserReadDao extends Dao<Long, UserReadModel> {

    boolean existsByUserJidAndTypeAndJid(String userJid, String type, String jid);

    long countReadByUserJidAndTypeAndJids(String userJid, String type, Collection<String> jids);
}
