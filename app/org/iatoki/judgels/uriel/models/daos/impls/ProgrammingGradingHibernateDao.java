package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.sandalphon.models.daos.impls.AbstractProgrammingGradingHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.uriel.models.entities.ProgrammingGradingModel;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("gradingDao")
public final class ProgrammingGradingHibernateDao extends AbstractProgrammingGradingHibernateDao<ProgrammingGradingModel> implements ProgrammingGradingDao {

    public ProgrammingGradingHibernateDao() {
        super(ProgrammingGradingModel.class);
    }

    @Override
    public ProgrammingGradingModel createGradingModel() {
        return new ProgrammingGradingModel();
    }
}
