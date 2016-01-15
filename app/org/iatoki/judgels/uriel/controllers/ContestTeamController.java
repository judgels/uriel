package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.jophiel.BasicActivityKeys;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.play.views.html.layouts.messageView;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.ContestTeamCoach;
import org.iatoki.judgels.uriel.forms.ContestTeamCoachAddForm;
import org.iatoki.judgels.uriel.ContestTeamCoachNotFoundException;
import org.iatoki.judgels.uriel.forms.ContestTeamCoachUploadForm;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.forms.ContestTeamMemberAddForm;
import org.iatoki.judgels.uriel.ContestTeamMemberNotFoundException;
import org.iatoki.judgels.uriel.forms.ContestTeamMemberUploadForm;
import org.iatoki.judgels.uriel.ContestTeamNotFoundException;
import org.iatoki.judgels.uriel.forms.ContestTeamUpsertForm;
import org.iatoki.judgels.uriel.UploadResult;
import org.iatoki.judgels.uriel.services.ContestTeamService;
import org.iatoki.judgels.uriel.user.UserService;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.uriel.views.html.contest.team.listCreateTeamsView;
import org.iatoki.judgels.uriel.views.html.contest.team.listScreenedTeamsView;
import org.iatoki.judgels.uriel.views.html.contest.team.editTeamView;
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
    private static final String TEAM = "team";
    private static final String COACH = "coach";
    private static final String MEMBER = "member";
    private static final String CONTEST = "contest";

    private final ContestContestantService contestContestantService;
    private final ContestService contestService;
    private final ContestTeamService contestTeamService;
    private final JophielPublicAPI jophielPublicAPI;
    private final UserService userService;

    @Inject
    public ContestTeamController(ContestContestantService contestContestantService, ContestService contestService, ContestTeamService contestTeamService, JophielPublicAPI jophielPublicAPI, UserService userService) {
        this.contestContestantService = contestContestantService;
        this.contestService = contestService;
        this.contestTeamService = contestTeamService;
        this.jophielPublicAPI = jophielPublicAPI;
        this.userService = userService;
    }

    @Transactional
    public Result startTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);

        if (contest.isLocked() || !contest.containsModule(ContestModules.TEAM) || !contestTeam.getContestJid().equals(contest.getJid()) || !ContestControllerUtils.getInstance().isAllowedToStartContestForTeamAsCoach(contest, contestTeam, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        contestTeamService.startTeamAsCoach(contest.getJid(), contestTeam.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
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
        if (!contest.containsModule(ContestModules.TEAM) || !ContestControllerUtils.getInstance().isSupervisorOrAbove(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Page<ContestTeam> pageOfContestTeams = contestTeamService.getPageOfTeamsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        boolean canUpdate = !contest.isLocked() && isAllowedToSuperviseTeams(contest);
        Form<ContestTeamUpsertForm> contestTeamUpsertForm = Form.form(ContestTeamUpsertForm.class);

        return showlistCreateTeam(pageOfContestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, contestTeamUpsertForm, contest);
    }

    @Transactional(readOnly = true)
    public Result viewScreenedTeams(long contestId) throws ContestNotFoundException {
        return listScreenedTeams(contestId, 0, "id", "asc");
    }

    @Transactional(readOnly = true)
    public Result listScreenedTeams(long contestId, long pageIndex, String orderBy, String orderDir) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contest.containsModule(ContestModules.TEAM) || !contestTeamService.isUserACoachOfAnyTeamInContest(contest.getJid(), IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Page<ContestTeam> pageOfContestTeams = contestTeamService.getPageOfTeamsInContestByCoachJid(contest.getJid(), IdentityUtils.getUserJid(), pageIndex, PAGE_SIZE, orderBy, orderDir);

        return showListScreenedTeams(pageOfContestTeams, contest, pageIndex, orderBy, orderDir, ContestControllerUtils.getInstance().isAllowedToViewStartContestForTeamButtonInContest(contest, IdentityUtils.getUserJid()));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateTeam(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !contest.containsModule(ContestModules.TEAM) || !isAllowedToSuperviseTeams(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestTeamUpsertForm> contestTeamUpsertForm = Form.form(ContestTeamUpsertForm.class).bindFromRequest();

        if (formHasErrors(contestTeamUpsertForm)) {
            Page<ContestTeam> pageOfContestTeams = contestTeamService.getPageOfTeamsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            return showlistCreateTeam(pageOfContestTeams, pageIndex, orderBy, orderDir, filterString, true, contestTeamUpsertForm, contest);
        }

        ContestTeamUpsertForm contestTeamUpsertData = contestTeamUpsertForm.get();

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart teamImage = body.getFile("teamImage");

        ContestTeam contestTeam;
        if (teamImage != null) {
            try {
                contestTeam = contestTeamService.createContestTeam(contest.getJid(), contestTeamUpsertData.name, teamImage.getFile(), FilenameUtils.getExtension(teamImage.getFilename()), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            } catch (IOException e) {
                Page<ContestTeam> contestTeams = contestTeamService.getPageOfTeamsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
                boolean canUpdate = isAllowedToSuperviseTeams(contest);
                contestTeamUpsertForm.reject("team.avatar.error.cantUpdate");

                return showlistCreateTeam(contestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, contestTeamUpsertForm, contest);
            }
        } else {
            contestTeam = contestTeamService.createContestTeam(contest.getJid(), contestTeamUpsertData.name, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.ADD_IN.construct(CONTEST, contest.getJid(), contest.getName(), TEAM, contestTeam.getJid(), contestTeam.getName()));

        return redirect(routes.ContestTeamController.viewTeams(contest.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);

        if (contest.isLocked() || !contest.containsModule(ContestModules.TEAM) || !isAllowedToSuperviseTeams(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestTeamUpsertForm contestTeamUpsertData = new ContestTeamUpsertForm();
        contestTeamUpsertData.name = contestTeam.getName();
        Form<ContestTeamUpsertForm> contestTeamUpsertForm = Form.form(ContestTeamUpsertForm.class).fill(contestTeamUpsertData);

        return showEditTeam(contestTeamUpsertForm, contest, contestTeam);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);
        if (contest.isLocked() || !contest.containsModule(ContestModules.TEAM) || !isAllowedToSuperviseTeams(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestTeamUpsertForm> contestTeamUpsertForm = Form.form(ContestTeamUpsertForm.class).bindFromRequest();

        if (formHasErrors(contestTeamUpsertForm)) {
            return showEditTeam(contestTeamUpsertForm, contest, contestTeam);
        }

        ContestTeamUpsertForm contestTeamUpsertData = contestTeamUpsertForm.get();
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart teamImage = body.getFile("teamImage");

        if (teamImage != null) {
            try {
                contestTeamService.updateContestTeam(contestTeam.getJid(), contestTeamUpsertData.name, teamImage.getFile(), FilenameUtils.getExtension(teamImage.getFilename()), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            } catch (IOException e) {
                contestTeamUpsertForm.reject("team.avatar.error.cantUpdate");

                return showEditTeam(contestTeamUpsertForm, contest, contestTeam);
            }
        } else {
            contestTeamService.updateContestTeam(contestTeam.getJid(), contestTeamUpsertData.name, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        if (!contestTeam.getName().equals(contestTeamUpsertData.name)) {
            UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.RENAME_IN.construct(CONTEST, contest.getJid(), contest.getName(), TEAM, contestTeam.getJid(), contestTeam.getName(), contestTeamUpsertData.name));
        }
        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.ADD_IN.construct(CONTEST, contest.getJid(), contest.getName(), TEAM, contestTeam.getJid(), contestTeamUpsertData.name));

        return redirect(routes.ContestTeamController.viewTeams(contest.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);
        if (!contest.containsModule(ContestModules.TEAM) || !ContestControllerUtils.getInstance().isSupervisorOrAbove(contest, IdentityUtils.getUserJid()) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestTeamCoachAddForm> contestTeamCoachAddForm = Form.form(ContestTeamCoachAddForm.class);
        Form<ContestTeamCoachUploadForm> contestTeamCoachUploadForm = Form.form(ContestTeamCoachUploadForm.class);
        Form<ContestTeamMemberAddForm> contestTeamMemberAddForm = Form.form(ContestTeamMemberAddForm.class);
        Form<ContestTeamMemberUploadForm> contestTeamMemberUploadForm = Form.form(ContestTeamMemberUploadForm.class);

        return showViewTeam(contestTeamCoachAddForm, contestTeamCoachUploadForm, contestTeamMemberAddForm, contestTeamMemberUploadForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseTeams(contest));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postAddTeamCoach(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);

        if (contest.isLocked() || !contest.containsModule(ContestModules.TEAM) || !isAllowedToSuperviseTeams(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestTeamCoachAddForm> contestTeamCoachAddForm = Form.form(ContestTeamCoachAddForm.class).bindFromRequest();

        if (formHasErrors(contestTeamCoachAddForm)) {
            return showViewTeamWithCoachAddForm(contestTeamCoachAddForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseTeams(contest));
        }

        ContestTeamCoachAddForm contestTeamCoachCreateData = contestTeamCoachAddForm.get();

        JophielUser jophielUser;
        try {
            jophielUser = jophielPublicAPI.findUserByUsername(contestTeamCoachCreateData.username);
        } catch (JudgelsAPIClientException e) {
            jophielUser = null;
        }

        if (jophielUser == null) {
            contestTeamCoachAddForm.reject("error.team.userNotFound");

            return showViewTeamWithCoachAddForm(contestTeamCoachAddForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseTeams(contest));
        }

        if (contestTeamService.isUserACoachInTeam(jophielUser.getJid(), contestTeam.getJid())) {
            contestTeamCoachAddForm.reject("error.team.userAlreadyACoach");

            return showViewTeamWithCoachAddForm(contestTeamCoachAddForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseTeams(contest));
        }

        if (contestContestantService.isContestantInContest(contest.getJid(), jophielUser.getJid())) {
            contestTeamCoachAddForm.reject("error.team.userAlreadyAContestant");

            return showViewTeamWithCoachAddForm(contestTeamCoachAddForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseTeams(contest));
        }

        contestTeamService.createContestTeamCoach(contestTeam.getJid(), jophielUser.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        userService.upsertUserFromJophielUser(jophielUser, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.ADD_TO_IN.construct(CONTEST, contest.getJid(), contest.getName(), TEAM, contestTeam.getJid(), contestTeam.getName(), COACH, jophielUser.getJid(), jophielUser.getUsername()));

        return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadTeamCoach(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);

        if (contest.isLocked() || !contest.containsModule(ContestModules.TEAM) || !isAllowedToSuperviseTeams(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
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
                JophielUser jophielUser;
                try {
                    jophielUser = jophielPublicAPI.findUserByUsername(username);
                } catch (JudgelsAPIClientException e) {
                    jophielUser = null;
                }

                if (jophielUser == null) {
                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userNotFound")));
                }

                if (contestTeamService.isUserACoachInTeam(jophielUser.getJid(), contestTeam.getJid())) {
                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userAlreadyACoach")));
                }

                if (contestContestantService.isContestantInContest(contest.getJid(), jophielUser.getJid())) {
                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userAlreadyAContestant")));
                }

                userService.upsertUserFromJophielUser(jophielUser, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
                contestTeamService.createContestTeamCoach(contestTeam.getJid(), jophielUser.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            }
            UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.UPLOAD_TO_IN.construct(CONTEST, contest.getJid(), contest.getName(), TEAM, contestTeam.getJid(), contestTeam.getName(), COACH, null, userFile.getName()));
        }
        List<UploadResult> failedUploads = failedUploadsBuilder.build();

        return showUploadTeamCoach(failedUploads, contest, contestTeam);
    }

    @Transactional
    public Result removeTeamCoach(long contestId, long contestTeamId, long contestTeamCoachId) throws ContestNotFoundException, ContestTeamNotFoundException, ContestTeamCoachNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);
        ContestTeamCoach contestTeamCoach = contestTeamService.findContestTeamCoachById(contestTeamCoachId);
        if (contest.isLocked() || !contest.containsModule(ContestModules.TEAM) || !isAllowedToSuperviseTeams(contest) || !contestTeam.getContestJid().equals(contest.getJid()) || !contestTeamCoach.getTeamJid().equals(contestTeam.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        contestTeamService.removeContestTeamCoachById(contestTeamCoach.getId());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.REMOVE_FROM_IN.construct(CONTEST, contest.getJid(), contest.getName(), TEAM, contestTeam.getJid(), contestTeam.getName(), COACH, contestTeamCoach.getCoachJid(), JidCacheServiceImpl.getInstance().getDisplayName(contestTeamCoach.getCoachJid())));

        return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postAddTeamMember(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);

        if (contest.isLocked() || !contest.containsModule(ContestModules.TEAM) || !isAllowedToSuperviseTeams(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestTeamMemberAddForm> contestTeamMemberAddForm = Form.form(ContestTeamMemberAddForm.class).bindFromRequest();

        if (formHasErrors(contestTeamMemberAddForm)) {
            return showViewTeamWithMemberAddForm(contestTeamMemberAddForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseTeams(contest));
        }

        ContestTeamMemberAddForm contestTeamMemberAddData = contestTeamMemberAddForm.get();

        JophielUser jophielUser;
        try {
            jophielUser = jophielPublicAPI.findUserByUsername(contestTeamMemberAddData.username);
        } catch (JudgelsAPIClientException e) {
            jophielUser = null;
        }

        if (jophielUser == null) {
            contestTeamMemberAddForm.reject("error.team.userNotFound");

            return showViewTeamWithMemberAddForm(contestTeamMemberAddForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseTeams(contest));
        }

        if (contestTeamService.isUserPartOfAnyTeamInContest(contest.getJid(), jophielUser.getJid())) {
            contestTeamMemberAddForm.reject("error.team.userAlreadyHasTeam");

            return showViewTeamWithMemberAddForm(contestTeamMemberAddForm, contest, contestTeam, contestTeamService.getCoachesOfTeam(contestTeam.getJid()), contestTeamService.getMembersOfTeam(contestTeam.getJid()), isAllowedToSuperviseTeams(contest));
        }

        if (!contestContestantService.isContestantInContest(contest.getJid(), jophielUser.getJid())) {
            contestContestantService.createContestContestant(contest.getJid(), jophielUser.getJid(), ContestContestantStatus.APPROVED, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
        contestTeamService.createContestTeamMember(contestTeam.getJid(), jophielUser.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.ADD_TO_IN.construct(CONTEST, contest.getJid(), contest.getName(), TEAM, contestTeam.getJid(), contestTeam.getName(), MEMBER, jophielUser.getJid(), jophielUser.getUsername()));

        return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadTeamMember(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);
        if (contest.isLocked() || !contest.containsModule(ContestModules.TEAM) || !isAllowedToSuperviseTeams(contest) || !contestTeam.getContestJid().equals(contest.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
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
                JophielUser jophielUser;
                try {
                    jophielUser = jophielPublicAPI.findUserByUsername(username);
                } catch (JudgelsAPIClientException e) {
                    jophielUser = null;
                }

                if (jophielUser == null) {
                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userNotFound")));
                }

                if (contestTeamService.isUserPartOfAnyTeamInContest(contest.getJid(), jophielUser.getJid())) {
                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userAlreadyHasTeam")));
                }

                if (!contestContestantService.isContestantInContest(contest.getJid(), jophielUser.getJid())) {
                    contestContestantService.createContestContestant(contest.getJid(), jophielUser.getJid(), ContestContestantStatus.APPROVED, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
                }

                contestTeamService.createContestTeamMember(contestTeam.getJid(), jophielUser.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            }

            UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.UPLOAD_TO_IN.construct(CONTEST, contest.getJid(), contest.getName(), TEAM, contestTeam.getJid(), contestTeam.getName(), MEMBER, null, userFile.getName()));
        }
        List<UploadResult> failedUploads = failedUploadsBuilder.build();

        return showUploadTeamMember(failedUploads, contest, contestTeam);
    }

    @Transactional
    public Result removeTeamMember(long contestId, long contestTeamId, long contestTeamMemberId) throws ContestNotFoundException, ContestTeamNotFoundException, ContestTeamMemberNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamById(contestTeamId);
        ContestTeamMember contestTeamMember = contestTeamService.findContestTeamMemberById(contestTeamMemberId);
        if (contest.isLocked() || !contest.containsModule(ContestModules.TEAM) || !isAllowedToSuperviseTeams(contest) || !contestTeam.getContestJid().equals(contest.getJid()) || !contestTeamMember.getTeamJid().equals(contestTeam.getJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        contestTeamService.removeContestTeamMemberById(contestTeamMember.getId());

        UrielControllerUtils.getInstance().addActivityLog(BasicActivityKeys.REMOVE_FROM_IN.construct(CONTEST, contest.getJid(), contest.getName(), TEAM, contestTeam.getJid(), contestTeam.getName(), MEMBER, contestTeamMember.getMemberJid(), JidCacheServiceImpl.getInstance().getDisplayName(contestTeamMember.getMemberJid())));

        return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
    }

    private Result showlistCreateTeam(Page<ContestTeam> pageOfContestTeams, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, Form<ContestTeamUpsertForm> contestTeamUpsertForm, Contest contest) {
        LazyHtml content = new LazyHtml(listCreateTeamsView.render(contest.getId(), pageOfContestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, contestTeamUpsertForm, ContestControllerUtils.getInstance().hasContestBegun(contest)));
        content.appendLayout(c -> heading3Layout.render(Messages.get("team.list"), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("team.list"), routes.ContestTeamController.viewTeams(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Teams");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showListScreenedTeams(Page<ContestTeam> pageOfContestTeams, Contest contest, long pageIndex, String orderBy, String orderDir, boolean isAllowedToStartContest) {
        LazyHtml content = new LazyHtml(listScreenedTeamsView.render(contest.getId(), pageOfContestTeams, pageIndex, orderBy, orderDir, isAllowedToStartContest));
        content.appendLayout(c -> heading3Layout.render(Messages.get("team.list"), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("team.list"), routes.ContestTeamController.viewTeams(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Teams");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditTeam(Form<ContestTeamUpsertForm> contestTeamUpsertForm, Contest contest, ContestTeam contestTeam) {
        LazyHtml content = new LazyHtml(editTeamView.render(contest.getId(), contestTeam.getId(), contestTeamUpsertForm));
        content.appendLayout(c -> heading3Layout.render(Messages.get("team.update"), c));
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId())),
                new InternalLink(Messages.get("team.update"), routes.ContestTeamController.editTeam(contest.getId(), contestTeam.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Team - Update");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showViewTeamWithCoachAddForm(Form<ContestTeamCoachAddForm> contestTeamCoachAddForm, Contest contest, ContestTeam contestTeam, List<ContestTeamCoach> contestTeamCoaches, List<ContestTeamMember> contestTeamMembers, boolean canUpdate) {
        Form<ContestTeamCoachUploadForm> contestTeamCoachUploadForm = Form.form(ContestTeamCoachUploadForm.class);
        Form<ContestTeamMemberAddForm> contestTeamMemberAddForm = Form.form(ContestTeamMemberAddForm.class);
        Form<ContestTeamMemberUploadForm> contestTeamMemberUploadForm = Form.form(ContestTeamMemberUploadForm.class);

        return showViewTeam(contestTeamCoachAddForm, contestTeamCoachUploadForm, contestTeamMemberAddForm, contestTeamMemberUploadForm, contest, contestTeam, contestTeamCoaches, contestTeamMembers, canUpdate);
    }

    private Result showViewTeamWithMemberAddForm(Form<ContestTeamMemberAddForm> contestTeamMemberAddForm, Contest contest, ContestTeam contestTeam, List<ContestTeamCoach> contestTeamCoaches, List<ContestTeamMember> contestTeamMembers, boolean canUpdate) {
        Form<ContestTeamCoachAddForm> contestTeamCoachAddForm = Form.form(ContestTeamCoachAddForm.class);
        Form<ContestTeamCoachUploadForm> contestTeamCoachUploadForm = Form.form(ContestTeamCoachUploadForm.class);
        Form<ContestTeamMemberUploadForm> contestTeamMemberUploadForm = Form.form(ContestTeamMemberUploadForm.class);

        return showViewTeam(contestTeamCoachAddForm, contestTeamCoachUploadForm, contestTeamMemberAddForm, contestTeamMemberUploadForm, contest, contestTeam, contestTeamCoaches, contestTeamMembers, canUpdate);
    }

    private Result showViewTeam(Form<ContestTeamCoachAddForm> contestTeamCoachAddForm, Form<ContestTeamCoachUploadForm> contestTeamCoachUploadForm, Form<ContestTeamMemberAddForm> contestTeamMemberAddForm, Form<ContestTeamMemberUploadForm> contestTeamMemberUploadForm, Contest contest, ContestTeam contestTeam, List<ContestTeamCoach> contestTeamCoaches, List<ContestTeamMember> contestTeamMembers, boolean canUpdate) {
        LazyHtml content = new LazyHtml(viewTeamView.render(contest.getId(), contestTeam, contestTeamCoachAddForm, contestTeamCoachUploadForm, contestTeamMemberAddForm, contestTeamMemberUploadForm, contestTeamCoaches, contestTeamMembers, canUpdate, jophielPublicAPI.getUserAutocompleteAPIEndpoint()));
        content.appendLayout(c -> heading3Layout.render(Messages.get("team.view"), c));
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
              new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId())),
              new InternalLink(Messages.get("team.view"), routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Team - View");

        return UrielControllerUtils.getInstance().lazyOk(content);
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
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
              new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId())),
              new InternalLink(Messages.get("team.view"), routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Teams - Upload Coach Result");

        return UrielControllerUtils.getInstance().lazyOk(content);
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
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
              new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId())),
              new InternalLink(Messages.get("team.view"), routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Teams - Upload Member Result");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.jumpToContestants(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseTeams(Contest contest) {
        return ContestControllerUtils.getInstance().isPermittedToSupervise(contest, ContestPermissions.TEAM, IdentityUtils.getUserJid());
    }
}
