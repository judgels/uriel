package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestDao;
import org.iatoki.judgels.uriel.models.domains.ContestModel;

public final class ContestHibernateDao extends AbstractJudgelsHibernateDao<ContestModel> implements ContestDao {
    public ContestHibernateDao() {
        super(ContestModel.class);
    }
}
