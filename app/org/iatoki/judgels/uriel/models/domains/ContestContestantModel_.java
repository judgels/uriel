package org.iatoki.judgels.uriel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestContestantModel.class)
public abstract class ContestContestantModel_ extends org.iatoki.judgels.commons.models.domains.AbstractModel_ {

    public static volatile SingularAttribute<ContestContestantModel, Long> id;
	public static volatile SingularAttribute<ContestContestantModel, String> userJid;
	public static volatile SingularAttribute<ContestContestantModel, String> contestJid;
	public static volatile SingularAttribute<ContestContestantModel, String> status;
    public static volatile SingularAttribute<ContestContestantModel, Long> contestStartTime;
}

