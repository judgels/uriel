package org.iatoki.judgels.uriel.contest.grading.programming;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.sandalphon.models.daos.BaseProgrammingGradingDao;

@ImplementedBy(ProgrammingGradingHibernateDao.class)
public interface ProgrammingGradingDao extends BaseProgrammingGradingDao<ProgrammingGradingModel> {

}
