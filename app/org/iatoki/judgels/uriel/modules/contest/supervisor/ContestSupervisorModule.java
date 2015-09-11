package org.iatoki.judgels.uriel.modules.contest.supervisor;

import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.modules.contest.TabbedContestModule;
import org.iatoki.judgels.uriel.controllers.routes;
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
        return routes.ContestController.jumpToSupervisors(contestId);
    }

    @Override
    public ContestModules getType() {
        return ContestModules.SUPERVISOR;
    }
}
