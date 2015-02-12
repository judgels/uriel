package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestSubmissionDao;
import org.iatoki.judgels.uriel.models.domains.ContestSubmissionModel;

public final class ContestSubmissionHibernateDao extends AbstractJudgelsHibernateDao<ContestSubmissionModel> implements ContestSubmissionDao {

    public ContestSubmissionHibernateDao() {
        super(ContestSubmissionModel.class);
    }
}
