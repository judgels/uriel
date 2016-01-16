package org.iatoki.judgels.uriel.contest.supervisor;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.Page;

@ImplementedBy(ContestSupervisorServiceImpl.class)
public interface ContestSupervisorService {

    boolean isContestSupervisorInContest(String contestJid, String contestSupervisorJid);

    ContestSupervisor findContestSupervisorInContestByUserJid(String contestJid, String userJid);

    ContestSupervisor findContestSupervisorById(long contestSupervisorId) throws ContestSupervisorNotFoundException;

    Page<ContestSupervisor> getPageOfSupervisorsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void createContestSupervisor(String contestJid, String userJid, ContestPermission contestPermission, String createUserJid, String createUserIpAddress);

    void updateContestSupervisor(long contestSupervisorId, ContestPermission contestPermission, String userJid, String userIpAddress);

    void deleteContestSupervisor(long contestSupervisorId);
}
