package org.iatoki.judgels.uriel.contest.problem;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestProblemModel.class)
public abstract class ContestProblemModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<ContestProblemModel, Long> id;
	public static volatile SingularAttribute<ContestProblemModel, Long> submissionsLimit;
	public static volatile SingularAttribute<ContestProblemModel, String> contestJid;
	public static volatile SingularAttribute<ContestProblemModel, String> problemJid;
	public static volatile SingularAttribute<ContestProblemModel, String> problemSecret;
	public static volatile SingularAttribute<ContestProblemModel, String> alias;
	public static volatile SingularAttribute<ContestProblemModel, String> status;
}

