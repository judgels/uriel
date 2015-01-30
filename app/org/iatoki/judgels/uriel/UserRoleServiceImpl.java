package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableList;
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
    public boolean isUserRoleExist(String userJid) {
        return userRoleDao.isExistByUserJid(userJid);
    }

    @Override
    public UserRole findUserRoleById(long userRoleId) {
        UserRoleModel userRoleModel = userRoleDao.findById(userRoleId);
        UserRole userRole = new UserRole(userRoleModel.id, userRoleModel.userJid, userRoleModel.username, userRoleModel.alias, Arrays.asList(userRoleModel.roles.split(",")));

        return userRole;
    }

    @Override
    public UserRole findUserRoleByUserJid(String userJid) {
        UserRoleModel userRoleModel = userRoleDao.findByUserJid(userJid);
        UserRole userRole = new UserRole(userRoleModel.id, userRoleModel.userJid, userRoleModel.username, userRoleModel.alias, Arrays.asList(userRoleModel.roles.split(",")));

        return userRole;
    }

    @Override
    public void createUserRole(String userJid, String username, List<String> roles) {
        UserRoleModel userRoleModel = new UserRoleModel();
        userRoleModel.userJid = userJid;
        userRoleModel.username = username;
        userRoleModel.alias = username;
        userRoleModel.roles = StringUtils.join(roles, ",");

        userRoleDao.persist(userRoleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateUserRole(String userJid, String username) {
        UserRoleModel userRoleModel = userRoleDao.findByUserJid(userJid);
        userRoleModel.userJid = userJid;
        userRoleModel.username = username;

        userRoleDao.edit(userRoleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateUserRole(long userRoleId, String alias, List<String> roles) {
        UserRoleModel userRoleModel = userRoleDao.findById(userRoleId);
        userRoleModel.alias = alias;
        userRoleModel.roles = StringUtils.join(roles, ",");

        userRoleDao.edit(userRoleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void deleteUserRole(long userRoleId) {
        UserRoleModel userRoleModel = userRoleDao.findById(userRoleId);
        userRoleDao.remove(userRoleModel);
    }

    @Override
    public Page<UserRole> pageUserRole(long page, long pageSize, String sortBy, String order, String filterString) {
        long totalPage = userRoleDao.countByFilter(filterString);
        List<UserRoleModel> userRoleModels = userRoleDao.findByFilterAndSort(filterString, sortBy, order, page * pageSize, pageSize);
        ImmutableList.Builder<UserRole> listBuilder = ImmutableList.builder();

        for (UserRoleModel userRoleModel : userRoleModels) {
            listBuilder.add(new UserRole(userRoleModel.id, userRoleModel.userJid, userRoleModel.username, userRoleModel.alias, Arrays.asList(userRoleModel.roles.split(","))));
        }

        Page<UserRole> ret = new Page<>(listBuilder.build(), totalPage, page, pageSize);
        return ret;
    }

    @Override
    public void upsertUserRoleFromJophielUserJid(String userJid) {
        User user = JophielUtils.getUserByJid(userJid);

        if (userRoleDao.isExistByUserJid(userJid)) {
            UserRoleModel userRoleModel = userRoleDao.findByUserJid(userJid);
            userRoleModel.username = user.getUsername();

            userRoleDao.persist(userRoleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } else {
            UserRoleModel userRoleModel = new UserRoleModel();
            userRoleModel.userJid = user.getJid();
            userRoleModel.username = user.getUsername();
            userRoleModel.alias = user.getName();
            userRoleModel.roles = "user";

            userRoleDao.edit(userRoleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

}
