package org.iatoki.judgels.uriel.modules.team;

import com.google.gson.Gson;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.modules.TabbedContestModule;
import org.iatoki.judgels.uriel.views.html.contest.modules.emptyFormView;
import play.data.Form;
import play.i18n.Messages;
import play.api.mvc.Call;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestTeamModule implements TabbedContestModule {

    @Override
    public ContestModules getType() {
        return ContestModules.TEAM;
    }

    @Override
    public String getTabName() {
        return Messages.get("team.teams");
    }

    @Override
    public boolean isAllowedToViewTab(ContestControllerUtils contestControllerUtils, Contest contest, String userJid) {
        return contestControllerUtils.isSupervisorOrAbove(contest, userJid);
    }

    @Override
    public Call getDefaultJumpTo(long contestId) {
        return org.iatoki.judgels.uriel.controllers.routes.ContestController.jumpToTeams(contestId);
    }

    @Override
    public void updateModuleBasedOnStyleChange(ContestStyle contestStyle) {

    }

    @Override
    public String toJSONString() {
        return new Gson().toJson(this);
    }

    @Override
    public Html generateConfigFormInput(Form<?> form) {
        return emptyFormView.render();
    }

    @Override
    public Form<?> generateConfigForm() {
        return Form.form();
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        return Form.form().bindFromRequest(request);
    }
}
