package org.iatoki.judgels.uriel.controllers.api.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.gabriel.SubmissionSource;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.sandalphon.problem.programming.submission.ProgrammingSubmissionException;
import org.iatoki.judgels.sandalphon.problem.programming.submission.ProgrammingSubmissionUtils;
import org.iatoki.judgels.sandalphon.problem.programming.submission.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.contest.Contest;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.contest.submission.programming.ProgrammingSubmissionLocalFileSystemProvider;
import org.iatoki.judgels.uriel.contest.ContestService;
import play.db.jpa.Transactional;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

@Singleton
public final class TestingContestSubmissionAPIController extends AbstractJudgelsAPIController {

    private final ContestService contestService;
    private final ProgrammingSubmissionService submissionService;
    private final FileSystemProvider submissionLocalFileSystemProvider;

    @Inject
    public TestingContestSubmissionAPIController(ContestService contestService, ProgrammingSubmissionService submissionService, @ProgrammingSubmissionLocalFileSystemProvider FileSystemProvider submissionLocalFileSystemProvider) {
        this.contestService = contestService;
        this.submissionService = submissionService;
        this.submissionLocalFileSystemProvider = submissionLocalFileSystemProvider;
    }

    @Transactional
    public Result singleFileBlackBoxSubmit() {
        Http.MultipartFormData body = request().body().asMultipartFormData();

        Map<String, String[]> form = body.asFormUrlEncoded();

        String testingSecret = form.get("stressTestSecret")[0];

        if (!testingSecret.equals(UrielProperties.getInstance().getUrielStressTestSecret())) {
            return notFound();
        }

        String contestJid = form.get("containerJid")[0];
        Contest contest = contestService.findContestByJid(contestJid);

        String userJid = form.get("userJid")[0];
        String problemJid = form.get("problemJid")[0];
        String language = form.get("problemLanguage")[0];
        String engine = form.get("problemEngine")[0];

        String submissionJid;
        try {
            SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromNewSubmission(new Http.MultipartFormData() {
                @Override
                public Map<String, String[]> asFormUrlEncoded() {
                    return ImmutableMap.of("language", new String[]{language}, "sourceFileFieldKeys", new String[]{"source"});
                }

                @Override
                public List<FilePart> getFiles() {
                    return ImmutableList.of(body.getFile("source"));
                }
            });
            submissionJid = submissionService.submit(problemJid, contest.getJid(), engine, language, null, submissionSource, userJid, "localhost");
            ProgrammingSubmissionUtils.storeSubmissionFiles(submissionLocalFileSystemProvider, null, submissionJid, submissionSource);
        } catch (ProgrammingSubmissionException e) {
            return badRequest();
        }

        return ok();
    }
}
