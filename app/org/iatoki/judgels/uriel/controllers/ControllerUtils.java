package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.UserActivityMessage;
import org.iatoki.judgels.jophiel.controllers.forms.ViewpointForm;
import org.iatoki.judgels.commons.controllers.AbstractControllerUtils;
import org.iatoki.judgels.commons.views.html.layouts.sidebarLayout;
import org.iatoki.judgels.commons.views.html.layouts.profileView;
import org.iatoki.judgels.commons.views.html.layouts.menusLayout;
import org.iatoki.judgels.jophiel.views.html.viewas.viewAsLayout;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.services.impls.UserActivityMessageServiceImpl;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Http;

public final class ControllerUtils extends AbstractControllerUtils {

    private static ControllerUtils INSTANCE;

    private final Jophiel jophiel;

    private ControllerUtils(Jophiel jophiel) {
        this.jophiel = jophiel;
    }

    @Override
    public void appendSidebarLayout(LazyHtml content) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()));
        if (isAdmin()) {
            internalLinkBuilder.add(new InternalLink(Messages.get("user.users"), routes.UserController.index()));
        }
        LazyHtml sidebarContent = new LazyHtml(profileView.render(
                IdentityUtils.getUsername(),
                IdentityUtils.getUserRealName(),
                org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.profile(org.iatoki.judgels.uriel.controllers.routes.ApplicationController.afterProfile(routes.ContestController.index().absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()),
                org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.logout(routes.ApplicationController.index().absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure())
        ));
        if (UrielUtils.trullyHasRole("admin")) {
            Form<ViewpointForm> form = Form.form(ViewpointForm.class);
            if (JudgelsUtils.hasViewPoint()) {
                ViewpointForm viewpointForm = new ViewpointForm();
                viewpointForm.username = IdentityUtils.getUsername();
                form.fill(viewpointForm);
            }
            sidebarContent.appendLayout(c -> viewAsLayout.render(form, jophiel.getAutoCompleteEndPoint(), "javascripts/userAutoComplete.js", org.iatoki.judgels.uriel.controllers.routes.ApplicationController.postViewAs(), org.iatoki.judgels.uriel.controllers.routes.ApplicationController.resetViewAs(), c));
        }
        sidebarContent.appendLayout(c -> menusLayout.render(internalLinkBuilder.build(), c));

        content.appendLayout(c -> sidebarLayout.render(sidebarContent.render(), c));
    }

    public boolean isAdmin() {
        return UrielUtils.hasRole("admin");
    }

    public void addActivityLog(String log) {
        try {
            if (JudgelsUtils.hasViewPoint()) {
                log += " view as " +  IdentityUtils.getUserJid();
            }
            UserActivityMessageServiceImpl.getInstance().addUserActivityMessage(new UserActivityMessage(System.currentTimeMillis(), UrielUtils.getRealUserJid(), log, IdentityUtils.getIpAddress()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void buildInstance(Jophiel jophiel) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("ControllerUtils instance has already been built");
        }
        INSTANCE = new ControllerUtils(jophiel);
    }

    static ControllerUtils getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("ControllerUtils instance has not been built");
        }
        return INSTANCE;
    }

}
