package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.commons.models.domains.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestScoreboardModel.class)
public abstract class ContestScoreboardModel_  extends AbstractModel_ {
    public static volatile SingularAttribute<ContestScoreboardModel, Long> id;
    public static volatile SingularAttribute<ContestScoreboardModel, String> contestJid;
    public static volatile SingularAttribute<ContestScoreboardModel, String> type;
    public static volatile SingularAttribute<ContestScoreboardModel, String> scoreboard;
}
