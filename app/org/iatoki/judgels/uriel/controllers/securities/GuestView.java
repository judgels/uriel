package org.iatoki.judgels.uriel.controllers.securities;

import org.iatoki.judgels.jophiel.JophielSessionUtils;
import org.iatoki.judgels.play.IdentityUtils;
import play.mvc.Http;
import play.mvc.Security;

public class GuestView extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context context) {
        if ((IdentityUtils.getUserJid() == null) || !JophielSessionUtils.isSessionValid(context)) {
            context.session().put("userJid", "guest");
        }
        return IdentityUtils.getUserJid();
    }
}
