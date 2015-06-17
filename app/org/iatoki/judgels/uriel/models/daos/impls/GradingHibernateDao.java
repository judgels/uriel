package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.sandalphon.commons.models.daos.hibernate.AbstractGradingHibernateDao;
import org.iatoki.judgels.uriel.models.daos.GradingDao;
import org.iatoki.judgels.uriel.models.entities.GradingModel;

public final class GradingHibernateDao extends AbstractGradingHibernateDao<GradingModel> implements GradingDao {
    public GradingHibernateDao() {
        super(GradingModel.class);
    }

    @Override
    public GradingModel createGradingModel() {
        return new GradingModel();
    }
}
