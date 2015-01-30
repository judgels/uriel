package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.UserRoleModel;

import java.util.List;

public interface UserRoleDao extends Dao<Long, UserRoleModel> {

    boolean isExistByUserJid(String userJid);

    UserRoleModel findByUserJid(String userJid);

    long countByFilter(String filterString);

    List<UserRoleModel> findByFilterAndSort(String filterString, String sortBy, String order, long first, long max);

    List<String> findUserJidByFilter(String filterString);

}
