package org.iatoki.judgels.uriel.models.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserReadModel.class)
public abstract class UserReadModel_ extends org.iatoki.judgels.play.models.entities.AbstractModel_ {

    public static volatile SingularAttribute<UserReadModel, Long> id;
    public static volatile SingularAttribute<UserReadModel, String> userJid;
	public static volatile SingularAttribute<UserReadModel, String> type;
    public static volatile SingularAttribute<UserReadModel, String> readJid;
}

