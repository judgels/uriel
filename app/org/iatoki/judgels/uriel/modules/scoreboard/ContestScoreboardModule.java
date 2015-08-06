package org.iatoki.judgels.uriel.modules.scoreboard;

import com.google.gson.Gson;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.modules.TabbedContestModule;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.views.html.contest.modules.scoreboardFormView;
import play.data.Form;
import play.i18n.Messages;
import play.api.mvc.Call;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestScoreboardModule implements TabbedContestModule {

    private boolean isOfficialScoreboardAllowed;
    private boolean isIncognitoScoreboard;
    private long scoreboardFreezeTime;

    public boolean isOfficialScoreboardAllowed() {
        return isOfficialScoreboardAllowed;
    }

    public boolean isIncognitoScoreboard() {
        return isIncognitoScoreboard;
    }

    public long getScoreboardFreezeTime() {
        return scoreboardFreezeTime;
    }

    public ContestScoreboardModule(boolean isOfficialScoreboardAllowed, boolean isIncognitoScoreboard, long scoreboardFreezeTime) {
        this.isOfficialScoreboardAllowed = isOfficialScoreboardAllowed;
        this.isIncognitoScoreboard = isIncognitoScoreboard;
        this.scoreboardFreezeTime = scoreboardFreezeTime;
    }

    @Override
    public String getTabName() {
        return Messages.get("scoreboard.scoreboard");
    }

    @Override
    public boolean isAllowedToViewTab(ContestControllerUtils contestControllerUtils, Contest contest, String userJid) {
        return true;
    }

    @Override
    public Call getDefaultJumpTo(long contestId) {
        return org.iatoki.judgels.uriel.controllers.routes.ContestController.jumpToScoreboard(contestId);
    }

    @Override
    public ContestModules getType() {
        return ContestModules.SCOREBOARD;
    }

    @Override
    public void updateModuleBasedOnStyleChange(ContestStyle contestStyle) {

    }

    @Override
    public String toJSONString() {
        return new Gson().toJson(this);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Html generateConfigFormInput(Form<?> form) {
        if (ContestScoreboardConfigForm.class.equals(form.get().getClass())) {
            return scoreboardFormView.render((Form<ContestScoreboardConfigForm>) form);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public Form<?> generateConfigForm() {
        ContestScoreboardConfigForm formData = new ContestScoreboardConfigForm();
        formData.isIncognitoScoreboard = isIncognitoScoreboard;
        formData.isOfficialScoreboardAllowed = isOfficialScoreboardAllowed;
        formData.scoreboardFreezeTime = scoreboardFreezeTime;

        return Form.form(ContestScoreboardConfigForm.class).fill(formData);
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        Form<ContestScoreboardConfigForm> form = Form.form(ContestScoreboardConfigForm.class).bindFromRequest(request);
        ContestScoreboardConfigForm data = form.get();
        this.isOfficialScoreboardAllowed = data.isOfficialScoreboardAllowed;
        this.isIncognitoScoreboard = data.isIncognitoScoreboard;
        this.scoreboardFreezeTime = data.scoreboardFreezeTime;

        return form;
    }
}
