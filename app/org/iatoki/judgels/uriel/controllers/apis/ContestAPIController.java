package org.iatoki.judgels.uriel.controllers.apis;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
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

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class ContestAPIController extends Controller {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;

    public ContestAPIController(ContestService contestService) {
        this.contestService = contestService;
    }

    public Result unreadAnnouncement(long contestId) {
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

    public Result unreadClarification(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToEnterContest(contest)) {
            long unreadCount = contestService.getUnreadContestClarificationsCount(IdentityUtils.getUserJid(), contest.getJid());
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

    public Result unansweredClarification(long contestId) {
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

    public Result renderTeamAvatarImage(String imageName) {
        String imageURL = contestService.getTeamAvatarImageURL(imageName);

        try {
            new URL(imageURL);
            return redirect(imageURL);
        } catch (MalformedURLException e) {
            File image = new File(imageURL);
            if (image.exists()) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                response().setHeader("Cache-Control", "no-transform,public,max-age=300,s-maxage=900");
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
                return ((isContestStarted(contest)) && (isCoach(contest) || (isContestant(contest) && (contestService.isContestEntered(contest.getJid(), IdentityUtils.getUserJid())))));
            }
        }
    }

    private boolean isAllowedToSuperviseClarifications(Contest contest) {
        return isAdmin() || isManager(contest) || (isSupervisor(contest) && contestService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).isClarification());
    }
}