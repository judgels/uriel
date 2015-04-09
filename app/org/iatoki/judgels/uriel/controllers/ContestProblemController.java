package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.commons.views.html.layouts.heading3WithActionLayout;
import org.iatoki.judgels.gabriel.GradingSource;
import org.iatoki.judgels.gabriel.commons.SubmissionAdapters;
import org.iatoki.judgels.gabriel.commons.SubmissionException;
import org.iatoki.judgels.gabriel.commons.SubmissionService;
import org.iatoki.judgels.sandalphon.commons.SandalphonUtils;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.ContestProblemCreateForm;
import org.iatoki.judgels.uriel.ContestProblemStatus;
import org.iatoki.judgels.uriel.ContestProblemUpdateForm;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestStyleConfigICPC;
import org.iatoki.judgels.uriel.ContestStyleConfigIOI;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.JidCacheService;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.problem.createProblemView;
import org.iatoki.judgels.uriel.views.html.contest.problem.listOpenedProblemsView;
import org.iatoki.judgels.uriel.views.html.contest.problem.listProblemsView;
import org.iatoki.judgels.uriel.views.html.contest.problem.updateProblemView;
import org.iatoki.judgels.uriel.views.html.contest.problem.viewProblemView;
import org.iatoki.judgels.uriel.views.html.layouts.accessTypeByStatusLayout;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.net.URI;
import java.util.Date;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public class ContestProblemController extends Controller {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;
    private final SubmissionService submissionService;

    public ContestProblemController(ContestService contestService, SubmissionService submissionService) {
        this.contestService = contestService;
        this.submissionService = submissionService;
    }

    public Result viewOpenedProblems(long contestId) {
        return listOpenedProblems(contestId, 0, "alias", "asc", "");
    }

    public Result listOpenedProblems(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            Page<ContestProblem> contestProblems = contestService.pageContestProblemsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString, ContestProblemStatus.OPEN.name());
            ImmutableList.Builder<ContestProblem> replacementBuilder = ImmutableList.builder();
            for (ContestProblem contestProblem : contestProblems.getData()) {
                contestProblem.setTotalSubmissions(submissionService.countSubmissionsByContestJidByUser(contest.getJid(), contestProblem.getProblemJid(), IdentityUtils.getUserJid()));
                replacementBuilder.add(contestProblem);
            }
            contestProblems = new Page<>(replacementBuilder.build(), contestProblems.getTotalRowsCount(), contestProblems.getPageIndex(), contestProblems.getPageSize());

            LazyHtml content = new LazyHtml(listOpenedProblemsView.render(contest.getId(), contestProblems, pageIndex, orderBy, orderDir, filterString));
            content.appendLayout(c -> heading3Layout.render(Messages.get("problem.problems"), c));
            if (isAllowedToSuperviseProblems(contest)) {
                content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestProblemController.viewOpenedProblems(contest.getId()), routes.ContestProblemController.viewProblems(contest.getId()), c));
            }
            appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                  new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                  new InternalLink(Messages.get("problem.problems"), routes.ContestProblemController.viewOpenedProblems(contest.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problems");

            ControllerUtils.getInstance().addActivityLog("Open list of opened problems in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result viewProblem(long contestId, long contestProblemId) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);
        if (isAllowedToEnterContest(contest) && isAllowedToViewProblem(contest, contestProblem)) {
            long submissionLeft = -1;
            if (contestProblem.getSubmissionsLimit() != 0) {
                submissionLeft = contestProblem.getSubmissionsLimit() - submissionService.countSubmissionsByContestJidByUser(contest.getJid(), contestProblem.getProblemJid(), IdentityUtils.getUserJid());
            }

            int tOTPCode = SandalphonUtils.calculateTOTPCode(contestProblem.getProblemSecret(), System.currentTimeMillis());
            String requestUrl = SandalphonUtils.getTOTPEndpoint(contestProblem.getProblemJid(), tOTPCode, ContestControllerUtils.getInstance().getCurrentStatementLanguage(), routes.ContestProblemController.postSubmitProblem(contestId, contestProblem.getProblemJid()).absoluteURL(request()), routes.ContestProblemController.switchLanguage(contestId, contestProblemId).absoluteURL(request())).toString();
            String requestBody = "";

            ContestConfiguration config = contestService.findContestConfigurationByContestJid(contest.getJid());
            String styleConfig = config.getStyleConfig();

            if (contest.isICPC()) {
                requestBody = new Gson().toJson(new Gson().fromJson(styleConfig, ContestStyleConfigICPC.class).getLanguageRestriction());
            } else if (contest.isIOI()) {
                requestBody = new Gson().toJson(new Gson().fromJson(styleConfig, ContestStyleConfigIOI.class).getLanguageRestriction());
            }

            LazyHtml content = new LazyHtml(viewProblemView.render(requestUrl, requestBody, submissionLeft));
            appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                  new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                  new InternalLink(Messages.get("problem.problems"), routes.ContestProblemController.viewOpenedProblems(contest.getId())),
                  new InternalLink(contestProblem.getAlias(), routes.ContestProblemController.viewProblem(contest.getId(), contestProblem.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problem - View");

            ControllerUtils.getInstance().addActivityLog("View problem " + contestProblem.getAlias() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result renderImage(long contestId, long contestProblemId, String imageFilename) {
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);

        URI imageUri = SandalphonUtils.getRenderImageUri(contestProblem.getProblemJid(), imageFilename);

        return redirect(imageUri.toString());
    }

    public Result postSubmitProblem(long contestId, String problemJid) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestJidAndContestProblemJid(contest.getJid(), problemJid);

        if (isAllowedToDoContest(contest) && contestProblem.getContestJid().equals(contest.getJid())) {

            if ((contestProblem.getSubmissionsLimit() == 0) || (submissionService.countSubmissionsByContestJidByUser(contest.getJid(), contestProblem.getProblemJid(), IdentityUtils.getUserJid()) < contestProblem.getSubmissionsLimit())) {
                Http.MultipartFormData body = request().body().asMultipartFormData();

                String gradingLanguage = body.asFormUrlEncoded().get("language")[0];
                String gradingEngine = body.asFormUrlEncoded().get("engine")[0];

                try {
                    GradingSource source = SubmissionAdapters.fromGradingEngine(gradingEngine).createGradingSourceFromNewSubmission(body);
                    String submissionJid = submissionService.submit(problemJid, contest.getJid(), gradingEngine, gradingLanguage, ImmutableSet.of(), source);
                    SubmissionAdapters.fromGradingEngine(gradingEngine).storeSubmissionFiles(UrielProperties.getInstance().getSubmissionDir(), submissionJid, source);

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
            return tryEnteringContest(contest);
        }
    }

    public Result switchLanguage(long contestId, long contestProblemId) {
        String languageCode = DynamicForm.form().bindFromRequest().get("langCode");
        ContestControllerUtils.getInstance().setCurrentStatementLanguage(languageCode);

        return redirect(routes.ContestProblemController.viewProblem(contestId, contestProblemId));
    }

    public Result viewProblems(long contestId) {
        return listProblems(contestId, 0, "alias", "asc", "");
    }

    public Result listProblems(long contestId, long page, String sortBy, String orderBy, String filterString) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseProblems(contest)) {
            Page<ContestProblem> contestProblemPage = contestService.pageContestProblemsByContestJid(contest.getJid(), page, PAGE_SIZE, sortBy, orderBy, filterString, null);

            LazyHtml content = new LazyHtml(listProblemsView.render(contest.getId(), contestProblemPage, page, sortBy, orderBy, filterString));

            content.appendLayout(c -> heading3WithActionLayout.render(Messages.get("problem.list"), new InternalLink(Messages.get("commons.create"), routes.ContestProblemController.createProblem(contestId)), c));
            content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestProblemController.viewOpenedProblems(contest.getId()), routes.ContestProblemController.viewProblems(contest.getId()), c));
            appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                  new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                  new InternalLink(Messages.get("problem.problem"), routes.ContestProblemController.viewOpenedProblems(contest.getId())),
                  new InternalLink(Messages.get("status.supervisor"), routes.ContestProblemController.viewProblems(contest.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problems");

            ControllerUtils.getInstance().addActivityLog("Open all problems in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result createProblem(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseProblems(contest)) {
            Form<ContestProblemCreateForm> form = Form.form(ContestProblemCreateForm.class);
            form = form.fill(new ContestProblemCreateForm(0));

            ControllerUtils.getInstance().addActivityLog("Try to add problem in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showCreateProblem(form, contest);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateProblem(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseProblems(contest)) {
            Form<ContestProblemCreateForm> form = Form.form(ContestProblemCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showCreateProblem(form, contest);
            } else {
                ContestProblemCreateForm contestProblemCreateForm = form.get();
                String problemName = SandalphonUtils.verifyProblemJid(contestProblemCreateForm.problemJid);
                if ((problemName != null) && (!contestService.isContestProblemInContestByProblemJidOrAlias(contest.getJid(), contestProblemCreateForm.problemJid, contestProblemCreateForm.alias))) {
                    contestService.createContestProblem(contest.getId(), contestProblemCreateForm.problemJid, contestProblemCreateForm.problemSecret, contestProblemCreateForm.alias, contestProblemCreateForm.submissionsLimit, ContestProblemStatus.valueOf(contestProblemCreateForm.status));
                    JidCacheService.getInstance().putDisplayName(contestProblemCreateForm.problemJid, problemName, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

                    ControllerUtils.getInstance().addActivityLog("Add problem " + contestProblemCreateForm.alias + " in contest " + contest.getName() + ".");

                    return redirect(routes.ContestProblemController.viewProblems(contest.getId()));
                } else {
                    form.reject("error.problem.create.problemJidOrAlias.invalid");
                    return showCreateProblem(form, contest);
                }
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateProblem(long contestId, long contestProblemId) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);
        if (isAllowedToSuperviseProblems(contest) && contestProblem.getContestJid().equals(contest.getJid())) {
            ContestProblemUpdateForm contestProblemUpdateForm = new ContestProblemUpdateForm(contestProblem);
            Form<ContestProblemUpdateForm> form = Form.form(ContestProblemUpdateForm.class).fill(contestProblemUpdateForm);

            ControllerUtils.getInstance().addActivityLog("Try to update problem " + contestProblem.getAlias() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateProblem(form, contest, contestProblem);
        } else {
            return tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateProblem(long contestId, long contestProblemId) {
        Contest contest = contestService.findContestById(contestId);
        ContestProblem contestProblem = contestService.findContestProblemByContestProblemId(contestProblemId);
        if (isAllowedToSuperviseProblems(contest) && contestProblem.getContestJid().equals(contest.getJid())) {
            Form<ContestProblemUpdateForm> form = Form.form(ContestProblemUpdateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateProblem(form, contest, contestProblem);
            } else {
                ContestProblemUpdateForm contestProblemUpdateForm = form.get();
                contestService.updateContestProblem(contestProblem.getId(), contestProblemUpdateForm.problemSecret, contestProblemUpdateForm.alias, contestProblemUpdateForm.submissionsLimit, ContestProblemStatus.valueOf(contestProblemUpdateForm.status));

                ControllerUtils.getInstance().addActivityLog("Update problem " + contestProblem.getAlias() + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestProblemController.viewProblems(contest.getId()));
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    private Result showCreateProblem(Form<ContestProblemCreateForm> form, Contest contest){
        LazyHtml content = new LazyHtml(createProblemView.render(contest.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("problem.create"), c));
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestProblemController.viewOpenedProblems(contest.getId()), routes.ContestProblemController.viewProblems(contest.getId()), c));
        appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                new InternalLink(Messages.get("problem.problem"), routes.ContestProblemController.viewProblems(contest.getId())),
                new InternalLink(Messages.get("problem.create"), routes.ContestProblemController.createProblem(contest.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problem - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateProblem(Form<ContestProblemUpdateForm> form, Contest contest, ContestProblem contestProblem){
        LazyHtml content = new LazyHtml(updateProblemView.render(contest.getId(), contestProblem.getId(), form));
        content.appendLayout(c -> heading3Layout.render(Messages.get("problem.update"), c));
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestProblemController.viewOpenedProblems(contest.getId()), routes.ContestProblemController.viewProblems(contest.getId()), c));
        appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                new InternalLink(Messages.get("problem.problem"), routes.ContestProblemController.viewOpenedProblems(contest.getId())),
                new InternalLink(Messages.get("problem.update"), routes.ContestProblemController.updateProblem(contest.getId(), contestProblem.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Problem - Update");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendTabsLayout(LazyHtml content, Contest contest) {
        Date contestEndTime = contest.getEndTime();
        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
            contestEndTime = new Date(contestContestant.getContestEnterTime() + contestTypeConfigVirtual.getContestDuration());
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, isSupervisorOrAbove(contest), contestEndTime);
    }

    private boolean isManager(Contest contest) {
        return contestService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isSupervisor(Contest contest) {
        return contestService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isCoach(Contest contest) {
        return contestService.isUserCoachInAnyTeamByContestJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isContestant(Contest contest) {
        return contestService.isContestContestantInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isContestStarted(Contest contest) {
        return (!new Date().before(contest.getStartTime()));
    }

    private boolean isContestEnded(Contest contest) {
        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);

            return (System.currentTimeMillis() > (contestContestant.getContestEnterTime() + contestTypeConfigVirtual.getContestDuration()));
        } else {
            return new Date().after(contest.getEndTime());
        }
    }

    private boolean isAllowedToEnterContest(Contest contest) {
        if (ControllerUtils.getInstance().isAdmin() || isManager(contest) || isSupervisor(contest)) {
            return true;
        }
        if (contest.isStandard()) {
            return ((isContestant(contest) && isContestStarted(contest)) || (isCoach(contest)));
        } else {
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
            if (contestTypeConfigVirtual.getStartTrigger().equals(ContestTypeConfigVirtualStartTrigger.CONTESTANT)) {
                return (isContestant(contest) && (isContestStarted(contest)));
            } else {
                return ((isContestStarted(contest)) && (isCoach(contest) || (isContestant(contest) && (contestService.isContestEntered(contest.getJid(), IdentityUtils.getUserJid())))));
            }
        }
    }

    private boolean isAllowedToDoContest(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest) || isSupervisor(contest) || (isContestant(contest) && (contestService.isContestEntered(contest.getJid(), IdentityUtils.getUserJid()))  && isContestStarted(contest) && !isContestEnded(contest));
    }

    private boolean isSupervisorOrAbove(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest) || (isSupervisor(contest));
    }

    private boolean isAllowedToSuperviseProblems(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest) || (isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isProblem());
    }

    private boolean isAllowedToViewProblem(Contest contest, ContestProblem contestProblem) {
        return contestProblem.getContestJid().equals(contest.getJid()) && (ControllerUtils.getInstance().isAdmin() || isAllowedToSuperviseProblems(contest) || contestProblem.getStatus() == ContestProblemStatus.OPEN);
    }

    private Result tryEnteringContest(Contest contest) {
        if (isAllowedToEnterContest(contest)) {
            return redirect(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()));
        } else {
            return redirect(routes.ContestController.viewContest(contest.getId()));
        }
    }
}