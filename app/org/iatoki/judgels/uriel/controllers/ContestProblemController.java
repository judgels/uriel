package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.sandalphon.Sandalphon;
import org.iatoki.judgels.sandalphon.services.SubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.forms.ContestProblemCreateForm;
import org.iatoki.judgels.uriel.ContestProblemNotFoundException;
import org.iatoki.judgels.uriel.ContestProblemStatus;
import org.iatoki.judgels.uriel.forms.ContestProblemUpdateForm;
import org.iatoki.judgels.uriel.services.ContestProblemService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.ICPCContestStyleConfig;
import org.iatoki.judgels.uriel.IOIContestStyleConfig;
import org.iatoki.judgels.uriel.services.ContestSupervisorService;
import org.iatoki.judgels.uriel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.problem.createProblemView;
import org.iatoki.judgels.uriel.views.html.contest.problem.listUsedProblemsView;
import org.iatoki.judgels.uriel.views.html.contest.problem.listProblemsView;
import org.iatoki.judgels.uriel.views.html.contest.problem.updateProblemView;
import org.iatoki.judgels.uriel.views.html.contest.problem.viewProblemView;
import org.iatoki.judgels.uriel.views.html.contest.problem.viewProblemCriticalView;
import org.iatoki.judgels.uriel.views.html.layouts.accessTypeByStatusLayout;
import play.data.DynamicForm;
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
import java.net.URI;
import java.util.Arrays;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public class ContestProblemController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 50;

    private final Sandalphon sandalphon;
    private final ContestService contestService;
    private final ContestProblemService contestProblemService;
    private final ContestSupervisorService contestSupervisorService;
    private final SubmissionService submissionService;

    @Inject
    public ContestProblemController(Sandalphon sandalphon, ContestService contestService, ContestProblemService contestProblemService, ContestSupervisorService contestSupervisorService, SubmissionService submissionService) {
        this.sandalphon = sandalphon;
        this.contestService = contestService;
        this.contestProblemService = contestProblemService;
        this.contestSupervisorService = contestSupervisorService;
        this.submissionService = submissionService;
    }

    @Transactional(readOnly = true)
    public Result viewUsedProblems(long contestId) throws ContestNotFoundException {
        return listUsedProblems(contestId, 0);
    }

    @Transactional(readOnly = true)
    public Result listUsedProblems(long contestId, long pageIndex) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isAllowedToEnterContest(contest)) {
            Page<ContestProblem> contestProblems = contestProblemService.pageUsedContestProblemsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE);
            ImmutableList.Builder<ContestProblem> replacementBuilder = ImmutableList.builder();
            for (ContestProblem contestProblem : contestProblems.getData()) {
                contestProblem.setTotalSubmissions(submissionService.countSubmissionsByContestJidByUser(contest.getJid(), contestProblem.getProblemJid(), IdentityUtils.getUserJid()));
                replacementBuilder.add(contestProblem);
            }
            contestProblems = new Page<>(replacementBuilder.build(), contestProblems.getTotalRowsCount(), contestProblems.getPageIndex(), contestProblems.getPageSize());

            LazyHtml content = new LazyHtml(listUsedProblemsView.render(contest.getId(), contestProblems, pageIndex));
            content.appendLayout(c -> heading3Layout.render(Messages.get("problem.problems"), c));
            if (isAllowedToSuperviseProblems(contest)) {
                appendSubtabsLayout(content, contest);
            }
            ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, contest,
                    new InternalLink(Messages.get("problem.list"), routes.ContestProblemController.viewUsedProblems(contest.getId()))
            );

            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problems");

            ControllerUtils.getInstance().addActivityLog("Open list of valid problems in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    public Result viewProblem(long contestId, long contestProblemId) throws ContestNotFoundException, ContestProblemNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestProblemService.findContestProblemByContestProblemId(contestProblemId);
        if (ContestControllerUtils.getInstance().isAllowedToEnterContest(contest) && isAllowedToViewProblem(contest, contestProblem)) {
            long submissionsLeft = -1;
            if (contestProblem.getSubmissionsLimit() != 0) {
                submissionsLeft = contestProblem.getSubmissionsLimit() - submissionService.countSubmissionsByContestJidByUser(contest.getJid(), contestProblem.getProblemJid(), IdentityUtils.getUserJid());
            }

            String requestUrl = sandalphon.getProblemStatementRenderUri().toString();
            String requestBody = "";

            ContestConfiguration config = contestService.findContestConfigurationByContestJid(contest.getJid());
            String styleConfig = config.getStyleConfig();

            if (contest.isICPC()) {
                requestBody = sandalphon.getProblemStatementRenderRequestBody(contestProblem.getProblemJid(), contestProblem.getProblemSecret(), System.currentTimeMillis(), ContestControllerUtils.getInstance().getCurrentStatementLanguage(), routes.ContestSubmissionController.postSubmitProblem(contestId, contestProblem.getProblemJid()).absoluteURL(request(), request().secure()), routes.ContestProblemController.switchLanguage(contestId, contestProblemId).absoluteURL(request(), request().secure()), null, new Gson().fromJson(styleConfig, ICPCContestStyleConfig.class).getLanguageRestriction());
            } else if (contest.isIOI()) {
                requestBody = sandalphon.getProblemStatementRenderRequestBody(contestProblem.getProblemJid(), contestProblem.getProblemSecret(), System.currentTimeMillis(), ContestControllerUtils.getInstance().getCurrentStatementLanguage(), routes.ContestSubmissionController.postSubmitProblem(contestId, contestProblem.getProblemJid()).absoluteURL(request(), request().secure()), routes.ContestProblemController.switchLanguage(contestId, contestProblemId).absoluteURL(request(), request().secure()), null, new Gson().fromJson(styleConfig, IOIContestStyleConfig.class).getLanguageRestriction());
            }

            LazyHtml content;
            if (UrielProperties.getInstance().isContestCritial(contest.getJid())) {
                content = new LazyHtml(viewProblemCriticalView.render(requestUrl, requestBody, submissionsLeft, contestProblem.getStatus() == ContestProblemStatus.CLOSED, contest, contestProblem));
            } else {
                content = new LazyHtml(viewProblemView.render(requestUrl, requestBody, submissionsLeft, contestProblem.getStatus() == ContestProblemStatus.CLOSED));
            }
            ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, contest,
                    new InternalLink(Messages.get("status.contestant"), routes.ContestProblemController.viewUsedProblems(contest.getId())),
                    new InternalLink(contestProblem.getAlias(), routes.ContestProblemController.viewProblem(contest.getId(), contestProblem.getId()))
            );

            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problem - View");

            ControllerUtils.getInstance().addActivityLog("View problem " + contestProblem.getAlias() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    public Result renderImage(long contestId, long contestProblemId, String imageFilename) throws ContestNotFoundException, ContestProblemNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestProblemService.findContestProblemByContestProblemId(contestProblemId);
        if (contest.getJid().equals(contestProblem.getContestJid())) {
            URI imageUri = sandalphon.getProblemMediaRenderUri(contestProblem.getProblemJid(), imageFilename);

            return redirect(imageUri.toString());
        } else {
            return notFound();
        }
    }

    public Result switchLanguage(long contestId, long contestProblemId) {
        String languageCode = DynamicForm.form().bindFromRequest().get("langCode");
        ContestControllerUtils.getInstance().setCurrentStatementLanguage(languageCode);

        return redirect(routes.ContestProblemController.viewProblem(contestId, contestProblemId));
    }

    @Transactional(readOnly = true)
    public Result viewProblems(long contestId) throws ContestNotFoundException {
        return listProblems(contestId, 0, "alias", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listProblems(long contestId, long page, String sortBy, String orderBy, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseProblems(contest)) {
            Page<ContestProblem> contestProblemPage = contestProblemService.pageContestProblemsByContestJid(contest.getJid(), page, PAGE_SIZE, sortBy, orderBy, filterString, null);

            LazyHtml content = new LazyHtml(listProblemsView.render(contest.getId(), contestProblemPage, page, sortBy, orderBy, filterString));
            content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("problem.list"), new InternalLink(Messages.get("commons.create"), routes.ContestProblemController.createProblem(contestId)), c));
            appendSubtabsLayout(content, contest);
            ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, contest,
                    new InternalLink(Messages.get("problem.list"), routes.ContestProblemController.viewProblems(contest.getId()))
            );
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problems");

            ControllerUtils.getInstance().addActivityLog("Open all problems in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createProblem(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseProblems(contest)) {
            Form<ContestProblemCreateForm> form = Form.form(ContestProblemCreateForm.class);
            form = form.fill(new ContestProblemCreateForm(0));

            ControllerUtils.getInstance().addActivityLog("Try to add problem in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showCreateProblem(form, contest);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateProblem(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseProblems(contest)) {
            Form<ContestProblemCreateForm> form = Form.form(ContestProblemCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateProblem(form, contest);
            } else {
                ContestProblemCreateForm contestProblemCreateForm = form.get();
                String problemName = null;
                try {
                    problemName = sandalphon.verifyProblemJid(contestProblemCreateForm.problemJid);
                } catch (IOException e) {
                    form.reject("error.system.sandalphon.connection");
                    return showCreateProblem(form, contest);
                }

                if ((problemName != null) && (!contestProblemService.isContestProblemInContestByProblemJidOrAlias(contest.getJid(), contestProblemCreateForm.problemJid, contestProblemCreateForm.alias))) {
                    contestProblemService.createContestProblem(contest.getId(), contestProblemCreateForm.problemJid, contestProblemCreateForm.problemSecret, contestProblemCreateForm.alias, contestProblemCreateForm.submissionsLimit, ContestProblemStatus.valueOf(contestProblemCreateForm.status));
                    JidCacheServiceImpl.getInstance().putDisplayName(contestProblemCreateForm.problemJid, problemName, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

                    ControllerUtils.getInstance().addActivityLog("Add problem " + contestProblemCreateForm.alias + " in contest " + contest.getName() + ".");

                    return redirect(routes.ContestProblemController.viewProblems(contest.getId()));
                } else {
                    form.reject("error.problem.create.problemJidOrAlias.invalid");
                    return showCreateProblem(form, contest);
                }
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateProblem(long contestId, long contestProblemId) throws ContestNotFoundException, ContestProblemNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestProblemService.findContestProblemByContestProblemId(contestProblemId);
        if (isAllowedToSuperviseProblems(contest) && contestProblem.getContestJid().equals(contest.getJid())) {
            ContestProblemUpdateForm contestProblemUpdateForm = new ContestProblemUpdateForm(contestProblem);
            Form<ContestProblemUpdateForm> form = Form.form(ContestProblemUpdateForm.class).fill(contestProblemUpdateForm);

            ControllerUtils.getInstance().addActivityLog("Try to update problem " + contestProblem.getAlias() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateProblem(form, contest, contestProblem);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateProblem(long contestId, long contestProblemId) throws ContestNotFoundException, ContestProblemNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestProblemService.findContestProblemByContestProblemId(contestProblemId);
        if (isAllowedToSuperviseProblems(contest) && contestProblem.getContestJid().equals(contest.getJid())) {
            Form<ContestProblemUpdateForm> form = Form.form(ContestProblemUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateProblem(form, contest, contestProblem);
            } else {
                ContestProblemUpdateForm contestProblemUpdateForm = form.get();
                contestProblemService.updateContestProblem(contestProblem.getId(), contestProblemUpdateForm.alias, contestProblemUpdateForm.submissionsLimit, ContestProblemStatus.valueOf(contestProblemUpdateForm.status));

                ControllerUtils.getInstance().addActivityLog("Update problem " + contestProblem.getAlias() + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestProblemController.viewProblems(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    private Result showCreateProblem(Form<ContestProblemCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createProblemView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("problem.create"), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestProblemController.viewProblems(contest.getId())),
                new InternalLink(Messages.get("problem.create"), routes.ContestProblemController.createProblem(contest.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problem - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateProblem(Form<ContestProblemUpdateForm> form, Contest contest, ContestProblem contestProblem){
        LazyHtml content = new LazyHtml(updateProblemView.render(contest.getId(), contestProblem, form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("problem.update") + " " + contestProblem.getAlias(), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestProblemController.viewProblems(contest.getId())),
                new InternalLink(Messages.get("problem.update"), routes.ContestProblemController.updateProblem(contest.getId(), contestProblem.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problem - Update");

        return ControllerUtils.getInstance().lazyOk(content);
    }


    private void appendSubtabsLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestProblemController.viewUsedProblems(contest.getId()), routes.ContestProblemController.viewProblems(contest.getId()), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("problem.problems"), routes.ContestController.jumpToProblems(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseProblems(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest) || (ContestControllerUtils.getInstance().isSupervisor(contest) && contestSupervisorService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isProblem());
    }

    private boolean isAllowedToViewProblem(Contest contest, ContestProblem contestProblem) {
        return contestProblem.getContestJid().equals(contest.getJid()) && contestProblem.getStatus() != ContestProblemStatus.UNUSED;
    }
}
