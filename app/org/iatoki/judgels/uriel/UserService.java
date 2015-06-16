package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jophiel.services.BaseUserService;

import java.util.List;

public interface UserService extends BaseUserService {

    User findUserById(long userId) throws UserNotFoundException;

    User findUserByUserJid(String userJid);

    void createUser(String userJid, List<String> roles);

    void updateUser(long userId, List<String> roles);

    void deleteUser(long userId);

    Page<User> pageUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void upsertUserFromJophielUserJid(String userJid);

    void upsertUserFromJophielUserJid(String userJid, List<String> roles);

}
