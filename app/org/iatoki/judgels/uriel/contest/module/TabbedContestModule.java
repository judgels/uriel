package org.iatoki.judgels.uriel.contest.module;

import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.contest.ContestControllerUtils;
import play.api.mvc.Call;

public abstract class TabbedContestModule extends ContestModule {

    public abstract String getTabName();

    public abstract boolean isAllowedToViewTab(ContestControllerUtils contestControllerUtils, Contest contest, String userJid);

    public abstract Call getDefaultJumpTo(long contestId);
}
