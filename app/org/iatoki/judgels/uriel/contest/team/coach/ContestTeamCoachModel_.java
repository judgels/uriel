package org.iatoki.judgels.uriel.contest.team.coach;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestTeamCoachModel.class)
public abstract class ContestTeamCoachModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<ContestTeamCoachModel, Long> id;
    public static volatile SingularAttribute<ContestTeamCoachModel, String> teamJid;
    public static volatile SingularAttribute<ContestTeamCoachModel, String> coachJid;
}
