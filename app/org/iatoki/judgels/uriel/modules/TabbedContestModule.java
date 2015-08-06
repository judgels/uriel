package org.iatoki.judgels.uriel.modules;

import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import play.api.mvc.Call;

public interface TabbedContestModule extends ContestModule {

    String getTabName();

    boolean isAllowedToViewTab(ContestControllerUtils contestControllerUtils, Contest contest, String userJid);

    Call getDefaultJumpTo(long contestId);
}
