package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestClarificationDao;
import org.iatoki.judgels.uriel.models.domains.ContestClarificationModel;

public final class ContestClarificationHibernateDao extends AbstractHibernateDao<Long, ContestClarificationModel> implements ContestClarificationDao {
    public ContestClarificationHibernateDao() {
        super(ContestClarificationModel.class);
    }
}
