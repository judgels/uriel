package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.baseLayout;
import org.iatoki.judgels.commons.views.html.layouts.breadcrumbsLayout;
import org.iatoki.judgels.commons.views.html.layouts.headerFooterLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.sidebarLayout;
import org.iatoki.judgels.uriel.JidCacheService;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.UserRole;
import org.iatoki.judgels.uriel.UserRoleService;
import org.iatoki.judgels.uriel.UserRoleUpdateForm;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.Authorized;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.userrole.listView;
import org.iatoki.judgels.uriel.views.html.userrole.updateView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Arrays;
import java.util.List;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = {"admin"})
public final class UserRoleController extends Controller {

    private static final long PAGE_SIZE = 20;
    private UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;

        JudgelsUtils.updateUserJidCache(JidCacheService.getInstance());
    }

    public Result index() {
        return list(0, "id", "asc", "");
    }

    private Result showUpdate(Form<UserRoleUpdateForm> form, long userRoleId) {
        UserRole userRole = userRoleService.findUserRoleById(userRoleId);
        LazyHtml content = new LazyHtml(updateView.render(form, userRoleId));
        content.appendLayout(c -> headingLayout.render(Messages.get("userRole.update"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("userRole.userRoles"), routes.UserRoleController.index()),
                new InternalLink(Messages.get("userRole.update"), routes.UserRoleController.update(userRoleId))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User Roles - Update");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    @AddCSRFToken
    public Result update(long userRoleId) {
        UserRole userRole = userRoleService.findUserRoleById(userRoleId);
        UserRoleUpdateForm userRoleUpdateForm = new UserRoleUpdateForm();
        userRoleUpdateForm.roles = StringUtils.join(userRole.getRoles(), ",");
        Form<UserRoleUpdateForm> form = Form.form(UserRoleUpdateForm.class).fill(userRoleUpdateForm);

        return showUpdate(form, userRoleId);
    }

    @RequireCSRFCheck
    public Result postUpdate(long userRoleId) {
        Form<UserRoleUpdateForm> form = Form.form(UserRoleUpdateForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return showUpdate(form, userRoleId);
        } else {
            UserRoleUpdateForm userRoleUpdateForm = form.get();
            userRoleService.updateUserRole(userRoleId, Arrays.asList(userRoleUpdateForm.roles.split(",")));

            return redirect(routes.UserRoleController.index());
        }
    }

    public Result delete(long userRoleId) {
        userRoleService.deleteUserRole(userRoleId);

        return redirect(routes.UserRoleController.index());
    }

    public Result list(long page, String sortBy, String orderBy, String filterString) {
        Page<UserRole> currentPage = userRoleService.pageUserRoles(page, PAGE_SIZE, sortBy, orderBy, filterString);

        LazyHtml content = new LazyHtml(listView.render(currentPage, sortBy, orderBy, filterString));
        content.appendLayout(c -> headingLayout.render(Messages.get("userRole.list"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("userRole.userRoles"), routes.UserRoleController.index())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "User Roles - List");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
