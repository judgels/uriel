package org.iatoki.judgels.uriel.modules.contest.virtual;

import org.iatoki.judgels.uriel.modules.contest.ContestModule;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.views.html.contest.modules.virtualFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestVirtualModule extends ContestModule {

    private long virtualDuration;

    public ContestVirtualModule(long virtualDuration) {
        this.virtualDuration = virtualDuration;
    }

    public long getVirtualDuration() {
        return virtualDuration;
    }

    @Override
    public ContestModules getType() {
        return ContestModules.VIRTUAL;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Html generateConfigFormInput(Form<?> form) {
        if (!ContestVirtualConfigForm.class.equals(form.get().getClass())) {
            throw new RuntimeException();
        }

        return virtualFormView.render((Form<ContestVirtualConfigForm>) form);
    }

    @Override
    public Form<?> generateConfigForm() {
        ContestVirtualConfigForm formData = new ContestVirtualConfigForm();
        formData.virtualDuration = virtualDuration;

        return Form.form(ContestVirtualConfigForm.class).fill(formData);
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        Form<ContestVirtualConfigForm> form = Form.form(ContestVirtualConfigForm.class).bindFromRequest(request);
        ContestVirtualConfigForm data = form.get();
        this.virtualDuration = data.virtualDuration;

        return form;
    }
}
