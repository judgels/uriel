package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.controllers.AbstractControllerUtils;
import org.iatoki.judgels.commons.views.html.layouts.sidebarLayout;
import org.iatoki.judgels.uriel.UrielUtils;
import play.i18n.Messages;
import play.mvc.Http;

public final class ControllerUtils extends AbstractControllerUtils {

    private static ControllerUtils INSTANCE = new ControllerUtils();

    @Override
    public void appendSidebarLayout(LazyHtml content) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()));
        if (isAdmin()) {
            internalLinkBuilder.add(new InternalLink(Messages.get("userRole.userRoles"), routes.UserRoleController.index()));
        }
        content.appendLayout(c -> sidebarLayout.render(
            IdentityUtils.getUsername(),
            IdentityUtils.getUserRealName(),
            org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.profile(org.iatoki.judgels.uriel.controllers.routes.ApplicationController.afterProfile(routes.ContestController.index().absoluteURL(Http.Context.current().request())).absoluteURL(Http.Context.current().request())).absoluteURL(Http.Context.current().request()),
            org.iatoki.judgels.jophiel.commons.controllers.routes.JophielClientController.logout(routes.ApplicationController.index().absoluteURL(Http.Context.current().request())).absoluteURL(Http.Context.current().request()),
            internalLinkBuilder.build(), c)
        );
    }

    public boolean isAdmin() {
        return UrielUtils.hasRole("admin");
    }

    static ControllerUtils getInstance() {
        return INSTANCE;
    }

}
