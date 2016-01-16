package org.iatoki.judgels.uriel.contest.module.scoreboard;

import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.contest.ContestControllerUtils;
import org.iatoki.judgels.uriel.contest.module.ContestModules;
import org.iatoki.judgels.uriel.contest.module.TabbedContestModule;
import org.iatoki.judgels.uriel.contest.module.scoreboard.html.scoreboardFormView;
import play.api.mvc.Call;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestScoreboardModule extends TabbedContestModule {

    private boolean isIncognitoScoreboard;

    public ContestScoreboardModule(boolean isIncognitoScoreboard) {
        this.isIncognitoScoreboard = isIncognitoScoreboard;
    }

    public boolean isIncognitoScoreboard() {
        return isIncognitoScoreboard;
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
        return org.iatoki.judgels.uriel.contest.routes.ContestController.jumpToScoreboards(contestId);
    }

    @Override
    public ContestModules getType() {
        return ContestModules.SCOREBOARD;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Html generateConfigFormInput(Form<?> form) {
        if (!ContestScoreboardConfigForm.class.equals(form.get().getClass())) {
            throw new RuntimeException();
        }

        return scoreboardFormView.render((Form<ContestScoreboardConfigForm>) form);
    }

    @Override
    public Form<?> generateConfigForm() {
        ContestScoreboardConfigForm formData = new ContestScoreboardConfigForm();
        formData.isIncognitoScoreboard = isIncognitoScoreboard;

        return Form.form(ContestScoreboardConfigForm.class).fill(formData);
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        Form<ContestScoreboardConfigForm> form = Form.form(ContestScoreboardConfigForm.class).bindFromRequest(request);
        ContestScoreboardConfigForm data = form.get();
        this.isIncognitoScoreboard = data.isIncognitoScoreboard;

        return form;
    }
}
