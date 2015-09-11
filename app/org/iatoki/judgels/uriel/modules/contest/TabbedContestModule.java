package org.iatoki.judgels.uriel.modules.contest;

import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import play.api.mvc.Call;

public abstract class TabbedContestModule extends ContestModule {

    public abstract String getTabName();

    public abstract boolean isAllowedToViewTab(ContestControllerUtils contestControllerUtils, Contest contest, String userJid);

    public abstract Call getDefaultJumpTo(long contestId);
}
