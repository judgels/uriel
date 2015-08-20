package org.iatoki.judgels.uriel.controllers;

import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestManager;
import org.iatoki.judgels.uriel.forms.ContestManagerCreateForm;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.services.ContestManagerService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.UserService;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.Authorized;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.manager.listCreateManagersView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public class ContestManagerController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final ContestManagerService contestManagerService;
    private final ContestService contestService;
    private final Jophiel jophiel;
    private final UserService userService;

    @Inject
    public ContestManagerController(ContestManagerService contestManagerService, ContestService contestService, Jophiel jophiel, UserService userService) {
        this.contestManagerService = contestManagerService;
        this.contestService = contestService;
        this.jophiel = jophiel;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewManagers(long contestId) throws ContestNotFoundException {
        return listCreateManagers(contestId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listCreateManagers(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Page<ContestManager> pageOfContestManagers = contestManagerService.getPageOfManagersInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
        boolean canUpdate = ControllerUtils.getInstance().isAdmin();
        Form<ContestManagerCreateForm> contestManagerCreateForm = Form.form(ContestManagerCreateForm.class);

        return showListCreateManager(pageOfContestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, contestManagerCreateForm, contest);
    }

    @Authorized(value = "admin")
    @Transactional
    @RequireCSRFCheck
    public Result postCreateManager(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        Form<ContestManagerCreateForm> contestManagerCreateForm = Form.form(ContestManagerCreateForm.class).bindFromRequest();

        if (formHasErrors(contestManagerCreateForm)) {
            Page<ContestManager> pageOfContestManagers = contestManagerService.getPageOfManagersInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
            boolean canUpdate = ControllerUtils.getInstance().isAdmin();

            return showListCreateManager(pageOfContestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, contestManagerCreateForm, contest);
        }

        ContestManagerCreateForm contestManagerCreateData = contestManagerCreateForm.get();
        String userJid;
        try {
            userJid = jophiel.verifyUsername(contestManagerCreateData.username);
        } catch (IOException e) {
            userJid = null;
        }

        if ((userJid == null) || !contestManagerService.isManagerInContest(contest.getJid(), userJid)) {
            contestManagerCreateForm.reject("error.manager.create.userJid.invalid");

            Page<ContestManager> contestManagers = contestManagerService.getPageOfManagersInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
            boolean canUpdate = ControllerUtils.getInstance().isAdmin();

            return showListCreateManager(contestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, contestManagerCreateForm, contest);
        }

        userService.upsertUserFromJophielUserJid(userJid);
        contestManagerService.createContestManager(contest.getId(), userJid);

        ControllerUtils.getInstance().addActivityLog("Add manager " + contestManagerCreateData.username + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestManagerController.viewManagers(contest.getId()));
    }

    private Result showListCreateManager(Page<ContestManager> pageOfContestManagers, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, Form<ContestManagerCreateForm> contestManagerCreateForm, Contest contest) {
        LazyHtml content = new LazyHtml(listCreateManagersView.render(contest.getId(), pageOfContestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, contestManagerCreateForm, jophiel.getAutoCompleteEndPoint()));
        content.appendLayout(c -> heading3Layout.render(Messages.get("manager.list"), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest);
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Managers");

        ControllerUtils.getInstance().addActivityLog("Open list of managers in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("manager.managers"), routes.ContestController.jumpToManagers(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }
}
