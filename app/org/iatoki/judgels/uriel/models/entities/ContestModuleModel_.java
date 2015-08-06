package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContestModuleModel.class)
public abstract class ContestModuleModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<ContestModuleModel, Long> id;
        public static volatile SingularAttribute<ContestModuleModel, String> contestJid;
        public static volatile SingularAttribute<ContestModuleModel, String> name;
        public static volatile SingularAttribute<ContestModuleModel, String> config;
        public static volatile SingularAttribute<ContestModuleModel, String> enabled;
}
