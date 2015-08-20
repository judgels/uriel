package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.messageView;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.ContestTeamCoach;
import org.iatoki.judgels.uriel.forms.ContestTeamCoachCreateForm;
import org.iatoki.judgels.uriel.ContestTeamCoachNotFoundException;
import org.iatoki.judgels.uriel.forms.ContestTeamCoachUploadForm;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.forms.ContestTeamMemberCreateForm;
import org.iatoki.judgels.uriel.ContestTeamMemberNotFoundException;
import org.iatoki.judgels.uriel.forms.ContestTeamMemberUploadForm;
import org.iatoki.judgels.uriel.ContestTeamNotFoundException;
import org.iatoki.judgels.uriel.forms.ContestTeamUpsertForm;
import org.iatoki.judgels.uriel.UploadResult;
import org.iatoki.judgels.uriel.services.ContestSupervisorService;
import org.iatoki.judgels.uriel.services.ContestTeamService;
import org.iatoki.judgels.uriel.services.UserService;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.team.listCreateTeamsView;
import org.iatoki.judgels.uriel.views.html.contest.team.listScreenedTeamsView;
import org.iatoki.judgels.uriel.views.html.contest.team.updateTeamView;
import org.iatoki.judgels.uriel.views.html.contest.team.viewTeamView;
import org.iatoki.judgels.uriel.views.html.uploadResultView;
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
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public class ContestTeamController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 1000;

    private final ContestContestantService contestContestantService;
    private final ContestService contestService;
    private final ContestSupervisorService contestSupervisorService;
    private final ContestTeamService contestTeamService;
    private final Jophiel jophiel;
    private final UserService userService;

    @Inject
    public ContestTeamController(ContestContestantService contestContestantService, ContestService contestService, ContestSupervisorService contestSupervisorService, ContestTeamService contestTeamService, Jophiel jophiel, UserService userService) {
        this.contestContestantService = contestContestantService;
        this.contestService = contestService;
        this.contestSupervisorService = contestSupervisorService;
        this.contestTeamService = contestTeamService;
        this.jophiel = jophiel;
        this.userService = userService;
    }

    @Transactional
    public Result startTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);

        if (!contestTeam.getContestJid().equals(contest.getJid()) || !ContestControllerUtils.getInstance().isAllowedToStartContestAsCoach(contest, contestTeam)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        contestTeamService.startTeamAsCoach(contest.getJid(), contestTeam.getJid());
        return redirect(routes.ContestTeamController.viewScreenedTeams(contest.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewTeams(long contestId) throws ContestNotFoundException {
        return listCreateTeams(contestId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listCreateTeams(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Page<ContestTeam> pageOfContestTeams = contestTeamService.getPageOfTeamsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        boolean canUpdate = isAllowedToSuperviseContestants(contest);
        Form<ContestTeamUpsertForm> contestTeamUpsertForm = Form.form(ContestTeamUpsertForm.class);

        return showListCreateTeam(pageOfContestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, contestTeamUpsertForm, contest);
    }

    @Transactional(readOnly = true)
    public Result viewScreenedTeams(long contestId) throws ContestNotFoundException {
        return listScreenedTeams(contestId, 0, "id", "asc");
    }

    @Transactional(readOnly = true)
    public Result listScreenedTeams(long contestId, long pageIndex, String orderBy, String orderDir) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contestTeamService.isUserACoachOfAnyTeamInContest(contest.getJid(), IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Page<ContestTeam> pageOfContestTeams = contestTeamService.getPageOfTeamsInContestByCoachJid(contest.getJid(), IdentityUtils.getUserJid(), pageIndex, PAGE_SIZE, orderBy, orderDir);

        return showListScreenedTeams(pageOfContestTeams, contest, pageIndex, orderBy, orderDir, ContestControllerUtils.getInstance().isAllowedToStartAnyContestAsCoach(contest));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateTeam(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseContestants(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Form<ContestTeamUpsertForm> contestTeamUpsertForm = Form.form(ContestTeamUpsertForm.class).bindFromRequest();

        if (formHasErrors(contestTeamUpsertForm)) {
            Page<ContestTeam> pageOfContestTeams = contestTeamService.getPageOfTeamsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = isAllowedToSuperviseContestants(contest);

            return showListCreateTeam(pageOfContestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, contestTeamUpsertForm, contest);
        }

        ContestTeamUpsertForm contestTeamUpsertData = contestTeamUpsertForm.get();

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart teamImage = body.getFile("teamImage");

        if (teamImage != null) {
            try {
                contestTeamService.createContestTeam(contest.getId(), contestTeamUpsertData.name, teamImage.getFile(), FilenameUtils.getExtension(teamImage.getFilename()));
            } catch (IOException e) {
                Page<ContestTeam> contestTeams = contestTeamService.getPageOfTeamsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
                boolean canUpdate = isAllowedToSuperviseContestants(contest);
                contestTeamUpsertForm.reject("team.avatar.error.cantUpdate");

                return showListCreateTeam(contestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, contestTeamUpsertForm, contest);
            }
        } else {
            contestTeamService.createContestTeam(contest.getId(), contestTeamUpsertData.name);
        }

        ControllerUtils.getInstance().addActivityLog("Create team " + contestTeamUpsertData.name + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestTeamController.viewTeams(contest.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);

        if (!isAllowedToSuperviseContestants(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        ContestTeamUpsertForm contestTeamUpsertData = new ContestTeamUpsertForm();
        contestTeamUpsertData.name = contestTeam.getName();
        Form<ContestTeamUpsertForm> contestTeamUpsertForm = Form.form(ContestTeamUpsertForm.class).fill(contestTeamUpsertData);

        ControllerUtils.getInstance().addActivityLog("Try to update team " + contestTeam.getName() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateTeam(contestTeamUpsertForm, contest, contestTeam);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);
        if (!isAllowedToSuperviseContestants(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Form<ContestTeamUpsertForm> contestTeamUpsertForm = Form.form(ContestTeamUpsertForm.class).bindFromRequest();

        if (formHasErrors(contestTeamUpsertForm)) {
            return showUpdateTeam(contestTeamUpsertForm, contest, contestTeam);
        }

        ContestTeamUpsertForm contestTeamUpsertData = contestTeamUpsertForm.get();
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart teamImage = body.getFile("teamImage");

        if (teamImage != null) {
            try {
                contestTeamService.updateContestTeam(contestTeam.getId(), contestTeamUpsertData.name, teamImage.getFile(), FilenameUtils.getExtension(teamImage.getFilename()));
            } catch (IOException e) {
                contestTeamUpsertForm.reject("team.avatar.error.cantUpdate");

                return showUpdateTeam(contestTeamUpsertForm, contest, contestTeam);
            }
        } else {
            contestTeamService.updateContestTeam(contestTeam.getId(), contestTeamUpsertData.name);
        }

        ControllerUtils.getInstance().addActivityLog("Update team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestTeamController.viewTeams(contest.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);
        if (!ContestControllerUtils.getInstance().isSupervisorOrAbove(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Form<ContestTeamCoachCreateForm> contestTeamCoachCreateForm = Form.form(ContestTeamCoachCreateForm.class);
        Form<ContestTeamCoachUploadForm> contestTeamCoachUploadForm = Form.form(ContestTeamCoachUploadForm.class);
        Form<ContestTeamMemberCreateForm> contestTeamMemberCreateForm = Form.form(ContestTeamMemberCreateForm.class);
        Form<ContestTeamMemberUploadForm> contestTeamMemberUploadForm = Form.form(ContestTeamMemberUploadForm.class);

        ControllerUtils.getInstance().addActivityLog("View team " + contestTeam.getName() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showViewTeam(contestTeamCoachCreateForm, contestTeamCoachUploadForm, contestTeamMemberCreateForm, contestTeamMemberUploadForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateTeamCoach(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);

        if (!isAllowedToSuperviseContestants(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Form<ContestTeamCoachCreateForm> contestTeamCoachCreateForm = Form.form(ContestTeamCoachCreateForm.class).bindFromRequest();

        if (formHasErrors(contestTeamCoachCreateForm)) {
            return showViewTeamWithCoachCreateForm(contestTeamCoachCreateForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
        }

        ContestTeamCoachCreateForm contestTeamCoachCreateData = contestTeamCoachCreateForm.get();

        String userJid;
        try {
            userJid = jophiel.verifyUsername(contestTeamCoachCreateData.username);
        } catch (IOException e) {
            userJid = null;
        }

        if (userJid == null) {
            contestTeamCoachCreateForm.reject("error.team.userNotFound");

            return showViewTeamWithCoachCreateForm(contestTeamCoachCreateForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
        }

        if (contestTeamService.isUserACoachInTeam(userJid, contestTeam.getJid())) {
            contestTeamCoachCreateForm.reject("error.team.userAlreadyACoach");

            return showViewTeamWithCoachCreateForm(contestTeamCoachCreateForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
        }

        if (contestContestantService.isContestantInContest(contest.getJid(), userJid)) {
            contestTeamCoachCreateForm.reject("error.team.userAlreadyAContestant");

            return showViewTeamWithCoachCreateForm(contestTeamCoachCreateForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
        }

        contestTeamService.createContestTeamCoach(contestTeam.getJid(), userJid);
        userService.upsertUserFromJophielUserJid(userJid);

        ControllerUtils.getInstance().addActivityLog("Add " + contestTeamCoachCreateData.username + " as coach on team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadTeamCoach(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);

        if (!isAllowedToSuperviseContestants(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        ImmutableList.Builder<UploadResult> failedUploadsBuilder = ImmutableList.builder();
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        file = body.getFile("usernames");
        if (file != null) {
            File userFile = file.getFile();
            String[] usernames;
            try {
                usernames = FileUtils.readFileToString(userFile).split("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (String username : usernames) {
                String userJid;
                try {
                    userJid = jophiel.verifyUsername(username);
                } catch (IOException e) {
                    userJid = null;
                }

                if (userJid == null) {
                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userNotFound")));
                }

                if (contestTeamService.isUserACoachInTeam(userJid, contestTeam.getJid())) {
                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userAlreadyACoach")));
                }

                if (contestContestantService.isContestantInContest(contest.getJid(), userJid)) {
                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userAlreadyAContestant")));
                }

                userService.upsertUserFromJophielUserJid(userJid);
                contestTeamService.createContestTeamCoach(contestTeam.getJid(), userJid);
            }
            ControllerUtils.getInstance().addActivityLog("Upload contest team coaches in contest " + contest.getName() + ".");
        }
        List<UploadResult> failedUploads = failedUploadsBuilder.build();

        return showUploadTeamCoach(failedUploads, contest, contestTeam);
    }

    @Transactional
    public Result removeTeamCoach(long contestId, long contestTeamId, long contestTeamCoachId) throws ContestNotFoundException, ContestTeamNotFoundException, ContestTeamCoachNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);
        ContestTeamCoach contestTeamCoach = contestTeamService.findContestTeamCoachById(contestTeamCoachId);
        if (!isAllowedToSuperviseContestants(contest) || !contestTeam.getContestJid().equals(contest.getJid()) || !contestTeamCoach.getTeamJid().equals(contestTeam.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        contestTeamService.removeContestTeamCoachById(contestTeamCoach.getId());

        ControllerUtils.getInstance().addActivityLog("Remove " + contestTeamCoach.getCoachJid() + " from coach on team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateTeamMember(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);

        if (!isAllowedToSuperviseContestants(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        Form<ContestTeamMemberCreateForm> contestTeamMemberCreateForm = Form.form(ContestTeamMemberCreateForm.class).bindFromRequest();

        if (formHasErrors(contestTeamMemberCreateForm)) {
            return showViewTeamWithMemberCreateForm(contestTeamMemberCreateForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
        }

        ContestTeamMemberCreateForm contestTeamMemberCreateData = contestTeamMemberCreateForm.get();

        String userJid;
        try {
            userJid = jophiel.verifyUsername(contestTeamMemberCreateData.username);
        } catch (IOException e) {
            userJid = null;
        }

        if (userJid == null) {
            contestTeamMemberCreateForm.reject("error.team.userNotFound");

            return showViewTeamWithMemberCreateForm(contestTeamMemberCreateForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
        }

        if (contestTeamService.isUserPartOfAnyTeamInContest(contest.getJid(), userJid)) {
            contestTeamMemberCreateForm.reject("error.team.userAlreadyHasTeam");

            return showViewTeamWithMemberCreateForm(contestTeamMemberCreateForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
        }

        if (!contestContestantService.isContestantInContest(contest.getJid(), userJid)) {
            contestContestantService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.APPROVED);
        }
        contestTeamService.createContestTeamMember(contestTeam.getJid(), userJid);

        ControllerUtils.getInstance().addActivityLog("Add " + contestTeamMemberCreateData.username + " as member on team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

        return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadTeamMember(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);
        if (!isAllowedToSuperviseContestants(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }

        ImmutableList.Builder<UploadResult> failedUploadsBuilder = ImmutableList.builder();
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        file = body.getFile("usernames");
        if (file != null) {
            File userFile = file.getFile();
            String[] usernames;
            try {
                usernames = FileUtils.readFileToString(userFile).split("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (String username : usernames) {
                String userJid;
                try {
                    userJid = jophiel.verifyUsername(username);
                } catch (IOException e) {
                    userJid = null;
                }

                if (userJid == null) {
                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userNotFound")));
                }

                if (contestTeamService.isUserPartOfAnyTeamInContest(contest.getJid(), userJid)) {
                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userAlreadyHasTeam")));
                }

                if (!contestContestantService.isContestantInContest(contest.getJid(), userJid)) {
                    contestContestantService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.APPROVED);
                }

                contestTeamService.createContestTeamMember(contestTeam.getJid(), userJid);
            }

            ControllerUtils.getInstance().addActivityLog("Upload contest team members in contest " + contest.getName() + ".");
        }
        List<UploadResult> failedUploads = failedUploadsBuilder.build();

        return showUploadTeamMember(failedUploads, contest, contestTeam);
    }

    @Transactional
    public Result removeTeamMember(long contestId, long contestTeamId, long contestTeamMemberId) throws ContestNotFoundException, ContestTeamNotFoundException, ContestTeamMemberNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);
        ContestTeamMember contestTeamMember = contestTeamService.findContestTeamMemberById(contestTeamMemberId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid()) && contestTeamMember.getTeamJid().equals(contestTeam.getJid())) {
            contestTeamService.removeContestTeamMemberById(contestTeamMember.getId());

            ControllerUtils.getInstance().addActivityLog("Remove " + contestTeamMember.getMemberJid() + " from member on team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

            return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    private Result showListCreateTeam(Page<ContestTeam> pageOfContestTeams, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, Form<ContestTeamUpsertForm> contestTeamUpsertForm, Contest contest) {
        LazyHtml content = new LazyHtml(listCreateTeamsView.render(contest.getId(), pageOfContestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, contestTeamUpsertForm, ContestControllerUtils.getInstance().hasContestBegun(contest)));
        content.appendLayout(c -> heading3Layout.render(Messages.get("team.list"), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("team.list"), routes.ContestTeamController.viewTeams(contest.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Teams");

        ControllerUtils.getInstance().addActivityLog("List all teams in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showListScreenedTeams(Page<ContestTeam> pageOfContestTeams, Contest contest, long pageIndex, String orderBy, String orderDir, boolean isAllowedToStartContest) {
        LazyHtml content = new LazyHtml(listScreenedTeamsView.render(contest.getId(), pageOfContestTeams, pageIndex, orderBy, orderDir, isAllowedToStartContest));
        content.appendLayout(c -> heading3Layout.render(Messages.get("team.list"), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("team.list"), routes.ContestTeamController.viewTeams(contest.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Teams");

        ControllerUtils.getInstance().addActivityLog("List all screened teams in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateTeam(Form<ContestTeamUpsertForm> contestTeamUpsertForm, Contest contest, ContestTeam contestTeam) {
        LazyHtml content = new LazyHtml(updateTeamView.render(contest.getId(), contestTeam.getId(), contestTeamUpsertForm));
        content.appendLayout(c -> heading3Layout.render(Messages.get("team.update"), c));
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId())),
                new InternalLink(Messages.get("team.update"), routes.ContestTeamController.updateTeam(contest.getId(), contestTeam.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Team - Update");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showViewTeamWithCoachCreateForm(Form<ContestTeamCoachCreateForm> contestTeamCoachCreateForm, Contest contest, ContestTeam contestTeam, List<ContestTeamCoach> contestTeamCoaches, List<ContestTeamMember> contestTeamMembers, boolean canUpdate) {
        Form<ContestTeamCoachUploadForm> contestTeamCoachUploadForm = Form.form(ContestTeamCoachUploadForm.class);
        Form<ContestTeamMemberCreateForm> contestTeamMemberCreateForm = Form.form(ContestTeamMemberCreateForm.class);
        Form<ContestTeamMemberUploadForm> contestTeamMemberUploadForm = Form.form(ContestTeamMemberUploadForm.class);

        return showViewTeam(contestTeamCoachCreateForm, contestTeamCoachUploadForm, contestTeamMemberCreateForm, contestTeamMemberUploadForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
    }

    private Result showViewTeamWithMemberCreateForm(Form<ContestTeamMemberCreateForm> contestTeamMemberCreateForm, Contest contest, ContestTeam contestTeam, List<ContestTeamCoach> contestTeamCoaches, List<ContestTeamMember> contestTeamMembers, boolean canUpdate) {
        Form<ContestTeamCoachCreateForm> contestTeamCoachCreateForm = Form.form(ContestTeamCoachCreateForm.class);
        Form<ContestTeamCoachUploadForm> contestTeamCoachUploadForm = Form.form(ContestTeamCoachUploadForm.class);
        Form<ContestTeamMemberUploadForm> contestTeamMemberUploadForm = Form.form(ContestTeamMemberUploadForm.class);

        return showViewTeam(contestTeamCoachCreateForm, contestTeamCoachUploadForm, contestTeamMemberCreateForm, contestTeamMemberUploadForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
    }

    private Result showViewTeam(Form<ContestTeamCoachCreateForm> contestTeamCoachCreateForm, Form<ContestTeamCoachUploadForm> contestTeamCoachUploadForm, Form<ContestTeamMemberCreateForm> contestTeamMemberCreateForm, Form<ContestTeamMemberUploadForm> contestTeamMemberUploadForm, Contest contest, ContestTeam contestTeam, List<ContestTeamCoach> contestTeamCoaches, List<ContestTeamMember> contestTeamMembers, boolean canUpdate) {
        LazyHtml content = new LazyHtml(viewTeamView.render(contest.getId(), contestTeam, contestTeamCoachCreateForm, contestTeamCoachUploadForm, contestTeamMemberCreateForm, contestTeamMemberUploadForm, contestTeamCoaches, contestTeamMembers, canUpdate, jophiel.getAutoCompleteEndPoint()));
        content.appendLayout(c -> heading3Layout.render(Messages.get("team.view"), c));
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
              new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId())),
              new InternalLink(Messages.get("team.view"), routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Team - View");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUploadTeamCoach(List<UploadResult> failedUploads, Contest contest, ContestTeam contestTeam) {
        LazyHtml content;
        if (failedUploads.size() > 0) {
            content = new LazyHtml(uploadResultView.render(failedUploads));
        } else {
            content = new LazyHtml(messageView.render(Messages.get("contest.team.coach.upload.success")));
        }
        content.appendLayout(c -> heading3Layout.render(Messages.get("contest.team.coach.upload.result"), c));

        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
              new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId())),
              new InternalLink(Messages.get("team.view"), routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Teams - Upload Coach Result");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUploadTeamMember(List<UploadResult> failedUploads, Contest contest, ContestTeam contestTeam) {
        LazyHtml content;
        if (failedUploads.size() > 0) {
            content = new LazyHtml(uploadResultView.render(failedUploads));
        } else {
            content = new LazyHtml(messageView.render(Messages.get("contest.team.member.upload.success")));
        }
        content.appendLayout(c -> heading3Layout.render(Messages.get("contest.team.member.upload.result"), c));

        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
              new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId())),
              new InternalLink(Messages.get("team.view"), routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Teams - Upload Member Result");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.jumpToContestants(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseContestants(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest) || (ContestControllerUtils.getInstance().isSupervisor(contest) && contestSupervisorService.findContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid()).getContestPermission().isAllowed(ContestPermissions.CONTESTANT));
    }
}
