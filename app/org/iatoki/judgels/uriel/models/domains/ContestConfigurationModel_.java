package org.iatoki.judgels.uriel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel_;

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
