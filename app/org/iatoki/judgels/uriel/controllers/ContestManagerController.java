package org.iatoki.judgels.uriel.controllers;

import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestManager;
import org.iatoki.judgels.uriel.controllers.forms.ContestManagerCreateForm;
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
public class ContestManagerController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final Jophiel jophiel;
    private final ContestService contestService;
    private final ContestManagerService contestManagerService;
    private final UserService userService;

    @Inject
    public ContestManagerController(Jophiel jophiel, ContestService contestService, ContestManagerService contestManagerService, UserService userService) {
        this.jophiel = jophiel;
        this.contestService = contestService;
        this.contestManagerService = contestManagerService;
        this.userService = userService;
    }

    @Transactional
    @AddCSRFToken
    public Result viewManagers(long contestId) throws ContestNotFoundException {
        return listCreateManagers(contestId, 0, "id", "asc", "");
    }

    @Transactional
    @AddCSRFToken
    public Result listCreateManagers(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            Page<ContestManager> contestManagers = contestManagerService.pageContestManagersByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = ControllerUtils.getInstance().isAdmin();

            Form<ContestManagerCreateForm> form = Form.form(ContestManagerCreateForm.class);

            return showListCreateManager(contestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Authorized(value = {"admin"})
    @Transactional(readOnly = true)
    @RequireCSRFCheck
    public Result postCreateManager(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        Form<ContestManagerCreateForm> form = Form.form(ContestManagerCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            Page<ContestManager> contestManagers = contestManagerService.pageContestManagersByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = ControllerUtils.getInstance().isAdmin();

            return showListCreateManager(contestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
        } else {
            ContestManagerCreateForm contestManagerCreateForm = form.get();
            try {
                String userJid = jophiel.verifyUsername(contestManagerCreateForm.username);
                if ((userJid != null) && (!contestManagerService.isContestManagerInContestByUserJid(contest.getJid(), userJid))) {
                    userService.upsertUserFromJophielUserJid(userJid);
                    contestManagerService.createContestManager(contest.getId(), userJid);

                    ControllerUtils.getInstance().addActivityLog("Add manager " + contestManagerCreateForm.username + " in contest " + contest.getName() + ".");

                    return redirect(routes.ContestManagerController.viewManagers(contest.getId()));
                } else {
                    form.reject("error.manager.create.userJid.invalid");

                    Page<ContestManager> contestManagers = contestManagerService.pageContestManagersByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
                    boolean canUpdate = ControllerUtils.getInstance().isAdmin();

                    return showListCreateManager(contestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
                }
            } catch (IOException e) {
                form.reject("error.manager.create.userJid.invalid");

                Page<ContestManager> contestManagers = contestManagerService.pageContestManagersByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
                boolean canUpdate = ControllerUtils.getInstance().isAdmin();

                return showListCreateManager(contestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
            }
        }
    }

    private Result showListCreateManager(Page<ContestManager> contestManagers, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, Form<ContestManagerCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(listCreateManagersView.render(contest.getId(), contestManagers, pageIndex, orderBy, orderDir, filterString, canUpdate, form, jophiel.getAutoCompleteEndPoint()));
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
