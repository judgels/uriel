package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.UrielUtils;
import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.modules.ContestModuleComparator;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.modules.TabbedContestModule;
import org.iatoki.judgels.uriel.modules.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.modules.registration.ContestRegistrationModule;
import org.iatoki.judgels.uriel.modules.trigger.ContestTrigger;
import org.iatoki.judgels.uriel.modules.trigger.ContestTriggerModule;
import org.iatoki.judgels.uriel.modules.virtual.ContestVirtualModule;
import org.iatoki.judgels.uriel.services.ContestContestantPasswordService;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestManagerService;
import org.iatoki.judgels.uriel.services.ContestModuleService;
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
    private final ContestModuleService contestModuleService;
    private final ContestTeamService contestTeamService;
    private final ContestContestantPasswordService contestContestantPasswordService;

    public ContestControllerUtils(ContestContestantService contestContestantService, ContestSupervisorService contestSupervisorService, ContestManagerService contestManagerService, ContestModuleService contestModuleService, ContestTeamService contestTeamService, ContestContestantPasswordService contestContestantPasswordService) {
        this.contestContestantService = contestContestantService;
        this.contestSupervisorService = contestSupervisorService;
        this.contestManagerService = contestManagerService;
        this.contestModuleService = contestModuleService;
        this.contestTeamService = contestTeamService;
        this.contestContestantPasswordService = contestContestantPasswordService;
    }

    public static synchronized void buildInstance(ContestContestantService contestContestantService, ContestSupervisorService contestSupervisorService, ContestModuleService contestModuleService, ContestManagerService contestManagerService, ContestTeamService contestTeamService, ContestContestantPasswordService contestPasswordService) {
        if (instance != null) {
            throw new UnsupportedOperationException("ContestControllerUtils instance has already been built");
        }
        instance = new ContestControllerUtils(contestContestantService, contestSupervisorService, contestManagerService, contestModuleService, contestTeamService, contestPasswordService);
    }

    static ContestControllerUtils getInstance() {
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

    public boolean isManager(Contest contest) {
        return contestManagerService.isManagerInContest(contest.getJid(), IdentityUtils.getUserJid());
    }

    public boolean isSupervisor(Contest contest) {
        return contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.SUPERVISOR) && contestSupervisorService.isContestSupervisorInContest(contest.getJid(), IdentityUtils.getUserJid());
    }

    public boolean isCoach(Contest contest) {
        return contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.TEAM) && contestTeamService.isUserACoachOfAnyTeamInContest(contest.getJid(), IdentityUtils.getUserJid());
    }

    public boolean isContestant(Contest contest) {
        return contestContestantService.isContestantInContest(contest.getJid(), IdentityUtils.getUserJid());
    }

    public boolean isSupervisorOrAbove(Contest contest) {
        return UrielControllerUtils.getInstance().isAdmin() || isManager(contest) || (isSupervisor(contest));
    }

    public boolean isCoachOrAbove(Contest contest) {
        return isCoach(contest) || isSupervisorOrAbove(contest);
    }

    public boolean hasContestBegun(Contest contest) {
        if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.DURATION)) {
            ContestDurationModule contestDurationModule = (ContestDurationModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.DURATION);
            return new Date().after(contestDurationModule.getBeginTime());
        }

        return true;
    }

    public boolean hasContestEnded(Contest contest) {
        if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.DURATION)) {
            ContestDurationModule contestDurationModule = (ContestDurationModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.DURATION);
            return !new Date().before(contestDurationModule.getEndTime());
        }

        return false;
    }

    public boolean hasContestStarted(Contest contest) {
        if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.VIRTUAL)) {
            ContestContestant contestContestant = contestContestantService.findContestantInContestAndJid(contest.getJid(), IdentityUtils.getUserJid());
            return contestContestant.getContestStartTime() != 0;
        }

        return hasContestBegun(contest);
    }

    public boolean hasContestFinished(Contest contest) {
        if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.VIRTUAL) && isContestant(contest)) {
            ContestContestant contestContestant = contestContestantService.findContestantInContestAndJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestVirtualModule contestVirtualModule = (ContestVirtualModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.VIRTUAL);

            return contestContestant.getContestStartTime() != 0 && (System.currentTimeMillis() > (contestContestant.getContestStartTime() + contestVirtualModule.getVirtualDuration()));
        }

        return hasContestEnded(contest);
    }

    public boolean isAllowedToViewContest(Contest contest) {
        return !contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.LIMITED) || isCoachOrAbove(contest) || isContestant(contest);
    }

    public boolean isAllowedToManageContest(Contest contest) {
        return UrielControllerUtils.getInstance().isAdmin() || isManager(contest);
    }

    public boolean isAllowedToRegisterContest(Contest contest) {
        if (isSupervisorOrAbove(contest)) {
            return false;
        }

        boolean result = !isContestant(contest) && !hasContestEnded(contest);
        if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.REGISTRATION)) {
            ContestRegistrationModule contestRegistrationModule = (ContestRegistrationModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.REGISTRATION);

            result = result && (contestRegistrationModule.getRegisterStartTime() < System.currentTimeMillis()) && (contestRegistrationModule.getRegisterStartTime() + contestRegistrationModule.getRegisterDuration() > System.currentTimeMillis()) && ((contestRegistrationModule.getMaxRegistrants() == 0) || (contestContestantService.countContestantsInContest(contest.getJid()) < contestRegistrationModule.getMaxRegistrants()));
        } else {
            result = false;
        }

        return result;
    }

    public boolean isAllowedToUnregisterContest(Contest contest) {
        return isContestant(contest) && !contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.LIMITED) && !hasContestStarted(contest);
    }

    public boolean isAllowedToViewEnterContestButton(Contest contest) {
        if (isCoachOrAbove(contest)) {
            return true;
        }
        if (!isContestant(contest)) {
            return false;
        }

        return hasContestBegun(contest);
    }

    public boolean isAllowedToEnterContest(Contest contest) {
        if (!isAllowedToViewEnterContestButton(contest)) {
            return false;
        }
        if (isCoachOrAbove(contest) || UrielUtils.trullyHasRole("admin")) {
            return true;
        }
        if (!isContestant(contest)) {
            return false;
        } else if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.PASSWORD)) {
            String password = contestContestantPasswordService.getContestantPassword(contest.getJid(), IdentityUtils.getUserJid());
            return ((password == null) && (hasEstablishedContestWithPasswordCookie(password)));
        } else {
            return true;
        }
    }

    public boolean requiresPasswordToEnterContest(Contest contest) {
        if (UrielUtils.trullyHasRole("admin")) {
            return false;
        }
        if (isSupervisorOrAbove(contest)) {
            return false;
        }
        return contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.PASSWORD);
    }

    public boolean isAllowedToStartContestAsContestant(Contest contest) {
        if (!hasContestBegun(contest) || hasContestEnded(contest)) {
            return false;
        }

        if (isSupervisorOrAbove(contest)) {
            return false;
        }
        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.VIRTUAL) || !isContestant(contest)) {
            return false;
        }

        if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.TRIGGER)) {
            ContestTriggerModule contestTriggerModule = (ContestTriggerModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.TRIGGER);
            if (contestTriggerModule.getContestTrigger().equals(ContestTrigger.TEAM_MEMBER)) {
                return !hasContestStarted(contest);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isAllowedToStartAnyContestAsCoach(Contest contest) {
        if (!hasContestBegun(contest) || hasContestEnded(contest)) {
            return false;
        }

        if (isSupervisorOrAbove(contest)) {
            return true;
        }
        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.VIRTUAL) || !isCoach(contest)) {
            return false;
        }

        if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.TRIGGER)) {
            ContestTriggerModule contestTriggerModule = (ContestTriggerModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.TRIGGER);
            if (contestTriggerModule.getContestTrigger().equals(ContestTrigger.COACH)) {
                return !hasContestStarted(contest);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isAllowedToStartContestAsCoach(Contest contest, ContestTeam contestTeam) {
        if (!hasContestBegun(contest) || hasContestEnded(contest)) {
            return false;
        }

        if (isSupervisorOrAbove(contest)) {
            return true;
        }
        if (!contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.VIRTUAL) || !contestTeamService.isUserACoachInTeam(IdentityUtils.getUserJid(), contestTeam.getJid())) {
            return false;
        }

        if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.TRIGGER)) {
            ContestTriggerModule contestTriggerModule = (ContestTriggerModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.TRIGGER);
            if (contestTriggerModule.getContestTrigger().equals(ContestTrigger.COACH)) {
                return !hasContestStarted(contest);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isAllowedToDoContest(Contest contest) {
        if (isSupervisorOrAbove(contest)) {
            return true;
        }
        return isAllowedToEnterContest(contest) && hasContestStarted(contest) && !hasContestFinished(contest);
    }

    public Result tryEnteringContest(Contest contest) {
        if (!isAllowedToEnterContest(contest)) {
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

    public void appendTabsLayout(LazyHtml content, Contest contest) {
        final Date contestBeginTime;
        final Date contestEndTime;
        if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.VIRTUAL) && (isContestant(contest))) {
            ContestContestant contestContestant = contestContestantService.findContestantInContestAndJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestVirtualModule contestVirtualModule = (ContestVirtualModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.VIRTUAL);

            long endTime = contestContestant.getContestStartTime() + contestVirtualModule.getVirtualDuration();
            if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.DURATION)) {
                ContestDurationModule contestDurationModule = (ContestDurationModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.DURATION);
                contestBeginTime = contestDurationModule.getBeginTime();
                endTime = Math.min(endTime, contestDurationModule.getBeginTime().getTime() + contestDurationModule.getContestDuration());
            } else {
                contestBeginTime = new Date(contestContestant.getContestStartTime());
            }
            contestEndTime = new Date(endTime);
        } else if (contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.DURATION)) {
            ContestDurationModule contestDurationModule = (ContestDurationModule) contestModuleService.findModuleInContestByType(contest.getJid(), ContestModules.DURATION);
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
        for (ContestModule contestModule : contestModuleService.getModulesInContest(contest.getJid())) {
            if ((contestModule instanceof TabbedContestModule) && (((TabbedContestModule) contestModule).isAllowedToViewTab(ContestControllerUtils.getInstance(), contest, IdentityUtils.getUserJid()))) {
                moduleWithTabs.add((TabbedContestModule) contestModule);
            }
        }
        Collections.sort(moduleWithTabs, new ContestModuleComparator());
        for (TabbedContestModule contestModule : moduleWithTabs) {
            internalLinkBuilder.add(new InternalLink(contestModule.getTabName(), contestModule.getDefaultJumpTo(contest.getId())));
        }

        if (((contestModuleService.contestContainsEnabledModule(contest.getJid(), ContestModules.SUPERVISOR)) && (isSupervisorOrAbove(contest))) || ((UrielControllerUtils.getInstance().isAdmin()) || (isManager(contest)))) {
            internalLinkBuilder.add(new InternalLink(Messages.get("contestant.contestants"), routes.ContestController.jumpToContestants(contest.getId())));
            internalLinkBuilder.add(new InternalLink(Messages.get("manager.managers"), routes.ContestController.jumpToManagers(contest.getId())));
        }

        content.appendLayout(c -> contestTimeLayout.render(contestBeginTime, contestEndTime, c));
        content.appendLayout(c -> tabLayout.render(internalLinkBuilder.build(), c));

        if (isAllowedToManageContest(contest)) {
            content.appendLayout(c -> headingWithActionLayout.render(contest.getName(), new InternalLink(Messages.get("contest.update"), routes.ContestController.updateContestGeneralConfig(contest.getId())), c));
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
