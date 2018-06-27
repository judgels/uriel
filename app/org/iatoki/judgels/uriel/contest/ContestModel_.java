package org.iatoki.judgels.uriel.contest;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.util.Date;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestModel.class)
public abstract class ContestModel_ extends org.iatoki.judgels.play.model.AbstractJudgelsModel_ {

	public static volatile SingularAttribute<ContestModel, String> name;
	public static volatile SingularAttribute<ContestModel, String> description;
	public static volatile SingularAttribute<ContestModel, String> style;
	public static volatile SingularAttribute<ContestModel, Boolean> locked;
	public static volatile SingularAttribute<ContestModel, Long> duration;
	public static volatile SingularAttribute<ContestModel, Date> beginTime;
}

