package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.JudgelsDao;
import org.iatoki.judgels.uriel.models.domains.ContestModel;

import java.util.List;
import java.util.Map;

public interface ContestDao extends JudgelsDao<ContestModel> {
    List<ContestModel> getRunningContests(long timeNow);

    long countAllowedContests(String filterString, String userJid, boolean isAdmin);

    List<ContestModel> findSortedAllowedContestsByFilters(String orderBy, String orderDir, String filterString, String userJid, boolean isAdmin, long offset, long limit);

}
