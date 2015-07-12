package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.UserInfo;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Singleton
@Named("userService")
public final class UserServiceImpl implements UserService {

    private final Jophiel jophiel;
    private final UserDao userDao;

    @Inject
    public UserServiceImpl(Jophiel jophiel, UserDao userDao) {
        this.jophiel = jophiel;
        this.userDao = userDao;
    }

    @Override
    public void upsertUser(String userJid, String accessToken, String idToken, String refreshToken, long expireTime) {
        if (userDao.existsByUserJid(userJid)) {
            UserModel userModel = userDao.findByUserJid(userJid);

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
        return userDao.existsByUserJid(userJid);
    }

    @Override
    public User findUserById(long userId) throws UserNotFoundException {
        UserModel userModel = userDao.findById(userId);
        if (userModel != null) {
            return createUserFromUserModel(userModel);
        } else {
            throw new UserNotFoundException("User not found.");
        }
    }

    @Override
    public User findUserByUserJid(String userJid) {
        UserModel userModel = userDao.findByUserJid(userJid);
        return createUserFromUserModel(userModel);
    }

    @Override
    public void createUser(String userJid, List<String> roles) {
        UserModel userModel = new UserModel();
        userModel.userJid = userJid;
        userModel.roles = StringUtils.join(roles, ",");

        userDao.persist(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateUser(long userId, List<String> roles) {
        UserModel userModel = userDao.findById(userId);
        userModel.roles = StringUtils.join(roles, ",");

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void deleteUser(long userId) {
        UserModel userModel = userDao.findById(userId);
        userDao.remove(userModel);
    }

    @Override
    public Page<User> pageUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = userDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
        List<UserModel> userModels = userDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);
        List<User> users = Lists.transform(userModels, m -> createUserFromUserModel(m));
        return new Page<>(users, totalPages, pageIndex, pageSize);
    }

    @Override
    public void upsertUserFromJophielUserJid(String userJid) {
        upsertUserFromJophielUserJid(userJid, UrielUtils.getDefaultRoles());
    }

    @Override
    public void upsertUserFromJophielUserJid(String userJid, List<String> roles) {
        try {
            UserInfo user = jophiel.getUserByUserJid(userJid);

            if (!userDao.existsByUserJid(userJid))
                createUser(user.getJid(), roles);

            JidCacheServiceImpl.getInstance().putDisplayName(user.getJid(), JudgelsUtils.getUserDisplayName(user.getUsername(), user.getName()), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            AvatarCacheServiceImpl.getInstance().putImageUrl(user.getJid(), user.getProfilePictureUrl(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } catch (IOException e) {
            // do nothing
        }
    }

    @Override
    public UserTokens getUserTokensByUserJid(String userJid) {
        UserModel userModel = userDao.findByUserJid(userJid);

        return createUserTokensFromUserModel(userModel);
    }

    private UserTokens createUserTokensFromUserModel(UserModel userModel) {
        return new UserTokens(userModel.userJid, userModel.accessToken, userModel.refreshToken, userModel.idToken, userModel.expirationTime);
    }

    private User createUserFromUserModel(UserModel userModel) {
        return new User(userModel.id, userModel.userJid, Arrays.asList(userModel.roles.split(",")));
    }
}
