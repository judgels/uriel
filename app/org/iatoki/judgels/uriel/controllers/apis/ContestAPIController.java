package org.iatoki.judgels.uriel.controllers.apis;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.imageio.ImageIO;
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

@Transactional
public final class ContestAPIController extends Controller {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;

    public ContestAPIController(ContestService contestService) {
        this.contestService = contestService;
    }


    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result unreadAnnouncement(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            long unreadCount = contestService.getUnreadContestAnnouncementsCount(IdentityUtils.getUserJid(), contest.getJid());
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
    public Result unreadClarification(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            long unreadCount;
            if (isCoach(contest)) {
                List<ContestTeamMember> contestTeamMemberList = contestService.findContestTeamMembersByContestJidAndCoachJid(contest.getJid(), IdentityUtils.getUserJid());
                unreadCount = contestService.getUnreadContestClarificationsCount(contestTeamMemberList.stream().map(ctm -> ctm.getMemberJid()).collect(Collectors.toList()), IdentityUtils.getUserJid(), contest.getJid(), false);
            } else {
                unreadCount = contestService.getUnreadContestClarificationsCount(ImmutableList.of(IdentityUtils.getUserJid()), IdentityUtils.getUserJid(), contest.getJid(), true);
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
    public Result unansweredClarification(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToSuperviseClarifications(contest)) {
            long unreadCount = contestService.getUnansweredContestClarificationsCount(contest.getJid());
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
    public Result renderTeamAvatarImage(String imageName) {
        response().setHeader("Cache-Control", "no-transform,public,max-age=300,s-maxage=900");

        String imageURL = contestService.getTeamAvatarImageURL(imageName);
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

        if (contestService.isContestScoreboardExistByContestJidAndScoreboardType(contestJid, contestScoreboardType)) {
            contestScoreboard = contestService.findContestScoreboardByContestJidAndScoreboardType(contestJid, contestScoreboardType);
        } else {
            // Resort to the official one.
            contestScoreboard = contestService.findContestScoreboardByContestJidAndScoreboardType(contestJid, ContestScoreboardType.OFFICIAL);
        }

        return ok(new Gson().toJson(contestScoreboard));
    }

    private boolean isAdmin() {
        return UrielUtils.hasRole("admin");
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

    private boolean isAllowedToEnterContest(Contest contest) {
        if (isAdmin() || isManager(contest) || isSupervisor(contest)) {
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
                return ((isContestStarted(contest)) && (isCoach(contest) || (isContestant(contest) && (contestService.isContestStarted(contest.getJid(), IdentityUtils.getUserJid())))));
            }
        }
    }

    private boolean isAllowedToSuperviseClarifications(Contest contest) {
        return isAdmin() || isManager(contest) || (isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isClarification());
    }
}
