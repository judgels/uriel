package org.iatoki.judgels.uriel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestTeamMemberModel.class)
public abstract class ContestTeamCoachModel_ extends AbstractModel_ {
    public static volatile SingularAttribute<ContestTeamMemberModel, Long> id;
    public static volatile SingularAttribute<ContestTeamMemberModel, String> contestJid;
    public static volatile SingularAttribute<ContestTeamMemberModel, String> memberJid;
}
