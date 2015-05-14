package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestScopeConfigPublic;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.views.html.contest.contestTimeLayout;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class ContestControllerUtils {

    private ContestService contestService;

    private static final String CURRENT_CONTEST_WITH_PASSWORD_KEY = "currentContestWithPassword";

    static final ContestControllerUtils INSTANCE = new ContestControllerUtils();

    private ContestControllerUtils() {
        // prevent instantiation
    }

    public void setContestService(ContestService contestService) {
        this.contestService = contestService;
    }

    public static ContestControllerUtils getInstance() {
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
        return contestService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    boolean isSupervisor(Contest contest) {
        return contestService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    boolean isCoach(Contest contest) {
        return contestService.isUserCoachInAnyTeamByContestJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    boolean isContestant(Contest contest) {
        return contestService.isContestContestantInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
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
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            return contestContestant.getContestStartTime() != 0;
        }
        return false;
    }

    boolean hasContestFinished(Contest contest) {
        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);

            return contestContestant.getContestStartTime() != 0 && (System.currentTimeMillis() > (contestContestant.getContestStartTime() + contestTypeConfigVirtual.getContestDuration()));
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
            ContestScopeConfigPublic contestScopeConfigPublic = new Gson().fromJson(contestConfiguration.getScopeConfig(), ContestScopeConfigPublic.class);

            result = result && (contestScopeConfigPublic.getRegisterStartTime() < System.currentTimeMillis()) && (contestScopeConfigPublic.getRegisterEndTime() > System.currentTimeMillis()) && ((contestScopeConfigPublic.getMaxRegistrants() == 0) || (contestService.getContestContestantCount(contest.getJid()) < contestScopeConfigPublic.getMaxRegistrants()));
        } else {
            result = false;
        }

        return result;
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
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
            if (contestTypeConfigVirtual.getStartTrigger().equals(ContestTypeConfigVirtualStartTrigger.CONTESTANT)) {
                return (isContestant(contest) && (hasContestStarted(contest)));
            } else {
                return ((hasContestStarted(contest)) && (isCoach(contest) || (isContestant(contest) && (contestService.isContestStarted(contest.getJid(), IdentityUtils.getUserJid())))));
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
            String password = contestService.getContestantPassword(contest.getJid(), IdentityUtils.getUserJid());
            if (password == null) {
                return false;
            }
            return hasEstablishedContestWithPasswordCookie(password);
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
        ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
        if (contestTypeConfigVirtual.getStartTrigger().equals(ContestTypeConfigVirtualStartTrigger.CONTESTANT)) {
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
        ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
        if (contestTypeConfigVirtual.getStartTrigger().equals(ContestTypeConfigVirtualStartTrigger.COACH)) {
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
        if (!contest.isVirtual() || !contestService.isUserCoachByUserJidAndTeamJid(IdentityUtils.getUserJid(), contestTeam.getJid())) {
            return false;
        }

        ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
        ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
        if (contestTypeConfigVirtual.getStartTrigger().equals(ContestTypeConfigVirtualStartTrigger.COACH)) {
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
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
            contestEndTime = new Date(Math.min(contestContestant.getContestStartTime() + contestTypeConfigVirtual.getContestDuration(), contest.getEndTime().getTime()));
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
