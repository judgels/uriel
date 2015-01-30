package org.iatoki.judgels.uriel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SubmissionModel.class)
public abstract class SubmissionModel_ extends org.iatoki.judgels.commons.models.domains.AbstractJudgelsModel_ {

	public static volatile SingularAttribute<SubmissionModel, String> userJid;
	public static volatile SingularAttribute<SubmissionModel, Long> score;
	public static volatile SingularAttribute<SubmissionModel, String> gradingResult;
	public static volatile SingularAttribute<SubmissionModel, String> verdict;
	public static volatile SingularAttribute<SubmissionModel, String> language;
	public static volatile SingularAttribute<SubmissionModel, String> problemJid;
	public static volatile SingularAttribute<SubmissionModel, String> contestJid;

}

