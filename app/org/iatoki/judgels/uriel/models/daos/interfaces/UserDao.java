package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.UserModel;

public interface UserDao extends Dao<Long, UserModel> {

    boolean existsByUserJid(String userJid);

    UserModel findByUserJid(String userJid);
}
