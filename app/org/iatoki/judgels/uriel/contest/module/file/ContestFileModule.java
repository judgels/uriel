package org.iatoki.judgels.uriel.contest.module.file;

import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.contest.ContestControllerUtils;
import org.iatoki.judgels.uriel.contest.module.ContestModules;
import org.iatoki.judgels.uriel.contest.module.TabbedContestModule;
import play.api.mvc.Call;
import play.i18n.Messages;

public final class ContestFileModule extends TabbedContestModule {

    @Override
    public ContestModules getType() {
        return ContestModules.FILE;
    }

    @Override
    public String getTabName() {
        return Messages.get("file.files");
    }

    @Override
    public boolean isAllowedToViewTab(ContestControllerUtils contestControllerUtils, Contest contest, String userJid) {
        return contestControllerUtils.isSupervisorOrAbove(contest, userJid);
    }

    @Override
    public Call getDefaultJumpTo(long contestId) {
        return org.iatoki.judgels.uriel.contest.file.routes.ContestFileController.listFiles(contestId);
    }
}
