package org.iatoki.judgels.uriel.contest.contestant.password;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestContestantPasswordModel.class)
public abstract class ContestContestantPasswordModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<ContestContestantPasswordModel, Long> id;
    public static volatile SingularAttribute<ContestContestantPasswordModel, String> contestJid;
    public static volatile SingularAttribute<ContestContestantPasswordModel, String> contestantJid;
    public static volatile SingularAttribute<ContestContestantPasswordModel, String> password;
}
