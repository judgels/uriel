package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.JudgelsDao;
import org.iatoki.judgels.uriel.models.entities.ContestModel;

import java.util.Collection;
import java.util.List;

public interface ContestDao extends JudgelsDao<ContestModel> {
    List<ContestModel> getRunningContests(long timeNow);

    List<ContestModel> getRunningContestsWithinContestJids(long timeNow, Collection<String> contestJids);

    long countContestsWithinContestJidsOrIsRunningPublic(String filterString, Collection<String> contestJids, long timeNow);

    List<ContestModel> findSortedContestsWithinContestJidsOrIsRunningPublicByFilters(String orderBy, String orderDir, String filterString, Collection<String> contestJids, long offset, long limit, long timeNow);

}
