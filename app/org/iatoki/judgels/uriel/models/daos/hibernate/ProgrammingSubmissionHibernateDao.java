package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.sandalphon.models.daos.hibernate.AbstractProgrammingSubmissionHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.uriel.models.entities.ProgrammingSubmissionModel;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("programmingSubmissionDao")
public final class ProgrammingSubmissionHibernateDao extends AbstractProgrammingSubmissionHibernateDao<ProgrammingSubmissionModel> implements ProgrammingSubmissionDao {

    public ProgrammingSubmissionHibernateDao() {
        super(ProgrammingSubmissionModel.class);
    }

    @Override
    public ProgrammingSubmissionModel createSubmissionModel() {
        return new ProgrammingSubmissionModel();
    }
}
