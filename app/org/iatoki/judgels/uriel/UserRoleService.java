package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.Page;

import java.util.List;

public interface UserRoleService {

    boolean existsByUserJid(String userJid);

    UserRole findUserRoleById(long userRoleId);

    UserRole findUserRoleByUserJid(String userJid);

    void createUserRole(String userJid, List<String> roles);

    void updateUserRole(long userRoleId, List<String> roles);

    void deleteUserRole(long userRoleId);

    Page<UserRole> pageUserRoles(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void upsertUserRoleFromJophielUserJid(String userJid);
}