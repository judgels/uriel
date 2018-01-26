package org.iatoki.judgels.uriel;

import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.jophiel.JophielClientControllerUtils;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.jophiel.viewpoint.ViewpointForm;
import org.iatoki.judgels.uriel.user.User;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.avatar.AvatarCacheServiceImpl;
import org.iatoki.judgels.uriel.user.UserService;
import org.iatoki.judgels.uriel.jid.JidCacheServiceImpl;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Singleton
public final class ApplicationController extends AbstractJudgelsController {

    private final JophielPublicAPI jophielPublicAPI;
    private final UserService userService;

    @Inject
    public ApplicationController(JophielPublicAPI jophielPublicAPI, UserService userService) {
        this.jophielPublicAPI = jophielPublicAPI;
        this.userService = userService;
    }

    public Result index() {
        return redirect(org.iatoki.judgels.uriel.contest.routes.ContestController.index());
    }

    public Result auth(String returnUri) {
        if (session().containsKey("username") && session().containsKey("role")) {
            return redirect(returnUri);
        } else if (session().containsKey("username")) {
            return redirect(routes.ApplicationController.authRole(returnUri));
        } else {
            try {
                String newReturnUri = routes.ApplicationController.afterLogin(URLEncoder.encode(returnUri, "UTF-8")).absoluteURL(request(), request().secure());
                return redirect(org.iatoki.judgels.jophiel.routes.JophielClientController.login(newReturnUri));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Transactional
    public Result authRole(String returnUri) {
        if (session().containsKey("username") && session().containsKey("role")) {
            return redirect(returnUri);
        }

        String userRoleJid = IdentityUtils.getUserJid();
        if (!userService.existsByUserJid(userRoleJid)) {
            userService.createUser(userRoleJid, UrielUtils.getDefaultRoles(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            UrielUtils.saveRolesInSession(UrielUtils.getDefaultRoles());
            return redirect(returnUri);
        }

        User userRole = userService.findUserByJid(userRoleJid);
        UrielUtils.saveRolesInSession(userRole.getRoles());
        return redirect(returnUri);
    }

    @Transactional
    public Result afterLogin(String returnUri) {
        if (!session().containsKey("role")) {
            String newReturnUri = routes.ApplicationController.afterLogin(returnUri).absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.authRole(newReturnUri));
        }

        JudgelsPlayUtils.updateUserJidCache(JidCacheServiceImpl.getInstance());
        JophielClientControllerUtils.updateUserAvatarCache(AvatarCacheServiceImpl.getInstance());

        if (JudgelsPlayUtils.hasViewPoint()) {
            try {
                UrielUtils.backupSession();
                UrielUtils.setUserSession(jophielPublicAPI.findUserByJid(JudgelsPlayUtils.getViewPoint()), userService.findUserByJid(JudgelsPlayUtils.getViewPoint()));
            } catch (JudgelsAPIClientException e) {
                JudgelsPlayUtils.removeViewPoint();
                UrielUtils.restoreSession();
            }
        }
        return redirect(returnUri);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postViewAs() {
        Form<ViewpointForm> viewpointForm = Form.form(ViewpointForm.class).bindFromRequest();

        if (!formHasErrors(viewpointForm) && UrielUtils.trullyHasRole("admin")) {
            ViewpointForm viewpointData = viewpointForm.get();
            try {
                JophielUser jophielUser = jophielPublicAPI.findUserByUsername(viewpointData.username);
                if (jophielUser != null) {
                    userService.upsertUserFromJophielUser(jophielUser, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
                    if (!JudgelsPlayUtils.hasViewPoint()) {
                        UrielUtils.backupSession();
                    }
                    JudgelsPlayUtils.setViewPointInSession(jophielUser.getJid());
                    UrielUtils.setUserSession(jophielUser, userService.findUserByJid(jophielUser.getJid()));
                }
            } catch (JudgelsAPIClientException e) {
                // do nothing
                e.printStackTrace();
            }
        }
        return redirectToReferer();
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result resetViewAs() {
        JudgelsPlayUtils.removeViewPoint();
        UrielUtils.restoreSession();

        return redirectToReferer();
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result logout(String returnUri) {
        session().clear();
        return redirect(returnUri);
    }
}
