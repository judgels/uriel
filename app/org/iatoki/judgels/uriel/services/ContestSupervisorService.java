package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestPermission;
import org.iatoki.judgels.uriel.ContestSupervisor;
import org.iatoki.judgels.uriel.ContestSupervisorNotFoundException;

public interface ContestSupervisorService {

    boolean isContestSupervisorInContest(String contestJid, String contestSupervisorJid);

    ContestSupervisor findContestSupervisorInContestByUserJid(String contestJid, String userJid);

    ContestSupervisor findContestSupervisorById(long contestSupervisorId) throws ContestSupervisorNotFoundException;

    Page<ContestSupervisor> getPageOfSupervisorsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void createContestSupervisor(long contestId, String userJid, ContestPermission contestPermission);

    void updateContestSupervisor(long contestSupervisorId, ContestPermission contestPermission);
}
