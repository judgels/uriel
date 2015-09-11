package org.iatoki.judgels.uriel.modules.contest.clarificationtimelimit;

import org.iatoki.judgels.uriel.modules.contest.ContestModule;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.views.html.contest.modules.clarificationTimeLimitFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestClarificationTimeLimitModule extends ContestModule {

    private long clarificationDuration;

    public ContestClarificationTimeLimitModule(long clarificationDuration) {
        this.clarificationDuration = clarificationDuration;
    }

    public long getClarificationDuration() {
        return clarificationDuration;
    }

    @Override
    public ContestModules getType() {
        return ContestModules.CLARIFICATION_TIME_LIMIT;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Html generateConfigFormInput(Form<?> form) {
        if (!ContestClarificationTimeLimitConfigForm.class.equals(form.get().getClass())) {
            throw new RuntimeException();
        }

        return clarificationTimeLimitFormView.render((Form<ContestClarificationTimeLimitConfigForm>) form);
    }

    @Override
    public Form<?> generateConfigForm() {
        ContestClarificationTimeLimitConfigForm formData = new ContestClarificationTimeLimitConfigForm();
        formData.clarificationDuration = clarificationDuration;

        return Form.form(ContestClarificationTimeLimitConfigForm.class).fill(formData);
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        Form<ContestClarificationTimeLimitConfigForm> form = Form.form(ContestClarificationTimeLimitConfigForm.class).bindFromRequest(request);
        ContestClarificationTimeLimitConfigForm data = form.get();
        this.clarificationDuration = data.clarificationDuration;

        return form;
    }
}
