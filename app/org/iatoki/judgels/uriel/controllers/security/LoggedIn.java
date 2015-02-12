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
        if ((context.session().get("username") != null) && (context.request().cookie("JOURID") != null)) {
            String jID = context.request().cookie("JOURID").value();
            if (context.session().get("idToken").startsWith(jID)) {
                System.out.println("A 1");
                return context.session().get("username");
            } else {
                System.out.println("A 2");
                return null;
            }
        } else if (context.session().get("username") != null) {
            System.out.println("A 3");
            context.session().clear();
            return null;
        } else {
            System.out.println("A 4");
            return null;
        }
    }

    @Override
    public Result onUnauthorized(Http.Context context) {
        return redirect(org.iatoki.judgels.uriel.controllers.routes.ApplicationController.auth(context.request().uri()));
    }
}
