package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.ContestProblemNotFoundException;
import org.iatoki.judgels.uriel.ContestProblemStatus;

import java.util.List;
import java.util.Map;

public interface ContestProblemService {

    boolean isContestProblemInContestByProblemJidOrAlias(String contestJid, String contestProblemJid, String contestProblemAlias);

    ContestProblem findContestProblemByContestProblemId(long contestProblemId) throws ContestProblemNotFoundException;

    ContestProblem findContestProblemByContestJidAndContestProblemJid(String contestJid, String contestProblemJid);

    List<ContestProblem> findOpenedContestProblemByContestJid(String contestJid);

    Page<ContestProblem> pageContestProblemsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status);

    Page<ContestProblem> pageUsedContestProblemsByContestJid(String contestJid, long pageIndex, long pageSize);

    Map<String, String> findProblemJidToAliasMapByContestJid(String contestJid);

    void createContestProblem(long contestId, String problemJid, String problemSecret, String alias, long submissionsLimit, ContestProblemStatus status);

    void updateContestProblem(long contestProblemId, String alias, long submissionsLimit, ContestProblemStatus status);
}
