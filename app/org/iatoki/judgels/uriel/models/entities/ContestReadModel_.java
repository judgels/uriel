package org.iatoki.judgels.uriel.models.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestReadModel.class)
public abstract class ContestReadModel_ extends org.iatoki.judgels.play.models.domains.AbstractModel_ {

    public static volatile SingularAttribute<ContestReadModel, Long> id;
    public static volatile SingularAttribute<ContestReadModel, String> userJid;
	public static volatile SingularAttribute<ContestReadModel, Long> readId;
	public static volatile SingularAttribute<ContestReadModel, String> type;

}

