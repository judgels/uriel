package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.uriel.User;
import org.iatoki.judgels.uriel.UserService;
import org.iatoki.judgels.uriel.UserUpdateForm;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.Authorized;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.user.listUsersView;
import org.iatoki.judgels.uriel.views.html.user.updateUserView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Arrays;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = {"admin"})
public final class UserController extends Controller {

    private static final long PAGE_SIZE = 20;
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public Result index() {
        return listUsers(0, "id", "asc", "");
    }

    public Result listUsers(long page, String sortBy, String orderBy, String filterString) {
        Page<User> currentPage = userService.pageUsers(page, PAGE_SIZE, sortBy, orderBy, filterString);

        LazyHtml content = new LazyHtml(listUsersView.render(currentPage, sortBy, orderBy, filterString));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.list"), c));

        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("user.users"), routes.UserController.index())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Users");

        ControllerUtils.getInstance().addActivityLog("List all users <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @AddCSRFToken
    public Result updateUser(long userId) {
        User user = userService.findUserById(userId);
        UserUpdateForm userRoleUpdateForm = new UserUpdateForm(user);
        Form<UserUpdateForm> form = Form.form(UserUpdateForm.class).fill(userRoleUpdateForm);

        ControllerUtils.getInstance().addActivityLog("Try to update user " + user.getUserJid() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdate(form, userId);
    }

    @RequireCSRFCheck
    public Result postUpdateUser(long userId) {
        Form<UserUpdateForm> form = Form.form(UserUpdateForm.class).bindFromRequest();
        User user = userService.findUserById(userId);

        if (form.hasErrors()) {
            return showUpdate(form, user.getId());
        } else {
            UserUpdateForm userRoleUpdateForm = form.get();
            userService.updateUser(user.getId(), Arrays.asList(userRoleUpdateForm.roles.split(",")));

            ControllerUtils.getInstance().addActivityLog("Update user " + user.getUserJid() + ".");

            return redirect(routes.UserController.index());
        }
    }

    public Result deleteUser(long userId) {
        User user = userService.findUserById(userId);
        userService.deleteUser(user.getId());

        ControllerUtils.getInstance().addActivityLog("Try to update user " + user.getUserJid() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.UserController.index());
    }

    private Result showUpdate(Form<UserUpdateForm> form, long userId) {
        LazyHtml content = new LazyHtml(updateUserView.render(form, userId));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.update"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("user.users"), routes.UserController.index()),
              new InternalLink(Messages.get("user.update"), routes.UserController.updateUser(userId))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User - Update");
        return ControllerUtils.getInstance().lazyOk(content);
    }
}
