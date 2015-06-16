package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.commons.models.domains.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestManagerModel.class)
public abstract class ContestManagerModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<ContestManagerModel, Long> id;
	public static volatile SingularAttribute<ContestManagerModel, String> userJid;
	public static volatile SingularAttribute<ContestManagerModel, String> contestJid;

}

