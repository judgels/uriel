package org.iatoki.judgels.uriel.modules.file;

import com.google.gson.Gson;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.routes;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.modules.TabbedContestModule;
import org.iatoki.judgels.uriel.views.html.contest.modules.emptyFormView;
import play.data.Form;
import play.i18n.Messages;
import play.api.mvc.Call;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestFileModule implements TabbedContestModule {

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
        return routes.ContestFileController.listFiles(contestId);
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
