package org.iatoki.judgels.uriel.controllers;

import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestManager;
import org.iatoki.judgels.uriel.ContestManagerNotFoundException;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.Authorized;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.forms.ContestManagerCreateForm;
import org.iatoki.judgels.uriel.services.ContestManagerService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.UserService;
import org.iatoki.judgels.uriel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.uriel.views.html.contest.manager.listAddManagersView;
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
import java.util.Arrays;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public class ContestManagerController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final ContestManagerService contestManagerService;
    private final ContestService contestService;
    private final JophielPublicAPI jophielPublicAPI;
    private final UserService userService;

    @Inject
    public ContestManagerController(ContestManagerService contestManagerService, ContestService contestService, JophielPublicAPI jophielPublicAPI, UserService userService) {
        this.contestManagerService = contestManagerService;
        this.contestService = contestService;
        this.jophielPublicAPI = jophielPublicAPI;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewManagers(long contestId) throws ContestNotFoundException {
        return listAddManagers(contestId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listAddManagers(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isSupervisorOrAbove(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Page<ContestManager> pageOfContestManagers = contestManagerService.getPageOfManagersInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
        boolean canUpdate = !contest.isLocked() && UrielControllerUtils.getInstance().isAdmin();
        Form<ContestManagerCreateForm> contestManagerCreateForm = Form.form(ContestManagerCreateForm.class);

        return showlistAddManager(pageOfContestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, contestManagerCreateForm, contest);
    }

    @Authorized(value = "admin")
    @Transactional
    @RequireCSRFCheck
    public Result postAddManager(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (contest.isLocked()) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestManagerCreateForm> contestManagerCreateForm = Form.form(ContestManagerCreateForm.class).bindFromRequest();

        if (formHasErrors(contestManagerCreateForm)) {
            Page<ContestManager> pageOfContestManagers = contestManagerService.getPageOfManagersInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
            boolean canUpdate = UrielControllerUtils.getInstance().isAdmin();

            return showlistAddManager(pageOfContestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, contestManagerCreateForm, contest);
        }

        ContestManagerCreateForm contestManagerCreateData = contestManagerCreateForm.get();
        JophielUser jophielUser;
        try {
            jophielUser = jophielPublicAPI.findUserByUsername(contestManagerCreateData.username);
        } catch (JudgelsAPIClientException e) {
            jophielUser = null;
        }

        if ((jophielUser == null) || contestManagerService.isManagerInContest(contest.getJid(), jophielUser.getJid())) {
            contestManagerCreateForm.reject("error.manager.create.userJid.invalid");

            Page<ContestManager> contestManagers = contestManagerService.getPageOfManagersInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
            boolean canUpdate = UrielControllerUtils.getInstance().isAdmin();

            return showlistAddManager(contestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, contestManagerCreateForm, contest);
        }

        userService.upsertUserFromJophielUser(jophielUser, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        contestManagerService.createContestManager(contest.getJid(), jophielUser.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog("Add manager " + contestManagerCreateData.username + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestManagerController.viewManagers(contest.getId()));
    }

    @Authorized(value = "admin")
    @Transactional
    public Result removeManager(long contestId, long contestManagerId) throws ContestNotFoundException, ContestManagerNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestManager contestManager = contestManagerService.findContestManagerById(contestManagerId);
        if (contest.isLocked() || !contestManager.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        contestManagerService.deleteContestManager(contestManager.getId());

        UrielControllerUtils.getInstance().addActivityLog("Delete manager " + JidCacheServiceImpl.getInstance().getDisplayName(contestManager.getUserJid()) + ".");

        return redirect(routes.UserController.index());
    }


    private Result showlistAddManager(Page<ContestManager> pageOfContestManagers, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, Form<ContestManagerCreateForm> contestManagerCreateForm, Contest contest) {
        LazyHtml content = new LazyHtml(listAddManagersView.render(contest.getId(), pageOfContestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, contestManagerCreateForm, jophielPublicAPI.getUserAutocompleteAPIEndpoint()));
        content.appendLayout(c -> heading3Layout.render(Messages.get("manager.list"), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest);
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Managers");

        UrielControllerUtils.getInstance().addActivityLog("Open list of managers in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("manager.managers"), routes.ContestController.jumpToManagers(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }
}
