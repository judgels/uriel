package org.iatoki.judgels.uriel;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.jophiel.JophielClientControllerUtils;
import org.iatoki.judgels.jophiel.activity.ActivityKey;
import org.iatoki.judgels.jophiel.activity.UserActivityMessage;
import org.iatoki.judgels.jophiel.activity.UserActivityMessageServiceImpl;
import org.iatoki.judgels.jophiel.logincheck.html.isLoggedInLayout;
import org.iatoki.judgels.jophiel.logincheck.html.isLoggedOutLayout;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsControllerUtils;
import org.iatoki.judgels.play.controllers.ControllerUtils;
import org.iatoki.judgels.play.views.html.layouts.contentLayout;
import org.iatoki.judgels.play.views.html.layouts.guestLoginView;
import org.iatoki.judgels.play.views.html.layouts.menusLayout;
import org.iatoki.judgels.play.views.html.layouts.profileView;
import org.iatoki.judgels.play.views.html.layouts.sidebarLayout;
import org.iatoki.judgels.uriel.activity.ActivityLogServiceImpl;
import play.i18n.Messages;
import play.mvc.Http;

public final class UrielControllerUtils extends AbstractJudgelsControllerUtils {

    private static UrielControllerUtils INSTANCE;

    private final JophielClientAPI jophielClientAPI;
    private final JophielPublicAPI jophielPublicAPI;

    public UrielControllerUtils(JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI) {
        this.jophielClientAPI = jophielClientAPI;
        this.jophielPublicAPI = jophielPublicAPI;
    }

    @Override
    public void appendSidebarLayout(LazyHtml content) {
        content.appendLayout(c -> contentLayout.render(c));

        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.contests"), org.iatoki.judgels.uriel.contest.routes.ContestController.index()));
        if (isAdmin()) {
            internalLinkBuilder.add(new InternalLink(Messages.get("user.users"), org.iatoki.judgels.uriel.user.routes.UserController.index()));
        }

        LazyHtml sidebarContent;
        if (UrielUtils.isGuest()) {
            sidebarContent = new LazyHtml(guestLoginView.render(routes.ApplicationController.auth(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()), JophielClientControllerUtils.getInstance().getRegisterUrl()));
        } else {
            sidebarContent = new LazyHtml(profileView.render(
                    IdentityUtils.getUsername(),
                    IdentityUtils.getUserRealName(),
                    org.iatoki.judgels.jophiel.routes.JophielClientController.profile().absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()),
                    org.iatoki.judgels.jophiel.routes.JophielClientController.logout(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure())
                ));
        }
        sidebarContent.appendLayout(c -> menusLayout.render(internalLinkBuilder.build(), c));

        content.appendLayout(c -> sidebarLayout.render(sidebarContent.render(), c));
        if (UrielUtils.isGuest()) {
            content.appendLayout(c -> isLoggedInLayout.render(jophielClientAPI.getUserIsLoggedInAPIEndpoint(), routes.ApplicationController.auth(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()), "lib/jophielcommons/javascripts/isLoggedIn.js", c));
        } else {
            content.appendLayout(c -> isLoggedOutLayout.render(jophielClientAPI.getUserIsLoggedInAPIEndpoint(), routes.ApplicationController.logout(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()), "lib/jophielcommons/javascripts/isLoggedOut.js", UrielUtils.getRealUserJid(), c));
        }
    }

    public boolean isAdmin() {
        return UrielUtils.hasRole("admin");
    }

    public void addActivityLog(ActivityKey activityKey) {
        if (!UrielUtils.isGuest()) {
            long time = System.currentTimeMillis();
            ActivityLogServiceImpl.getInstance().addActivityLog(activityKey, UrielUtils.getRealUsername(), time, UrielUtils.getRealUserJid(), IdentityUtils.getIpAddress());
            String log = UrielUtils.getRealUsername() + " " + activityKey.toString();
            try {
                if (JudgelsPlayUtils.hasViewPoint()) {
                    log += " view as " + IdentityUtils.getUsername();
                }
                UserActivityMessageServiceImpl.getInstance().addUserActivityMessage(new UserActivityMessage(System.currentTimeMillis(), UrielUtils.getRealUserJid(), log, IdentityUtils.getIpAddress()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void buildInstance(JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("ControllerUtils instance has already been built");
        }
        INSTANCE = new UrielControllerUtils(jophielClientAPI, jophielPublicAPI);
    }

    public static UrielControllerUtils getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("ControllerUtils instance has not been built");
        }
        return INSTANCE;
    }
}
