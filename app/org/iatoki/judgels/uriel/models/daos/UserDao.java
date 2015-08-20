package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.UserModel;

public interface UserDao extends Dao<Long, UserModel> {

    boolean existsByJid(String userJid);

    UserModel findByJid(String userJid);
}
