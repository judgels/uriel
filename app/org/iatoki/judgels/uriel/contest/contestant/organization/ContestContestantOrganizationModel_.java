package org.iatoki.judgels.uriel.contest.contestant.organization;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestContestantOrganizationModel.class)
public abstract class ContestContestantOrganizationModel_ extends AbstractModel_{

    public static volatile SingularAttribute<ContestContestantOrganizationModel, Long> id;
    public static volatile SingularAttribute<ContestContestantOrganizationModel, String> contestJid;
    public static volatile SingularAttribute<ContestContestantOrganizationModel, String> userJid;
    public static volatile SingularAttribute<ContestContestantOrganizationModel, String> organization;
}
