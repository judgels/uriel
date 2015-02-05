package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.SubmissionUpdaterService;
import org.iatoki.judgels.gabriel.GradingResult;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestSubmissionDao;
import org.iatoki.judgels.uriel.models.domains.ContestSubmissionModel;

public class SubmissionUpdaterServiceImpl implements SubmissionUpdaterService {

    private ContestSubmissionDao dao;

    public SubmissionUpdaterServiceImpl(ContestSubmissionDao dao) {
        this.dao = dao;
    }

    @Override
    public void updateResult(String submissionJid, GradingResult result) {
        ContestSubmissionModel submissionRecord = dao.findByJid(submissionJid);
        submissionRecord.verdictCode = result.getVerdict().getCode();
        submissionRecord.verdictName = result.getVerdict().getName();
        submissionRecord.score = result.getScore();
        submissionRecord.details = result.getDetailsAsJson();

        dao.edit(submissionRecord, "Grader", "Grader's IP");
    }
}
