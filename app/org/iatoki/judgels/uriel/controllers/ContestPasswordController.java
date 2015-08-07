package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.Lists;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantNotFoundException;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.modules.ContestModules;
import org.iatoki.judgels.uriel.services.ContestContestantPasswordService;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestModuleService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.ContestSupervisorService;
import org.iatoki.judgels.uriel.views.html.contest.contestant.password.listContestantPasswordsView;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Map;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public final class ContestPasswordController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 1000;

    private final ContestService contestService;
    private final ContestContestantService contestContestantService;
    private final ContestContestantPasswordService contestContestantPasswordService;
    private final ContestModuleService contestModuleService;
    private final ContestSupervisorService contestSupervisorService;

    @Inject
    public ContestPasswordController(ContestService contestService, ContestContestantService contestContestantService, ContestContestantPasswordService contestContestantPasswordService, ContestModuleService contestModuleService, ContestSupervisorService contestSupervisorService) {
        this.contestService = contestService;
        this.contestContestantService = contestContestantService;
        this.contestContestantPasswordService = contestContestantPasswordService;
        this.contestModuleService = contestModuleService;
        this.contestSupervisorService = contestSupervisorService;
    }

    @Transactional(readOnly = true)
    public Result viewContestantPasswords(long contestId) throws ContestNotFoundException {
        return listContestantPasswords(contestId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listContestantPasswords(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (contestModuleService.containEnabledModule(contest.getJid(), ContestModules.PASSWORD) && isAllowedToSuperviseContestants(contest)) {
            Page<ContestContestant> contestContestants = contestContestantService.pageContestContestantsByContestJid(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            Map<String, String> passwordsMap = contestContestantPasswordService.getContestantPasswordsMap(contest.getJid(), Lists.transform(contestContestants.getData(), c -> c.getUserJid()));

            return showListContestantPasswords(contestContestants, pageIndex, orderBy, orderDir, filterString, passwordsMap, contest);
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    public Result generateContestantPasswords(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (contestModuleService.containEnabledModule(contest.getJid(), ContestModules.PASSWORD) && isAllowedToSuperviseContestants(contest)) {
            contestContestantPasswordService.generateContestantPasswordForAllContestants(contest.getJid());
            return redirect(routes.ContestPasswordController.viewContestantPasswords(contest.getId()));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    @Transactional
    public Result generateContestantPassword(long contestId, long contestContestantId) throws ContestNotFoundException, ContestContestantNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestant = contestContestantService.findContestContestantByContestContestantId(contestContestantId);

        if (contestModuleService.containEnabledModule(contest.getJid(), ContestModules.PASSWORD) && isAllowedToSuperviseContestants(contest)) {
            contestContestantPasswordService.generateContestantPassword(contest.getJid(), contestant.getUserJid());
            return redirect(routes.ContestPasswordController.viewContestantPasswords(contest.getId()));
        } else {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest);
        }
    }

    private Result showListContestantPasswords(Page<ContestContestant> contestContestants, long pageIndex, String orderBy, String orderDir, String filterString, Map<String, String> passwordsMap, Contest contest) {
        LazyHtml content = new LazyHtml(listContestantPasswordsView.render(contest.getId(), contestContestants, pageIndex, orderBy, orderDir, filterString, passwordsMap));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.passwords"), c));

        ContestControllerUtils.getInstance().appendTabsLayout(content, contest);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("contestant.passwords"), routes.ContestPasswordController.viewContestantPasswords(contest.getId()))
        );
        ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Contestants");

        ControllerUtils.getInstance().addActivityLog("Open list of contestants in contest " + contest.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("contestant.passwords"), routes.ContestController.jumpToContestants(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseContestants(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || ContestControllerUtils.getInstance().isManager(contest) || (ContestControllerUtils.getInstance().isSupervisor(contest) && contestSupervisorService.findContestSupervisorByContestJidAndUserJid(contest.getJid(), IdentityUtils.getUserJid()).getContestPermission().isAllowed(ContestPermissions.CONTESTANT));
    }
}
