package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.UserActivityMessage;
import org.iatoki.judgels.jophiel.forms.SearchProfileForm;
import org.iatoki.judgels.jophiel.forms.ViewpointForm;
import org.iatoki.judgels.jophiel.services.impls.UserActivityMessageServiceImpl;
import org.iatoki.judgels.jophiel.views.html.client.linkedClientsLayout;
import org.iatoki.judgels.jophiel.views.html.isLoggedInLayout;
import org.iatoki.judgels.jophiel.views.html.isLoggedOutLayout;
import org.iatoki.judgels.jophiel.views.html.profile.searchProfileLayout;
import org.iatoki.judgels.jophiel.views.html.viewas.viewAsLayout;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsControllerUtils;
import org.iatoki.judgels.play.controllers.ControllerUtils;
import org.iatoki.judgels.play.views.html.layouts.guestLoginView;
import org.iatoki.judgels.play.views.html.layouts.menusLayout;
import org.iatoki.judgels.play.views.html.layouts.profileView;
import org.iatoki.judgels.play.views.html.layouts.sidebarLayout;
import org.iatoki.judgels.uriel.UrielUtils;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Http;

public final class UrielControllerUtils extends AbstractJudgelsControllerUtils {

    private static UrielControllerUtils INSTANCE;

    private final Jophiel jophiel;

    private UrielControllerUtils(Jophiel jophiel) {
        this.jophiel = jophiel;
    }

    @Override
    public void appendSidebarLayout(LazyHtml content) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()));
        if (isAdmin()) {
            internalLinkBuilder.add(new InternalLink(Messages.get("user.users"), routes.UserController.index()));
        }

        LazyHtml sidebarContent;
        if (UrielUtils.isGuest()) {
            sidebarContent = new LazyHtml(guestLoginView.render(routes.ApplicationController.auth(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()), jophiel.getRegisterUri().toString()));
        } else {
            sidebarContent = new LazyHtml(profileView.render(
                    IdentityUtils.getUsername(),
                    IdentityUtils.getUserRealName(),
                    org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.profile().absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()),
                    org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.logout(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure())
                ));
        }
        if (UrielUtils.trullyHasRole("admin")) {
            Form<ViewpointForm> form = Form.form(ViewpointForm.class);
            if (JudgelsPlayUtils.hasViewPoint()) {
                ViewpointForm viewpointForm = new ViewpointForm();
                viewpointForm.username = IdentityUtils.getUsername();
                form.fill(viewpointForm);
            }
            sidebarContent.appendLayout(c -> viewAsLayout.render(form, jophiel.getAutoCompleteEndPoint(), "lib/jophielcommons/javascripts/userAutoComplete.js", org.iatoki.judgels.uriel.controllers.routes.ApplicationController.postViewAs(), org.iatoki.judgels.uriel.controllers.routes.ApplicationController.resetViewAs(), c));
        }
        sidebarContent.appendLayout(c -> menusLayout.render(internalLinkBuilder.build(), c));
        sidebarContent.appendLayout(c -> linkedClientsLayout.render(jophiel.getLinkedClientsEndPoint(), "lib/jophielcommons/javascripts/linkedClients.js", c));
        Form<SearchProfileForm> searchProfileForm = Form.form(SearchProfileForm.class);
        sidebarContent.appendLayout(c -> searchProfileLayout.render(searchProfileForm, jophiel.getAutoCompleteEndPoint(), "lib/jophielcommons/javascripts/userAutoComplete.js", jophiel.getViewProfileEndPoint(), c));

        content.appendLayout(c -> sidebarLayout.render(sidebarContent.render(), c));
        if ((IdentityUtils.getUserJid() == null) || UrielUtils.isGuest()) {
            content.appendLayout(c -> isLoggedInLayout.render(jophiel.getIsLoggedInEndPoint(), routes.ApplicationController.auth(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()), "lib/jophielcommons/javascripts/isLoggedIn.js", c));
        } else {
            content.appendLayout(c -> isLoggedOutLayout.render(jophiel.getIsLoggedInEndPoint(), org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.logout(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()), "lib/jophielcommons/javascripts/isLoggedOut.js", c));
        }
    }

    public boolean isAdmin() {
        return UrielUtils.hasRole("admin");
    }

    public void addActivityLog(String log) {
        if (!UrielUtils.isGuest()) {
            String newLog = log;
            try {
                if (JudgelsPlayUtils.hasViewPoint()) {
                    newLog += " view as " + IdentityUtils.getUserJid();
                }
                UserActivityMessageServiceImpl.getInstance().addUserActivityMessage(new UserActivityMessage(System.currentTimeMillis(), UrielUtils.getRealUserJid(), newLog, IdentityUtils.getIpAddress()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void buildInstance(Jophiel jophiel) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("ControllerUtils instance has already been built");
        }
        INSTANCE = new UrielControllerUtils(jophiel);
    }

    static UrielControllerUtils getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("ControllerUtils instance has not been built");
        }
        return INSTANCE;
    }
}
