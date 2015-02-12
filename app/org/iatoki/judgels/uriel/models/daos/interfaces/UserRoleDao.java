package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.UserRoleModel;

public interface UserRoleDao extends Dao<Long, UserRoleModel> {

    boolean existsByUserJid(String userJid);

    UserRoleModel findByUserJid(String userJid);
}
