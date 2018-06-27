package org.iatoki.judgels.uriel.role;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.time.Instant;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AdminRoleModel.class)
public abstract class AdminRoleModel_ {
    public static volatile SingularAttribute<AdminRoleModel, Instant> createdAt;
    public static volatile SingularAttribute<AdminRoleModel, String> createdBy;
    public static volatile SingularAttribute<AdminRoleModel, String> createdIp;
    public static volatile SingularAttribute<AdminRoleModel, Long> id;

    public static volatile SingularAttribute<AdminRoleModel, String> userJid;

}

