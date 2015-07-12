package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.uriel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.User;
import org.iatoki.judgels.uriel.forms.UserCreateForm;
import org.iatoki.judgels.uriel.UserNotFoundException;
import org.iatoki.judgels.uriel.services.UserService;
import org.iatoki.judgels.uriel.forms.UserUpdateForm;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.Authorized;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.views.html.user.createUserView;
import org.iatoki.judgels.uriel.views.html.user.listUsersView;
import org.iatoki.judgels.uriel.views.html.user.updateUserView;
import org.iatoki.judgels.uriel.views.html.user.viewUserView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = {"admin"})
@Singleton
@Named
public final class UserController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final Jophiel jophiel;
    private final UserService userService;

    @Inject
    public UserController(Jophiel jophiel, UserService userService) {
        this.jophiel = jophiel;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Result index() {
        return listUsers(0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listUsers(long pageIndex, String sortBy, String orderBy, String filterString) {
        Page<User> currentPage = userService.pageUsers(pageIndex, PAGE_SIZE, sortBy, orderBy, filterString);

        LazyHtml content = new LazyHtml(listUsersView.render(currentPage, sortBy, orderBy, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.list"), new InternalLink(Messages.get("commons.create"), routes.UserController.createUser()), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Users - List");

        ControllerUtils.getInstance().addActivityLog("List all users <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createUser() {
        UserCreateForm userCreateForm = new UserCreateForm(UrielUtils.getDefaultRoles());
        Form<UserCreateForm> form = Form.form(UserCreateForm.class).fill(userCreateForm);

        ControllerUtils.getInstance().addActivityLog("Try to create user <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showCreateUser(form);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateUser() {
        Form<UserCreateForm> form = Form.form(UserCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateUser(form);
        } else {
            UserCreateForm userCreateForm = form.get();
            try {
                String userJid = jophiel.verifyUsername(userCreateForm.username);
                if (userJid == null) {
                    form.reject(Messages.get("user.create.error.usernameNotFound"));
                    return showCreateUser(form);
                } else {
                    if (userService.existsByUserJid(userJid)) {
                        form.reject(Messages.get("user.create.error.userAlreadyExists"));
                        return showCreateUser(form);
                    } else {
                        userService.upsertUserFromJophielUserJid(userJid, userCreateForm.getRolesAsList());

                        ControllerUtils.getInstance().addActivityLog("Create user " + userJid + ".");

                        return redirect(routes.UserController.index());
                    }
                }
            } catch (IOException e) {
                form.reject(Messages.get("user.create.error.usernameNotFound"));
                return showCreateUser(form);
            }
        }
    }

    @Transactional(readOnly = true)
    public Result viewUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        LazyHtml content = new LazyHtml(viewUserView.render(user));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.user") + " #" + user.getId() + ": " + JidCacheServiceImpl.getInstance().getDisplayName(user.getUserJid()), new InternalLink(Messages.get("commons.update"), routes.UserController.updateUser(user.getId())), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.view"), routes.UserController.viewUser(user.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User - View");

        ControllerUtils.getInstance().addActivityLog("View user " + user.getUserJid() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        UserUpdateForm userUpdateForm = new UserUpdateForm(user.getRoles());
        Form<UserUpdateForm> form = Form.form(UserUpdateForm.class).fill(userUpdateForm);

        ControllerUtils.getInstance().addActivityLog("Try to update user " + user.getUserJid() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateUser(form, user);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        Form<UserUpdateForm> form = Form.form(UserUpdateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showUpdateUser(form, user);
        } else {
            UserUpdateForm userUpdateForm = form.get();
            userService.updateUser(user.getId(), userUpdateForm.getRolesAsList());

            ControllerUtils.getInstance().addActivityLog("Update user " + user.getUserJid() + ".");

            return redirect(routes.UserController.index());
        }
    }

    @Transactional
    public Result deleteUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        userService.deleteUser(user.getId());

        ControllerUtils.getInstance().addActivityLog("Delete user " + user.getUserJid() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.UserController.index());
    }

    private Result showCreateUser(Form<UserCreateForm> form) {
        LazyHtml content = new LazyHtml(createUserView.render(form, jophiel.getAutoCompleteEndPoint()));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.create"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.create"), routes.UserController.createUser())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateUser(Form<UserUpdateForm> form, User user) {
        LazyHtml content = new LazyHtml(updateUserView.render(form, user.getId()));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.user") + " #" + user.getId() + ": " + JidCacheServiceImpl.getInstance().getDisplayName(user.getUserJid()), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.update"), routes.UserController.updateUser(user.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User - Update");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
