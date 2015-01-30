package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.Page;

import java.util.List;

public interface UserRoleService {

    boolean isUserRoleExist(String userJid);

    UserRole findUserRoleById(long userRoleId);

    UserRole findUserRoleByUserJid(String userJid);

    void createUserRole(String userJid, String username, List<String> roles);

    void updateUserRole(String userJid, String username);

    void updateUserRole(long userRoleId, String alias, List<String> roles);

    void deleteUserRole(long userRoleId);

    Page<UserRole> pageUserRole(long page, long pageSize, String sortBy, String order, String filterString);

    void upsertUserRoleFromJophielUserJid(String userJid);

}