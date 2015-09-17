package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jophiel.UserTokens;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.User;
import org.iatoki.judgels.uriel.UserNotFoundException;
import org.iatoki.judgels.uriel.models.daos.UserDao;
import org.iatoki.judgels.uriel.models.entities.UserModel;
import org.iatoki.judgels.uriel.services.UserService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

@Singleton
@Named("userService")
public final class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Inject
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void upsertUser(String userJid, String accessToken, String idToken, String refreshToken, long expireTime) {
        if (userDao.existsByJid(userJid)) {
            UserModel userModel = userDao.findByJid(userJid);

            userModel.accessToken = accessToken;
            userModel.refreshToken = refreshToken;
            userModel.idToken = idToken;
            userModel.expirationTime = expireTime;

            userDao.edit(userModel, "guest", IdentityUtils.getIpAddress());
        } else {
            UserModel userModel = new UserModel();
            userModel.userJid = userJid;
            userModel.roles = StringUtils.join(UrielUtils.getDefaultRoles(), ",");

            userModel.accessToken = accessToken;
            userModel.refreshToken = refreshToken;
            userModel.idToken = idToken;
            userModel.expirationTime = expireTime;

            userDao.persist(userModel, "guest", IdentityUtils.getIpAddress());
        }
    }

    @Override
    public boolean existsByUserJid(String userJid) {
        return userDao.existsByJid(userJid);
    }

    @Override
    public org.iatoki.judgels.uriel.User findUserById(long userId) throws UserNotFoundException {
        UserModel userModel = userDao.findById(userId);
        if (userModel == null) {
            throw new UserNotFoundException("User not found.");
        }

        return createUserFromUserModel(userModel);
    }

    @Override
    public org.iatoki.judgels.uriel.User findUserByJid(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);
        return createUserFromUserModel(userModel);
    }

    @Override
    public void createUser(String userJid, List<String> roles, String createUserJid, String createUserIpAddress) {
        UserModel userModel = new UserModel();
        userModel.userJid = userJid;
        userModel.roles = StringUtils.join(roles, ",");

        userDao.persist(userModel, createUserJid, createUserIpAddress);
    }

    @Override
    public void updateUser(String userJid, List<String> roles, String updateUserJid, String updateUserIpAddress) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.roles = StringUtils.join(roles, ",");

        userDao.edit(userModel, updateUserJid, updateUserIpAddress);
    }

    @Override
    public void deleteUser(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);
        userDao.remove(userModel);
    }

    @Override
    public Page<org.iatoki.judgels.uriel.User> getPageOfUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = userDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
        List<UserModel> userModels = userDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);
        List<org.iatoki.judgels.uriel.User> users = Lists.transform(userModels, m -> createUserFromUserModel(m));
        return new Page<>(users, totalPages, pageIndex, pageSize);
    }

    @Override
    public void upsertUserFromJophielUser(JophielUser jophielUser, String upsertUserJid, String upsertUserIpAddress) {
        upsertUserFromJophielUser(jophielUser, UrielUtils.getDefaultRoles(), upsertUserJid, upsertUserIpAddress);
    }

    @Override
    public void upsertUserFromJophielUser(JophielUser jophielUser, List<String> roles, String upsertUserJid, String upsertUserIpAddress) {
        if (!userDao.existsByJid(jophielUser.getJid())) {
            createUser(jophielUser.getJid(), roles, upsertUserJid, upsertUserIpAddress);
        }

        JidCacheServiceImpl.getInstance().putDisplayName(jophielUser.getJid(), JudgelsPlayUtils.getUserDisplayName(jophielUser.getUsername()), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        AvatarCacheServiceImpl.getInstance().putImageUrl(jophielUser.getJid(), jophielUser.getProfilePictureUrl(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public UserTokens getUserTokensByUserJid(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);

        return createUserTokensFromUserModel(userModel);
    }

    private static UserTokens createUserTokensFromUserModel(UserModel userModel) {
        return new UserTokens(userModel.userJid, userModel.accessToken, userModel.refreshToken, userModel.idToken, userModel.expirationTime);
    }

    private static User createUserFromUserModel(UserModel userModel) {
        return new User(userModel.id, userModel.userJid, Arrays.asList(userModel.roles.split(",")));
    }
}
