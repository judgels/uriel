package org.iatoki.judgels.uriel.controllers;

import org.iatoki.judgels.FileInfo;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.forms.ContestFileUploadForm;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.services.ContestFileService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.views.html.contest.file.listFilesView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public final class ContestFileController extends AbstractJudgelsController {

    private final ContestService contestService;
    private final ContestFileService contestFileService;

    @Inject
    public ContestFileController(ContestService contestService, ContestFileService contestFileService) {
        this.contestService = contestService;
        this.contestFileService = contestFileService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewFiles(long contestId) throws ContestNotFoundException {
        return listFiles(contestId);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listFiles(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToManageFiles(contest)) {
            Form<ContestFileUploadForm> form = Form.form(ContestFileUploadForm.class);
            List<FileInfo> fileInfos = contestFileService.getContestFiles(contest.getJid());
            return showListFiles(form, contest, fileInfos);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadFile(long contestId) throws ContestNotFoundException{
        Contest contest = contestService.findContestById(contestId);
        if (isAllowedToManageFiles(contest)) {
            Form<ContestFileUploadForm> form = Form.form(ContestFileUploadForm.class).bindFromRequest();
            if (form.hasErrors() || form.hasGlobalErrors()) {
                List<FileInfo> fileInfos = contestFileService.getContestFiles(contest.getJid());
                return showListFiles(form, contest, fileInfos);
            } else {
                Http.MultipartFormData body = request().body().asMultipartFormData();
                Http.MultipartFormData.FilePart file;

                file = body.getFile("file");
                if (file != null) {
                    File contestFile = file.getFile();
                    try {
                        contestFileService.uploadContestFile(contest.getJid(), contestFile, file.getFilename());
                        return redirect(routes.ContestFileController.viewFiles(contest.getId()));
                    } catch (IOException e) {
                        form.reject("file.cannotUploadFile");
                        List<FileInfo> fileInfos = contestFileService.getContestFiles(contest.getJid());
                        return showListFiles(form, contest, fileInfos);
                    }
                }
                List<FileInfo> fileInfos = contestFileService.getContestFiles(contest.getJid());
                return showListFiles(form, contest, fileInfos);
            }
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional(readOnly = true)
    public Result downloadFile(long contestId, String filename, String any) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (ContestControllerUtils.getInstance().isAllowedToEnterContest(contest)) {
            String fileURL = contestFileService.getContestFileURL(contest.getJid(), filename);
            try {
                new URL(fileURL);
                return redirect(fileURL);
            } catch (MalformedURLException e) {
                File file = new File(fileURL);
                if (!file.exists()) {
                    return Results.notFound();
                }
                Controller.response().setContentType("application/x-download");
                Controller.response().setHeader("Content-disposition", "attachment; filename=" + file.getName());
                return ok(file);
            }
        } else {
            return notFound();
        }
    }

    private Result showListFiles(Form<ContestFileUploadForm> form, Contest contest, List<FileInfo> fileInfos) {
        LazyHtml content = new LazyHtml(listFilesView.render(form, contest, fileInfos));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("file.list"), routes.ContestFileController.viewFiles(contest.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Files - List");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("file.files"), routes.ContestController.jumpToFiles(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToManageFiles(Contest contest) {
        return ContestControllerUtils.getInstance().isSupervisorOrAbove(contest);
    }
}
