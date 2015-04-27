package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.controllers.AbstractControllerUtils;
import org.iatoki.judgels.commons.views.html.layouts.sidebarLayout;
import org.iatoki.judgels.uriel.UserViewpointForm;
import org.iatoki.judgels.uriel.views.html.layouts.viewAsLayout;
import org.iatoki.judgels.jophiel.commons.UserActivity;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.UserActivityServiceImpl;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Http;

public final class ControllerUtils extends AbstractControllerUtils {

    private static final ControllerUtils INSTANCE = new ControllerUtils();

    @Override
    public void appendTemplateLayout(LazyHtml content, String title) {
        if (UrielUtils.trullyHasRole("admin")) {
            Form<UserViewpointForm> form = Form.form(UserViewpointForm.class);
            if (UrielUtils.hasViewPoint()) {
                UserViewpointForm userViewpointForm = new UserViewpointForm();
                userViewpointForm.username = IdentityUtils.getUsername();
                form.fill(userViewpointForm);
            }
            content.appendLayout(c -> viewAsLayout.render(form, c));
        }
        super.appendTemplateLayout(content, title);
    }

    @Override
    public void appendSidebarLayout(LazyHtml content) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()));
        if (isAdmin()) {
            internalLinkBuilder.add(new InternalLink(Messages.get("user.users"), routes.UserController.index()));
        }
        content.appendLayout(c -> sidebarLayout.render(
            IdentityUtils.getUsername(),
            IdentityUtils.getUserRealName(),
            org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.profile(org.iatoki.judgels.uriel.controllers.routes.ApplicationController.afterProfile(routes.ContestController.index().absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()),
            org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.logout(routes.ApplicationController.index().absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()),
            internalLinkBuilder.build(), c)
        );
    }

    public boolean isAdmin() {
        return UrielUtils.hasRole("admin");
    }

    public void addActivityLog(String log) {
        try {
            UserActivityServiceImpl.getInstance().addUserActivity(new UserActivity(System.currentTimeMillis(), IdentityUtils.getUserJid(), log, IdentityUtils.getIpAddress()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static ControllerUtils getInstance() {
        return INSTANCE;
    }

}
