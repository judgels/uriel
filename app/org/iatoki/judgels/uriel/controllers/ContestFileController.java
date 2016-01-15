package org.iatoki.judgels.uriel.controllers;

import org.iatoki.judgels.FileInfo;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.UrielControllerUtils;
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
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public final class ContestFileController extends AbstractJudgelsController {

    private final ContestFileService contestFileService;
    private final ContestService contestService;

    @Inject
    public ContestFileController(ContestFileService contestFileService, ContestService contestService) {
        this.contestFileService = contestFileService;
        this.contestService = contestService;
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
        if (!isAllowedToManageFiles(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestFileUploadForm> contestFileUploadForm = Form.form(ContestFileUploadForm.class);
        List<FileInfo> fileInfos = contestFileService.getContestFiles(contest.getJid());
        return showListFiles(contestFileUploadForm, contest, fileInfos);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadFile(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !isAllowedToManageFiles(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Form<ContestFileUploadForm> contestFileUploadForm = Form.form(ContestFileUploadForm.class).bindFromRequest();
        if (formHasErrors(contestFileUploadForm)) {
            List<FileInfo> fileInfos = contestFileService.getContestFiles(contest.getJid());
            return showListFiles(contestFileUploadForm, contest, fileInfos);
        }

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        file = body.getFile("file");
        if (file != null) {
            File contestFile = file.getFile();
            try {
                contestFileService.uploadContestFile(contest.getJid(), contestFile, file.getFilename());
                return redirect(routes.ContestFileController.viewFiles(contest.getId()));
            } catch (IOException e) {
                contestFileUploadForm.reject("file.cannotUploadFile");
                List<FileInfo> fileInfos = contestFileService.getContestFiles(contest.getJid());
                return showListFiles(contestFileUploadForm, contest, fileInfos);
            }
        }

        List<FileInfo> fileInfos = contestFileService.getContestFiles(contest.getJid());
        return showListFiles(contestFileUploadForm, contest, fileInfos);
    }

    private Result showListFiles(Form<ContestFileUploadForm> contestFileUploadForm, Contest contest, List<FileInfo> fileInfos) {
        LazyHtml content = new LazyHtml(listFilesView.render(contestFileUploadForm, contest, fileInfos));
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("file.list"), routes.ContestFileController.viewFiles(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Files - List");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("file.files"), routes.ContestController.jumpToFiles(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToManageFiles(Contest contest) {
        return ContestControllerUtils.getInstance().isPermittedToSupervise(contest, ContestPermissions.FILE, IdentityUtils.getUserJid());
    }
}
