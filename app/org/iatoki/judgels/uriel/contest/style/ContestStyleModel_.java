package org.iatoki.judgels.uriel.contest.style;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestStyleModel.class)
public abstract class ContestStyleModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<ContestStyleModel, Long> id;
    public static volatile SingularAttribute<ContestStyleModel, String> contestJid;
    public static volatile SingularAttribute<ContestStyleModel, String> style;
    public static volatile SingularAttribute<ContestStyleModel, String> config;
}
