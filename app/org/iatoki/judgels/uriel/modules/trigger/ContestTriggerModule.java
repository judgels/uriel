package org.iatoki.judgels.uriel.modules.trigger;

import com.google.gson.Gson;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.views.html.contest.modules.triggerFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestTriggerModule implements ContestModule {

    private String contestTrigger;

    public ContestTriggerModule(ContestTrigger contestTrigger) {
        this.contestTrigger = contestTrigger.name();
    }

    public ContestTrigger getContestTrigger() {
        return ContestTrigger.valueOf(contestTrigger);
    }

    @Override
    public ContestModules getType() {
        return ContestModules.TRIGGER;
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
        if (!ContestTriggerConfigForm.class.equals(form.get().getClass())) {
            throw new RuntimeException();
        }

        return triggerFormView.render((Form<ContestTriggerConfigForm>) form);
    }

    @Override
    public Form<?> generateConfigForm() {
        ContestTriggerConfigForm formData = new ContestTriggerConfigForm();
        formData.contestTrigger = contestTrigger;

        return Form.form(ContestTriggerConfigForm.class).fill(formData);
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        Form<ContestTriggerConfigForm> form = Form.form(ContestTriggerConfigForm.class).bindFromRequest(request);
        ContestTriggerConfigForm data = form.get();
        this.contestTrigger = data.contestTrigger;

        return form;
    }
}
