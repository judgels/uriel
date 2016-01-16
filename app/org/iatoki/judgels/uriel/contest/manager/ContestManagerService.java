package org.iatoki.judgels.uriel.contest.manager;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.Page;

@ImplementedBy(ContestManagerServiceImpl.class)
public interface ContestManagerService {

    boolean isManagerInContest(String contestJid, String contestManagerJid);

    ContestManager findContestManagerById(long contestManagerId) throws ContestManagerNotFoundException;

    Page<ContestManager> getPageOfManagersInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void createContestManager(String contestJid, String userJid, String createUserJid, String createUserIpAddress);

    void deleteContestManager(long contestManagerId);
}
