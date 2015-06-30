package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.sandalphon.models.daos.impls.AbstractSubmissionHibernateDao;
import org.iatoki.judgels.uriel.models.daos.SubmissionDao;
import org.iatoki.judgels.uriel.models.entities.SubmissionModel;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("submissionDao")
public final class SubmissionHibernateDao extends AbstractSubmissionHibernateDao<SubmissionModel> implements SubmissionDao {

    public SubmissionHibernateDao() {
        super(SubmissionModel.class);
    }

    @Override
    public SubmissionModel createSubmissionModel() {
        return new SubmissionModel();
    }

}