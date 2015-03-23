package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.gabriel.commons.models.daos.hibernate.AbstractSubmissionHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.SubmissionDao;
import org.iatoki.judgels.uriel.models.domains.SubmissionModel;

import java.util.List;

public final class SubmissionHibernateDao extends AbstractSubmissionHibernateDao<SubmissionModel> implements SubmissionDao {
    public SubmissionHibernateDao() {
        super(SubmissionModel.class);
    }

    @Override
    public SubmissionModel createSubmissionModel() {
        return new SubmissionModel();
    }

}