package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.ListTableSelectionForm;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.gabriel.GradingLanguageRegistry;
import org.iatoki.judgels.gabriel.GradingSource;
import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.gabriel.commons.SubmissionAdapters;
import org.iatoki.judgels.gabriel.commons.SubmissionException;
import org.iatoki.judgels.gabriel.commons.SubmissionNotFoundException;
import org.iatoki.judgels.gabriel.commons.SubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.ContestProblemStatus;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.JidCacheService;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.submission.listScreenedSubmissionsView;
import org.iatoki.judgels.uriel.views.html.contest.submission.listSubmissionsView;
import org.iatoki.judgels.uriel.views.html.layouts.accessTypeByStatusLayout;
import play.data.Form;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class ContestSubmissionController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;
    private final SubmissionService submissionService;
    private final FileSystemProvider submissionFileProvider;

    public ContestSubmissionController(ContestService contestService, SubmissionService submissionService, FileSystemProvider submissionFileProvider) {
        this.contestService = contestService;
        this.submissionService = submissionService;
        this.submissionFileProvider = submissionFileProvider;
    }

    public Result postSubmitProblem(long contestId, String problemJid) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestJidAndContestProblemJid(contest.getJid(), problemJid);

        if (ContestControllerUtils.getInstance().isAllowedToDoContest(contest) && contestProblem.getContestJid().equals(contest.getJid()) && !ContestControllerUtils.getInstance().isCoach(contest) && contestProblem.getStatus() != ContestProblemStatus.UNUSED) {

            if (contestProblem.getStatus() == ContestProblemStatus.CLOSED) {
                return redirect(routes.ContestProblemController.viewProblem(contestId, contestProblem.getId()));
            } else if ((contestProblem.getSubmissionsLimit() == 0) || (submissionService.countSubmissionsByContestJidByUser(contest.getJid(), contestProblem.getProblemJid(), IdentityUtils.getUserJid()) < contestProblem.getSubmissionsLimit())) {
                Http.MultipartFormData body = request().body().asMultipartFormData();

                String gradingLanguage = body.asFormUrlEncoded().get("language")[0];
                String gradingEngine = body.asFormUrlEncoded().get("engine")[0];

                try {
                    GradingSource source = SubmissionAdapters.fromGradingEngine(gradingEngine).createGradingSourceFromNewSubmission(body);
                    String submissionJid = submissionService.submit(problemJid, contest.getJid(), gradingEngine, gradingLanguage, null, source, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
                    SubmissionAdapters.fromGradingEngine(gradingEngine).storeSubmissionFiles(submissionFileProvider, submissionJid, source);

                    ControllerUtils.getInstance().addActivityLog("Submit to problem " + contestProblem.getAlias() + " in contest " + contest.getName() + ".");

                } catch (SubmissionException e) {
                    flash("submissionError", e.getMessage());

                    return redirect(routes.ContestProblemController.viewProblem(contestId, contestProblem.getId()));
                }
            } else {
                flash("submissionError", "submission.limit.reached");
                return redirect(routes.ContestProblemController.viewProblem(contestId, contestProblem.getId()));
            }

            return redirect(routes.ContestSubmissionController.viewScreenedSubmissions(contestId));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    public Result viewScreenedSubmissions(long contestId) throws ContestNotFoundException {
        return listScreenedSubmissions(contestId, 0, "id", "desc", null);
    }

    public Result listScreenedSubmissions(long contestId, long pageIndex, String orderBy, String orderDir, String problemJid) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (ContestControllerUtils.getInstance().isAllowedToEnterContest(contest) && !ContestControllerUtils.getInstance().isCoach(contest)) {
            String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

            Page<Submission> submissions = submissionService.pageSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, IdentityUtils.getUserJid(), actualProblemJid, contest.getJid());
            Map<String, String> problemJidToAliasMap = contestService.findProblemJidToAliasMapByContestJid(contest.getJid());
            Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

            LazyHtml content = new LazyHtml(listScreenedSubmissionsView.render(contestId, submissions, problemJidToAliasMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, actualProblemJid));
            content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
            if (isAllowedToSuperviseSubmissions(contest)) {
                appendSubtabsLayout(content, contest);
            }
            ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, contest,
                    new InternalLink(Messages.get("submission.list"), routes.ContestSubmissionController.viewScreenedSubmissions(contest.getId()))
            );
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Submissions");

            ControllerUtils.getInstance().addActivityLog("List own submissions in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    public Result viewSubmission(long contestId, long submissionId) throws ContestNotFoundException, SubmissionNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        Submission submission = submissionService.findSubmissionById(submissionId);

        if (ContestControllerUtils.getInstance().isAllowedToEnterContest(contest) && isAllowedToViewSubmission(contest, submission)) {
            GradingSource source = SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).createGradingSourceFromPastSubmission(submissionFileProvider, submission.getJid());
            String authorName = JidCacheService.getInstance().getDisplayName(submission.getAuthorJid());
            ContestProblem contestProblem = contestService.findContestProblemByContestJidAndContestProblemJid(contest.getJid(), submission.getProblemJid());
            String contestProblemAlias = contestProblem.getAlias();
            String contestProblemName = JidCacheService.getInstance().getDisplayName(contestProblem.getProblemJid());
            String gradingLanguageName = GradingLanguageRegistry.getInstance().getLanguage(submission.getGradingLanguage()).getName();

            LazyHtml content = new LazyHtml(SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).renderViewSubmission(submission, source, authorName, contestProblemAlias, contestProblemName, gradingLanguageName, contest.getName()));

            ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, contest,
                    new InternalLink(Messages.get("status.supervisor"), routes.ContestSubmissionController.viewSubmissions(contest.getId())),
                    new InternalLink(Messages.get("submission.view"), routes.ContestSubmissionController.viewSubmission(contest.getId(), submission.getId()))
            );

            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Submission - View");

            ControllerUtils.getInstance().addActivityLog("View submission " + submission.getId() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    public Result viewSubmissions(long contestId) throws ContestNotFoundException {
        return listSubmissions(contestId, 0, "id", "desc", null, null);
    }

    public Result listSubmissions(long contestId, long pageIndex, String orderBy, String orderDir, String contestantJid, String problemJid) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (isAllowedToSuperviseSubmissions(contest)) {

            String actualContestantJid = "(none)".equals(contestantJid) ? null : contestantJid;
            String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

            Page<Submission> submissions = submissionService.pageSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, actualContestantJid, actualProblemJid, contest.getJid());
            Map<String, String> problemJidToAliasMap = contestService.findProblemJidToAliasMapByContestJid(contest.getJid());
            List<ContestContestant> contestants = contestService.findAllContestContestantsByContestJid(contest.getJid());
            List<String> contestantJids = Lists.transform(contestants, c -> c.getUserJid());
            Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

            LazyHtml content = new LazyHtml(listSubmissionsView.render(contestId, submissions, contestantJids, problemJidToAliasMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, actualContestantJid, actualProblemJid));
            content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
            appendSubtabsLayout(content, contest);
            ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            appendBreadcrumbsLayout(content, contest,
                    new InternalLink(Messages.get("status.supervisor"), routes.ContestSubmissionController.viewSubmissions(contest.getId()))
            );

            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - All Submissions");

            ControllerUtils.getInstance().addActivityLog("List all submissions in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    public Result regradeSubmission(long contestId, long submissionId, long pageIndex, String orderBy, String orderDir, String contestantJid, String problemJid) throws ContestNotFoundException, SubmissionNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseSubmissions(contest)) {

            Submission submission = submissionService.findSubmissionById(submissionId);
            GradingSource source = SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).createGradingSourceFromPastSubmission(submissionFileProvider, submission.getJid());
            submissionService.regrade(submission.getJid(), source, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            ControllerUtils.getInstance().addActivityLog("Regrade submission " + submission.getId() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return redirect(routes.ContestSubmissionController.listSubmissions(contestId, pageIndex, orderBy, orderDir, contestantJid, problemJid));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    public Result regradeSubmissions(long contestId, long pageIndex, String orderBy, String orderDir, String contestantJid, String problemJid) throws ContestNotFoundException, SubmissionNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseSubmissions(contest)) {
            ListTableSelectionForm data = Form.form(ListTableSelectionForm.class).bindFromRequest().get();

            List<Submission> submissions;

            if (data.selectAll) {
                submissions = submissionService.findSubmissionsWithoutGradingsByFilters(orderBy, orderDir, contestantJid, problemJid, contest.getJid());
            } else if (data.selectJids != null) {
                submissions = submissionService.findSubmissionsWithoutGradingsByJids(data.selectJids);
            } else {
                return redirect(routes.ContestSubmissionController.listSubmissions(contestId, pageIndex, orderBy, orderDir, contestantJid, problemJid));
            }

            for (Submission submission : submissions) {
                GradingSource source = SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).createGradingSourceFromPastSubmission(submissionFileProvider, submission.getJid());
                submissionService.regrade(submission.getJid(), source, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            }

            ControllerUtils.getInstance().addActivityLog("Regrade some submissions in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return redirect(routes.ContestSubmissionController.listSubmissions(contestId, pageIndex, orderBy, orderDir, contestantJid, problemJid));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    private void appendSubtabsLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestSubmissionController.viewScreenedSubmissions(contest.getId()), routes.ContestSubmissionController.viewSubmissions(contest.getId()), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("submission.submissions"), routes.ContestController.jumpToSubmissions(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseSubmissions(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest) || (ContestControllerUtils.getInstance().isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isSubmission());
    }

    private boolean isAllowedToViewSubmission(Contest contest, Submission submission) {
        return submission.getContestJid().equals(contest.getJid()) && (ControllerUtils.getInstance().isAdmin() || isAllowedToSuperviseSubmissions(contest) || submission.getAuthorJid().equals(IdentityUtils.getUserJid()));
    }
}
