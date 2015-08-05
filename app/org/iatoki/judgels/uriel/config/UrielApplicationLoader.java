package org.iatoki.judgels.uriel.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import org.iatoki.judgels.uriel.UrielProperties;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;

public final class UrielApplicationLoader extends GuiceApplicationLoader {

    @Override
    public GuiceApplicationBuilder builder(Context context) {
        org.iatoki.judgels.uriel.BuildInfo$ buildInfo = org.iatoki.judgels.uriel.BuildInfo$.MODULE$;
        JudgelsPlayProperties.buildInstance(buildInfo.name(), buildInfo.version(), ConfigFactory.load());

        Config config = ConfigFactory.load();
        UrielProperties.buildInstance(config);

        return super.builder(context);
    }
}
