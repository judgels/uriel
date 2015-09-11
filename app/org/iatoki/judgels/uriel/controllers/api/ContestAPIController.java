package org.iatoki.judgels.uriel.controllers.api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.modules.contest.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.modules.contest.trigger.ContestTrigger;
import org.iatoki.judgels.uriel.modules.contest.trigger.ContestTriggerModule;
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
public final class ContestAPIController extends AbstractJudgelsAPIController {

    private final ContestAnnouncementService contestAnnouncementService;
    private final ContestClarificationService contestClarificationService;
    private final ContestContestantService contestContestantService;
    private final ContestManagerService contestManagerService;
    private final ContestScoreboardService contestScoreboardService;
    private final ContestService contestService;
    private final ContestSupervisorService contestSupervisorService;
    private final ContestTeamService contestTeamService;

    @Inject
    public ContestAPIController(ContestAnnouncementService contestAnnouncementService, ContestClarificationService contestClarificationService, ContestContestantService contestContestantService, ContestManagerService contestManagerService, ContestScoreboardService contestScoreboardService, ContestService contestService, ContestSupervisorService contestSupervisorService, ContestTeamService contestTeamService) {
        this.contestAnnouncementService = contestAnnouncementService;
        this.contestClarificationService = contestClarificationService;
        this.contestContestantService = contestContestantService;
        this.contestManagerService = contestManagerService;
        this.contestScoreboardService = contestScoreboardService;
        this.contestService = contestService;
        this.contestSupervisorService = contestSupervisorService;
        this.contestTeamService = contestTeamService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result unreadAnnouncement(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!isAllowedToEnterContest(contest)) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("success", false);
            return ok(jsonResponse);
        }

        long unreadCount = contestAnnouncementService.countUnreadAnnouncementsInContest(IdentityUtils.getUserJid(), contest.getJid());
        ObjectNode jsonResponse = Json.newObject();
        jsonResponse.put("success", true);
        jsonResponse.put("count", unreadCount);
        return ok(jsonResponse);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result unreadClarification(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("success", false);
            return ok(jsonResponse);
        }

        long unreadCount;
        if (isCoach(contest)) {
            List<ContestTeamMember> contestTeamMemberList = contestTeamService.getCoachedMembersInContest(contest.getJid(), IdentityUtils.getUserJid());
            unreadCount = contestClarificationService.countUnreadClarificationsInContest(contestTeamMemberList.stream().map(ContestTeamMember::getMemberJid).collect(Collectors.toList()), IdentityUtils.getUserJid(), contest.getJid(), false);
        } else {
            unreadCount = contestClarificationService.countUnreadClarificationsInContest(ImmutableList.of(IdentityUtils.getUserJid()), IdentityUtils.getUserJid(), contest.getJid(), true);
        }
        ObjectNode jsonResponse = Json.newObject();
        jsonResponse.put("success", true);
        jsonResponse.put("count", unreadCount);
        return ok(jsonResponse);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result unansweredClarification(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!isAllowedToSuperviseClarifications(contest)) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("success", false);
            return ok(jsonResponse);
        }

        long unreadCount = contestClarificationService.countUnansweredClarificationsInContest(contest.getJid());
        ObjectNode jsonResponse = Json.newObject();
        jsonResponse.put("success", true);
        jsonResponse.put("count", unreadCount);
        return ok(jsonResponse);
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
            if (!image.exists()) {
                return notFound();
            }

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
        }
    }

    @Transactional(readOnly = true)
    public Result getScoreboard() {
        DynamicForm dForm = DynamicForm.form().bindFromRequest();

        String secret = dForm.get("secret");
        String contestJid = dForm.get("containerJid");
        String type = dForm.get("type");

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

        if (contestScoreboardService.scoreboardExistsInContestByType(contestJid, contestScoreboardType)) {
            contestScoreboard = contestScoreboardService.findScoreboardInContestByType(contestJid, contestScoreboardType);
        } else {
            // Resort to the official one.
            contestScoreboard = contestScoreboardService.findScoreboardInContestByType(contestJid, ContestScoreboardType.OFFICIAL);
        }

        return ok(new Gson().toJson(contestScoreboard));
    }

    private boolean isAdmin() {
        return UrielUtils.hasRole("admin");
    }

    private boolean isManager(Contest contest) {
        return contestManagerService.isManagerInContest(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isSupervisor(Contest contest) {
        return contestSupervisorService.isContestSupervisorInContest(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isCoach(Contest contest) {
        return contestTeamService.isUserACoachOfAnyTeamInContest(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isContestant(Contest contest) {
        return contestContestantService.isContestantInContest(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isContestStarted(Contest contest) {
        if (!contest.containsModule(ContestModules.DURATION)) {
            return true;
        }

        ContestDurationModule contestDurationModule = (ContestDurationModule) contest.getModule(ContestModules.DURATION);
        return (!new Date().before(contestDurationModule.getBeginTime()));
    }

    private boolean isAllowedToEnterContest(Contest contest) {
        if (isAdmin() || isManager(contest) || isSupervisor(contest)) {
            return true;
        }
        if (!contest.containsModule(ContestModules.VIRTUAL)) {
            return ((isContestant(contest) && isContestStarted(contest)) || isCoach(contest));
        }

        if (!contest.containsModule(ContestModules.TRIGGER)) {
            return (isContestStarted(contest) && (isCoach(contest) || (isContestant(contest) && contestContestantService.hasContestantStartContest(contest.getJid(), IdentityUtils.getUserJid()))));
        }

        ContestTriggerModule contestTriggerModule = (ContestTriggerModule) contest.getModule(ContestModules.TRIGGER);

        if (contestTriggerModule.getContestTrigger().equals(ContestTrigger.TEAM_MEMBER)) {
            return (isContestant(contest) && (isContestStarted(contest)));
        }

        return (isContestStarted(contest) && (isCoach(contest) || (isContestant(contest) && contestContestantService.hasContestantStartContest(contest.getJid(), IdentityUtils.getUserJid()))));
    }

    private boolean isAllowedToSuperviseClarifications(Contest contest) {
        return isAdmin() || isManager(contest) || (isSupervisor(contest) && contestSupervisorService.findContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid()).getContestPermission().isAllowed(ContestPermissions.CLARIFICATION));
    }
}
