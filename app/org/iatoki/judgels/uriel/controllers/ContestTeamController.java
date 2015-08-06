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

    private final Jophiel jophiel;
    private final ContestService contestService;
    private final ContestTeamService contestTeamService;
    private final ContestContestantService contestContestantService;
    private final ContestSupervisorService contestSupervisorService;
    private final UserService userService;

    @Inject
    public ContestTeamController(Jophiel jophiel, ContestService contestService, ContestTeamService contestTeamService, ContestContestantService contestContestantService, ContestSupervisorService contestSupervisorService, UserService userService) {
        this.jophiel = jophiel;
        this.contestService = contestService;
        this.contestTeamService = contestTeamService;
        this.contestContestantService = contestContestantService;
        this.contestSupervisorService = contestSupervisorService;
        this.userService = userService;
    }

    @Transactional
    public Result startTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamByContestTeamId(contestTeamId);

        if (contestTeam.getContestJid().equals(contest.getJid()) && ContestControllerUtils.getInstance().isAllowedToStartContestAsCoach(contest, contestTeam)) {
            contestTeamService.startTeamAsCoach(contest.getJid(), contestTeam.getJid());
            return redirect(routes.ContestTeamController.viewScreenedTeams(contest.getId()));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
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
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            Page<ContestTeam> contestTeams = contestTeamService.pageContestTeamsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = isAllowedToSuperviseContestants(contest);

            Form<ContestTeamUpsertForm> form = Form.form(ContestTeamUpsertForm.class);

            return showListCreateTeam(contestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    public Result viewScreenedTeams(long contestId) throws ContestNotFoundException {
        return listScreenedTeams(contestId, 0, "id", "asc");
    }

    @Transactional(readOnly = true)
    public Result listScreenedTeams(long contestId, long pageIndex, String orderBy, String orderDir) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (contestTeamService.isUserCoachInAnyTeamByContestJid(contest.getJid(), IdentityUtils.getUserJid())) {
            Page<ContestTeam> contestTeams = contestTeamService.pageContestTeamsByContestJidAndCoachJid(contest.getJid(), IdentityUtils.getUserJid(), pageIndex, PAGE_SIZE, orderBy, orderDir);

            return showListScreenedTeams(contestTeams, contest, pageIndex, orderBy, orderDir, ContestControllerUtils.getInstance().isAllowedToStartAnyContestAsCoach(contest));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateTeam(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseContestants(contest)) {
            Form<ContestTeamUpsertForm> form = Form.form(ContestTeamUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                Page<ContestTeam> contestTeams = contestTeamService.pageContestTeamsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

                boolean canUpdate = isAllowedToSuperviseContestants(contest);

                return showListCreateTeam(contestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
            } else {
                ContestTeamUpsertForm contestTeamUpsertForm = form.get();

                Http.MultipartFormData body = request().body().asMultipartFormData();
                Http.MultipartFormData.FilePart teamImage = body.getFile("teamImage");

                if (teamImage != null) {
                    try {
                        contestTeamService.createContestTeam(contest.getId(), contestTeamUpsertForm.name, teamImage.getFile(), FilenameUtils.getExtension(teamImage.getFilename()));
                    } catch (IOException e) {
                        Page<ContestTeam> contestTeams = contestTeamService.pageContestTeamsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
                        boolean canUpdate = isAllowedToSuperviseContestants(contest);
                        form.reject("team.avatar.error.cantUpdate");

                        return showListCreateTeam(contestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
                    }
                } else {
                    contestTeamService.createContestTeam(contest.getId(), contestTeamUpsertForm.name);
                }

                ControllerUtils.getInstance().addActivityLog("Create team " + contestTeamUpsertForm.name + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestTeamController.viewTeams(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            ContestTeamUpsertForm contestTeamUpsertForm = new ContestTeamUpsertForm(contestTeam);
            Form<ContestTeamUpsertForm> form = Form.form(ContestTeamUpsertForm.class).fill(contestTeamUpsertForm);

            ControllerUtils.getInstance().addActivityLog("Try to update team " + contestTeam.getName() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateTeam(form, contest, contestTeam);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            Form<ContestTeamUpsertForm> form = Form.form(ContestTeamUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                return showUpdateTeam(form, contest, contestTeam);
            } else {
                ContestTeamUpsertForm contestTeamUpsertForm = form.get();
                Http.MultipartFormData body = request().body().asMultipartFormData();
                Http.MultipartFormData.FilePart teamImage = body.getFile("teamImage");

                if (teamImage != null) {
                    try {
                        contestTeamService.updateContestTeam(contestTeam.getId(), contestTeamUpsertForm.name, teamImage.getFile(), FilenameUtils.getExtension(teamImage.getFilename()));
                    } catch (IOException e) {
                        form.reject("team.avatar.error.cantUpdate");

                        return showUpdateTeam(form, contest, contestTeam);
                    }
                } else {
                    contestTeamService.updateContestTeam(contestTeam.getId(), contestTeamUpsertForm.name);
                }

                ControllerUtils.getInstance().addActivityLog("Update team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestTeamController.viewTeams(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamByContestTeamId(contestTeamId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class);
            Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
            Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class);
            Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

            ControllerUtils.getInstance().addActivityLog("View team " + contestTeam.getName() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestTeamService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestTeamService.findContestTeamMembersByTeamJid(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateTeamCoach(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class);
                Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);
                return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestTeamService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestTeamService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
            } else {
                ContestTeamCoachCreateForm contestTeamCoachCreateForm = form.get();

                try {
                    String userJid = jophiel.verifyUsername(contestTeamCoachCreateForm.username);
                    if (userJid != null) {
                        if (!contestTeamService.isUserCoachByUserJidAndTeamJid(userJid, contestTeam.getJid())) {
                            if (!contestContestantService.isContestContestantInContestByUserJid(contest.getJid(), userJid)) {
                                contestTeamService.createContestTeamCoach(contestTeam.getJid(), userJid);
                                userService.upsertUserFromJophielUserJid(userJid);

                                ControllerUtils.getInstance().addActivityLog("Add " + contestTeamCoachCreateForm.username + " as coach on team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

                                return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
                            } else {
                                form.reject("error.team.userAlreadyAContestant");
                                Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                                Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class);
                                Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                                return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestTeamService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestTeamService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                            }
                        } else {
                            form.reject("error.team.userAlreadyACoach");
                            Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                            Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class);
                            Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                            return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestTeamService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestTeamService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                        }
                    } else {
                        form.reject("error.team.userNotFound");
                        Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                        Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class);
                        Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                        return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestTeamService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestTeamService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                    }
                } catch (IOException e) {
                    form.reject("error.team.userNotFound");
                    Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                    Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class);
                    Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                    return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestTeamService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestTeamService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                }
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadTeamCoach(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            ImmutableList.Builder<UploadResult> failedUploadsBuilder = ImmutableList.builder();
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart file;

            file = body.getFile("usernames");
            if (file != null) {
                File userFile = file.getFile();
                try {
                    String[] usernames = FileUtils.readFileToString(userFile).split("\n");
                    for (String username : usernames) {
                        try {
                            String userJid = jophiel.verifyUsername(username);
                            if (userJid != null) {
                                if (!contestTeamService.isUserCoachByUserJidAndTeamJid(userJid, contestTeam.getJid())) {
                                    if (!contestContestantService.isContestContestantInContestByUserJid(contest.getJid(), userJid)) {
                                        userService.upsertUserFromJophielUserJid(userJid);
                                        contestTeamService.createContestTeamCoach(contestTeam.getJid(), userJid);
                                    } else {
                                        failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userAlreadyAContestant")));
                                    }
                                } else {
                                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userAlreadyACoach")));
                                }
                            } else {
                                failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userNotFound")));
                            }
                        } catch (IOException e) {
                            failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userNotFound")));
                        }
                    }

                    ControllerUtils.getInstance().addActivityLog("Upload contest team coaches in contest " + contest.getName() + ".");

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            List<UploadResult> failedUploads = failedUploadsBuilder.build();

            return showUploadTeamCoach(failedUploads, contest, contestTeam);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    public Result removeTeamCoach(long contestId, long contestTeamId, long contestTeamCoachId) throws ContestNotFoundException, ContestTeamNotFoundException, ContestTeamCoachNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamByContestTeamId(contestTeamId);
        ContestTeamCoach contestTeamCoach = contestTeamService.findContestTeamCoachByContestTeamCoachId(contestTeamCoachId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid()) && contestTeamCoach.getTeamJid().equals(contestTeam.getJid())) {
            contestTeamService.removeContestTeamCoachByContestTeamCoachId(contestTeamCoach.getId());

            ControllerUtils.getInstance().addActivityLog("Remove " + contestTeamCoach.getCoachJid() + " from coach on team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

            return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateTeamMember(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class).bindFromRequest();

            if (form3.hasErrors() || form3.hasGlobalErrors()) {
                Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class);
                Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestTeamService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestTeamService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
            } else {
                ContestTeamMemberCreateForm contestTeamMemberCreateForm = form3.get();

                try {
                    String userJid = jophiel.verifyUsername(contestTeamMemberCreateForm.username);
                    if (userJid != null) {
                        if (!contestTeamService.isUserInAnyTeamByContestJid(contest.getJid(), userJid)) {
                            if (!contestContestantService.isContestContestantInContestByUserJid(contest.getJid(), userJid)) {
                                contestContestantService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.APPROVED);
                            }
                            contestTeamService.createContestTeamMember(contestTeam.getJid(), userJid);

                            ControllerUtils.getInstance().addActivityLog("Add " + contestTeamMemberCreateForm.username + " as member on team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

                            return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
                        } else {
                            form3.reject("error.team.userAlreadyHasTeam");
                            Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class);
                            Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                            Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                            return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestTeamService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestTeamService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                        }
                    } else {
                        form3.reject("error.team.userNotFound");
                        Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class);
                        Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                        Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                        return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestTeamService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestTeamService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                    }
                } catch (IOException e) {
                    form3.reject("error.team.userNotFound");
                    Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class);
                    Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                    Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                    return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestTeamService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestTeamService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                }
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadTeamMember(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            ImmutableList.Builder<UploadResult> failedUploadsBuilder = ImmutableList.builder();
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart file;

            file = body.getFile("usernames");
            if (file != null) {
                File userFile = file.getFile();
                try {
                    String[] usernames = FileUtils.readFileToString(userFile).split("\n");
                    for (String username : usernames) {
                        try {
                            String userJid = jophiel.verifyUsername(username);

                            if (userJid != null) {
                                if (!contestTeamService.isUserInAnyTeamByContestJid(contest.getJid(), userJid)) {
                                    if (!contestContestantService.isContestContestantInContestByUserJid(contest.getJid(), userJid)) {
                                        contestContestantService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.APPROVED);
                                    }
                                    contestTeamService.createContestTeamMember(contestTeam.getJid(), userJid);
                                } else {
                                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userAlreadyHasTeam")));
                                }
                            } else {
                                failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userNotFound")));
                            }
                        } catch (IOException e) {
                            failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userNotFound")));
                        }
                    }

                    ControllerUtils.getInstance().addActivityLog("Upload contest team members in contest " + contest.getName() + ".");

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            List<UploadResult> failedUploads = failedUploadsBuilder.build();

            return showUploadTeamMember(failedUploads, contest, contestTeam);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    public Result removeTeamMember(long contestId, long contestTeamId, long contestTeamMemberId) throws ContestNotFoundException, ContestTeamNotFoundException, ContestTeamMemberNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestTeamService.findContestTeamByContestTeamId(contestTeamId);
        ContestTeamMember contestTeamMember = contestTeamService.findContestTeamMemberByContestTeamMemberId(contestTeamMemberId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid()) && contestTeamMember.getTeamJid().equals(contestTeam.getJid())) {
            contestTeamService.removeContestTeamMemberByContestTeamMemberId(contestTeamMember.getId());

            ControllerUtils.getInstance().addActivityLog("Remove " + contestTeamMember.getMemberJid() + " from member on team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

            return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    private Result showListCreateTeam(Page<ContestTeam> contestTeams, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, Form<ContestTeamUpsertForm> form, Contest contest){
        LazyHtml content = new LazyHtml(listCreateTeamsView.render(contest.getId(), contestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, form, ContestControllerUtils.getInstance().hasContestBegun(contest)));
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

    private Result showListScreenedTeams(Page<ContestTeam> contestTeams, Contest contest, long pageIndex, String orderBy, String orderDir, boolean isAllowedToStartContest){
        LazyHtml content = new LazyHtml(listScreenedTeamsView.render(contest.getId(), contestTeams, pageIndex, orderBy, orderDir, isAllowedToStartContest));
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

    private Result showUpdateTeam(Form<ContestTeamUpsertForm> form, Contest contest, ContestTeam contestTeam){
        LazyHtml content = new LazyHtml(updateTeamView.render(contest.getId(), contestTeam.getId(), form));
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

    private Result showViewTeam(Form<ContestTeamCoachCreateForm> form, Form<ContestTeamCoachUploadForm> form2, Form<ContestTeamMemberCreateForm> form3, Form<ContestTeamMemberUploadForm> form4, Contest contest, ContestTeam contestTeam, List<ContestTeamCoach> contestTeamCoaches, List<ContestTeamMember> contestTeamMembers, boolean canUpdate) {
        LazyHtml content = new LazyHtml(viewTeamView.render(contest.getId(), contestTeam, form, form2, form3, form4, contestTeamCoaches, contestTeamMembers, canUpdate, jophiel.getAutoCompleteEndPoint()));
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
        return ControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest) || (ContestControllerUtils.getInstance().isSupervisor(contest) && contestSupervisorService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).getContestPermission().isAllowed(ContestPermissions.CONTESTANT));
    }
}
