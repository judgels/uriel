package org.iatoki.judgels.uriel.contest.module.delayedgrading;

import org.iatoki.judgels.uriel.contest.module.ContestModule;
import org.iatoki.judgels.uriel.contest.module.ContestModules;
import org.iatoki.judgels.uriel.contest.module.delayedgrading.html.delayedGradingFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public final class DelayedGradingModule extends ContestModule {
    private long delayDuration;

    public DelayedGradingModule(long delayDuration) {
        this.delayDuration = delayDuration;
    }

    public long getDelayDuration() {
        return delayDuration;
    }

    @Override
    public ContestModules getType() {
        return ContestModules.DELAYED_GRADING;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Html generateConfigFormInput(Form<?> form) {
        if (!DelayedGradingConfigForm.class.equals(form.get().getClass())) {
            throw new RuntimeException();
        }

        return delayedGradingFormView.render((Form<DelayedGradingConfigForm>) form);
    }

    @Override
    public Form<?> generateConfigForm() {
        DelayedGradingConfigForm formData = new DelayedGradingConfigForm();
        formData.delayDuration = delayDuration;

        return Form.form(DelayedGradingConfigForm.class).fill(formData);
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        Form<DelayedGradingConfigForm> form = Form.form(DelayedGradingConfigForm.class).bindFromRequest(request);
        DelayedGradingConfigForm data = form.get();
        this.delayDuration = data.delayDuration;

        return form;
    }
}
