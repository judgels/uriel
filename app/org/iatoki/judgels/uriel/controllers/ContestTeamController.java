package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.accessTypesLayout;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.commons.views.html.layouts.messageView;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.ContestTeamCoach;
import org.iatoki.judgels.uriel.ContestTeamCoachCreateForm;
import org.iatoki.judgels.uriel.ContestTeamCoachNotFoundException;
import org.iatoki.judgels.uriel.ContestTeamCoachUploadForm;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.ContestTeamMemberCreateForm;
import org.iatoki.judgels.uriel.ContestTeamMemberNotFoundException;
import org.iatoki.judgels.uriel.ContestTeamMemberUploadForm;
import org.iatoki.judgels.uriel.ContestTeamNotFoundException;
import org.iatoki.judgels.uriel.ContestTeamUpsertForm;
import org.iatoki.judgels.uriel.UploadResult;
import org.iatoki.judgels.uriel.UserService;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public class ContestTeamController extends BaseController {

    private static final long PAGE_SIZE = 1000;

    private final Jophiel jophiel;
    private final ContestService contestService;
    private final UserService userService;

    public ContestTeamController(Jophiel jophiel, ContestService contestService, UserService userService) {
        this.jophiel = jophiel;
        this.contestService = contestService;
        this.userService = userService;
    }

    public Result startTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);

        if (contestTeam.getContestJid().equals(contest.getJid()) && ContestControllerUtils.getInstance().isAllowedToStartContestAsCoach(contest, contestTeam)) {
            contestService.startTeamAsCoach(contest.getJid(), contestTeam.getJid());
            return redirect(routes.ContestTeamController.viewScreenedTeams(contest.getId()));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result viewTeams(long contestId) throws ContestNotFoundException {
        return listCreateTeams(contestId, 0, "id", "asc", "");
    }

    public Result viewScreenedTeams(long contestId) throws ContestNotFoundException {
        return listScreenedTeams(contestId, 0, "id", "asc");
    }

    public Result listScreenedTeams(long contestId, long pageIndex, String orderBy, String orderDir) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (contestService.isUserCoachInAnyTeamByContestJid(contest.getJid(), IdentityUtils.getUserJid())) {
            Page<ContestTeam> contestTeams = contestService.pageContestTeamsByContestJidAndCoachJid(contest.getJid(), IdentityUtils.getUserJid(), pageIndex, PAGE_SIZE, orderBy, orderDir);

            return showListScreenedTeams(contestTeams, contest, pageIndex, orderBy, orderDir, ContestControllerUtils.getInstance().isAllowedToStartAnyContestAsCoach(contest));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result listCreateTeams(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest)) {
            Page<ContestTeam> contestTeams = contestService.pageContestTeamsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            boolean canUpdate = isAllowedToSuperviseContestants(contest);

            Form<ContestTeamUpsertForm> form = Form.form(ContestTeamUpsertForm.class);

            return showListCreateTeam(contestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateTeam(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseContestants(contest)) {
            Form<ContestTeamUpsertForm> form = Form.form(ContestTeamUpsertForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                Page<ContestTeam> contestTeams = contestService.pageContestTeamsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

                boolean canUpdate = isAllowedToSuperviseContestants(contest);

                return showListCreateTeam(contestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
            } else {
                ContestTeamUpsertForm contestTeamUpsertForm = form.get();

                Http.MultipartFormData body = request().body().asMultipartFormData();
                Http.MultipartFormData.FilePart teamImage = body.getFile("teamImage");

                if (teamImage != null) {
                    try {
                        contestService.createContestTeam(contest.getId(), contestTeamUpsertForm.name, teamImage.getFile(), FilenameUtils.getExtension(teamImage.getFilename()));
                    } catch (IOException e) {
                        Page<ContestTeam> contestTeams = contestService.pageContestTeamsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
                        boolean canUpdate = isAllowedToSuperviseContestants(contest);
                        form.reject("team.avatar.error.cantUpdate");

                        return showListCreateTeam(contestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, form, contest);
                    }
                } else {
                    contestService.createContestTeam(contest.getId(), contestTeamUpsertForm.name);
                }

                ControllerUtils.getInstance().addActivityLog("Create team " + contestTeamUpsertForm.name + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestTeamController.viewTeams(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result updateTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            ContestTeamUpsertForm contestTeamUpsertForm = new ContestTeamUpsertForm(contestTeam);
            Form<ContestTeamUpsertForm> form = Form.form(ContestTeamUpsertForm.class).fill(contestTeamUpsertForm);

            ControllerUtils.getInstance().addActivityLog("Try to update team " + contestTeam.getName() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showUpdateTeam(form, contest, contestTeam);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUpdateTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
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
                        contestService.updateContestTeam(contestTeam.getId(), contestTeamUpsertForm.name, teamImage.getFile(), FilenameUtils.getExtension(teamImage.getFilename()));
                    } catch (IOException e) {
                        form.reject("team.avatar.error.cantUpdate");

                        return showUpdateTeam(form, contest, contestTeam);
                    }
                } else {
                    contestService.updateContestTeam(contestTeam.getId(), contestTeamUpsertForm.name);
                }

                ControllerUtils.getInstance().addActivityLog("Update team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

                return redirect(routes.ContestTeamController.viewTeams(contest.getId()));
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @AddCSRFToken
    public Result viewTeam(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        if (ContestControllerUtils.getInstance().isSupervisorOrAbove(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class);
            Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
            Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class);
            Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

            ControllerUtils.getInstance().addActivityLog("View team " + contestTeam.getName() + " in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), isAllowedToSuperviseContestants(contest));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateTeamCoach(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class).bindFromRequest();

            if (form.hasErrors() || form.hasGlobalErrors()) {
                Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class);
                Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);
                return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
            } else {
                ContestTeamCoachCreateForm contestTeamCoachCreateForm = form.get();

                String userJid = jophiel.verifyUsername(contestTeamCoachCreateForm.username);
                if (userJid != null) {
                    if (!contestService.isUserCoachByUserJidAndTeamJid(userJid, contestTeam.getJid())) {
                        if (!contestService.isContestContestantInContestByUserJid(contest.getJid(), userJid)) {
                            contestService.createContestTeamCoach(contestTeam.getJid(), userJid);
                            userService.upsertUserFromJophielUserJid(userJid);

                            ControllerUtils.getInstance().addActivityLog("Add " + contestTeamCoachCreateForm.username + " as coach on team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

                            return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
                        } else {
                            form.reject("error.team.userAlreadyAContestant");
                            Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                            Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class);
                            Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                            return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                        }
                    } else {
                        form.reject("error.team.userAlreadyACoach");
                        Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                        Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class);
                        Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                        return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                    }
                } else {
                    form.reject("error.team.userNotFound");
                    Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                    Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class);
                    Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                    return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                }
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUploadTeamCoach(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
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
                        String userJid = jophiel.verifyUsername(username);
                        if (userJid != null) {
                            if (!contestService.isUserCoachByUserJidAndTeamJid(userJid, contestTeam.getJid())) {
                                if (!contestService.isContestContestantInContestByUserJid(contest.getJid(), userJid)) {
                                    userService.upsertUserFromJophielUserJid(userJid);
                                    contestService.createContestTeamCoach(contestTeam.getJid(), userJid);
                                } else {
                                    failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userAlreadyAContestant")));
                                }
                            } else {
                                failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userAlreadyACoach")));
                            }
                        } else {
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

    public Result removeTeamCoach(long contestId, long contestTeamId, long contestTeamCoachId) throws ContestNotFoundException, ContestTeamNotFoundException, ContestTeamCoachNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        ContestTeamCoach contestTeamCoach = contestService.findContestTeamCoachByContestTeamCoachId(contestTeamCoachId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid()) && contestTeamCoach.getTeamJid().equals(contestTeam.getJid())) {
            contestService.removeContestTeamCoachByContestTeamCoachId(contestTeamCoach.getId());

            ControllerUtils.getInstance().addActivityLog("Remove " + contestTeamCoach.getCoachJid() + " from coach on team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

            return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postCreateTeamMember(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid())) {
            Form<ContestTeamMemberCreateForm> form3 = Form.form(ContestTeamMemberCreateForm.class).bindFromRequest();

            if (form3.hasErrors() || form3.hasGlobalErrors()) {
                Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class);
                Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
            } else {
                ContestTeamMemberCreateForm contestTeamMemberCreateForm = form3.get();

                String userJid = jophiel.verifyUsername(contestTeamMemberCreateForm.username);
                if (userJid != null) {
                    if (!contestService.isUserInAnyTeamByContestJid(contest.getJid(), userJid)) {
                        if (!contestService.isContestContestantInContestByUserJid(contest.getJid(), userJid)) {
                            contestService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.APPROVED);
                        }
                        contestService.createContestTeamMember(contestTeam.getJid(), userJid);

                        ControllerUtils.getInstance().addActivityLog("Add " + contestTeamMemberCreateForm.username + " as member on team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

                        return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
                    } else {
                        form3.reject("error.team.userAlreadyHasTeam");
                        Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class);
                        Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                        Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                        return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                    }
                } else {
                    form3.reject("error.team.userNotFound");
                    Form<ContestTeamCoachCreateForm> form = Form.form(ContestTeamCoachCreateForm.class);
                    Form<ContestTeamCoachUploadForm> form2 = Form.form(ContestTeamCoachUploadForm.class);
                    Form<ContestTeamMemberUploadForm> form4 = Form.form(ContestTeamMemberUploadForm.class);

                    return showViewTeam(form, form2, form3, form4, contest, contestTeam, contestService.findContestTeamCoachesByTeamJid(contestTeam.getJid()), contestService.findContestTeamMembersByTeamJid(contestTeam.getJid()), true);
                }
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @RequireCSRFCheck
    public Result postUploadTeamMember(long contestId, long contestTeamId) throws ContestNotFoundException, ContestTeamNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
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
                        String userJid = jophiel.verifyUsername(username);

                        if (userJid != null) {
                            if (!contestService.isUserInAnyTeamByContestJid(contest.getJid(), userJid)) {
                                if (!contestService.isContestContestantInContestByUserJid(contest.getJid(), userJid)) {
                                    contestService.createContestContestant(contest.getId(), userJid, ContestContestantStatus.APPROVED);
                                }
                                contestService.createContestTeamMember(contestTeam.getJid(), userJid);
                            } else {
                                failedUploadsBuilder.add(new UploadResult(username, Messages.get("error.team.userAlreadyHasTeam")));
                            }
                        } else {
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

    public Result removeTeamMember(long contestId, long contestTeamId, long contestTeamMemberId) throws ContestNotFoundException, ContestTeamNotFoundException, ContestTeamMemberNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestTeam contestTeam = contestService.findContestTeamByContestTeamId(contestTeamId);
        ContestTeamMember contestTeamMember = contestService.findContestTeamMemberByContestTeamMemberId(contestTeamMemberId);
        if (isAllowedToSuperviseContestants(contest) && contestTeam.getContestJid().equals(contest.getJid()) && contestTeamMember.getTeamJid().equals(contestTeam.getJid())) {
            contestService.removeContestTeamMemberByContestTeamMemberId(contestTeamMember.getId());

            ControllerUtils.getInstance().addActivityLog("Remove " + contestTeamMember.getMemberJid() + " from member on team " + contestTeam.getName() + " in contest " + contest.getName() + ".");

            return redirect(routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    private Result showListCreateTeam(Page<ContestTeam> contestTeams, long pageIndex, String orderBy, String orderDir, String filterString, boolean canUpdate, Form<ContestTeamUpsertForm> form, Contest contest){
        LazyHtml content = new LazyHtml(listCreateTeamsView.render(contest.getId(), contestTeams, pageIndex, orderBy, orderDir, filterString, canUpdate, form, ContestControllerUtils.getInstance().hasContestBegun(contest)));
        content.appendLayout(c -> heading3Layout.render(Messages.get("team.list"), c));
        appendSubtabsLayout(content, contest);
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
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
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
        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
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

        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
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

        content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())), new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId()))), c));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
              new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId())),
              new InternalLink(Messages.get("team.view"), routes.ContestTeamController.viewTeam(contest.getId(), contestTeam.getId()))
        );

        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Teams - Upload Member Result");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendSubtabsLayout(LazyHtml content, Contest contest) {
        ImmutableList.Builder<InternalLink> internalLinks = ImmutableList.builder();
        internalLinks.add(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())));
        internalLinks.add(new InternalLink(Messages.get("team.teams"), routes.ContestTeamController.viewTeams(contest.getId())));

        if (contest.requiresPassword()) {
            internalLinks.add(new InternalLink(Messages.get("contestant.passwords"), routes.ContestContestantController.viewContestantPasswords(contest.getId())));
        }

        content.appendLayout(c -> accessTypesLayout.render(internalLinks.build(), c));
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
        return ControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest) || (ContestControllerUtils.getInstance().isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isContestant());
    }
}
