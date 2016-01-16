package org.iatoki.judgels.uriel.contest.module.registration;

import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.uriel.contest.module.ContestModule;
import org.iatoki.judgels.uriel.contest.module.ContestModules;
import org.iatoki.judgels.uriel.contest.module.registration.html.registrationFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestRegistrationModule extends ContestModule {

    private long registerStartTime;
    private boolean manualApproval;
    private long registerDuration;
    private long maxRegistrants;

    public ContestRegistrationModule(long registerStartTime, boolean manualApproval, long registerDuration, long maxRegistrants) {
        this.registerStartTime = registerStartTime;
        this.manualApproval = manualApproval;
        this.registerDuration = registerDuration;
        this.maxRegistrants = maxRegistrants;
    }

    public long getRegisterStartTime() {
        return registerStartTime;
    }

    public boolean isManualApproval() {
        return manualApproval;
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
        formData.manualApproval = manualApproval;
        formData.registerDuration = registerDuration;
        formData.maxRegistrants = maxRegistrants;

        return Form.form(ContestRegistrationConfigForm.class).fill(formData);
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        Form<ContestRegistrationConfigForm> form = Form.form(ContestRegistrationConfigForm.class).bindFromRequest(request);
        ContestRegistrationConfigForm data = form.get();
        this.registerStartTime = JudgelsPlayUtils.parseDateTime(data.registerStartTime);
        this.manualApproval = data.manualApproval;
        this.registerDuration = data.registerDuration;
        this.maxRegistrants = data.maxRegistrants;

        return form;
    }
}
