package org.iatoki.judgels.uriel.contest.module;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.List;

@ImplementedBy(ContestModuleHibernateDao.class)
public interface ContestModuleDao extends Dao<Long, ContestModuleModel> {

    boolean existsEnabledInContestByName(String contestJid, String contestModuleName);

    boolean existsInContestByName(String contestJid, String contestModuleName);

    ContestModuleModel findInContestByName(String contestJid, String contestModuleName);

    List<ContestModuleModel> getEnabledInContest(String contestJid);

    List<ContestModuleModel> getEnabledByName(String contestModuleName);
}
