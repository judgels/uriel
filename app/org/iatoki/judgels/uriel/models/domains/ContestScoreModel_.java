package org.iatoki.judgels.uriel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestScoreModel.class)
public abstract class ContestScoreModel_ extends AbstractModel_ {
    public static volatile SingularAttribute<ContestScoreModel, Long> id;
    public static volatile SingularAttribute<ContestScoreModel, String> contestJid;
    public static volatile SingularAttribute<ContestScoreModel, String> contestantJid;
    public static volatile SingularAttribute<ContestScoreModel, String> scores;
}
