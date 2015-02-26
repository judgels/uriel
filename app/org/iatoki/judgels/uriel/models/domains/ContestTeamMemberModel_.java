package org.iatoki.judgels.uriel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestScoreboardModel.class)
public abstract class ContestTeamMemberModel_ extends AbstractModel_ {
    public static volatile SingularAttribute<ContestScoreboardModel, Long> id;
    public static volatile SingularAttribute<ContestScoreboardModel, String> contestJid;
    public static volatile SingularAttribute<ContestScoreboardModel, String> name;
    public static volatile SingularAttribute<ContestScoreboardModel, String> teamImageName;
}
