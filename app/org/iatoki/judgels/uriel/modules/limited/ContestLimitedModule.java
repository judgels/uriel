package org.iatoki.judgels.uriel.modules.limited;

import com.google.gson.Gson;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.views.html.contest.modules.emptyFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestLimitedModule implements ContestModule {

    @Override
    public ContestModules getType() {
        return ContestModules.LIMITED;
    }

    @Override
    public void updateModuleBasedOnStyleChange(ContestStyle contestStyle) {

    }

    @Override
    public String toJSONString() {
        return new Gson().toJson(this);
    }

    @Override
    public Html generateConfigFormInput(Form<?> form) {
        return emptyFormView.render();
    }

    @Override
    public Form<?> generateConfigForm() {
        return Form.form();
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        return Form.form().bindFromRequest(request);
    }
}
