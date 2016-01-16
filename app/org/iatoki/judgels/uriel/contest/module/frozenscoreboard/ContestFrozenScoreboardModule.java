package org.iatoki.judgels.uriel.contest.module.frozenscoreboard;

import org.iatoki.judgels.uriel.contest.module.ContestModule;
import org.iatoki.judgels.uriel.contest.module.ContestModules;
import org.iatoki.judgels.uriel.contest.module.frozenscoreboard.html.frozenScoreboardFormView;
import play.data.Form;
import play.mvc.Http;
import play.twirl.api.Html;

public final class ContestFrozenScoreboardModule extends ContestModule {

    private boolean isOfficialScoreboardAllowed;
    private long scoreboardFreezeTime;

    public ContestFrozenScoreboardModule(boolean isOfficialScoreboardAllowed, long scoreboardFreezeTime) {
        this.isOfficialScoreboardAllowed = isOfficialScoreboardAllowed;
        this.scoreboardFreezeTime = scoreboardFreezeTime;
    }

    public boolean isOfficialScoreboardAllowed() {
        return isOfficialScoreboardAllowed;
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
        formData.isOfficialScoreboardAllowed = isOfficialScoreboardAllowed;
        formData.scoreboardFreezeTime = scoreboardFreezeTime;

        return Form.form(ContestFrozenScoreboardConfigForm.class).fill(formData);
    }

    @Override
    public Form<?> updateModuleByFormFromRequest(Http.Request request) {
        Form<ContestFrozenScoreboardConfigForm> form = Form.form(ContestFrozenScoreboardConfigForm.class).bindFromRequest(request);
        ContestFrozenScoreboardConfigForm data = form.get();
        this.isOfficialScoreboardAllowed = data.isOfficialScoreboardAllowed;
        this.scoreboardFreezeTime = data.scoreboardFreezeTime;

        return form;
    }
}
