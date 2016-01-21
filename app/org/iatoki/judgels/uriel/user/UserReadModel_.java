package org.iatoki.judgels.uriel.user;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserReadModel.class)
public abstract class UserReadModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<UserReadModel, Long> id;
    public static volatile SingularAttribute<UserReadModel, String> userJid;
	public static volatile SingularAttribute<UserReadModel, String> type;
    public static volatile SingularAttribute<UserReadModel, String> readJid;
}

