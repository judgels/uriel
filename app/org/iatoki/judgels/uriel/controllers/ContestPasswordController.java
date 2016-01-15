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
import org.iatoki.judgels.uriel.UrielControllerUtils;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.services.ContestContestantPasswordService;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.views.html.contest.contestant.password.listContestantPasswordsView;
import play.db.jpa.Transactional;
import play.i18n.Messages;
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

    private final ContestContestantPasswordService contestContestantPasswordService;
    private final ContestContestantService contestContestantService;
    private final ContestService contestService;

    @Inject
    public ContestPasswordController(ContestContestantPasswordService contestContestantPasswordService, ContestContestantService contestContestantService, ContestService contestService) {
        this.contestContestantPasswordService = contestContestantPasswordService;
        this.contestContestantService = contestContestantService;
        this.contestService = contestService;
    }

    @Transactional(readOnly = true)
    public Result viewContestantPasswords(long contestId) throws ContestNotFoundException {
        return listContestantPasswords(contestId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listContestantPasswords(long contestId, long pageIndex, String orderBy, String orderDir, String filterString) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (!contest.containsModule(ContestModules.PASSWORD) || !isAllowedToSuperviseContestants(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        Page<ContestContestant> pageOfContestContestants = contestContestantService.getPageOfContestantsInContest(contest.getJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
        Map<String, String> passwordsMap = contestContestantPasswordService.getContestantPasswordsMap(contest.getJid(), Lists.transform(pageOfContestContestants.getData(), c -> c.getUserJid()));

        return showListContestantPasswords(pageOfContestContestants, pageIndex, orderBy, orderDir, filterString, passwordsMap, contest);
    }

    @Transactional
    public Result generateContestantPasswords(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);

        if (contest.isLocked() || !contest.containsModule(ContestModules.PASSWORD) || !isAllowedToSuperviseContestants(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        contestContestantPasswordService.generateContestantPasswordForAllContestants(contest.getJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.ContestPasswordController.viewContestantPasswords(contest.getId()));
    }

    @Transactional
    public Result generateContestantPassword(long contestId, long contestContestantId) throws ContestNotFoundException, ContestContestantNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        ContestContestant contestant = contestContestantService.findContestantInContestById(contestContestantId);

        if (contest.isLocked() || !contest.containsModule(ContestModules.PASSWORD) || !isAllowedToSuperviseContestants(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        contestContestantPasswordService.generateContestantPassword(contest.getJid(), contestant.getUserJid(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.ContestPasswordController.viewContestantPasswords(contest.getId()));
    }

    private Result showListContestantPasswords(Page<ContestContestant> pageOfContestContestants, long pageIndex, String orderBy, String orderDir, String filterString, Map<String, String> passwordsMap, Contest contest) {
        LazyHtml content = new LazyHtml(listContestantPasswordsView.render(contest.getId(), pageOfContestContestants, pageIndex, orderBy, orderDir, filterString, passwordsMap));
        content.appendLayout(c -> heading3Layout.render(Messages.get("contestant.passwords"), c));

        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("contestant.passwords"), routes.ContestPasswordController.viewContestantPasswords(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Passwords");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("contestant.passwords"), routes.ContestController.jumpToContestants(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseContestants(Contest contest) {
        return ContestControllerUtils.getInstance().isPermittedToSupervise(contest, ContestPermissions.CONTESTANT, IdentityUtils.getUserJid());
    }
}
