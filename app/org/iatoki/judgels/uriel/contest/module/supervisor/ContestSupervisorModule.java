package org.iatoki.judgels.uriel.contest.module.supervisor;

import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.contest.ContestControllerUtils;
import org.iatoki.judgels.uriel.contest.module.ContestModules;
import org.iatoki.judgels.uriel.contest.module.TabbedContestModule;
import play.api.mvc.Call;
import play.i18n.Messages;

public final class ContestSupervisorModule extends TabbedContestModule {

    @Override
    public String getTabName() {
        return Messages.get("supervisor.supervisors");
    }

    @Override
    public boolean isAllowedToViewTab(ContestControllerUtils contestControllerUtils, Contest contest, String userJid) {
        return contestControllerUtils.isSupervisorOrAbove(contest, userJid);
    }

    @Override
    public Call getDefaultJumpTo(long contestId) {
        return org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToSupervisors(contestId);
    }

    @Override
    public ContestModules getType() {
        return ContestModules.SUPERVISOR;
    }
}
