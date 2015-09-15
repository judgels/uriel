package org.iatoki.judgels.uriel.controllers.api.internal;

import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.apis.JudgelsAPINotFoundException;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.controllers.ContestControllerUtils;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.services.ContestFileService;
import org.iatoki.judgels.uriel.services.ContestService;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class InternalContestFileAPIController extends AbstractJudgelsAPIController {

    private final ContestService contestService;
    private final ContestFileService contestFileService;

    @Inject
    public InternalContestFileAPIController(ContestService contestService, ContestFileService contestFileService) {
        this.contestService = contestService;
        this.contestFileService = contestFileService;
    }

    @Transactional(readOnly = true)
    public Result downloadFile(long contestId, String filename, String any) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid())) {
            throw new JudgelsAPINotFoundException();
        }

        String fileUrl = contestFileService.getContestFileURL(contest.getJid(), filename);
        return okAsDownload(fileUrl);
    }
}
