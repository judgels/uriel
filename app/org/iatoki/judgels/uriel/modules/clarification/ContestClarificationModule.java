package org.iatoki.judgels.uriel.modules.clarification;

import com.google.gson.Gson;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.modules.TabbedContestModule;
import org.iatoki.judgels.uriel.views.html.contest.modules.clarificationFormView;
import play.data.Form;
import play.i18n.Messages;
import play.api.mvc.Call;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestClarificationModule implements TabbedContestModule {

    private long clarificationDuration;

    public ContestClarificationModule(long clarificationDuration) {
        this.clarificationDuration = clarificationDuration;
    }

    public long getClarificationDuration() {
        return clarificationDuration;
    }

    @Override
    public ContestModules getType() {
        return ContestModules.CLARIFICATION;
    }

    @Override
    public String getTabName() {
        return Messages.get("clarification.clarifications");
    }

    @Override
    public boolean isAllowedToViewTab(ContestControllerUtils contestControllerUtils, Contest contest, String userJid) {
        return true;
    }

    @Override
    public Call getDefaultJumpTo(long contestId) {
        return org.iatoki.judgels.uriel.controllers.routes.ContestController.jumpToClarifications(contestId);
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
        if (!ContestClarificationConfigForm.class.equals(form.get().getClass())) {
            throw new RuntimeException();
        }

        return clarificationFormView.render((Form<ContestClarificationConfigForm>) form);
    }

    @Override
    public Form<?> generateConfigForm() {
        ContestClarificationConfigForm formData = new ContestClarificationConfigForm();
        formData.clarificationDuration = clarificationDuration;

        return Form.form(ContestClarificationConfigForm.class).fill(formData);
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        Form<ContestClarificationConfigForm> form = Form.form(ContestClarificationConfigForm.class).bindFromRequest(request);
        ContestClarificationConfigForm data = form.get();
        this.clarificationDuration = data.clarificationDuration;

        return form;
    }
}
