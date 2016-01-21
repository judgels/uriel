package org.iatoki.judgels.uriel.contest.grading.programming;

import org.iatoki.judgels.sandalphon.problem.programming.grading.AbstractProgrammingGradingHibernateDao;

import javax.inject.Singleton;

@Singleton
public final class ProgrammingGradingHibernateDao extends AbstractProgrammingGradingHibernateDao<ProgrammingGradingModel> implements ProgrammingGradingDao {

    public ProgrammingGradingHibernateDao() {
        super(ProgrammingGradingModel.class);
    }

    @Override
    public ProgrammingGradingModel createGradingModel() {
        return new ProgrammingGradingModel();
    }
}
