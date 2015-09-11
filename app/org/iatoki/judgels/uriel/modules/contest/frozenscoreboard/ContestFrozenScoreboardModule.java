package org.iatoki.judgels.uriel.modules.contest.frozenscoreboard;

import org.iatoki.judgels.uriel.modules.contest.ContestModule;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.views.html.contest.modules.frozenScoreboardFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestFrozenScoreboardModule extends ContestModule {

    private long scoreboardFreezeTime;

    public ContestFrozenScoreboardModule(long scoreboardFreezeTime) {
        this.scoreboardFreezeTime = scoreboardFreezeTime;
    }

    public long getScoreboardFreezeTime() {
        return scoreboardFreezeTime;
    }

    @Override
    public ContestModules getType() {
        return ContestModules.FROZEN_SCOREBOARD;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Html generateConfigFormInput(Form<?> form) {
        if (!ContestFrozenScoreboardConfigForm.class.equals(form.get().getClass())) {
            throw new RuntimeException();
        }

        return frozenScoreboardFormView.render((Form<ContestFrozenScoreboardConfigForm>) form);
    }

    @Override
    public Form<?> generateConfigForm() {
        ContestFrozenScoreboardConfigForm formData = new ContestFrozenScoreboardConfigForm();
        formData.scoreboardFreezeTime = scoreboardFreezeTime;

        return Form.form(ContestFrozenScoreboardConfigForm.class).fill(formData);
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        Form<ContestFrozenScoreboardConfigForm> form = Form.form(ContestFrozenScoreboardConfigForm.class).bindFromRequest(request);
        ContestFrozenScoreboardConfigForm data = form.get();
        this.scoreboardFreezeTime = data.scoreboardFreezeTime;

        return form;
    }
}
