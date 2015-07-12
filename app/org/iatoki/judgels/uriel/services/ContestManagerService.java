package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestManager;

public interface ContestManagerService {

    boolean isContestManagerInContestByUserJid(String contestJid, String contestManagerJid);

    Page<ContestManager> pageContestManagersByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void createContestManager(long contestId, String userJid);
}
