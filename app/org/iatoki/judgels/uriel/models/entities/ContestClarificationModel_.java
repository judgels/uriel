package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestClarificationModel.class)
public abstract class ContestClarificationModel_ extends AbstractJudgelsModel_ {

	public static volatile SingularAttribute<ContestClarificationModel, String> clarificationJid;
    public static volatile SingularAttribute<ContestClarificationModel, String> contestJid;
	public static volatile SingularAttribute<ContestClarificationModel, String> topicJid;
    public static volatile SingularAttribute<ContestClarificationModel, String> title;
	public static volatile SingularAttribute<ContestClarificationModel, String> question;
	public static volatile SingularAttribute<ContestClarificationModel, String> answer;
	public static volatile SingularAttribute<ContestClarificationModel, String> status;
}

