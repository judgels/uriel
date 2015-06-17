package org.iatoki.judgels.uriel.controllers.securities;

import org.iatoki.judgels.uriel.UrielUtils;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class HasRole extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context context) {
        return UrielUtils.getRolesFromSession();
    }

    @Override
    public Result onUnauthorized(Http.Context context) {
        return redirect(org.iatoki.judgels.uriel.controllers.routes.ApplicationController.authRole(context.request().uri()));
    }
}
