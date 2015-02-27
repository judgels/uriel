package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.jophiel.commons.JophielUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.uriel.models.daos.interfaces.UserRoleDao;
import org.iatoki.judgels.uriel.models.domains.UserRoleModel;

import java.util.Arrays;
import java.util.List;

public final class UserRoleServiceImpl implements UserRoleService {

    private UserRoleDao userRoleDao;

    public UserRoleServiceImpl(UserRoleDao userRoleDao) {
        this.userRoleDao = userRoleDao;
    }

    @Override
    public boolean existsByUserJid(String userJid) {
        return userRoleDao.existsByUserJid(userJid);
    }

    @Override
    public UserRole findUserRoleById(long userRoleId) {
        UserRoleModel userRoleModel = userRoleDao.findById(userRoleId);
        return createUserRoleFromModel(userRoleModel);
    }

    @Override
    public UserRole findUserRoleByUserJid(String userJid) {
        UserRoleModel userRoleModel = userRoleDao.findByUserJid(userJid);
        return createUserRoleFromModel(userRoleModel);
    }

    @Override
    public void createUserRole(String userJid, List<String> roles) {
        UserRoleModel userRoleModel = new UserRoleModel();
        userRoleModel.userJid = userJid;
        userRoleModel.roles = StringUtils.join(roles, ",");

        userRoleDao.persist(userRoleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateUserRole(long userRoleId, List<String> roles) {
        UserRoleModel userRoleModel = userRoleDao.findById(userRoleId);
        userRoleModel.roles = StringUtils.join(roles, ",");

        userRoleDao.edit(userRoleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void deleteUserRole(long userRoleId) {
        UserRoleModel userRoleModel = userRoleDao.findById(userRoleId);
        userRoleDao.remove(userRoleModel);
    }

    @Override
    public Page<UserRole> pageUserRoles(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = userRoleDao.countByFilters(filterString, ImmutableMap.of());
        List<UserRoleModel> userRoleModels = userRoleDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), pageIndex * pageSize, pageSize);
        List<UserRole> userRoles = Lists.transform(userRoleModels, m -> createUserRoleFromModel(m));
        return new Page<>(userRoles, totalPages, pageIndex, pageSize);
    }

    @Override
    public void upsertUserRoleFromJophielUserJid(String userJid) {
        User user = JophielUtils.getUserByJid(userJid);

        if (!userRoleDao.existsByUserJid(userJid)) {
            UserRoleModel userRoleModel = new UserRoleModel();
            userRoleModel.userJid = user.getJid();
            userRoleModel.roles = "user";

            userRoleDao.edit(userRoleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        JidCacheService.getInstance().putDisplayName(user.getJid(), user.getUsername(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        AvatarCacheService.getInstance().putImageUrl(user.getJid(), user.getProfilePictureUrl(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    private UserRole createUserRoleFromModel(UserRoleModel userRoleModel) {
        return new UserRole(userRoleModel.id, userRoleModel.userJid, Arrays.asList(userRoleModel.roles.split(",")));
    }
}
