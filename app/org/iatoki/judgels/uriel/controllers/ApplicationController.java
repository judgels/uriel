package org.iatoki.judgels.uriel.controllers;

import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.jophiel.commons.JophielUtils;
import org.iatoki.judgels.uriel.AvatarCacheService;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantCreateForm;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestContestantUploadForm;
import org.iatoki.judgels.uriel.JidCacheService;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.User;
import org.iatoki.judgels.uriel.UserService;
import org.iatoki.judgels.uriel.UserViewpointForm;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import java.io.IOException;

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
            return redirect(returnUri);
        } else if (session().containsKey("username")) {
            return redirect(routes.ApplicationController.authRole(returnUri));
        } else {
            returnUri = org.iatoki.judgels.uriel.controllers.routes.ApplicationController.afterLogin(returnUri).absoluteURL(request(), request().secure());
            return redirect(org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.login(returnUri));
        }
    }

    public Result authRole(String returnUri) {
        if ((session().containsKey("username")) && (session().containsKey("role"))) {
            return redirect(returnUri);
        } else {
            String userRoleJid = IdentityUtils.getUserJid();
            if (userService.existsByUserJid(userRoleJid)) {
                User userRole = userService.findUserByUserJid(userRoleJid);
                UrielUtils.saveRolesInSession(userRole.getRoles());
                return redirect(returnUri);
            } else {
                userService.createUser(userRoleJid, UrielUtils.getDefaultRoles());
                UrielUtils.saveRolesInSession(UrielUtils.getDefaultRoles());
                return redirect(returnUri);
            }
        }
    }

    public Result afterLogin(String returnUri) {
        if (session().containsKey("role")) {
            JudgelsUtils.updateUserJidCache(JidCacheService.getInstance());
            JophielUtils.updateUserAvatarCache(AvatarCacheService.getInstance());

            if (UrielUtils.hasViewPoint()) {
                try {
                    UrielUtils.backupSession();
                    UrielUtils.setUserSession(JophielUtils.getUserByUserJid(UrielUtils.getViewPoint()), userService.findUserByUserJid(UrielUtils.getViewPoint()));
                } catch (IOException e) {
                    UrielUtils.removeViewPoint();
                    UrielUtils.restoreSession();
                }
            }
            return redirect(returnUri);
        } else {
            returnUri = org.iatoki.judgels.uriel.controllers.routes.ApplicationController.afterLogin(returnUri).absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.authRole(returnUri));
        }
    }

    public Result afterProfile(String returnUri) {
        JudgelsUtils.updateUserJidCache(JidCacheService.getInstance());
        JophielUtils.updateUserAvatarCache(AvatarCacheService.getInstance());

        return redirect(returnUri);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result postViewAs() {
        Form<UserViewpointForm> form = Form.form(UserViewpointForm.class).bindFromRequest();

        if ((!(form.hasErrors() || form.hasGlobalErrors())) && (UrielUtils.trullyHasRole("admin"))) {
            UserViewpointForm userViewpointForm = form.get();
            String userJid = JophielUtils.verifyUsername(userViewpointForm.username);
            if (userJid != null) {
                try {
                    userService.upsertUserFromJophielUserJid(userJid);
                    if (!UrielUtils.hasViewPoint()) {
                        UrielUtils.backupSession();
                    }
                    UrielUtils.setViewPointInSession(userJid);
                    UrielUtils.setUserSession(JophielUtils.getUserByUserJid(userJid), userService.findUserByUserJid(userJid));

                    ControllerUtils.getInstance().addActivityLog("View as user " + userViewpointForm.username + ".");

                } catch (IOException e) {
                    UrielUtils.removeViewPoint();
                    UrielUtils.restoreSession();
                }
            }
        }
        return redirect(request().getHeader("Referer"));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result resetViewAs() {
        UrielUtils.removeViewPoint();
        UrielUtils.restoreSession();

        return redirect(request().getHeader("Referer"));
    }
}
