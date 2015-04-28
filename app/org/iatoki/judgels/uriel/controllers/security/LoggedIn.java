package org.iatoki.judgels.uriel.controllers.security;

import org.iatoki.judgels.jophiel.commons.controllers.security.BaseLoggedIn;
import play.mvc.Call;
import play.mvc.Http;

public class LoggedIn extends BaseLoggedIn {

    @Override
    public Call getRedirectCall(Http.Context context) {
        context.session().remove("role");
        return org.iatoki.judgels.uriel.controllers.routes.ApplicationController.auth(context.request().uri());
    }

}
