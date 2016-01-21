package org.iatoki.judgels.uriel.contest.grading.programming;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.sandalphon.problem.programming.grading.BaseProgrammingGradingDao;

@ImplementedBy(ProgrammingGradingHibernateDao.class)
public interface ProgrammingGradingDao extends BaseProgrammingGradingDao<ProgrammingGradingModel> {

}
