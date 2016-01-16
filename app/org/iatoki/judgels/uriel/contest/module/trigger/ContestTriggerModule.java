package org.iatoki.judgels.uriel.contest.module.trigger;

import org.iatoki.judgels.uriel.contest.module.ContestModule;
import org.iatoki.judgels.uriel.contest.module.ContestModules;
import org.iatoki.judgels.uriel.contest.module.trigger.html.triggerFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestTriggerModule extends ContestModule {

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
