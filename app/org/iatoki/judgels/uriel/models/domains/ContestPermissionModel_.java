package org.iatoki.judgels.uriel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestPermissionModel.class)
public abstract class ContestPermissionModel_ extends org.iatoki.judgels.commons.models.domains.AbstractModel_ {

	public static volatile SingularAttribute<ContestPermissionModel, String> userJid;
	public static volatile SingularAttribute<ContestPermissionModel, Boolean> problem;
	public static volatile SingularAttribute<ContestPermissionModel, Boolean> contestant;
	public static volatile SingularAttribute<ContestPermissionModel, Boolean> submission;
	public static volatile SingularAttribute<ContestPermissionModel, Boolean> clarification;
	public static volatile SingularAttribute<ContestPermissionModel, Long> id;
	public static volatile SingularAttribute<ContestPermissionModel, String> contestJid;
	public static volatile SingularAttribute<ContestPermissionModel, Boolean> announcement;

}

