package org.iatoki.judgels.uriel.contest.module.javaspecification;


import org.iatoki.judgels.uriel.contest.module.ContestModule;
import org.iatoki.judgels.uriel.contest.module.ContestModules;

public final class ContestJavaSpecificationModule extends ContestModule {

    public ContestJavaSpecificationModule() {
    }

    @Override
    public ContestModules getType() {
        return ContestModules.JAVA_SPECIFICATION;
    }
}
