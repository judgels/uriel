package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.baseLayout;
import org.iatoki.judgels.commons.views.html.layouts.breadcrumbsLayout;
import org.iatoki.judgels.commons.views.html.layouts.headerFooterLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.leftSidebarLayout;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.UserRole;
import org.iatoki.judgels.uriel.UserRoleService;
import org.iatoki.judgels.uriel.UserRoleUpdateForm;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.Authorized;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.userRole.listView;
import org.iatoki.judgels.uriel.views.html.userRole.updateView;
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
public final class UserRoleController extends Controller {

    private static final long PAGE_SIZE = 20;
    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    public Result index() {
        return list(0, "id", "asc", "");
    }

    private Result showUpdate(Form<UserRoleUpdateForm> form, long userRoleId) {
        LazyHtml content = new LazyHtml(updateView.render(form, userRoleId));
        content.appendLayout(c -> headingLayout.render(Messages.get("userRole.update"), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("userRole.userRoles"), routes.UserRoleController.index()),
                new InternalLink(Messages.get("userRole.update"), routes.UserRoleController.update(userRoleId))
        ), c));
        appendTemplateLayout(content);
        return lazyOk(content);
    }

    @AddCSRFToken
    public Result update(long userRoleId) {
        UserRole userRole = userRoleService.findUserRoleById(userRoleId);
        UserRoleUpdateForm userRoleUpdateForm = new UserRoleUpdateForm(userRole);
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
            userRoleService.updateUserRole(userRoleId, userRoleUpdateForm.alias, Arrays.asList(userRoleUpdateForm.roles.split(",")));

            return redirect(routes.UserRoleController.index());
        }
    }

    public Result delete(long userRoleId) {
        userRoleService.deleteUserRole(userRoleId);

        return redirect(routes.UserRoleController.index());
    }

    public Result list(long page, String sortBy, String orderBy, String filterString) {
        Page<UserRole> currentPage = userRoleService.pageUserRole(page, PAGE_SIZE, sortBy, orderBy, filterString);

        LazyHtml content = new LazyHtml(listView.render(currentPage, sortBy, orderBy, filterString));
        content.appendLayout(c -> headingLayout.render(Messages.get("userRole.list"), c));
        content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("userRole.userRoles"), routes.UserRoleController.index())
        ), c));
        appendTemplateLayout(content);

        return lazyOk(content);
    }

    private void appendTemplateLayout(LazyHtml content) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()));

        if (UrielUtils.hasRole("admin")) {
            internalLinkBuilder.add(new InternalLink(Messages.get("userRole.userRoles"), routes.UserRoleController.index()));
        }

        content.appendLayout(c -> leftSidebarLayout.render(
            IdentityUtils.getUsername(),
            IdentityUtils.getUserRealName(),
            "#",
            org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.logout(routes.ApplicationController.index().absoluteURL(request())).absoluteURL(request()),
            internalLinkBuilder.build(), c)
        );
        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("TODO", c));
    }

    private Result lazyOk(LazyHtml content) {
        return getResult(content, Http.Status.OK);
    }

    private Result getResult(LazyHtml content, int statusCode) {
        switch (statusCode) {
            case Http.Status.OK:
                return ok(content.render(0));
            case Http.Status.NOT_FOUND:
                return notFound(content.render(0));
            default:
                return badRequest(content.render(0));
        }
    }
}
