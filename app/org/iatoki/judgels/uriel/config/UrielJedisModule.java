package org.iatoki.judgels.uriel.config;

import org.iatoki.judgels.uriel.UrielModule;

public final class UrielJedisModule extends UrielModule {

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.uriel.models.daos.jedishibernate";
    }
}
