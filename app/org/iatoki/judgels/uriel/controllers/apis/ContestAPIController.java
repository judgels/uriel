package org.iatoki.judgels.uriel.controllers.apis;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.VirtualContestTypeConfig;
import org.iatoki.judgels.uriel.VirtualContestTypeConfigStartTrigger;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.services.ContestAnnouncementService;
import org.iatoki.judgels.uriel.services.ContestClarificationService;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestManagerService;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.ContestSupervisorService;
import org.iatoki.judgels.uriel.services.ContestTeamService;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named
public final class ContestAPIController extends Controller {

    private final ContestService contestService;
    private final ContestTeamService contestTeamService;
    private final ContestAnnouncementService contestAnnouncementService;
    private final ContestClarificationService contestClarificationService;
    private final ContestScoreboardService contestScoreboardService;
    private final ContestContestantService contestContestantService;
    private final ContestManagerService contestManagerService;
    private final ContestSupervisorService contestSupervisorService;

    @Inject
    public ContestAPIController(ContestService contestService, ContestTeamService contestTeamService, ContestAnnouncementService contestAnnouncementService, ContestClarificationService contestClarificationService, ContestScoreboardService contestScoreboardService, ContestContestantService contestContestantService, ContestManagerService contestManagerService, ContestSupervisorService contestSupervisorService) {
        this.contestService = contestService;
        this.contestTeamService = contestTeamService;
        this.contestAnnouncementService = contestAnnouncementService;
        this.contestClarificationService = contestClarificationService;
        this.contestScoreboardService = contestScoreboardService;
        this.contestContestantService = contestContestantService;
        this.contestManagerService = contestManagerService;
        this.contestSupervisorService = contestSupervisorService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result unreadAnnouncement(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            long unreadCount = contestAnnouncementService.getUnreadContestAnnouncementsCount(IdentityUtils.getUserJid(), contest.getJid());
            ObjectNode objectNode = Json.newObject();
            objectNode.put("success", true);
            objectNode.put("count", unreadCount);
            return ok(objectNode);
        } else {
            ObjectNode objectNode = Json.newObject();
            objectNode.put("success", false);
            return ok(objectNode);
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result unreadClarification(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            long unreadCount;
            if (isCoach(contest)) {
                List<ContestTeamMember> contestTeamMemberList = contestTeamService.findContestTeamMembersByContestJidAndCoachJid(contest.getJid(), IdentityUtils.getUserJid());
                unreadCount = contestClarificationService.getUnreadContestClarificationsCount(contestTeamMemberList.stream().map(ContestTeamMember::getMemberJid).collect(Collectors.toList()), IdentityUtils.getUserJid(), contest.getJid(), false);
            } else {
                unreadCount = contestClarificationService.getUnreadContestClarificationsCount(ImmutableList.of(IdentityUtils.getUserJid()), IdentityUtils.getUserJid(), contest.getJid(), true);
            }
            ObjectNode objectNode = Json.newObject();
            objectNode.put("success", true);
            objectNode.put("count", unreadCount);
            return ok(objectNode);
        } else {
            ObjectNode objectNode = Json.newObject();
            objectNode.put("success", false);
            return ok(objectNode);
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result unansweredClarification(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseClarifications(contest)) {
            long unreadCount = contestClarificationService.getUnansweredContestClarificationsCount(contest.getJid());
            ObjectNode objectNode = Json.newObject();
            objectNode.put("success", true);
            objectNode.put("count", unreadCount);
            return ok(objectNode);
        } else {
            ObjectNode objectNode = Json.newObject();
            objectNode.put("success", false);
            return ok(objectNode);
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result renderTeamAvatarImage(String imageName) {
        response().setHeader("Cache-Control", "no-transform,public,max-age=300,s-maxage=900");

        String imageURL = contestTeamService.getTeamAvatarImageURL(imageName);
        try {
            new URL(imageURL);
            return temporaryRedirect(imageURL);
        } catch (MalformedURLException e) {
            File image = new File(imageURL);
            if (image.exists()) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                response().setHeader("Last-Modified", sdf.format(new Date(image.lastModified())));

                if (request().hasHeader("If-Modified-Since")) {
                    try {
                        Date lastUpdate = sdf.parse(request().getHeader("If-Modified-Since"));
                        if (image.lastModified() > lastUpdate.getTime()) {
                            BufferedImage in = ImageIO.read(image);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();

                            String type = FilenameUtils.getExtension(image.getAbsolutePath());

                            ImageIO.write(in, type, baos);
                            return ok(baos.toByteArray()).as("image/" + type);
                        } else {
                            return status(304);
                        }
                    } catch (ParseException | IOException e2) {
                        throw new RuntimeException(e2);
                    }
                } else {
                    try {
                        BufferedImage in = ImageIO.read(image);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        String type = FilenameUtils.getExtension(image.getAbsolutePath());

                        ImageIO.write(in, type, baos);
                        return ok(baos.toByteArray()).as("image/" + type);
                    } catch (IOException e2) {
                        return internalServerError();
                    }
                }
            } else {
                return notFound();
            }
        }
    }

    @Transactional(readOnly = true)
    public Result getScoreboard() {
        DynamicForm form = DynamicForm.form().bindFromRequest();

        String secret = form.get("secret");
        String contestJid = form.get("contestJid");
        String type = form.get("type");

        if (secret == null || contestJid == null || type == null) {
            return notFound();
        }

        if (!UrielProperties.getInstance().getUrielScoreboardSecret().equals(secret)) {
            return notFound();
        }

        ContestScoreboardType contestScoreboardType;
        try {
            contestScoreboardType = ContestScoreboardType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return notFound();
        }

        ContestScoreboard contestScoreboard;

        if (contestScoreboardService.isContestScoreboardExistByContestJidAndScoreboardType(contestJid, contestScoreboardType)) {
            contestScoreboard = contestScoreboardService.findContestScoreboardByContestJidAndScoreboardType(contestJid, contestScoreboardType);
        } else {
            // Resort to the official one.
            contestScoreboard = contestScoreboardService.findContestScoreboardByContestJidAndScoreboardType(contestJid, ContestScoreboardType.OFFICIAL);
        }

        return ok(new Gson().toJson(contestScoreboard));
    }

    private boolean isAdmin() {
        return UrielUtils.hasRole("admin");
    }

    private boolean isManager(Contest contest) {
        return contestManagerService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isSupervisor(Contest contest) {
        return contestSupervisorService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isCoach(Contest contest) {
        return contestTeamService.isUserCoachInAnyTeamByContestJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isContestant(Contest contest) {
        return contestContestantService.isContestContestantInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isContestStarted(Contest contest) {
        return (!new Date().before(contest.getStartTime()));
    }

    private boolean isAllowedToEnterContest(Contest contest) {
        if (isAdmin() || isManager(contest) || isSupervisor(contest)) {
            return true;
        }
        if (contest.isStandard()) {
            return ((isContestant(contest) && isContestStarted(contest)) || (isCoach(contest)));
        } else {
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            VirtualContestTypeConfig virtualContestTypeConfig = new Gson().fromJson(contestConfiguration.getTypeConfig(), VirtualContestTypeConfig.class);
            if (virtualContestTypeConfig.getStartTrigger().equals(VirtualContestTypeConfigStartTrigger.CONTESTANT)) {
                return (isContestant(contest) && (isContestStarted(contest)));
            } else {
                return ((isContestStarted(contest)) && (isCoach(contest) || (isContestant(contest) && (contestContestantService.isContestStarted(contest.getJid(), IdentityUtils.getUserJid())))));
            }
        }
    }

    private boolean isAllowedToSuperviseClarifications(Contest contest) {
        return isAdmin() || isManager(contest) || (isSupervisor(contest) && contestSupervisorService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).getContestPermission().isAllowed(ContestPermissions.CLARIFICATION));
    }
}
