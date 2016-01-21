package org.iatoki.judgels.uriel.contest.submission.programming;

import org.iatoki.judgels.api.sealtiel.SealtielClientAPI;
import org.iatoki.judgels.sandalphon.problem.programming.submission.ProgrammingSubmissionService;
import org.iatoki.judgels.sandalphon.problem.programming.submission.AbstractProgrammingSubmissionServiceImpl;
import org.iatoki.judgels.uriel.contest.grading.programming.ProgrammingGradingDao;
import org.iatoki.judgels.uriel.contest.grading.programming.ProgrammingGradingModel;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ProgrammingSubmissionServiceImpl extends AbstractProgrammingSubmissionServiceImpl<ProgrammingSubmissionModel, ProgrammingGradingModel> implements ProgrammingSubmissionService {

    @Inject
    public ProgrammingSubmissionServiceImpl(ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, SealtielClientAPI sealtielClientAPI, @GabrielClientJid String gabrielClientJid) {
        super(programmingSubmissionDao, programmingGradingDao, sealtielClientAPI, gabrielClientJid);
    }
}
