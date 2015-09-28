package org.iatoki.judgels.uriel.modules.contest.registration;

import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.uriel.modules.contest.ContestModule;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.views.html.contest.modules.registrationFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

import java.util.Date;

public final class ContestRegistrationModule extends ContestModule {

    private long registerStartTime;
    private long registerDuration;
    private long maxRegistrants;

    public ContestRegistrationModule(Date registerStartTime, long registerDuration, long maxRegistrants) {
        this.registerStartTime = registerStartTime.getTime();
        this.registerDuration = registerDuration;
        this.maxRegistrants = maxRegistrants;
    }

    public long getRegisterStartTime() {
        return registerStartTime;
    }

    public long getRegisterDuration() {
        return registerDuration;
    }

    public long getMaxRegistrants() {
        return maxRegistrants;
    }

    @Override
    public ContestModules getType() {
        return ContestModules.REGISTRATION;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Html generateConfigFormInput(Form<?> form) {
        if (!ContestRegistrationConfigForm.class.equals(form.get().getClass())) {
            throw new RuntimeException();
        }

        return registrationFormView.render((Form<ContestRegistrationConfigForm>) form);
    }

    @Override
    public Form<?> generateConfigForm() {
        ContestRegistrationConfigForm formData = new ContestRegistrationConfigForm();
        formData.registerStartTime = JudgelsPlayUtils.formatDateTime(registerStartTime);
        formData.registerDuration = registerDuration;
        formData.maxRegistrants = maxRegistrants;

        return Form.form(ContestRegistrationConfigForm.class).fill(formData);
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        Form<ContestRegistrationConfigForm> form = Form.form(ContestRegistrationConfigForm.class).bindFromRequest(request);
        ContestRegistrationConfigForm data = form.get();
        this.registerStartTime = JudgelsPlayUtils.parseDateTime(data.registerStartTime);
        this.registerDuration = data.registerDuration;
        this.maxRegistrants = data.maxRegistrants;

        return form;
    }
}