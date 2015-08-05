package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestConfigurationModel.class)
public abstract class ContestConfigurationModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<ContestConfigurationModel, Long> id;
    public static volatile SingularAttribute<ContestConfigurationModel, String> contestJid;
	public static volatile SingularAttribute<ContestConfigurationModel, String> typeConfig;
    public static volatile SingularAttribute<ContestConfigurationModel, String> scopeConfig;
	public static volatile SingularAttribute<ContestConfigurationModel, String> styleConfig;

}

