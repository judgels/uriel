package org.iatoki.judgels.uriel.controllers.apis;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.gabriel.blackbox.BlackBoxGradingSource;
import org.iatoki.judgels.gabriel.commons.BlackBoxSubmissionAdapter;
import org.iatoki.judgels.gabriel.commons.SubmissionException;
import org.iatoki.judgels.gabriel.commons.SubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.UrielProperties;
import play.Play;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Transactional
public final class ContestTestingAPIController extends Controller {
    private final ContestService contestService;
    private final SubmissionService submissionService;

    public ContestTestingAPIController(ContestService contestService, SubmissionService submissionService) {
        this.contestService = contestService;
        this.submissionService = submissionService;
    }

    public Result singleFileBlackBoxSubmit() {
        Http.MultipartFormData body = request().body().asMultipartFormData();

        Map<String, String[]> form = body.asFormUrlEncoded();

        String testingSecret = form.get("stressTestSecret")[0];

        if (!testingSecret.equals(Play.application().configuration().getString("uriel.stressTestSecret"))) {
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
            adapter.storeSubmissionFiles(UrielProperties.getInstance().getSubmissionDir(), submissionJid, source);
        } catch (SubmissionException e) {
            return badRequest();
        }

        return ok(submissionJid);
    }
}