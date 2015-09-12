package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.sandalphon.SandalphonClientAPI;
import org.iatoki.judgels.api.sandalphon.SandalphonProblem;
import org.iatoki.judgels.api.sandalphon.SandalphonProblemStatementRenderRequestParam;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.ContestProblemNotFoundException;
import org.iatoki.judgels.uriel.ContestProblemStatus;
import org.iatoki.judgels.uriel.ICPCContestStyleConfig;
import org.iatoki.judgels.uriel.IOIContestStyleConfig;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.forms.ContestProblemCreateForm;
import org.iatoki.judgels.uriel.forms.ContestProblemUpdateForm;
import org.iatoki.judgels.uriel.services.ContestProblemService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.uriel.views.html.contest.problem.createProblemView;
import org.iatoki.judgels.uriel.views.html.contest.problem.listProblemsView;
import org.iatoki.judgels.uriel.views.html.contest.problem.listUsedProblemsView;
import org.iatoki.judgels.uriel.views.html.contest.problem.updateProblemView;
import org.iatoki.judgels.uriel.views.html.contest.problem.viewProblemCriticalView;
import org.iatoki.judgels.uriel.views.html.contest.problem.viewProblemView;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public class ContestProblemController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 50;

    private final ContestProblemService contestProblemService;
    private final ContestService contestService;
    private final ProgrammingSubmissionService programmingSubmissionService;
    private final SandalphonClientAPI sandalphonClientAPI;

    @Inject
    public ContestProblemController(ContestProblemService contestProblemService, ContestService contestService, ProgrammingSubmissionService programmingSubmissionService, SandalphonClientAPI sandalphonClientAPI) {
        this.contestProblemService = contestProblemService;
        this.contestService = contestService;
        this.programmingSubmissionService = programmingSubmissionService;
        this.sandalphonClientAPI = sandalphonClientAPI;
    }

    @Transactional(readOnly = true)
    public Result viewUsedProblems(long contestId) throws ContestNotFoundException {
        return listUsedProblems(contestId, 0);
    }

    @Transactional(readOnly = true)
    public Result listUsedProblems(long contestId, long pageIndex) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Page<ContestProblem> pageOfContestProblems = contestProblemService.getPageOfUsedProblemsInContest(contest.getJid(), pageIndex, PAGE_SIZE);
        ImmutableList.Builder<ContestProblem> replacementBuilder = ImmutableList.builder();
        for (ContestProblem contestProblem : pageOfContestProblems.getData()) {
            contestProblem.setTotalSubmissions(programmingSubmissionService.countProgrammingSubmissionsByUserJid(contest.getJid(), contestProblem.getProblemJid(), IdentityUtils.getUserJid()));
            replacementBuilder.add(contestProblem);
        }
        pageOfContestProblems = new Page<>(replacementBuilder.build(), pageOfContestProblems.getTotalRowsCount(), pageOfContestProblems.getPageIndex(), pageOfContestProblems.getPageSize());
        List<String> problemJids = pageOfContestProblems.getData().stream().map(cp -> cp.getProblemJid()).collect(Collectors.toList());
        Map<String, String> problemTitlesMap = SandalphonResourceDisplayNameUtils.buildTitlesMap(JidCacheServiceImpl.getInstance().getDisplayNames(problemJids), ContestControllerUtils.getInstance().getCurrentStatementLanguage());

        LazyHtml content = new LazyHtml(listUsedProblemsView.render(contest.getId(), pageOfContestProblems, pageIndex, problemTitlesMap));
        content.appendLayout(c -> heading3Layout.render(Messages.get("problem.problems"), c));
        if (isAllowedToSuperviseProblems(contest)) {
            appendSubtabsLayout(content, contest);
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("problem.list"), routes.ContestProblemController.viewUsedProblems(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problems");

        UrielControllerUtils.getInstance().addActivityLog("Open list of valid problems in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewProblem(long contestId, long contestProblemId) throws ContestNotFoundException, ContestProblemNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestProblemService.findContestProblemById(contestProblemId);
        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid()) || !isAllowedToViewProblem(contest, contestProblem)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        long submissionsLeft = -1;
        if (contestProblem.getSubmissionsLimit() != 0) {
            submissionsLeft = contestProblem.getSubmissionsLimit() - programmingSubmissionService.countProgrammingSubmissionsByUserJid(contest.getJid(), contestProblem.getProblemJid(), IdentityUtils.getUserJid());
        }

        SandalphonProblemStatementRenderRequestParam param = new SandalphonProblemStatementRenderRequestParam();

        param.setProblemSecret(contestProblem.getProblemSecret());
        param.setCurrentMillis(System.currentTimeMillis());
        param.setStatementLanguage(ContestControllerUtils.getInstance().getCurrentStatementLanguage());
        param.setSwitchStatementLanguageUrl(routes.ContestProblemController.switchLanguage(contestId, contestProblemId).absoluteURL(request(), request().secure()));
        param.setPostSubmitUrl(routes.ContestProgrammingSubmissionController.postSubmitProblem(contest.getId(), contestProblem.getProblemJid()).absoluteURL(request(), request().secure()));
        param.setReasonNotAllowedToSubmit(null);

        Set<String> allowedGradingLanguages;

        if (contest.isICPC()) {
            allowedGradingLanguages = ((ICPCContestStyleConfig) contest.getStyleConfig()).getLanguageRestriction().getAllowedLanguageNames();
        } else {
            allowedGradingLanguages = ((IOIContestStyleConfig) contest.getStyleConfig()).getLanguageRestriction().getAllowedLanguageNames();
        }

        param.setAllowedGradingLanguages(StringUtils.join(allowedGradingLanguages, ","));

        String requestUrl = sandalphonClientAPI.getProblemStatementRenderAPIEndpoint(contestProblem.getProblemJid());
        String requestBody = sandalphonClientAPI.constructProblemStatementRenderAPIRequestBody(contestProblem.getProblemJid(), param);

        LazyHtml content;
        if (UrielProperties.getInstance().isContestCritial(contest.getJid())) {
            content = new LazyHtml(viewProblemCriticalView.render(requestUrl, requestBody, submissionsLeft, contestProblem.getStatus() == ContestProblemStatus.CLOSED, contest, contestProblem));
        } else {
            content = new LazyHtml(viewProblemView.render(requestUrl, requestBody, submissionsLeft, contestProblem.getStatus() == ContestProblemStatus.CLOSED));
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.contestant"), routes.ContestProblemController.viewUsedProblems(contest.getId())),
                new InternalLink(contestProblem.getAlias(), routes.ContestProblemController.viewProblem(contest.getId(), contestProblem.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problem - View");

        UrielControllerUtils.getInstance().addActivityLog("View problem " + contestProblem.getAlias() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result renderImage(long contestId, long contestProblemId, String imageFilename) throws ContestNotFoundException, ContestProblemNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestProblemService.findContestProblemById(contestProblemId);
        if (!contest.getJid().equals(contestProblem.getContestJid())) {
            return notFound();
        }

        String imageUrl = sandalphonClientAPI.getProblemStatementMediaRenderAPIEndpoint(contestProblem.getProblemJid(), imageFilename);

        return redirect(imageUrl);
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
        if (!isAllowedToSuperviseProblems(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        boolean canDelete = ContestControllerUtils.getInstance().isManagerOrAbove(contest, IdentityUtils.getUserJid());

        Page<ContestProblem> pageOfContestProblems = contestProblemService.getPageOfProblemsInContest(contest.getJid(), page, PAGE_SIZE, sortBy, orderBy, filterString, null);
        List<String> problemJids = pageOfContestProblems.getData().stream().map(cp -> cp.getProblemJid()).collect(Collectors.toList());
        Map<String, String> problemSlugsMap = SandalphonResourceDisplayNameUtils.buildSlugsMap(JidCacheServiceImpl.getInstance().getDisplayNames(problemJids));

        LazyHtml content = new LazyHtml(listProblemsView.render(contest.getId(), pageOfContestProblems, page, sortBy, orderBy, filterString, canDelete, problemSlugsMap));
        content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("problem.list"), new InternalLink(Messages.get("commons.create"), routes.ContestProblemController.createProblem(contestId)), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("problem.list"), routes.ContestProblemController.viewProblems(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problems");

        UrielControllerUtils.getInstance().addActivityLog("Open all problems in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createProblem(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !isAllowedToSuperviseProblems(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestProblemCreateForm contestProblemCreateData = new ContestProblemCreateForm();
        contestProblemCreateData.submissionsLimit = 0;
        Form<ContestProblemCreateForm> contestProblemCreateForm = Form.form(ContestProblemCreateForm.class);
        contestProblemCreateForm = contestProblemCreateForm.fill(contestProblemCreateData);

        UrielControllerUtils.getInstance().addActivityLog("Try to add problem in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showCreateProblem(contestProblemCreateForm, contest);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateProblem(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !isAllowedToSuperviseProblems(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestProblemCreateForm> contestProblemCreateForm = Form.form(ContestProblemCreateForm.class).bindFromRequest();

        if (formHasErrors(contestProblemCreateForm)) {
            return showCreateProblem(contestProblemCreateForm, contest);
        }

        ContestProblemCreateForm contestProblemCreateData = contestProblemCreateForm.get();

        SandalphonProblem sandalphonProblem;
        try {
            sandalphonProblem = sandalphonClientAPI.findProblemByJid(contestProblemCreateData.problemJid);
        } catch (JudgelsAPIClientException e) {
            contestProblemCreateForm.reject("error.system.sandalphon.connection");
            return showCreateProblem(contestProblemCreateForm, contest);
        }

        if ((sandalphonProblem == null) || contestProblemService.isProblemInContestByJidOrAlias(contest.getJid(), contestProblemCreateData.problemJid, contestProblemCreateData.alias)) {
            contestProblemCreateForm.reject("error.problem.create.problemJidOrAlias.invalid");
            return showCreateProblem(contestProblemCreateForm, contest);
        }

        contestProblemService.createContestProblem(contest.getJid(), contestProblemCreateData.problemJid, contestProblemCreateData.problemSecret, contestProblemCreateData.alias, contestProblemCreateData.submissionsLimit, ContestProblemStatus.valueOf(contestProblemCreateData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        JidCacheServiceImpl.getInstance().putDisplayName(contestProblemCreateData.problemJid, sandalphonProblem.getDisplayName(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog("Add problem " + contestProblemCreateData.alias + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestProblemController.viewProblems(contest.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateProblem(long contestId, long contestProblemId) throws ContestNotFoundException, ContestProblemNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestProblemService.findContestProblemById(contestProblemId);
        if (contest.isLocked() || !isAllowedToSuperviseProblems(contest) || !contestProblem.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestProblemUpdateForm contestProblemUpdateData = new ContestProblemUpdateForm();
        contestProblemUpdateData.alias = contestProblem.getAlias();
        contestProblemUpdateData.submissionsLimit = contestProblem.getSubmissionsLimit();
        contestProblemUpdateData.status = contestProblem.getStatus().name();
        Form<ContestProblemUpdateForm> contestProblemUpdateForm = Form.form(ContestProblemUpdateForm.class).fill(contestProblemUpdateData);

        UrielControllerUtils.getInstance().addActivityLog("Try to update problem " + contestProblem.getAlias() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateProblem(contestProblemUpdateForm, contest, contestProblem);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateProblem(long contestId, long contestProblemId) throws ContestNotFoundException, ContestProblemNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestProblemService.findContestProblemById(contestProblemId);
        if (contest.isLocked() || !isAllowedToSuperviseProblems(contest) || !contestProblem.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestProblemUpdateForm> contestProblemUpdateForm = Form.form(ContestProblemUpdateForm.class).bindFromRequest();

        if (formHasErrors(contestProblemUpdateForm)) {
            return showUpdateProblem(contestProblemUpdateForm, contest, contestProblem);
        }

        ContestProblemUpdateForm contestProblemUpdateData = contestProblemUpdateForm.get();
        contestProblemService.updateContestProblem(contestProblem.getId(), contestProblemUpdateData.alias, contestProblemUpdateData.submissionsLimit, ContestProblemStatus.valueOf(contestProblemUpdateData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog("Update problem " + contestProblem.getAlias() + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestProblemController.viewProblems(contest.getId()));
    }

    @Transactional
    public Result deleteProblem(long contestId, long contestProblemId) throws ContestNotFoundException, ContestProblemNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestProblemService.findContestProblemById(contestProblemId);
        if (contest.isLocked() || !ContestControllerUtils.getInstance().isManagerOrAbove(contest, IdentityUtils.getUserJid()) || !contestProblem.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        contestProblemService.deleteContestProblem(contestProblem.getId());

        UrielControllerUtils.getInstance().addActivityLog("Delete problem " + JidCacheServiceImpl.getInstance().getDisplayName(contestProblem.getProblemJid()) + ".");

        return redirect(routes.UserController.index());
    }

    private Result showCreateProblem(Form<ContestProblemCreateForm> contestProblemCreateForm, Contest contest) {
        LazyHtml content = new LazyHtml(createProblemView.render(contest.getId(), contestProblemCreateForm));
        content.appendLayout(c -> heading3Layout.render(Messages.get("problem.create"), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestProblemController.viewProblems(contest.getId())),
                new InternalLink(Messages.get("problem.create"), routes.ContestProblemController.createProblem(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problem - Create");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateProblem(Form<ContestProblemUpdateForm> contestProblemUpdateForm, Contest contest, ContestProblem contestProblem) {
        LazyHtml content = new LazyHtml(updateProblemView.render(contest.getId(), contestProblem, contestProblemUpdateForm));
        content.appendLayout(c -> heading3Layout.render(Messages.get("problem.update") + " " + contestProblem.getAlias(), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestProblemController.viewProblems(contest.getId())),
                new InternalLink(Messages.get("problem.update"), routes.ContestProblemController.updateProblem(contest.getId(), contestProblem.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problem - Update");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }


    private void appendSubtabsLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestProblemController.viewUsedProblems(contest.getId()), routes.ContestProblemController.viewProblems(contest.getId()), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("problem.problems"), routes.ContestController.jumpToProblems(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseProblems(Contest contest) {
        return ContestControllerUtils.getInstance().isPermittedToSupervise(contest, ContestPermissions.PROBLEM, IdentityUtils.getUserJid());
    }

    private boolean isAllowedToViewProblem(Contest contest, ContestProblem contestProblem) {
        return contestProblem.getContestJid().equals(contest.getJid()) && contestProblem.getStatus() != ContestProblemStatus.UNUSED;
    }
}
