package org.iatoki.judgels.uriel.controllers.security;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Base64;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class LoggedIn extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context context) {
//        if ((context.session().get("username") != null) && (context.request().cookie("JOURID") != null)) {
//            String jID = context.request().cookie("JOURID").value();
//            if (jID.equals(context.session().get("idToken"))) {
//                return context.session().get("username");
//            } else {
//                return null;
//            }
//        } else if (context.session().get("username") != null) {
//            context.session().clear();
//            return null;
//        } else {
//            return null;
//        }
        return context.session().get("username");
    }

    @Override
    public Result onUnauthorized(Http.Context context) {
        return redirect(org.iatoki.judgels.uriel.controllers.routes.ApplicationController.auth(context.request().uri()));
    }
}
