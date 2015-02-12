package org.iatoki.judgels.uriel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestProblemModel.class)
public abstract class ContestProblemModel_ extends org.iatoki.judgels.commons.models.domains.AbstractModel_ {

    public static volatile SingularAttribute<ContestProblemModel, Long> id;
	public static volatile SingularAttribute<ContestProblemModel, Long> submissionsLimit;
	public static volatile SingularAttribute<ContestProblemModel, String> contestJid;
	public static volatile SingularAttribute<ContestProblemModel, String> problemJid;
	public static volatile SingularAttribute<ContestProblemModel, String> problemSecret;
	public static volatile SingularAttribute<ContestProblemModel, String> alias;
	public static volatile SingularAttribute<ContestProblemModel, String> status;

}

