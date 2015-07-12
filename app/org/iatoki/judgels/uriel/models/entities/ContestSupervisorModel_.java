package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestSupervisorModel.class)
public abstract class ContestSupervisorModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<ContestSupervisorModel, String> userJid;
	public static volatile SingularAttribute<ContestSupervisorModel, Boolean> problem;
	public static volatile SingularAttribute<ContestSupervisorModel, Boolean> contestant;
	public static volatile SingularAttribute<ContestSupervisorModel, Boolean> submission;
	public static volatile SingularAttribute<ContestSupervisorModel, Boolean> clarification;
	public static volatile SingularAttribute<ContestSupervisorModel, Long> id;
	public static volatile SingularAttribute<ContestSupervisorModel, String> contestJid;
	public static volatile SingularAttribute<ContestSupervisorModel, Boolean> announcement;

}

