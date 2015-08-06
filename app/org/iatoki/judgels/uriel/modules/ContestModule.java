package org.iatoki.judgels.uriel.modules;

import org.iatoki.judgels.uriel.ContestStyle;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public interface ContestModule {

    ContestModules getType();

    void updateModuleBasedOnStyleChange(ContestStyle contestStyle);

    String toJSONString();

    Html generateConfigFormInput(Form<?> form);

    Form<?> generateConfigForm();

    Form<?> updateModuleByFormFromRequest(Http.Request request);
}
