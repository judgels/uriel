package org.iatoki.judgels.uriel;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.SubmissionAdapters;
import org.iatoki.judgels.gabriel.FakeClientMessage;
import org.iatoki.judgels.gabriel.FakeSealtiel;
import org.iatoki.judgels.gabriel.GradingLanguageRegistry;
import org.iatoki.judgels.gabriel.GradingRequest;
import org.iatoki.judgels.gabriel.GradingSource;
import org.iatoki.judgels.gabriel.Verdict;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestSubmissionDao;
import org.iatoki.judgels.uriel.models.domains.ContestSubmissionModel;

import java.util.List;

public final class ContestSubmissionServiceImpl implements ContestSubmissionService {
    private final ContestSubmissionDao submissionDao;
    private final FakeSealtiel sealtiel;

    public ContestSubmissionServiceImpl(ContestSubmissionDao submissionDao, FakeSealtiel sealtiel) {
        this.submissionDao = submissionDao;
        this.sealtiel = sealtiel;
    }

    @Override
    public Page<ContestSubmission> pageSubmission(long page, long pageSize, String sortBy, String order, String contestJid, String problemJid, String authorJid) {
        long totalPage = submissionDao.countByFilters(contestJid, problemJid, authorJid);

        List<ContestSubmissionModel> submissionRecords = submissionDao.findByFilters(contestJid, problemJid, authorJid);
        List<ContestSubmission> submissions = Lists.transform(submissionRecords, record -> createSubmissionFromModel(record, contestJid));

        return new Page<>(submissions, totalPage, page, pageSize);
    }

    @Override
    public ContestSubmission findSubmissionById(String contestJid, long submissionId) {
        ContestSubmissionModel submissionRecord = submissionDao.findById(submissionId);
        return createSubmissionFromModel(submissionRecord, contestJid);
    }

    @Override
    public String submit(String contestJid, String problemJid, String problemGradingEngine, String gradingLanguage, long gradingLastUpdateTime, GradingSource source) {
        ContestSubmissionModel submissionRecord = new ContestSubmissionModel();
        submissionRecord.problemJid = problemJid;
        submissionRecord.contestJid = contestJid;
        submissionRecord.gradingLanguage = gradingLanguage;
        submissionRecord.gradingEngine = problemGradingEngine;
        submissionRecord.verdictCode = "?";
        submissionRecord.verdictName = "Pending";
        submissionRecord.score = 0;
        submissionRecord.details = "";

        submissionDao.persist(submissionRecord, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        GradingRequest request = SubmissionAdapters.fromGradingEngine(problemGradingEngine).createGradingRequest(submissionRecord.jid, problemJid, gradingLastUpdateTime, problemGradingEngine, gradingLanguage, source);

        FakeClientMessage message = new FakeClientMessage("some-target", request.getClass().getSimpleName(), new Gson().toJson(request));
        sealtiel.sendMessage(message);

        return submissionRecord.jid;
    }

    private ContestSubmission createSubmissionFromModel(ContestSubmissionModel record, String contestJid) {
        String language = GradingLanguageRegistry.getInstance().getLanguage(record.gradingLanguage).getName();
        return new ContestSubmission(record.id, record.jid, record.problemJid, contestJid, record.userCreate, language, record.gradingEngine, record.timeCreate, new Verdict(record.verdictCode, record.verdictName), record.score, record.details);
    }
}
