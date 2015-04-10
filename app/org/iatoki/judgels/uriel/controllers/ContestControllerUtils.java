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
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.views.html.contest.contestTimeLayout;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.util.Date;

public final class ContestControllerUtils {

    ContestService contestService;

    static ContestControllerUtils INSTANCE = new ContestControllerUtils();

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

    boolean isContestStarted(Contest contest) {
        return (!new Date().before(contest.getStartTime()));
    }

    boolean isContestEnded(Contest contest) {
        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);

            return (System.currentTimeMillis() > (contestContestant.getContestEnterTime() + contestTypeConfigVirtual.getContestDuration()));
        } else {
            return new Date().after(contest.getEndTime());
        }
    }

    boolean isAllowedToViewContest(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest) || isSupervisor(contest) || contest.isPublic() || isCoach(contest) || isContestant(contest);
    }

    boolean isAllowedToManageContest(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest);
    }

    boolean isAllowedToRegisterContest(Contest contest) {
        boolean result = !isContestant(contest) && !isContestEnded(contest);
        if (contest.isPublic()) {
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestScopeConfigPublic contestScopeConfigPublic = new Gson().fromJson(contestConfiguration.getScopeConfig(), ContestScopeConfigPublic.class);

            result = result && (contestScopeConfigPublic.getRegisterStartTime() < System.currentTimeMillis()) && (contestScopeConfigPublic.getRegisterEndTime() > System.currentTimeMillis()) && ((contestScopeConfigPublic.getMaxRegistrants() == 0) || (contestService.getContestContestantCount(contest.getJid()) < contestScopeConfigPublic.getMaxRegistrants()));
        } else {
            result = false;
        }

        return result;
    }

    boolean isAllowedToEnterContest(Contest contest) {
        if (ControllerUtils.getInstance().isAdmin() || isManager(contest) || isSupervisor(contest)) {
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

    boolean isAllowedToDoContest(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest) || isSupervisor(contest) || (isContestant(contest) && (contestService.isContestEntered(contest.getJid(), IdentityUtils.getUserJid()))  && isContestStarted(contest) && !isContestEnded(contest));
    }

    Result tryEnteringContest(Contest contest) {
        if (isAllowedToEnterContest(contest)) {
            return Results.redirect(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()));
        } else {
            return Results.redirect(routes.ContestController.viewContest(contest.getId()));
        }
    }

    void appendTabsLayout(LazyHtml content, Contest contest) {
        final Date contestEndTime;
        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
            contestEndTime = new Date(contestContestant.getContestEnterTime() + contestTypeConfigVirtual.getContestDuration());
        } else {
            contestEndTime = contest.getEndTime();
        }

        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.jumpToAnnouncements(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("problem.problems"), routes.ContestController.jumpToProblems(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("submission.submissions"), routes.ContestController.jumpToSubmissions(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("clarification.clarifications"), routes.ContestController.jumpToClarifications(contest.getId())));
        if (contest.isUsingScoreboard()) {
            internalLinkBuilder.add(new InternalLink(Messages.get("scoreboard.scoreboard"), routes.ContestController.jumpToScoreboard(contest.getId())));
        }
        if (isSupervisorOrAbove(contest)) {
            internalLinkBuilder.add(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.jumpToContestants(contest.getId())));
            internalLinkBuilder.add(new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestController.jumpToSupervisors(contest.getId())));
            internalLinkBuilder.add(new InternalLink(Messages.get("manager.managers"), routes.ContestController.jumpToManagers(contest.getId())));
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
}
