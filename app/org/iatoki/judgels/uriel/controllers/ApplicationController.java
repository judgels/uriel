package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.breadcrumbsLayout;
import org.iatoki.judgels.commons.views.html.layouts.leftSidebarLayout;
import org.iatoki.judgels.commons.views.html.layouts.headerFooterLayout;
import org.iatoki.judgels.commons.views.html.layouts.baseLayout;
import org.iatoki.judgels.jophiel.commons.views.html.auth.authView;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.UserRole;
import org.iatoki.judgels.uriel.UserRoleService;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

@Transactional
public final class ApplicationController extends Controller {

    private final UserRoleService userRoleService;

    public ApplicationController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    public Result index() {
        if ((session().containsKey("username")) && (session().containsKey("role"))) {
            return redirect(routes.ContestController.index());
        } else if (session().containsKey("username")) {
            String returnUri = routes.ContestController.index().absoluteURL(request());
            return redirect(routes.ApplicationController.authRole(returnUri));
        } else {
            String returnUri = routes.ContestController.index().absoluteURL(request());
            return redirect(routes.ApplicationController.auth(returnUri));
        }
    }

    public Result auth(String returnUri) {
        if ((session().containsKey("username")) && (session().containsKey("role"))) {
            return redirect(routes.ContestController.index());
        } else if (session().containsKey("username")) {
            return redirect(routes.ApplicationController.authRole(returnUri));
        } else {
            LazyHtml content = new LazyHtml(authView.render(org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.login(returnUri)));
            content.appendLayout(c -> headingLayout.render(Messages.get("commons.auth.login"), c));
            content.appendLayout(c -> breadcrumbsLayout.render(ImmutableList.of(), c));
            appendTemplateLayout(content);
            return getResult(content, Http.Status.OK);
        }
    }

    public Result authRole(String returnUri) {
        if ((session().containsKey("username")) && (session().containsKey("role"))) {
            return redirect(routes.ContestController.index());
        } else {
            String userRoleJid = IdentityUtils.getUserJid();
            String username = IdentityUtils.getUsername();
            String name = IdentityUtils.getUserRealName();
            if (userRoleService.isUserRoleExist(userRoleJid)) {
                userRoleService.updateUserRole(userRoleJid, username);
                UserRole userRole = userRoleService.findUserRoleByUserJid(userRoleJid);
                UrielUtils.saveRoleInSession(userRole.getRoles());
                return redirect(returnUri);
            } else {
                userRoleService.createUserRole(userRoleJid, name, UrielUtils.getDefaultRole());
                UrielUtils.saveRoleInSession(UrielUtils.getDefaultRole());
                return redirect(returnUri);
            }
        }
    }

    private void appendTemplateLayout(LazyHtml content) {
        content.appendLayout(c -> leftSidebarLayout.render(
                        IdentityUtils.getUsername(),
                        IdentityUtils.getUserRealName(),
                        "#",
                        org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.logout(routes.ApplicationController.index().absoluteURL(request())).absoluteURL(request()),
                        ImmutableList.of(), c)
        );

        content.appendLayout(c -> headerFooterLayout.render(c));
        content.appendLayout(c -> baseLayout.render("TODO", c));
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
