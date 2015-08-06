package org.iatoki.judgels.uriel.modules.virtual;

import com.google.gson.Gson;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.views.html.contest.modules.virtualFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestVirtualModule implements ContestModule {

    private long virtualDuration;

    public long getVirtualDuration() {
        return virtualDuration;
    }

    public ContestVirtualModule(long virtualDuration) {
        this.virtualDuration = virtualDuration;
    }

    @Override
    public ContestModules getType() {
        return ContestModules.VIRTUAL;
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
        if (ContestVirtualConfigForm.class.equals(form.get().getClass())) {
            return virtualFormView.render((Form<ContestVirtualConfigForm>) form);
        } else {
            throw new RuntimeException();
        }
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
