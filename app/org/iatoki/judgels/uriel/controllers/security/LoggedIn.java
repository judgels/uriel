package org.iatoki.judgels.uriel.controllers.security;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Base64;
import play.Play;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class LoggedIn extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context context) {
        if ((context.session().get("username") != null) && (context.request().cookie("JOID-" + Play.application().configuration().getString("jophiel.clientJid")) != null)) {
            String jID = context.request().cookie("JOID-" + Play.application().configuration().getString("jophiel.clientJid")).value();
            if (context.session().get("idToken").startsWith(jID)) {
                return context.session().get("username");
            } else {
                return null;
            }
        } else if (context.session().get("username") != null) {
            context.session().clear();
            return null;
        } else {
            return null;
        }
    }

    @Override
    public Result onUnauthorized(Http.Context context) {
        return redirect(org.iatoki.judgels.uriel.controllers.routes.ApplicationController.auth(context.request().uri()));
    }
}
