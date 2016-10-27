package org.iatoki.judgels.uriel.user;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.jophiel.activity.BasicActivityKeys;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.UrielControllerUtils;
import org.iatoki.judgels.uriel.contest.problem.ContestProblemService;
import org.iatoki.judgels.uriel.contest.problem.ContestProblemStatus;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.Authorized;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.jid.JidCacheServiceImpl;
import org.iatoki.judgels.uriel.user.html.addUserView;
import org.iatoki.judgels.uriel.user.html.editUserView;
import org.iatoki.judgels.uriel.user.html.listUsersView;
import org.iatoki.judgels.uriel.user.html.viewUserView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
public final class UserController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String USER = "user";

    private final JophielPublicAPI jophielPublicAPI;
    private final UserService userService;

    private final ContestProblemService contestProblemService;

    @Inject
    public UserController(JophielPublicAPI jophielPublicAPI, UserService userService, ContestProblemService contestProblemService) {
        this.jophielPublicAPI = jophielPublicAPI;
        this.userService = userService;
        this.contestProblemService = contestProblemService;
    }

    public Result index() {

        String contestJid = "";
        String[] jids = {};
        String[] secrets = {};

        for (int i = 0; i < jids.length; i++) {
            String alias = "" + (char)('A' + i);

            contestProblemService.createContestProblem(contestJid, jids[i], secrets[i], alias, 0, ContestProblemStatus.OPEN, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        return listUsers(0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listUsers(long pageIndex, String sortBy, String orderBy, String filterString) {
        Page<User> pageOfUsers = userService.getPageOfUsers(pageIndex, PAGE_SIZE, sortBy, orderBy, filterString);

        LazyHtml content = new LazyHtml(listUsersView.render(pageOfUsers, sortBy, orderBy, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.list"), new InternalLink(Messages.get("commons.create"), routes.UserController.addUser()), c));
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index())
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Users - List");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result addUser() {
        UserAddForm userAddData = new UserAddForm();
        userAddData.roles = StringUtils.join(UrielUtils.getDefaultRoles(), ",");
        Form<UserAddForm> userCreateForm = Form.form(UserAddForm.class).fill(userAddData);

        return showAddUser(userCreateForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postAddUser() {
        Form<UserAddForm> userCreateForm = Form.form(UserAddForm.class).bindFromRequest();

        if (formHasErrors(userCreateForm)) {
            return showAddUser(userCreateForm);
        }

        UserAddForm userCreateData = userCreateForm.get();
        JophielUser jophielUser;
        try {
            jophielUser = jophielPublicAPI.findUserByUsername(userCreateData.username);
        } catch (JudgelsAPIClientException e) {
            jophielUser = null;
        }

        if (jophielUser == null) {
            userCreateForm.reject(Messages.get("user.create.error.usernameNotFound"));
            return showAddUser(userCreateForm);
        }

        if (userService.existsByUserJid(jophielUser.getJid())) {
            userCreateForm.reject(Messages.get("user.create.error.userAlreadyExists"));
            return showAddUser(userCreateForm);
        }

        userService.upsertUserFromJophielUser(jophielUser, userCreateData.getRolesAsList(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.ADD.construct(USER, jophielUser.getJid(), jophielUser.getUsername()));

        return redirect(routes.UserController.index());
    }

    @Transactional(readOnly = true)
    public Result viewUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);

        LazyHtml content = new LazyHtml(viewUserView.render(user));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.user") + " #" + user.getId() + ": " + JidCacheServiceImpl.getInstance().getDisplayName(user.getUserJid()), new InternalLink(Messages.get("commons.update"), routes.UserController.editUser(user.getId())), c));
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.view"), routes.UserController.viewUser(user.getId()))
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "User - View");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        UserEditForm userEditData = new UserEditForm();
        userEditData.roles = StringUtils.join(user.getRoles(), ",");
        Form<UserEditForm> userEditForm = Form.form(UserEditForm.class).fill(userEditData);

        return showEditUser(userEditForm, user);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        Form<UserEditForm> userEditForm = Form.form(UserEditForm.class).bindFromRequest();

        if (formHasErrors(userEditForm)) {
            return showEditUser(userEditForm, user);
        }

        UserEditForm userEditData = userEditForm.get();
        userService.updateUser(user.getUserJid(), userEditData.getRolesAsList(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.EDIT.construct(USER, user.getUserJid(), JidCacheServiceImpl.getInstance().getDisplayName(user.getUserJid())));

        return redirect(routes.UserController.index());
    }

    @Transactional
    public Result removeUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        userService.deleteUser(user.getUserJid());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.REMOVE.construct(USER, user.getUserJid(), JidCacheServiceImpl.getInstance().getDisplayName(user.getUserJid())));

        return redirect(routes.UserController.index());
    }

    private Result showAddUser(Form<UserAddForm> userCreateForm) {
        LazyHtml content = new LazyHtml(addUserView.render(userCreateForm, jophielPublicAPI.getUserAutocompleteAPIEndpoint()));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.create"), c));
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.create"), routes.UserController.addUser())
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "User - Create");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditUser(Form<UserEditForm> userEditForm, User user) {
        LazyHtml content = new LazyHtml(editUserView.render(userEditForm, user.getId()));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.user") + " #" + user.getId() + ": " + JidCacheServiceImpl.getInstance().getDisplayName(user.getUserJid()), c));
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.update"), routes.UserController.editUser(user.getId()))
        ));
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "User - Update");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }
}
