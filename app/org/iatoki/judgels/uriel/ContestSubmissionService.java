package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.gabriel.GradingSource;

public interface ContestSubmissionService {

    Page<ContestSubmission> pageContestSubmissionsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String authorJid, String problemJid);

    ContestSubmission findContestSubmissionById(String contestJid, long contestSubmissionId);

    String submit(String contestJid, String problemJid, String gradingLanguage, String problemGradingEngine, long gradingLastUpdateTime, GradingSource source);
}
