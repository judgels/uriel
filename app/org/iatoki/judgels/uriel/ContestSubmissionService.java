package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.gabriel.GradingSource;

public interface ContestSubmissionService {

    Page<ContestSubmission> pageSubmission(long page, long pageSize, String sortBy, String order, String contestJid, String problemJid, String authorJid);

    ContestSubmission findSubmissionById(String contestJid, long submissionId);

    String submit(String contestJid, String problemJid, String problemGradingEngine, String gradingLanguage, long gradingLastUpdateTime, GradingSource source);
}
