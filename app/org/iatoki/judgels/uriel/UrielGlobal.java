package org.iatoki.judgels.uriel;

import org.iatoki.judgels.play.AbstractJudgelsGlobal;
import play.Application;

public final class UrielGlobal extends AbstractJudgelsGlobal {

    @Override
    public void onStart(Application application) {
        super.onStart(application);

        application.injector().instanceOf(UrielThreadsScheduler.class);
    }
}
