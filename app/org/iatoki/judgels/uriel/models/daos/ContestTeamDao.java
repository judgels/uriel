package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.JudgelsDao;
import org.iatoki.judgels.uriel.models.entities.ContestTeamModel;

import java.util.Collection;
import java.util.List;

public interface ContestTeamDao extends JudgelsDao<ContestTeamModel> {

    List<ContestTeamModel> getAllInContest(String contestJid);

    List<String> getJidsInContest(String contestJid);

    List<String> getContestJidsByJids(Collection<String> teamJids);
}
