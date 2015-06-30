package org.iatoki.judgels.uriel.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "org.iatoki.judgels.uriel.controllers",
        "org.iatoki.judgels.jophiel.controllers"
})
public class ControllerConfig {

}
