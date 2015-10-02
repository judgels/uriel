package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.ContestProblemNotFoundException;
import org.iatoki.judgels.uriel.ContestProblemStatus;

import java.util.List;
import java.util.Map;

public interface ContestProblemService {

    boolean isProblemInContestByJidOrAlias(String contestJid, String contestProblemJid, String contestProblemAlias);

    ContestProblem findContestProblemById(long contestProblemId) throws ContestProblemNotFoundException;

    ContestProblem findContestProblemInContestAndJid(String contestJid, String contestProblemJid);

    List<ContestProblem> getProblemsInContest(String contestJid);

    List<ContestProblem> getOpenedProblemsInContest(String contestJid);

    Page<ContestProblem> getPageOfProblemsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status);

    Page<ContestProblem> getPageOfUsedProblemsInContest(String contestJid, long pageIndex, long pageSize);

    Map<String, String> getMappedJidToAliasInContest(String contestJid);

    void createContestProblem(String contestJid, String problemJid, String problemSecret, String alias, long submissionsLimit, ContestProblemStatus status, String userJid, String userIpAddress);

    void updateContestProblem(long contestProblemId, String alias, long submissionsLimit, ContestProblemStatus status, String userJid, String userIpAddress);

    void deleteContestProblem(long contestProblemId);
}
