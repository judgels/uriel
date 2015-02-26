package org.iatoki.judgels.uriel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractJudgelsModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestTeamModel.class)
public abstract class ContestTeamModel_ extends AbstractJudgelsModel_ {
    public static volatile SingularAttribute<ContestTeamModel, Long> id;
    public static volatile SingularAttribute<ContestTeamModel, String> contestJid;
    public static volatile SingularAttribute<ContestTeamModel, String> name;
    public static volatile SingularAttribute<ContestTeamModel, String> teamImageName;
}
