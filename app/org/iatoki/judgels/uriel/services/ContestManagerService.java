package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestManager;

public interface ContestManagerService {

    boolean isManagerInContest(String contestJid, String contestManagerJid);

    Page<ContestManager> getPageOfManagersInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void createContestManager(long contestId, String userJid);
}
