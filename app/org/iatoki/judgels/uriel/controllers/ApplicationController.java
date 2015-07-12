package org.iatoki.judgels.uriel.controllers;

import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.forms.ViewpointForm;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.User;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.services.impls.AvatarCacheServiceImpl;
import org.iatoki.judgels.uriel.services.UserService;
import org.iatoki.judgels.uriel.services.impls.JidCacheServiceImpl;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
@Named
public final class ApplicationController extends AbstractJudgelsController {

    private final Jophiel jophiel;
    private final UserService userService;

    @Inject
    public ApplicationController(Jophiel jophiel, UserService userService) {
        this.jophiel = jophiel;
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
            return redirect(org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.login(returnUri));
        }
    }

    @Transactional
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

    @Transactional
    public Result afterLogin(String returnUri) {
        if (session().containsKey("role")) {
            JudgelsPlayUtils.updateUserJidCache(JidCacheServiceImpl.getInstance());
            Jophiel.updateUserAvatarCache(AvatarCacheServiceImpl.getInstance());

            if (JudgelsPlayUtils.hasViewPoint()) {
                try {
                    UrielUtils.backupSession();
                    UrielUtils.setUserSession(jophiel.getUserByUserJid(JudgelsPlayUtils.getViewPoint()), userService.findUserByUserJid(JudgelsPlayUtils.getViewPoint()));
                } catch (IOException e) {
                    JudgelsPlayUtils.removeViewPoint();
                    UrielUtils.restoreSession();
                }
            }
            return redirect(returnUri);
        } else {
            returnUri = org.iatoki.judgels.uriel.controllers.routes.ApplicationController.afterLogin(returnUri).absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.authRole(returnUri));
        }
    }

    @Transactional
    public Result afterProfile(String returnUri) {
        JudgelsPlayUtils.updateUserJidCache(JidCacheServiceImpl.getInstance());
        Jophiel.updateUserAvatarCache(AvatarCacheServiceImpl.getInstance());

        return redirect(returnUri);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postViewAs() {
        Form<ViewpointForm> form = Form.form(ViewpointForm.class).bindFromRequest();

        if ((!(form.hasErrors() || form.hasGlobalErrors())) && (UrielUtils.trullyHasRole("admin"))) {
            ViewpointForm viewpointForm = form.get();
            try {
                String userJid = jophiel.verifyUsername(viewpointForm.username);
                if (userJid != null) {
                    try {
                        userService.upsertUserFromJophielUserJid(userJid);
                        if (!JudgelsPlayUtils.hasViewPoint()) {
                            UrielUtils.backupSession();
                        }
                        JudgelsPlayUtils.setViewPointInSession(userJid);
                        UrielUtils.setUserSession(jophiel.getUserByUserJid(userJid), userService.findUserByUserJid(userJid));

                        ControllerUtils.getInstance().addActivityLog("View as user " + viewpointForm.username + ".");

                    } catch (IOException e) {
                        JudgelsPlayUtils.removeViewPoint();
                        UrielUtils.restoreSession();
                    }
                }
            } catch (IOException e) {
                // do nothing
                e.printStackTrace();
            }
        }
        return redirect(request().getHeader("Referer"));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result resetViewAs() {
        JudgelsPlayUtils.removeViewPoint();
        UrielUtils.restoreSession();

        return redirect(request().getHeader("Referer"));
    }
}
