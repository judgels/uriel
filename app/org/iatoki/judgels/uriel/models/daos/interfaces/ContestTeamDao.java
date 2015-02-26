package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.JudgelsDao;
import org.iatoki.judgels.uriel.models.domains.ContestModel;

import java.util.List;

public interface ContestTeamDao extends JudgelsDao<ContestModel> {
    List<ContestModel> getRunningContests(long timeNow);
}
