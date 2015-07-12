package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestSupervisor;
import org.iatoki.judgels.uriel.ContestSupervisorNotFoundException;

public interface ContestSupervisorService {

    boolean isContestSupervisorInContestByUserJid(String contestJid, String contestSupervisorJid);

    ContestSupervisor findContestSupervisorByContestJidAndUserJid(String contestJid, String userJid);

    ContestSupervisor findContestSupervisorByContestSupervisorId(long contestSupervisorId) throws ContestSupervisorNotFoundException;

    Page<ContestSupervisor> pageContestSupervisorsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void createContestSupervisor(long contestId, String userJid, boolean announcement, boolean problem, boolean submission, boolean clarification, boolean contestant);

    void updateContestSupervisor(long contestSupervisorId, boolean announcement, boolean problem, boolean submission, boolean clarification, boolean contestant);
}
