package org.iatoki.judgels.uriel.controllers.apis;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingSource;
import org.iatoki.judgels.sandalphon.BlackBoxSubmissionAdapter;
import org.iatoki.judgels.sandalphon.SubmissionException;
import org.iatoki.judgels.sandalphon.services.SubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.config.SubmissionLocalFile;
import org.iatoki.judgels.uriel.services.ContestService;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@Singleton
@Named
public final class ContestTestingAPIController extends Controller {

    private final ContestService contestService;
    private final SubmissionService submissionService;
    private final FileSystemProvider submissionLocalFileSystemProvider;

    @Inject
    public ContestTestingAPIController(ContestService contestService, SubmissionService submissionService, @SubmissionLocalFile FileSystemProvider submissionLocalFileSystemProvider) {
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

        String contestJid = form.get("contestJid")[0];
        Contest contest = contestService.findContestByJid(contestJid);

        String userJid = form.get("userJid")[0];
        String problemJid = form.get("problemJid")[0];
        String language = form.get("problemLanguage")[0];
        String engine = form.get("problemEngine")[0];

        Http.MultipartFormData.FilePart filePart = body.getFile("source");

        String filename = filePart.getFilename();
        File file = filePart.getFile();

        String fileContent;
        try {
            fileContent = FileUtils.readFileToString(file);
        } catch (IOException e) {
            return badRequest();
        }

        BlackBoxSubmissionAdapter adapter = new BlackBoxSubmissionAdapter();

        String submissionJid;
        try {
            BlackBoxGradingSource source = (BlackBoxGradingSource) adapter.createBlackBoxGradingSourceFromNewSubmission(language, ImmutableList.of("source"), ImmutableMap.of("source", filename), ImmutableMap.of("source", fileContent));
            submissionJid = submissionService.submit(problemJid, contest.getJid(), engine, language, null, source, userJid, "localhost");
            adapter.storeSubmissionFiles(submissionLocalFileSystemProvider, null, submissionJid, source);
        } catch (SubmissionException e) {
            return badRequest();
        }

        return ok(submissionJid);
    }
}
