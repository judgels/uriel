package org.iatoki.judgels.uriel.contest.announcement;

import org.iatoki.judgels.play.model.AbstractJudgelsModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestAnnouncementModel.class)
public abstract class ContestAnnouncementModel_ extends AbstractJudgelsModel_ {

	public static volatile SingularAttribute<ContestAnnouncementModel, String> contestJid;
	public static volatile SingularAttribute<ContestAnnouncementModel, String> title;
	public static volatile SingularAttribute<ContestAnnouncementModel, String> content;
	public static volatile SingularAttribute<ContestAnnouncementModel, String> status;
}

