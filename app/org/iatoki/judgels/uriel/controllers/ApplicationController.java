package org.iatoki.judgels.uriel.controllers;

import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.headerFooterLayout;
import org.iatoki.judgels.commons.views.html.layouts.baseLayout;
import org.iatoki.judgels.jophiel.commons.JophielUtils;
import org.iatoki.judgels.uriel.AvatarCacheService;
import org.iatoki.judgels.uriel.JidCacheService;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.User;
import org.iatoki.judgels.uriel.UserService;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

@Transactional
public final class ApplicationController extends BaseController {

    private final UserService userService;

    public ApplicationController(UserService userService) {
        this.userService = userService;
    }

    public Result index() {
        if ((session().containsKey("username")) && (session().containsKey("role"))) {
            return redirect(routes.ContestController.index());
        } else if (session().containsKey("username")) {
            String returnUri = routes.ContestController.index().absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.authRole(returnUri));
        } else {
            String returnUri = routes.ContestController.index().absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.auth(returnUri));
        }
    }

    public Result auth(String returnUri) {
        if ((session().containsKey("username")) && (session().containsKey("role"))) {
            return redirect(routes.ContestController.index());
        } else if (session().containsKey("username")) {
            return redirect(routes.ApplicationController.authRole(returnUri));
        } else {
            returnUri = org.iatoki.judgels.uriel.controllers.routes.ApplicationController.afterLogin(routes.ApplicationController.authRole(returnUri).absoluteURL(request(), request().secure())).absoluteURL(request(), request().secure());
            return redirect(org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.login(returnUri));
        }
    }

    public Result authRole(String returnUri) {
        if ((session().containsKey("username")) && (session().containsKey("role"))) {
            return redirect(routes.ContestController.index());
        } else {
            String userRoleJid = IdentityUtils.getUserJid();
            if (userService.existsByUserJid(userRoleJid)) {
                User userRole = userService.findUserByUserJid(userRoleJid);
                UrielUtils.saveRoleInSession(userRole.getRoles());
                return redirect(returnUri);
            } else {
                userService.createUser(userRoleJid, UrielUtils.getDefaultRole());
                UrielUtils.saveRoleInSession(UrielUtils.getDefaultRole());
                return redirect(returnUri);
            }
        }
    }

    public Result refreshAuth() {
        if (JophielUtils.checkSession(Http.Context.current()) != null) {
            return ok("");
        } else {
            String returnUri = routes.ApplicationController.refreshAuth().absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.auth(returnUri));
        }
    }

    public Result afterLogin(String returnUri) {
        JudgelsUtils.updateUserJidCache(JidCacheService.getInstance());
        JophielUtils.updateUserAvatarCache(AvatarCacheService.getInstance());

        return redirect(returnUri);
    }

    public Result afterProfile(String returnUri) {
        JudgelsUtils.updateUserJidCache(JidCacheService.getInstance());
        JophielUtils.updateUserAvatarCache(AvatarCacheService.getInstance());

        return redirect(returnUri);
    }

}
