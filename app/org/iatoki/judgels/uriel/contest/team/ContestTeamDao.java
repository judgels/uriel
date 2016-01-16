package org.iatoki.judgels.uriel.contest.team;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.JudgelsDao;

import java.util.Collection;
import java.util.List;

@ImplementedBy(ContestTeamHibernateDao.class)
public interface ContestTeamDao extends JudgelsDao<ContestTeamModel> {

    List<ContestTeamModel> getAllInContest(String contestJid);

    List<String> getJidsInContest(String contestJid);

    List<String> getContestJidsByJids(Collection<String> teamJids);
}
