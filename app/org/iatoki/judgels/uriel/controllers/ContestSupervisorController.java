package org.iatoki.judgels.uriel.controllers;

import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.ContestSupervisor;
import org.iatoki.judgels.uriel.controllers.forms.ContestSupervisorCreateForm;
import org.iatoki.judgels.uriel.ContestSupervisorNotFoundException;
import org.iatoki.judgels.uriel.controllers.forms.ContestSupervisorUpdateForm;
import org.iatoki.judgels.uriel.services.ContestSupervisorService;
import org.iatoki.judgels.uriel.services.UserService;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
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

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public class ContestSupervisorController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final Jophiel jophiel;
    private final ContestService contestService;
    private final ContestSupervisorService contestSupervisorService;
    private final UserService userRoleService;

    @Inject
    public ContestSupervisorController(Jophiel jophiel, ContestService contestService, ContestSupervisorService contestSupervisorService, UserService userRoleService) {
        this.jophiel = jophiel;
        this.contestService = contestService;
        this.contestSupervisorService = contestSupervisorService;
        this.userRoleService = userRoleService;
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
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            Page<ContestSupervisor> contestSupervisorPage = contestSupervisorService.pageContestSupervisorsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = isAllowedToManageSupervisors(contest);

            Form<ContestSupervisorCreateForm> form = Form.form(ContestSupervisorCreateForm.class);

            return showListCreateSupervisor(contestSupervisorPage, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateSupervisor(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToManageSupervisors(contest)) {
            Form<ContestSupervisorCreateForm> form = Form.form(ContestSupervisorCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                Page<ContestSupervisor> contestSupervisorPage = contestSupervisorService.pageContestSupervisorsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

                boolean canUpdate = isAllowedToManageSupervisors(contest);

                return showListCreateSupervisor(contestSupervisorPage, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
            } else {
                ContestSupervisorCreateForm contestSupervisorCreateForm = form.get();
                try {
                    String userJid = jophiel.verifyUsername(contestSupervisorCreateForm.username);
                    if ((userJid != null) && (!contestSupervisorService.isContestSupervisorInContestByUserJid(contest.getJid(), userJid))) {
                        userRoleService.upsertUserFromJophielUserJid(userJid);
                        contestSupervisorService.createContestSupervisor(contest.getId(), userJid, contestSupervisorCreateForm.announcement, contestSupervisorCreateForm.problem, contestSupervisorCreateForm.submission, contestSupervisorCreateForm.clarification, contestSupervisorCreateForm.contestant);

                        ControllerUtils.getInstance().addActivityLog("Add " + contestSupervisorCreateForm.username + " as supervisor in contest " + contest.getName() + ".");

                        return redirect(routes.ContestSupervisorController.viewSupervisors(contest.getId()));
                    } else {
                        form.reject("error.supervisor.create.userJid.invalid");

                        Page<ContestSupervisor> contestSupervisorPage = contestSupervisorService.pageContestSupervisorsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
                        boolean canUpdate = isAllowedToManageSupervisors(contest);

                        return showListCreateSupervisor(contestSupervisorPage, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
                    }
                } catch (IOException e) {
                    form.reject("error.supervisor.create.userJid.invalid");

                    Page<ContestSupervisor> contestSupervisorPage = contestSupervisorService.pageContestSupervisorsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
                    boolean canUpdate = isAllowedToManageSupervisors(contest);

                    return showListCreateSupervisor(contestSupervisorPage, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
                }
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateSupervisor(long contestId, long contestSupervisorId) throws ContestNotFoundException, ContestSupervisorNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestSupervisor contestSupervisor = contestSupervisorService.findContestSupervisorByContestSupervisorId(contestSupervisorId);
        if (isAllowedToManageSupervisors(contest) && contestSupervisor.getContestJid().equals(contest.getJid())) {
            ContestSupervisorUpdateForm contestSupervisorUpdateForm = new ContestSupervisorUpdateForm(contestSupervisor);
            Form<ContestSupervisorUpdateForm> form = Form.form(ContestSupervisorUpdateForm.class).fill(contestSupervisorUpdateForm);

            ControllerUtils.getInstance().addActivityLog("Try to update supervisor " + contestSupervisor.getUserJid() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateSupervisor(form, contest, contestSupervisor);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateSupervisor(long contestId, long contestSupervisorId) throws ContestNotFoundException, ContestSupervisorNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestSupervisor contestSupervisor = contestSupervisorService.findContestSupervisorByContestSupervisorId(contestSupervisorId);
        if (isAllowedToManageSupervisors(contest) && contestSupervisor.getContestJid().equals(contest.getJid())) {
            Form<ContestSupervisorUpdateForm> form = Form.form(ContestSupervisorUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateSupervisor(form, contest, contestSupervisor);
            } else {
                ContestSupervisorUpdateForm contestSupervisorUpdateForm = form.get();
                contestSupervisorService.updateContestSupervisor(contestSupervisor.getId(), contestSupervisorUpdateForm.announcement, contestSupervisorUpdateForm.problem, contestSupervisorUpdateForm.submission, contestSupervisorUpdateForm.clarification, contestSupervisorUpdateForm.contestant);

                ControllerUtils.getInstance().addActivityLog("Update supervisor " + contestSupervisor.getUserJid() + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestSupervisorController.viewSupervisors(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    private Result showListCreateSupervisor(Page<ContestSupervisor> contestSupervisorPage, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, Form<ContestSupervisorCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(listCreateSupervisorsView.render(contest.getId(), contestSupervisorPage, pageIndex, orderBy, orderDir, filterString, canUpdate, form, jophiel.getAutoCompleteEndPoint()));
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

    private Result showUpdateSupervisor(Form<ContestSupervisorUpdateForm> form, Contest contest, ContestSupervisor contestSupervisor){
        LazyHtml content = new LazyHtml(updateSupervisorView.render(contest.getId(), contestSupervisor.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("supervisor.update"), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance(). appendSidebarLayout(content);
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
