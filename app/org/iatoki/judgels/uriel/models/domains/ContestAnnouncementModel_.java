package org.iatoki.judgels.uriel.models.domains;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestAnnouncementModel.class)
public abstract class ContestAnnouncementModel_ extends org.iatoki.judgels.commons.models.domains.AbstractModel_ {

	public static volatile SingularAttribute<ContestAnnouncementModel, Long> id;
	public static volatile SingularAttribute<ContestAnnouncementModel, String> contestJid;
	public static volatile SingularAttribute<ContestAnnouncementModel, String> title;
	public static volatile SingularAttribute<ContestAnnouncementModel, String> announcement;
	public static volatile SingularAttribute<ContestAnnouncementModel, String> status;

}

