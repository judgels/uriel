package org.iatoki.judgels.uriel.contest.module;

import com.google.gson.Gson;
import org.iatoki.judgels.uriel.contest.style.ContestStyle;
import org.iatoki.judgels.uriel.contest.module.html.emptyFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public abstract class ContestModule {

    public abstract ContestModules getType();

    public void updateModuleBasedOnStyleChange(ContestStyle contestStyle) {
    }

    public String toJSONString() {
        return new Gson().toJson(this);
    }

    public Html generateConfigFormInput(Form<?> form) {
        return emptyFormView.render();
    }

    public Form<?> generateConfigForm() {
        return Form.form();
    }

    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        return Form.form().bindFromRequest(request);
    }
}
