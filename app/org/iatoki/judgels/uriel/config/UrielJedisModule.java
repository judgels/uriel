package org.iatoki.judgels.uriel.config;

public final class UrielJedisModule extends UrielModule {

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.uriel.models.daos.jedishibernate";
    }
}
