package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestPermission;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.ContestSupervisor;
import org.iatoki.judgels.uriel.ContestSupervisorNotFoundException;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.forms.ContestSupervisorCreateForm;
import org.iatoki.judgels.uriel.forms.ContestSupervisorUpdateForm;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.ContestSupervisorService;
import org.iatoki.judgels.uriel.services.UserService;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.listCreateSupervisorsView;
import org.iatoki.judgels.uriel.views.html.contest.supervisor.updateSupervisorView;
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
import java.util.stream.Collectors;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public class ContestSupervisorController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final Jophiel jophiel;
    private final ContestService contestService;
    private final ContestSupervisorService contestSupervisorService;
    private final UserService userService;

    @Inject
    public ContestSupervisorController(Jophiel jophiel, ContestService contestService, ContestSupervisorService contestSupervisorService, UserService userService) {
        this.jophiel = jophiel;
        this.contestService = contestService;
        this.contestSupervisorService = contestSupervisorService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewSupervisors(long contestId) throws ContestNotFoundException {
        return listCreateSupervisors(contestId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listCreateSupervisors(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Page<ContestSupervisor> pageOfContestSupervisors = contestSupervisorService.getPageOfSupervisorsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        boolean canUpdate = isAllowedToManageSupervisors(contest);
        ContestSupervisorCreateForm contestSupervisorCreateData = new ContestSupervisorCreateForm();
        contestSupervisorCreateData.isAllowedAll = true;
        contestSupervisorCreateData.allowedPermissions = Lists.newArrayList(ContestPermissions.values()).stream().collect(Collectors.toMap(p -> p.name(), p -> p.name()));

        Form<ContestSupervisorCreateForm> contestSupervisorCreateForm = Form.form(ContestSupervisorCreateForm.class).fill(contestSupervisorCreateData);

        return showListCreateSupervisor(pageOfContestSupervisors, pageIndex, orderBy, orderDir, filterString, canUpdate, contestSupervisorCreateForm, contest);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateSupervisor(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!isAllowedToManageSupervisors(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Form<ContestSupervisorCreateForm> contestSupervisorCreateForm = Form.form(ContestSupervisorCreateForm.class).bindFromRequest();

        if (formHasErrors(contestSupervisorCreateForm)) {
            Page<ContestSupervisor> pageOfContestSupervisors = contestSupervisorService.getPageOfSupervisorsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = isAllowedToManageSupervisors(contest);

            return showListCreateSupervisor(pageOfContestSupervisors, pageIndex, orderBy, orderDir, filterString, canUpdate, contestSupervisorCreateForm, contest);
        }

        ContestSupervisorCreateForm contestSupervisorCreateData = contestSupervisorCreateForm.get();
        String userJid;
        try {
            userJid = jophiel.verifyUsername(contestSupervisorCreateData.username);
        } catch (IOException e) {
            userJid = null;
        }

        if ((userJid == null) || !contestSupervisorService.isContestSupervisorInContest(contest.getJid(), userJid)) {
            contestSupervisorCreateForm.reject("error.supervisor.create.userJid.invalid");

            Page<ContestSupervisor> pageOfContestSupervisors = contestSupervisorService.getPageOfSupervisorsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
            boolean canUpdate = isAllowedToManageSupervisors(contest);

            return showListCreateSupervisor(pageOfContestSupervisors, pageIndex, orderBy, orderDir, filterString, canUpdate, contestSupervisorCreateForm, contest);
        }

        userService.upsertUserFromJophielUserJid(userJid);

        ContestPermission contestPermission;
        if (contestSupervisorCreateData.allowedPermissions != null) {
            contestPermission = new ContestPermission(ImmutableSet.of(), contestSupervisorCreateData.isAllowedAll);
        } else {
            contestPermission = new ContestPermission(contestSupervisorCreateData.allowedPermissions.keySet(), contestSupervisorCreateData.isAllowedAll);
        }
        contestSupervisorService.createContestSupervisor(contest.getId(), userJid, contestPermission);

        ControllerUtils.getInstance().addActivityLog("Add " + contestSupervisorCreateData.username + " as supervisor in contest " + contest.getName() + ".");

        return redirect(routes.ContestSupervisorController.viewSupervisors(contest.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateSupervisor(long contestId, long contestSupervisorId) throws ContestNotFoundException, ContestSupervisorNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestSupervisor contestSupervisor = contestSupervisorService.findContestSupervisorById(contestSupervisorId);
        if (!isAllowedToManageSupervisors(contest) || !contestSupervisor.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        ContestSupervisorUpdateForm contestSupervisorUpdateData = new ContestSupervisorUpdateForm();
        contestSupervisorUpdateData.isAllowedAll = contestSupervisor.getContestPermission().isAllowedAll();
        contestSupervisorUpdateData.allowedPermissions = contestSupervisor.getContestPermission().getAllowedPermissions().stream().collect(Collectors.toMap(p -> p, p -> p));

        Form<ContestSupervisorUpdateForm> contestSupervisorUpdateForm = Form.form(ContestSupervisorUpdateForm.class).fill(contestSupervisorUpdateData);

        ControllerUtils.getInstance().addActivityLog("Try to update supervisor " + contestSupervisor.getUserJid() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateSupervisor(contestSupervisorUpdateForm, contest, contestSupervisor);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateSupervisor(long contestId, long contestSupervisorId) throws ContestNotFoundException, ContestSupervisorNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestSupervisor contestSupervisor = contestSupervisorService.findContestSupervisorById(contestSupervisorId);
        if (!isAllowedToManageSupervisors(contest) || !contestSupervisor.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Form<ContestSupervisorUpdateForm> contestSupervisorUpdateForm = Form.form(ContestSupervisorUpdateForm.class).bindFromRequest();

        if (formHasErrors(contestSupervisorUpdateForm)) {
            return showUpdateSupervisor(contestSupervisorUpdateForm, contest, contestSupervisor);
        }

        ContestSupervisorUpdateForm contestSupervisorUpdateData = contestSupervisorUpdateForm.get();
        ContestPermission contestPermission;
        if (contestSupervisorUpdateData.allowedPermissions == null) {
            contestPermission = new ContestPermission(ImmutableSet.of(), contestSupervisorUpdateData.isAllowedAll);
        } else {
            contestPermission = new ContestPermission(contestSupervisorUpdateData.allowedPermissions.keySet(), contestSupervisorUpdateData.isAllowedAll);
        }
        contestSupervisorService.updateContestSupervisor(contestSupervisor.getId(), contestPermission);

        ControllerUtils.getInstance().addActivityLog("Update supervisor " + contestSupervisor.getUserJid() + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestSupervisorController.viewSupervisors(contest.getId()));
    }

    private Result showListCreateSupervisor(Page<ContestSupervisor> pageOfContestSupervisors, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, Form<ContestSupervisorCreateForm> contestSupervisorCreateForm, Contest contest) {
        LazyHtml content = new LazyHtml(listCreateSupervisorsView.render(contest.getId(), pageOfContestSupervisors, pageIndex, orderBy, orderDir, filterString, canUpdate, contestSupervisorCreateForm, jophiel.getAutoCompleteEndPoint()));
        content.appendLayout(c -> heading3Layout.render(Messages.get("supervisor.list"), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("supervisor.list"), routes.ContestSupervisorController.viewSupervisors(contest.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Supervisors");

        ControllerUtils.getInstance().addActivityLog("List all supervisors in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateSupervisor(Form<ContestSupervisorUpdateForm> contestSupervisorUpdateForm, Contest contest, ContestSupervisor contestSupervisor) {
        LazyHtml content = new LazyHtml(updateSupervisorView.render(contest.getId(), contestSupervisor.getId(), contestSupervisorUpdateForm));
        content.appendLayout(c -> heading3Layout.render(Messages.get("supervisor.update"), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("supervisor.update"), routes.ContestSupervisorController.updateSupervisor(contest.getId(), contestSupervisor.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Supervisor - Update");

        return ControllerUtils.getInstance().lazyOk(content);
    }


    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestController.jumpToSupervisors(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToManageSupervisors(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest);
    }
}
