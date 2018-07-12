package org.iatoki.judgels.uriel.contest.grading.programming;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.time.Instant;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ProgrammingDelayedGradingModel.class)
public abstract class AdminRoleModel_ {
    public static volatile SingularAttribute<ProgrammingDelayedGradingModel, Instant> createdAt;
    public static volatile SingularAttribute<ProgrammingDelayedGradingModel, String> createdBy;
    public static volatile SingularAttribute<ProgrammingDelayedGradingModel, String> createdIp;
    public static volatile SingularAttribute<ProgrammingDelayedGradingModel, Long> id;

    public static volatile SingularAttribute<ProgrammingDelayedGradingModel, String> userJid;

}

