package org.iatoki.judgels.uriel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestClarificationModel.class)
public abstract class ContestClarificationModel_ extends org.iatoki.judgels.commons.models.domains.AbstractModel_ {

	public static volatile SingularAttribute<ContestClarificationModel, Long> id;
	public static volatile SingularAttribute<ContestClarificationModel, String> contestJid;
	public static volatile SingularAttribute<ContestClarificationModel, String> topicJid;
	public static volatile SingularAttribute<ContestClarificationModel, String> question;
	public static volatile SingularAttribute<ContestClarificationModel, String> answer;
	public static volatile SingularAttribute<ContestClarificationModel, String> status;

}

