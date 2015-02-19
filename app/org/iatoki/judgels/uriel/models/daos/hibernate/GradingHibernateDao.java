package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.gabriel.commons.models.daos.hibernate.AbstractGradingHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.GradingDao;
import org.iatoki.judgels.uriel.models.domains.GradingModel;

public final class GradingHibernateDao extends AbstractGradingHibernateDao<GradingModel> implements GradingDao {
    public GradingHibernateDao() {
        super(GradingModel.class);
    }

    @Override
    public GradingModel createGradingModel() {
        return new GradingModel();
    }
}
