package org.iatoki.judgels.uriel.models.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestModel.class)
public abstract class ContestModel_ extends org.iatoki.judgels.play.models.domains.AbstractJudgelsModel_ {

	public static volatile SingularAttribute<ContestModel, String> scope;
	public static volatile SingularAttribute<ContestModel, String> name;
	public static volatile SingularAttribute<ContestModel, String> description;
	public static volatile SingularAttribute<ContestModel, String> style;
	public static volatile SingularAttribute<ContestModel, Long> startTime;
	public static volatile SingularAttribute<ContestModel, Long> endTime;
	public static volatile SingularAttribute<ContestModel, String> type;
    public static volatile SingularAttribute<ContestModel, Long> clarificationEndTime;
    public static volatile SingularAttribute<ContestModel, Boolean> isExclusive;
    public static volatile SingularAttribute<ContestModel, Boolean> isUsingScoreboard;
    public static volatile SingularAttribute<ContestModel, Boolean> isIncognitoScoreboard;
    public static volatile SingularAttribute<ContestModel, Boolean> requiresPassword;

}

