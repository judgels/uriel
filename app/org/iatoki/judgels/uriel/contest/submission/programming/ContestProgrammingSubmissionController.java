package org.iatoki.judgels.uriel.contest.submission.programming;

import com.google.common.collect.Lists;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.gabriel.GradingLanguageRegistry;
import org.iatoki.judgels.gabriel.SubmissionSource;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.forms.ListTableSelectionForm;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionException;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionUtils;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.sandalphon.adapters.GradingEngineAdapterRegistry;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestant;
import org.iatoki.judgels.uriel.contest.ContestNotFoundException;
import org.iatoki.judgels.uriel.contest.supervisor.ContestPermissions;
import org.iatoki.judgels.uriel.contest.problem.ContestProblem;
import org.iatoki.judgels.uriel.contest.problem.ContestProblemStatus;
import org.iatoki.judgels.uriel.activity.UrielActivityKeys;
import org.iatoki.judgels.uriel.UrielControllerUtils;
import org.iatoki.judgels.uriel.contest.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestantService;
import org.iatoki.judgels.uriel.contest.problem.ContestProblemService;
import org.iatoki.judgels.uriel.contest.ContestService;
import org.iatoki.judgels.uriel.jid.JidCacheServiceImpl;
import org.iatoki.judgels.uriel.contest.submission.programming.html.listScreenedSubmissionsView;
import org.iatoki.judgels.uriel.contest.submission.programming.html.listSubmissionsView;
import org.iatoki.judgels.uriel.contest.html.accessTypeByStatusLayout;
import play.data.Form;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
public final class ContestProgrammingSubmissionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String SUBMISSION = "submission";
    private static final String PROGRAMMING_FILES = "programming_files";
    private static final String PROBLEM = "problem";
    private static final String CONTEST = "contest";

    private final ContestContestantService contestContestantService;
    private final ContestProblemService contestProblemService;
    private final ContestService contestService;
    private final ProgrammingSubmissionService programmingSubmissionService;
    private final FileSystemProvider programmingSubmissionLocalFileSystemProvider;
    private final FileSystemProvider programmingSubmissionRemoteFileSystemProvider;

    @Inject
    public ContestProgrammingSubmissionController(ContestContestantService contestContestantService, ContestProblemService contestProblemService, ContestService contestService, ProgrammingSubmissionService programmingSubmissionService, @ProgrammingSubmissionLocalFileSystemProvider FileSystemProvider programmingSubmissionLocalFileSystemProvider, @ProgrammingSubmissionRemoteFileSystemProvider @Nullable FileSystemProvider programmingSubmissionRemoteFileSystemProvider) {
        this.contestContestantService = contestContestantService;
        this.contestProblemService = contestProblemService;
        this.contestService = contestService;
        this.programmingSubmissionService = programmingSubmissionService;
        this.programmingSubmissionLocalFileSystemProvider = programmingSubmissionLocalFileSystemProvider;
        this.programmingSubmissionRemoteFileSystemProvider = programmingSubmissionRemoteFileSystemProvider;
    }

    @Transactional
    public Result postSubmitProblem(long contestId, String problemJid) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestProblemService.findContestProblemInContestByJid(contest.getJid(), problemJid);

        if (!ContestControllerUtils.getInstance().isAllowedToDoContest(contest, IdentityUtils.getUserJid()) || !contestProblem.getContestJid().equals(contest.getJid()) || contestProblem.getStatus() == ContestProblemStatus.UNUSED) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        if (contestProblem.getStatus() == ContestProblemStatus.CLOSED) {
            return redirect(org.iatoki.judgels.uriel.contest.problem.routes.ContestProblemController.viewProblem(contestId, contestProblem.getId()));
        }

        if ((contestProblem.getSubmissionsLimit() != 0) && (programmingSubmissionService.countProgrammingSubmissionsByUserJid(contest.getJid(), contestProblem.getProblemJid(), IdentityUtils.getUserJid()) >= contestProblem.getSubmissionsLimit())) {
            flash("submissionError", "submission.limit.reached");
            return redirect(org.iatoki.judgels.uriel.contest.problem.routes.ContestProblemController.viewProblem(contestId, contestProblem.getId()));
        }

        Http.MultipartFormData body = request().body().asMultipartFormData();

        String gradingLanguage = body.asFormUrlEncoded().get("language")[0];
        String gradingEngine = body.asFormUrlEncoded().get("engine")[0];
        String submissionJid;

        try {
            SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromNewSubmission(body);
            submissionJid = programmingSubmissionService.submit(problemJid, contest.getJid(), gradingEngine, gradingLanguage, null, submissionSource, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            ProgrammingSubmissionUtils.storeSubmissionFiles(programmingSubmissionLocalFileSystemProvider, programmingSubmissionRemoteFileSystemProvider, submissionJid, submissionSource);

        } catch (ProgrammingSubmissionException e) {
            flash("submissionError", e.getMessage());

            return redirect(org.iatoki.judgels.uriel.contest.problem.routes.ContestProblemController.viewProblem(contestId, contestProblem.getId()));
        }

        UrielControllerUtils.getInstance().addActivityLog(UrielActivityKeys.SUBMIT.construct(CONTEST, contest.getJid(), contest.getName(), PROBLEM, contestProblem.getProblemJid(), SandalphonResourceDisplayNameUtils.parseSlugByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(contestProblem.getProblemJid())), SUBMISSION, submissionJid, PROGRAMMING_FILES));

        return redirect(routes.ContestProgrammingSubmissionController.viewScreenedSubmissions(contestId));
    }

    @Transactional(readOnly = true)
    public Result viewScreenedSubmissions(long contestId) throws ContestNotFoundException {
        return listScreenedSubmissions(contestId, 0, "id", "desc", null);
    }

    @Transactional(readOnly = true)
    public Result listScreenedSubmissions(long contestId, long pageIndex, String orderBy, String orderDir, String problemJid) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid()) || ContestControllerUtils.getInstance().isCoach(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

        Page<ProgrammingSubmission> pageOfProgrammingSubmissions = programmingSubmissionService.getPageOfProgrammingSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, IdentityUtils.getUserJid(), actualProblemJid, contest.getJid());
        Map<String, String> problemJidToAliasMap = contestProblemService.getMappedJidToAliasInContest(contest.getJid());
        Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

        LazyHtml content = new LazyHtml(listScreenedSubmissionsView.render(contestId, pageOfProgrammingSubmissions, problemJidToAliasMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, actualProblemJid));
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        if (isAllowedToSuperviseSubmissions(contest)) {
            appendSubtabsLayout(content, contest);
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("submission.list"), routes.ContestProgrammingSubmissionController.viewScreenedSubmissions(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Submissions");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewSubmission(long contestId, long submissionId) throws ContestNotFoundException, ProgrammingSubmissionNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ProgrammingSubmission submission = programmingSubmissionService.findProgrammingSubmissionById(submissionId);

        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid()) || !isAllowedToViewSubmission(contest, submission)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromPastSubmission(programmingSubmissionLocalFileSystemProvider, programmingSubmissionRemoteFileSystemProvider, submission.getJid());
        String authorName = JidCacheServiceImpl.getInstance().getDisplayName(submission.getAuthorJid());
        ContestProblem contestProblem = contestProblemService.findContestProblemInContestByJid(contest.getJid(), submission.getProblemJid());
        String contestProblemAlias = contestProblem.getAlias();
        String contestProblemName = SandalphonResourceDisplayNameUtils.parseTitleByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(contestProblem.getProblemJid()), ContestControllerUtils.getInstance().getCurrentStatementLanguage());
        String gradingLanguageName = GradingLanguageRegistry.getInstance().getLanguage(submission.getGradingLanguage()).getName();

        LazyHtml content = new LazyHtml(GradingEngineAdapterRegistry.getInstance().getByGradingEngineName(submission.getGradingEngine()).renderViewSubmission(submission, submissionSource, authorName, contestProblemAlias, contestProblemName, gradingLanguageName, contest.getName()));

        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestProgrammingSubmissionController.viewSubmissions(contest.getId())),
                new InternalLink(Messages.get("submission.view"), routes.ContestProgrammingSubmissionController.viewSubmission(contest.getId(), submission.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Submission - View");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewSubmissions(long contestId) throws ContestNotFoundException {
        return listSubmissions(contestId, 0, "id", "desc", null, null);
    }

    @Transactional(readOnly = true)
    public Result listSubmissions(long contestId, long pageIndex, String orderBy, String orderDir, String contestantJid, String problemJid) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!isAllowedToSuperviseSubmissions(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        String actualContestantJid = "(none)".equals(contestantJid) ? null : contestantJid;
        String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

        Page<ProgrammingSubmission> pageOfProgrammingSubmissions = programmingSubmissionService.getPageOfProgrammingSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, actualContestantJid, actualProblemJid, contest.getJid());
        Map<String, String> problemJidToAliasMap = contestProblemService.getMappedJidToAliasInContest(contest.getJid());
        List<ContestContestant> contestants = contestContestantService.getContestantsInContest(contest.getJid());
        List<String> contestantJids = Lists.transform(contestants, c -> c.getUserJid());
        Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

        LazyHtml content = new LazyHtml(listSubmissionsView.render(contestId, pageOfProgrammingSubmissions, contestantJids, problemJidToAliasMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, actualContestantJid, actualProblemJid));
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestProgrammingSubmissionController.viewSubmissions(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - All Submissions");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional
    public Result regradeSubmission(long contestId, long submissionId, long pageIndex, String orderBy, String orderDir, String contestantJid, String problemJid) throws ContestNotFoundException, ProgrammingSubmissionNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !isAllowedToSuperviseSubmissions(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ProgrammingSubmission programmingSubmission = programmingSubmissionService.findProgrammingSubmissionById(submissionId);
        SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromPastSubmission(programmingSubmissionLocalFileSystemProvider, programmingSubmissionRemoteFileSystemProvider, programmingSubmission.getJid());
        programmingSubmissionService.regrade(programmingSubmission.getJid(), submissionSource, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(UrielActivityKeys.REGRADE.construct(CONTEST, contest.getJid(), contest.getName(), PROBLEM, programmingSubmission.getProblemJid(), JidCacheServiceImpl.getInstance().getDisplayName(programmingSubmission.getProblemJid()), SUBMISSION, programmingSubmission.getJid(), programmingSubmission.getId() + ""));

        return redirect(routes.ContestProgrammingSubmissionController.listSubmissions(contestId, pageIndex, orderBy, orderDir, contestantJid, problemJid));
    }

    @Transactional
    public Result regradeSubmissions(long contestId, long pageIndex, String orderBy, String orderDir, String contestantJid, String problemJid) throws ContestNotFoundException, ProgrammingSubmissionNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !isAllowedToSuperviseSubmissions(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ListTableSelectionForm data = Form.form(ListTableSelectionForm.class).bindFromRequest().get();

        List<ProgrammingSubmission> programmingSubmissions;

        if (data.selectAll) {
            programmingSubmissions = programmingSubmissionService.getProgrammingSubmissionsByFilters(orderBy, orderDir, contestantJid, problemJid, contest.getJid());
        } else if (data.selectJids != null) {
            programmingSubmissions = programmingSubmissionService.getProgrammingSubmissionsByJids(data.selectJids);
        } else {
            return redirect(routes.ContestProgrammingSubmissionController.listSubmissions(contestId, pageIndex, orderBy, orderDir, contestantJid, problemJid));
        }

        for (ProgrammingSubmission programmingSubmission : programmingSubmissions) {
            SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromPastSubmission(programmingSubmissionLocalFileSystemProvider, programmingSubmissionRemoteFileSystemProvider, programmingSubmission.getJid());
            programmingSubmissionService.regrade(programmingSubmission.getJid(), submissionSource, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            UrielControllerUtils.getInstance().addActivityLog(UrielActivityKeys.REGRADE.construct(CONTEST, contest.getJid(), contest.getName(), PROBLEM, programmingSubmission.getProblemJid(), JidCacheServiceImpl.getInstance().getDisplayName(programmingSubmission.getProblemJid()), SUBMISSION, programmingSubmission.getJid(), programmingSubmission.getId() + ""));
        }

        return redirect(routes.ContestProgrammingSubmissionController.listSubmissions(contestId, pageIndex, orderBy, orderDir, contestantJid, problemJid));
    }

    private void appendSubtabsLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestProgrammingSubmissionController.viewScreenedSubmissions(contest.getId()), routes.ContestProgrammingSubmissionController.viewSubmissions(contest.getId()), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("submission.submissions"), org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToSubmissions(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseSubmissions(Contest contest) {
        return ContestControllerUtils.getInstance().isPermittedToSupervise(contest, ContestPermissions.SUBMISSION, IdentityUtils.getUserJid());
    }

    private boolean isAllowedToViewSubmission(Contest contest, ProgrammingSubmission submission) {
        return submission.getContainerJid().equals(contest.getJid()) && (UrielControllerUtils.getInstance().isAdmin() || isAllowedToSuperviseSubmissions(contest) || submission.getAuthorJid().equals(IdentityUtils.getUserJid()));
    }
}
