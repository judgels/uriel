package org.iatoki.judgels.uriel.contest.supervisor;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestSupervisorModel.class)
public abstract class ContestSupervisorModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<ContestSupervisorModel, Long> id;
	public static volatile SingularAttribute<ContestSupervisorModel, String> contestJid;
	public static volatile SingularAttribute<ContestSupervisorModel, String> userJid;
	public static volatile SingularAttribute<ContestSupervisorModel, String> permission;
}

