package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.alertLayout;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.modules.contest.ContestModule;
import org.iatoki.judgels.uriel.modules.contest.ContestModuleComparator;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.modules.contest.TabbedContestModule;
import org.iatoki.judgels.uriel.modules.contest.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.modules.contest.registration.ContestRegistrationModule;
import org.iatoki.judgels.uriel.modules.contest.trigger.ContestTrigger;
import org.iatoki.judgels.uriel.modules.contest.trigger.ContestTriggerModule;
import org.iatoki.judgels.uriel.modules.contest.virtual.ContestVirtualModule;
import org.iatoki.judgels.uriel.services.ContestContestantPasswordService;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestManagerService;
import org.iatoki.judgels.uriel.services.ContestSupervisorService;
import org.iatoki.judgels.uriel.services.ContestTeamService;
import org.iatoki.judgels.uriel.views.html.contest.contestTimeLayout;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class ContestControllerUtils {

    private static ContestControllerUtils instance;

    private final ContestContestantService contestContestantService;
    private final ContestSupervisorService contestSupervisorService;
    private final ContestManagerService contestManagerService;
    private final ContestTeamService contestTeamService;
    private final ContestContestantPasswordService contestContestantPasswordService;

    public ContestControllerUtils(ContestContestantService contestContestantService, ContestSupervisorService contestSupervisorService, ContestManagerService contestManagerService, ContestTeamService contestTeamService, ContestContestantPasswordService contestContestantPasswordService) {
        this.contestContestantService = contestContestantService;
        this.contestSupervisorService = contestSupervisorService;
        this.contestManagerService = contestManagerService;
        this.contestTeamService = contestTeamService;
        this.contestContestantPasswordService = contestContestantPasswordService;
    }

    public static synchronized void buildInstance(ContestContestantService contestContestantService, ContestSupervisorService contestSupervisorService, ContestManagerService contestManagerService, ContestTeamService contestTeamService, ContestContestantPasswordService contestPasswordService) {
        if (instance != null) {
            throw new UnsupportedOperationException("ContestControllerUtils instance has already been built");
        }
        instance = new ContestControllerUtils(contestContestantService, contestSupervisorService, contestManagerService, contestTeamService, contestPasswordService);
    }

    public static ContestControllerUtils getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("ContestControllerUtils instance has not been built");
        }
        return instance;
    }

    public void setCurrentStatementLanguage(String languageCode) {
        Controller.session("currentStatementLanguage", languageCode);
    }

    public String getCurrentStatementLanguage() {
        String lang = Controller.session("currentStatementLanguage");
        if (lang == null) {
            return "en-US";
        }

        return lang;
    }

    public boolean isAdmin() {
        return UrielControllerUtils.getInstance().isAdmin();
    }

    public boolean isManager(Contest contest, String userJid) {
        return contestManagerService.isManagerInContest(contest.getJid(), userJid);
    }

    public boolean isSupervisor(Contest contest, String userJid) {
        return contest.containsModule(ContestModules.SUPERVISOR) && contestSupervisorService.isContestSupervisorInContest(contest.getJid(), userJid);
    }

    public boolean isCoach(Contest contest, String userJid) {
        return contest.containsModule(ContestModules.TEAM) && contestTeamService.isUserACoachOfAnyTeamInContest(contest.getJid(), userJid);
    }

    public boolean isContestant(Contest contest, String userJid) {
        return contestContestantService.isContestantInContest(contest.getJid(), userJid);
    }

    public boolean isManagerOrAbove(Contest contest, String userJid) {
        return isAdmin() || isManager(contest, userJid);
    }

    public boolean isSupervisorOrAbove(Contest contest, String userJid) {
        return isManagerOrAbove(contest, userJid) || isSupervisor(contest, userJid);
    }

    public boolean isCoachOrAbove(Contest contest, String userJid) {
        return isSupervisorOrAbove(contest, userJid) || isCoach(contest, userJid);
    }

    public boolean isPermittedToSupervise(Contest contest, ContestPermissions contestPermissions, String userJid) {
        return isManagerOrAbove(contest, userJid) || (isSupervisor(contest, userJid) && contestSupervisorService.findContestSupervisorInContestByUserJid(contest.getJid(), userJid).getContestPermission().isAllowed(contestPermissions));
    }

    public boolean hasContestBegun(Contest contest) {
        if (contest.containsModule(ContestModules.DURATION)) {
            ContestDurationModule contestDurationModule = (ContestDurationModule) contest.getModule(ContestModules.DURATION);
            return new Date().after(contestDurationModule.getBeginTime());
        }

        return true;
    }

    public boolean hasContestEnded(Contest contest) {
        if (contest.containsModule(ContestModules.DURATION)) {
            ContestDurationModule contestDurationModule = (ContestDurationModule) contest.getModule(ContestModules.DURATION);
            return !new Date().before(contestDurationModule.getEndTime());
        }

        return false;
    }

    public boolean hasRegisteredToContest(Contest contest, String userJid) {
        if (!contest.containsModule(ContestModules.REGISTRATION)) {
            return false;
        }

        return contestContestantService.hasRegisteredToContest(contest.getJid(), userJid);
    }

    public boolean hasContestStarted(Contest contest, String userJid) {
        if (contest.containsModule(ContestModules.VIRTUAL)) {
            if (!contestContestantService.isContestantInContest(contest.getJid(), userJid)) {
                return false;
            }

            ContestContestant contestContestant = contestContestantService.findContestantInContestAndJid(contest.getJid(), userJid);
            return contestContestant.getContestStartTime() != 0;
        }

        return hasContestBegun(contest);
    }

    public boolean hasContestFinished(Contest contest, String userJid) {
        if (contest.containsModule(ContestModules.VIRTUAL) && isContestant(contest, userJid)) {
            ContestContestant contestContestant = contestContestantService.findContestantInContestAndJid(contest.getJid(), userJid);
            ContestVirtualModule contestVirtualModule = (ContestVirtualModule) contest.getModule(ContestModules.VIRTUAL);

            return contestContestant.getContestStartTime() != 0 && (System.currentTimeMillis() > (contestContestant.getContestStartTime() + contestVirtualModule.getVirtualDuration()));
        }

        return hasContestEnded(contest);
    }

    public boolean isAllowedToViewContest(Contest contest, String userJid) {
        return !contest.containsModule(ContestModules.LIMITED) || isCoachOrAbove(contest, userJid) || isContestant(contest, userJid);
    }

    public boolean isAllowedToManageContest(Contest contest, String userJid) {
        return isManagerOrAbove(contest, userJid);
    }

    public boolean isAllowedToRegisterContest(Contest contest, String userJid) {
        if (!contest.containsModule(ContestModules.REGISTRATION)) {
            return false;
        }

        if (isSupervisorOrAbove(contest, userJid)) {
            return false;
        }

        ContestRegistrationModule contestRegistrationModule = (ContestRegistrationModule) contest.getModule(ContestModules.REGISTRATION);
        return (contestRegistrationModule.getRegisterStartTime() < System.currentTimeMillis()) && (contestRegistrationModule.getRegisterStartTime() + contestRegistrationModule.getRegisterDuration() > System.currentTimeMillis()) && ((contestRegistrationModule.getMaxRegistrants() == 0) || (contestContestantService.countContestantsInContest(contest.getJid()) < contestRegistrationModule.getMaxRegistrants())) && !hasContestEnded(contest) && !hasRegisteredToContest(contest, userJid);
    }

    public boolean isAllowedToUnregisterContest(Contest contest, String userJid) {
        if (!contest.containsModule(ContestModules.REGISTRATION)) {
            return false;
        }

        return !hasContestStarted(contest, userJid) && isContestant(contest, userJid);
    }

    public boolean isAllowedToViewEnterContestButton(Contest contest, String userJid) {
        if (isCoachOrAbove(contest, userJid)) {
            return true;
        }
        if (contest.containsModule(ContestModules.REGISTRATION) || contest.containsModule(ContestModules.LIMITED)) {
            return hasContestStarted(contest, userJid) && isContestant(contest, userJid);
        }

        return hasContestStarted(contest, userJid);
    }

    public boolean isAllowedToEnterContest(Contest contest, String userJid) {
        if (!isAllowedToViewEnterContestButton(contest, userJid)) {
            return false;
        }
        if (isCoachOrAbove(contest, userJid)) {
            return true;
        }

        if (contest.containsModule(ContestModules.REGISTRATION) || contest.containsModule(ContestModules.LIMITED)) {
            return isContestant(contest, userJid);
        }

        if (contest.containsModule(ContestModules.PASSWORD)) {
            String password = contestContestantPasswordService.getContestantPassword(contest.getJid(), userJid);
            return ((password == null) && hasEstablishedContestWithPasswordCookie(password));
        }

        return true;
    }

    public boolean requiresPasswordToEnterContest(Contest contest, String userJid) {
        if (UrielUtils.trullyHasRole("admin")) {
            return false;
        }

        if (isSupervisorOrAbove(contest, userJid)) {
            return false;
        }

        return contest.containsModule(ContestModules.PASSWORD);
    }

    public boolean isAllowedToStartContestAsContestant(Contest contest, String userJid) {
        if (!hasContestBegun(contest) || hasContestEnded(contest)) {
            return false;
        }

        if (userJid.startsWith("guest")) {
            return false;
        }

        if (isSupervisorOrAbove(contest, userJid)) {
            return false;
        }

        if (!contest.containsModule(ContestModules.VIRTUAL) || !isContestant(contest, userJid)) {
            return false;
        }

        if (contest.containsModule(ContestModules.TRIGGER)) {
            return false;
        }

        return !hasContestStarted(contest, userJid);
    }

    public boolean isAllowedToViewStartContestForTeamButtonInContest(Contest contest, String userJid) {
        if (!contest.containsModule(ContestModules.VIRTUAL) || !hasContestBegun(contest) || hasContestEnded(contest)) {
            return false;
        }

        if (userJid.startsWith("guest")) {
            return false;
        }

        if (isSupervisorOrAbove(contest, userJid)) {
            return true;
        }
        if (!isCoach(contest, userJid)) {
            return false;
        }

        if (contest.containsModule(ContestModules.TRIGGER)) {
            ContestTriggerModule contestTriggerModule = (ContestTriggerModule) contest.getModule(ContestModules.TRIGGER);
            if (contestTriggerModule.getContestTrigger().equals(ContestTrigger.COACH)) {
                return hasContestBegun(contest);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isAllowedToStartContestForTeamAsCoach(Contest contest, ContestTeam contestTeam, String userJid) {
        if (!isAllowedToViewStartContestForTeamButtonInContest(contest, userJid)) {
            return false;
        }

        if (!contestTeamService.isUserACoachInTeam(userJid, contestTeam.getJid())) {
            return false;
        }

        return !contestTeam.isStarted();
    }

    public boolean isAllowedToDoContest(Contest contest, String userJid) {
        if (contest.isLocked()) {
            return false;
        }

        if (isSupervisorOrAbove(contest, userJid)) {
            return true;
        }

        if (isCoach(contest, userJid)) {
            return false;
        }

        if (contest.containsModule(ContestModules.PAUSE)) {
            return false;
        }

        return isAllowedToEnterContest(contest, userJid) && hasContestStarted(contest, userJid) && !hasContestFinished(contest, userJid);
    }

    public Result tryEnteringContest(Contest contest, String userJid) {
        if (!isAllowedToEnterContest(contest, userJid)) {
            return Results.redirect(routes.ContestController.viewContest(contest.getId()));
        }

        return Results.redirect(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()));
    }

    public void establishContestWithPasswordCookie(String contestPassword) {
        Controller.response().setCookie(contestPassword, "true", (int) TimeUnit.SECONDS.convert(5, TimeUnit.HOURS));
    }

    public boolean hasEstablishedContestWithPasswordCookie(String contestPassword) {
        return Controller.request().cookie(contestPassword) != null;
    }

    public void appendTabsLayout(LazyHtml content, Contest contest, String userJid) {
        final Date contestBeginTime;
        final Date contestEndTime;
        if (contest.containsModule(ContestModules.VIRTUAL) && (isContestant(contest, userJid))) {
            ContestContestant contestContestant = contestContestantService.findContestantInContestAndJid(contest.getJid(), userJid);
            ContestVirtualModule contestVirtualModule = (ContestVirtualModule) contest.getModule(ContestModules.VIRTUAL);

            long endTime = contestContestant.getContestStartTime() + contestVirtualModule.getVirtualDuration();
            if (contest.containsModule(ContestModules.DURATION)) {
                ContestDurationModule contestDurationModule = (ContestDurationModule) contest.getModule(ContestModules.DURATION);
                contestBeginTime = contestDurationModule.getBeginTime();
                endTime = Math.min(endTime, contestDurationModule.getBeginTime().getTime() + contestDurationModule.getContestDuration());
            } else {
                contestBeginTime = new Date(contestContestant.getContestStartTime());
            }
            contestEndTime = new Date(endTime);
        } else if (contest.containsModule(ContestModules.DURATION)) {
            ContestDurationModule contestDurationModule = (ContestDurationModule) contest.getModule(ContestModules.DURATION);
            contestBeginTime = contestDurationModule.getBeginTime();
            contestEndTime = new Date(contestDurationModule.getBeginTime().getTime() + contestDurationModule.getContestDuration());
        } else {
            contestBeginTime = null;
            contestEndTime = null;
        }

        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("announcement.announcements"), routes.ContestController.jumpToAnnouncements(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("problem.problems"), routes.ContestController.jumpToProblems(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("submission.submissions"), routes.ContestController.jumpToSubmissions(contest.getId())));

        List<TabbedContestModule> moduleWithTabs = Lists.newArrayList();
        for (ContestModule contestModule : contest.getModules()) {
            if ((contestModule instanceof TabbedContestModule) && (((TabbedContestModule) contestModule).isAllowedToViewTab(ContestControllerUtils.getInstance(), contest, IdentityUtils.getUserJid()))) {
                moduleWithTabs.add((TabbedContestModule) contestModule);
            }
        }
        Collections.sort(moduleWithTabs, new ContestModuleComparator());
        for (TabbedContestModule contestModule : moduleWithTabs) {
            internalLinkBuilder.add(new InternalLink(contestModule.getTabName(), contestModule.getDefaultJumpTo(contest.getId())));
        }

        if (isSupervisorOrAbove(contest, userJid)) {
            internalLinkBuilder.add(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.jumpToContestants(contest.getId())));
            internalLinkBuilder.add(new InternalLink(Messages.get("manager.managers"), routes.ContestController.jumpToManagers(contest.getId())));
        }

        content.appendLayout(c -> contestTimeLayout.render(contestBeginTime, contestEndTime, c));
        if (contest.containsModule(ContestModules.PAUSE)) {
            content.appendLayout(c -> alertLayout.render(Messages.get("contest.isPaused"), c));
        }
        if (contest.isLocked()) {
            content.appendLayout(c -> alertLayout.render(Messages.get("contest.isLocked"), c));
        }
        content.appendLayout(c -> tabLayout.render(internalLinkBuilder.build(), c));

        if (isAllowedToManageContest(contest, userJid)) {
            content.appendLayout(c -> headingWithActionLayout.render(contest.getName(), new InternalLink(Messages.get("contest.update"), routes.ContestController.editContestGeneralConfig(contest.getId())), c));
        } else {
            content.appendLayout(c -> headingLayout.render(contest.getName(), c));
        }
    }

    public ImmutableList.Builder<InternalLink> getContestBreadcrumbsBuilder(Contest contest) {
        ImmutableList.Builder<InternalLink> internalLinks = ImmutableList.builder();
        internalLinks
                .add(new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()))
                .add(new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())));

        return internalLinks;
    }
}
