package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.PublicContestScopeConfig;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.VirtualContestTypeConfig;
import org.iatoki.judgels.uriel.VirtualContestTypeConfigStartTrigger;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestManagerService;
import org.iatoki.judgels.uriel.services.ContestPasswordService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.ContestSupervisorService;
import org.iatoki.judgels.uriel.services.ContestTeamService;
import org.iatoki.judgels.uriel.views.html.contest.contestTimeLayout;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class ContestControllerUtils {

    private ContestService contestService;
    private ContestContestantService contestContestantService;
    private ContestSupervisorService contestSupervisorService;
    private ContestManagerService contestManagerService;
    private ContestTeamService contestTeamService;
    private ContestPasswordService contestPasswordService;

    private static final String CURRENT_CONTEST_WITH_PASSWORD_KEY = "currentContestWithPassword";

    static ContestControllerUtils INSTANCE;

    private ContestControllerUtils(ContestService contestService, ContestContestantService contestContestantService, ContestSupervisorService contestSupervisorService, ContestManagerService contestManagerService, ContestTeamService contestTeamService, ContestPasswordService contestPasswordService) {
        this.contestService = contestService;
        this.contestContestantService = contestContestantService;
        this.contestSupervisorService = contestSupervisorService;
        this.contestManagerService = contestManagerService;
        this.contestTeamService = contestTeamService;
        this.contestPasswordService = contestPasswordService;
    }

    public static synchronized void buildInstance(ContestService contestService, ContestContestantService contestContestantService, ContestSupervisorService contestSupervisorService, ContestManagerService contestManagerService, ContestTeamService contestTeamService, ContestPasswordService contestPasswordService) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("ContestControllerUtils instance has already been built");
        }
        INSTANCE = new ContestControllerUtils(contestService, contestContestantService, contestSupervisorService, contestManagerService, contestTeamService, contestPasswordService);
    }

    public static ContestControllerUtils getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("ContestControllerUtils instance has not been built");
        }
        return INSTANCE;
    }

    void setCurrentStatementLanguage(String languageCode) {
        Controller.session("currentStatementLanguage", languageCode);
    }

    String getCurrentStatementLanguage() {
        String lang = Controller.session("currentStatementLanguage");
        if (lang == null) {
            return "en-US";
        } else {
            return lang;
        }
    }

    boolean isManager(Contest contest) {
        return contestManagerService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    boolean isSupervisor(Contest contest) {
        return contestSupervisorService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    boolean isCoach(Contest contest) {
        return contestTeamService.isUserCoachInAnyTeamByContestJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    boolean isContestant(Contest contest) {
        return contestContestantService.isContestContestantInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    boolean isSupervisorOrAbove(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest) || (isSupervisor(contest));
    }

    boolean isCoachOrAbove(Contest contest) {
        return isCoach(contest) || isSupervisorOrAbove(contest);
    }

    boolean hasContestBegun(Contest contest) {
        return (!new Date().before(contest.getStartTime()));
    }

    boolean hasContestEnded(Contest contest) {
        return (!new Date().before(contest.getEndTime()));
    }

    boolean hasContestStarted(Contest contest) {
        if (contest.isStandard()) {
            return hasContestBegun(contest);
        } else if (isContestant(contest)) {
            ContestContestant contestContestant = contestContestantService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            return contestContestant.getContestStartTime() != 0;
        }
        return false;
    }

    boolean hasContestFinished(Contest contest) {
        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestContestantService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            VirtualContestTypeConfig virtualContestTypeConfig = new Gson().fromJson(contestConfiguration.getTypeConfig(), VirtualContestTypeConfig.class);

            return contestContestant.getContestStartTime() != 0 && (System.currentTimeMillis() > (contestContestant.getContestStartTime() + virtualContestTypeConfig.getContestDuration()));
        } else {
            return hasContestEnded(contest);
        }
    }

    boolean isAllowedToViewContest(Contest contest) {
        return contest.isPublic() || isCoachOrAbove(contest) || isContestant(contest);
    }

    boolean isAllowedToManageContest(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest);
    }

    boolean isAllowedToRegisterContest(Contest contest) {
        if (isSupervisorOrAbove(contest)) {
            return false;
        }

        boolean result = !isContestant(contest) && !hasContestEnded(contest);
        if (contest.isPublic()) {
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            PublicContestScopeConfig publicContestScopeConfig = new Gson().fromJson(contestConfiguration.getScopeConfig(), PublicContestScopeConfig.class);

            result = result && (publicContestScopeConfig.getRegisterStartTime() < System.currentTimeMillis()) && (publicContestScopeConfig.getRegisterEndTime() > System.currentTimeMillis()) && ((publicContestScopeConfig.getMaxRegistrants() == 0) || (contestContestantService.getContestContestantCount(contest.getJid()) < publicContestScopeConfig.getMaxRegistrants()));
        } else {
            result = false;
        }

        return result;
    }

    boolean isAllowedToUnregisterContest(Contest contest) {
        return isContestant(contest) && contest.isPublic() && !hasContestStarted(contest);
    }

    boolean isAllowedToViewEnterContestButton(Contest contest) {
        if (isCoachOrAbove(contest)) {
            return true;
        }
        if (!isContestant(contest)) {
            return false;
        }

        if (contest.isStandard()) {
            return hasContestBegun(contest);
        } else {
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            VirtualContestTypeConfig virtualContestTypeConfig = new Gson().fromJson(contestConfiguration.getTypeConfig(), VirtualContestTypeConfig.class);
            if (virtualContestTypeConfig.getStartTrigger().equals(VirtualContestTypeConfigStartTrigger.CONTESTANT)) {
                return (isContestant(contest) && (hasContestStarted(contest)));
            } else {
                return ((hasContestStarted(contest)) && (isCoach(contest) || (isContestant(contest) && (contestContestantService.isContestStarted(contest.getJid(), IdentityUtils.getUserJid())))));
            }
        }
    }

    boolean isAllowedToEnterContest(Contest contest) {
        if (!isAllowedToViewEnterContestButton(contest)) {
            return false;
        }
        if (isCoachOrAbove(contest)) {
            return true;
        }
        if (!isContestant(contest)) {
            return false;
        }
        if (contest.requiresPassword() && !UrielUtils.trullyHasRole("admin")) {
            String password = contestPasswordService.getContestantPassword(contest.getJid(), IdentityUtils.getUserJid());
            return ((password == null) && (hasEstablishedContestWithPasswordCookie(password)));
        }
        return true;
    }

    boolean requiresPasswordToEnterContest(Contest contest) {
        if (UrielUtils.trullyHasRole("admin")) {
            return false;
        }
        if (isSupervisorOrAbove(contest)) {
            return false;
        }
        return contest.requiresPassword();
    }

    boolean isAllowedToStartContestAsContestant(Contest contest) {
        if (!hasContestBegun(contest) || hasContestEnded(contest)) {
            return false;
        }

        if (isSupervisorOrAbove(contest)) {
            return false;
        }
        if (!contest.isVirtual() || !isContestant(contest)) {
            return false;
        }

        ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
        VirtualContestTypeConfig virtualContestTypeConfig = new Gson().fromJson(contestConfiguration.getTypeConfig(), VirtualContestTypeConfig.class);
        if (virtualContestTypeConfig.getStartTrigger().equals(VirtualContestTypeConfigStartTrigger.CONTESTANT)) {
            return !hasContestStarted(contest);
        } else {
            return false;
        }
    }

    boolean isAllowedToStartAnyContestAsCoach(Contest contest) {
        if (!hasContestBegun(contest) || hasContestEnded(contest)) {
            return false;
        }

        if (isSupervisorOrAbove(contest)) {
            return true;
        }
        if (!contest.isVirtual() || !isCoach(contest)) {
            return false;
        }

        ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
        VirtualContestTypeConfig virtualContestTypeConfig = new Gson().fromJson(contestConfiguration.getTypeConfig(), VirtualContestTypeConfig.class);
        if (virtualContestTypeConfig.getStartTrigger().equals(VirtualContestTypeConfigStartTrigger.COACH)) {
            return !hasContestStarted(contest);
        } else {
            return false;
        }
    }

    boolean isAllowedToStartContestAsCoach(Contest contest, ContestTeam contestTeam) {
        if (!hasContestBegun(contest) || hasContestEnded(contest)) {
            return false;
        }

        if (isSupervisorOrAbove(contest)) {
            return true;
        }
        if (!contest.isVirtual() || !contestTeamService.isUserCoachByUserJidAndTeamJid(IdentityUtils.getUserJid(), contestTeam.getJid())) {
            return false;
        }

        ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
        VirtualContestTypeConfig virtualContestTypeConfig = new Gson().fromJson(contestConfiguration.getTypeConfig(), VirtualContestTypeConfig.class);
        if (virtualContestTypeConfig.getStartTrigger().equals(VirtualContestTypeConfigStartTrigger.COACH)) {
            return !hasContestStarted(contest);
        } else {
            return false;
        }
    }

    boolean isAllowedToDoContest(Contest contest) {
        if (isSupervisorOrAbove(contest)) {
            return true;
        }
        return isAllowedToEnterContest(contest) && hasContestStarted(contest) && !hasContestFinished(contest);
    }

    Result tryEnteringContest(Contest contest) {
        if (isAllowedToEnterContest(contest)) {
            return Results.redirect(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()));
        } else {
            return Results.redirect(routes.ContestController.viewContest(contest.getId()));
        }
    }

    void establishContestWithPasswordCookie(String contestPassword) {
        Controller.response().setCookie(contestPassword, "true", (int)TimeUnit.SECONDS.convert(5, TimeUnit.HOURS));
    }

    boolean hasEstablishedContestWithPasswordCookie(String contestPassword) {
        return Controller.request().cookie(contestPassword) != null;
    }

    void appendTabsLayout(LazyHtml content, Contest contest) {
        final Date contestEndTime;
        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestContestantService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            VirtualContestTypeConfig virtualContestTypeConfig = new Gson().fromJson(contestConfiguration.getTypeConfig(), VirtualContestTypeConfig.class);
            contestEndTime = new Date(Math.min(contestContestant.getContestStartTime() + virtualContestTypeConfig.getContestDuration(), contest.getEndTime().getTime()));
        } else {
            contestEndTime = contest.getEndTime();
        }

        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.jumpToAnnouncements(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("problem.problems"), routes.ContestController.jumpToProblems(contest.getId())));

        if (!isCoach(contest)) {
            internalLinkBuilder.add(new InternalLink(Messages.get("submission.submissions"), routes.ContestController.jumpToSubmissions(contest.getId())));
        }

        internalLinkBuilder.add(new InternalLink(Messages.get("clarification.clarifications"), routes.ContestController.jumpToClarifications(contest.getId())));
        if (contest.isUsingScoreboard()) {
            internalLinkBuilder.add(new InternalLink(Messages.get("scoreboard.scoreboard"), routes.ContestController.jumpToScoreboard(contest.getId())));
        }

        if (isCoachOrAbove(contest)) {
            internalLinkBuilder.add(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.jumpToContestants(contest.getId())));
        }

        if (isSupervisorOrAbove(contest)) {
            internalLinkBuilder.add(new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestController.jumpToSupervisors(contest.getId())));
            internalLinkBuilder.add(new InternalLink(Messages.get("manager.managers"), routes.ContestController.jumpToManagers(contest.getId())));
            internalLinkBuilder.add(new InternalLink(Messages.get("file.files"), routes.ContestController.jumpToFiles(contest.getId())));
        }
        content.appendLayout(c -> contestTimeLayout.render(contest.getStartTime(), contestEndTime, c));
        content.appendLayout(c -> tabLayout.render(internalLinkBuilder.build(), c));

        if (isAllowedToManageContest(contest)) {
            content.appendLayout(c -> headingWithActionLayout.render(contest.getName(), new InternalLink(Messages.get("contest.update"), routes.ContestController.updateContestGeneralConfig(contest.getId())), c));
        } else {
            content.appendLayout(c -> headingLayout.render(contest.getName(), c));
        }
    }

    ImmutableList.Builder<InternalLink> getContestBreadcrumbsBuilder(Contest contest) {
        ImmutableList.Builder<InternalLink> internalLinks = ImmutableList.builder();
        internalLinks
                .add(new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()))
                .add(new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())));

        return internalLinks;
    }

    private String constructContestPasswordSessionKey(Contest contest) {
        return contest.getJid() + "-password";
    }
}
