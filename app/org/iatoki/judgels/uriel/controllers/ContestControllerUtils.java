package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.views.html.contest.contestTimeLayout;
import play.i18n.Messages;

import java.util.Date;

public final class ContestControllerUtils {

    private static ContestControllerUtils INSTANCE = new ContestControllerUtils();

    public void appendTabsLayout(LazyHtml content, Contest contest, boolean isSupervisorOrAbove, Date contestEndTime) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("announcement.announcements"), routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("problem.problems"), routes.ContestProblemController.viewOpenedProblems(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("submission.submissions"), routes.ContestSubmissionController.viewScreenedSubmissions(contest.getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("clarification.clarifications"), routes.ContestClarificationController.viewScreenedClarifications(contest.getId())));
        if (contest.isUsingScoreboard()) {
            internalLinkBuilder.add(new InternalLink(Messages.get("scoreboard.scoreboard"), routes.ContestScoreboardController.viewScoreboard(contest.getId())));
        }
        if (isSupervisorOrAbove) {
            internalLinkBuilder.add(new InternalLink(Messages.get("contestant.contestants"), routes.ContestContestantController.viewContestants(contest.getId())));
            internalLinkBuilder.add(new InternalLink(Messages.get("supervisor.supervisors"), routes.ContestSupervisorController.viewSupervisors(contest.getId())));
            internalLinkBuilder.add(new InternalLink(Messages.get("manager.managers"), routes.ContestManagerController.viewManagers(contest.getId())));
        }
        content.appendLayout(c -> contestTimeLayout.render(contest.getStartTime(), contestEndTime, c));
        content.appendLayout(c -> tabLayout.render(internalLinkBuilder.build(), c));
        content.appendLayout(c -> headingLayout.render(contest.getName(), c));
    }

    static ContestControllerUtils getInstance() {
        return INSTANCE;
    }
}
