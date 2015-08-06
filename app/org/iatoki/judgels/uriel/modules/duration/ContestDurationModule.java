package org.iatoki.judgels.uriel.modules.duration;

import com.google.gson.Gson;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.modules.ContestModule;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.views.html.contest.modules.durationFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

import java.util.Date;

public final class ContestDurationModule implements ContestModule {

    private long beginTime;
    private long contestDuration;

    public Date getBeginTime() {
        return new Date(beginTime);
    }

    public long getContestDuration() {
        return contestDuration;
    }

    public Date getEndTime() {
        return new Date(beginTime +contestDuration);
    }

    public ContestDurationModule(Date beginTime, long contestDuration) {
        this.beginTime = beginTime.getTime();
        this.contestDuration = contestDuration;
    }

    @Override
    public ContestModules getType() {
        return ContestModules.DURATION;
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
        if (ContestDurationConfigForm.class.equals(form.get().getClass())) {
            return durationFormView.render((Form<ContestDurationConfigForm>) form);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public Form<?> generateConfigForm() {
        ContestDurationConfigForm formData = new ContestDurationConfigForm();
        formData.beginTime = JudgelsPlayUtils.formatDateTime(beginTime);
        formData.contestDuration = contestDuration;

        return Form.form(ContestDurationConfigForm.class).fill(formData);
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        Form<ContestDurationConfigForm> form = Form.form(ContestDurationConfigForm.class).bindFromRequest(request);
        ContestDurationConfigForm data = form.get();
        this.beginTime = JudgelsPlayUtils.parseDateTime(data.beginTime);
        this.contestDuration = data.contestDuration;

        return form;
    }
}
