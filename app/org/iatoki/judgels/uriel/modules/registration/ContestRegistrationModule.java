package org.iatoki.judgels.uriel.modules.registration;

import com.google.gson.Gson;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.views.html.contest.modules.registrationFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

import java.util.Date;

public final class ContestRegistrationModule implements ContestModule {

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
    public void updateModuleBasedOnStyleChange(ContestStyle contestStyle) {

    }

    @Override
    public String toJSONString() {
        return new Gson().toJson(this);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Html generateConfigFormInput(Form<?> form) {
        if (ContestRegistrationConfigForm.class.equals(form.get().getClass())) {
            return registrationFormView.render((Form<ContestRegistrationConfigForm>) form);
        } else {
            throw new RuntimeException();
        }
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
