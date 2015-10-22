package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableSet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3WithActionsLayout;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestPermissions;
import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.ContestScoreboardUtils;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.ContestTeamCoach;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.ICPCScoreboardContent;
import org.iatoki.judgels.uriel.ICPCScoreboardEntry;
import org.iatoki.judgels.uriel.IOIScoreboardContent;
import org.iatoki.judgels.uriel.IOIScoreboardEntry;
import org.iatoki.judgels.uriel.Scoreboard;
import org.iatoki.judgels.uriel.ScoreboardState;
import org.iatoki.judgels.uriel.UrielActivityKeys;
import org.iatoki.judgels.uriel.adapters.ScoreboardAdapter;
import org.iatoki.judgels.uriel.adapters.impls.ScoreboardAdapters;
import org.iatoki.judgels.uriel.controllers.securities.Authenticated;
import org.iatoki.judgels.uriel.controllers.securities.HasRole;
import org.iatoki.judgels.uriel.controllers.securities.LoggedIn;
import org.iatoki.judgels.uriel.modules.contest.ContestModules;
import org.iatoki.judgels.uriel.modules.contest.duration.ContestDurationModule;
import org.iatoki.judgels.uriel.modules.contest.frozenscoreboard.ContestFrozenScoreboardModule;
import org.iatoki.judgels.uriel.modules.contest.scoreboard.ContestScoreboardModule;
import org.iatoki.judgels.uriel.services.ContestContestantService;
import org.iatoki.judgels.uriel.services.ContestProblemService;
import org.iatoki.judgels.uriel.services.ContestScoreboardService;
import org.iatoki.judgels.uriel.services.ContestService;
import org.iatoki.judgels.uriel.services.ContestTeamService;
import org.iatoki.judgels.uriel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.uriel.views.html.layouts.accessTypeByStatusLayout;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public class ContestScoreboardController extends AbstractJudgelsController {

    private static final String SCOREBOARD = "scoreboard";
    private static final String CONTEST = "contest";

    private final ContestProblemService contestProblemService;
    private final ContestScoreboardService contestScoreboardService;
    private final ContestContestantService contestContestantService;
    private final ContestService contestService;
    private final ContestTeamService contestTeamService;
    private final ProgrammingSubmissionService programmingSubmissionService;

    @Inject
    public ContestScoreboardController(ContestProblemService contestProblemService, ContestScoreboardService contestScoreboardService, ContestContestantService contestContestantService, ContestService contestService, ContestTeamService contestTeamService, ProgrammingSubmissionService programmingSubmissionService) {
        this.contestProblemService = contestProblemService;
        this.contestScoreboardService = contestScoreboardService;
        this.contestContestantService = contestContestantService;
        this.contestService = contestService;
        this.contestTeamService = contestTeamService;
        this.programmingSubmissionService = programmingSubmissionService;
    }

    @Transactional(readOnly = true)
    public Result viewScoreboard(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!contest.containsModule(ContestModules.SCOREBOARD) || !ContestControllerUtils.getInstance().isAllowedToEnterContest(contest, IdentityUtils.getUserJid())) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestDurationModule contestDurationModule = ((ContestDurationModule) contest.getModule(ContestModules.DURATION));
        ContestScoreboardModule contestScoreboardModule = (ContestScoreboardModule) contest.getModule(ContestModules.SCOREBOARD);
        ContestScoreboardType contestScoreboardType = ContestScoreboardType.OFFICIAL;

        if (contest.containsModule(ContestModules.FROZEN_SCOREBOARD)) {
            ContestFrozenScoreboardModule contestFrozenScoreboardModule = (ContestFrozenScoreboardModule) contest.getModule(ContestModules.FROZEN_SCOREBOARD);
            if (!contestFrozenScoreboardModule.isOfficialScoreboardAllowed() && ((contestDurationModule.getEndTime().getTime() - contestFrozenScoreboardModule.getScoreboardFreezeTime()) < System.currentTimeMillis())) {
                if (contestScoreboardService.scoreboardExistsInContestByType(contest.getJid(), ContestScoreboardType.FROZEN)) {
                    contestScoreboardType = ContestScoreboardType.FROZEN;
                }
            }
        }

        ContestScoreboard contestScoreboard = null;
        if (contestScoreboardService.scoreboardExistsInContestByType(contest.getJid(), contestScoreboardType)) {
            contestScoreboard = contestScoreboardService.findScoreboardInContestByType(contest.getJid(), contestScoreboardType);
        }

        ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(contest.getStyle());

        LazyHtml content;
        if (contestScoreboard == null) {
            content = new LazyHtml(adapter.renderScoreboard(null, null, JidCacheServiceImpl.getInstance(), IdentityUtils.getUserJid(), false, ImmutableSet.of()));
        } else {
            Scoreboard scoreboard = contestScoreboard.getScoreboard();

            Set<String> openProblemJids = contestProblemService.getOpenedProblemsInContest(contest.getJid())
                    .stream()
                    .map(c -> c.getProblemJid())
                    .collect(Collectors.toSet());

            scoreboard = adapter.filterOpenProblems(scoreboard, openProblemJids);

            if (contestScoreboardModule.isIncognitoScoreboard()) {
                if (ContestControllerUtils.getInstance().isCoach(contest, IdentityUtils.getUserJid())) {
                    List<ContestTeamMember> contestTeamMembers = contestTeamService.getCoachedMembersInContest(contest.getJid(), IdentityUtils.getUserJid());
                    content = new LazyHtml(adapter.renderScoreboard(scoreboard, contestScoreboard.getLastUpdateTime(), JidCacheServiceImpl.getInstance(), IdentityUtils.getUserJid(), true, contestTeamMembers.stream().map(ct -> ct.getMemberJid()).collect(Collectors.toSet())));
                } else {
                    content = new LazyHtml(adapter.renderScoreboard(scoreboard, contestScoreboard.getLastUpdateTime(), JidCacheServiceImpl.getInstance(), IdentityUtils.getUserJid(), true, ImmutableSet.of(IdentityUtils.getUserJid())));
                }
            } else {
                content = new LazyHtml(adapter.renderScoreboard(scoreboard, contestScoreboard.getLastUpdateTime(), JidCacheServiceImpl.getInstance(), IdentityUtils.getUserJid(), false, scoreboard.getState().getContestantJids().stream().collect(Collectors.toSet())));
            }
        }

        if (isAllowedToSuperviseScoreboard(contest)) {
            appendSubtabsLayout(content, contest);
        }

        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.contestant"), routes.ContestScoreboardController.viewScoreboard(contest.getId()))
        );
        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Public Scoreboard");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewOfficialScoreboard(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!contest.containsModule(ContestModules.SCOREBOARD) || !isAllowedToSuperviseScoreboard(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestScoreboard contestScoreboard = null;
        if (contestScoreboardService.scoreboardExistsInContestByType(contest.getJid(), ContestScoreboardType.OFFICIAL)) {
            contestScoreboard = contestScoreboardService.findScoreboardInContestByType(contest.getJid(), ContestScoreboardType.OFFICIAL);
        }

        ScoreboardAdapter adapter = ScoreboardAdapters.fromContestStyle(contest.getStyle());

        LazyHtml content;
        if (contestScoreboard == null) {
            content = new LazyHtml(adapter.renderScoreboard(null, null, JidCacheServiceImpl.getInstance(), IdentityUtils.getUserJid(), false, ImmutableSet.of()));
        } else {
            Scoreboard scoreboard = contestScoreboard.getScoreboard();
            content = new LazyHtml(adapter.renderScoreboard(scoreboard, contestScoreboard.getLastUpdateTime(), JidCacheServiceImpl.getInstance(), IdentityUtils.getUserJid(), false, scoreboard.getState().getContestantJids().stream().collect(Collectors.toSet())));

            content.appendLayout(c -> heading3WithActionsLayout.render(Messages.get("scoreboard.scoreboard"), new InternalLink[]{new InternalLink(Messages.get("scoreboard.refresh"), routes.ContestScoreboardController.refreshAllScoreboard(contest.getId())), new InternalLink(Messages.get("data.download"), routes.ContestScoreboardController.downloadContestDataAsXLS(contest.getId()))}, c));
        }

        appendSubtabsLayout(content, contest);
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, IdentityUtils.getUserJid());
        UrielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, contest,
                new InternalLink(Messages.get("status.supervisor"), routes.ContestScoreboardController.viewOfficialScoreboard(contest.getId()))
        );

        UrielControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Official Scoreboard");

        return UrielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional
    public Result refreshAllScoreboard(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (contest.isLocked() || !contest.containsModule(ContestModules.SCOREBOARD) || !isAllowedToSuperviseScoreboard(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestScoreboardUtils.updateScoreboards(contest, contestService, contestScoreboardService, contestContestantService, programmingSubmissionService, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        UrielControllerUtils.getInstance().addActivityLog(UrielActivityKeys.REFRESH.construct(CONTEST, contest.getJid(), contest.getName(), SCOREBOARD, null, ""));

        return redirect(routes.ContestScoreboardController.viewOfficialScoreboard(contest.getId()));
    }

    @Transactional(readOnly = true)
    public Result downloadContestDataAsXLS(long contestId) throws ContestNotFoundException {
        Contest contest = contestService.findContestById(contestId);
        if (!contest.containsModule(ContestModules.SCOREBOARD) || !isAllowedToSuperviseScoreboard(contest)) {
            return ContestControllerUtils.getInstance().tryEnteringContest(contest, IdentityUtils.getUserJid());
        }

        ContestScoreboard contestScoreboard = contestScoreboardService.findScoreboardInContestByType(contest.getJid(), ContestScoreboardType.OFFICIAL);
        ScoreboardState scoreboardState = contestScoreboard.getScoreboard().getState();

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet(Messages.get("problem.problems"));

        int rowNum = 0;
        int cellNum = 0;
        Row row = sheet.createRow(rowNum++);
        Cell cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("problem.alias"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("problem.name"));
        for (int i = 0; i < scoreboardState.getProblemJids().size(); ++i) {
            row = sheet.createRow(rowNum++);
            cellNum = 0;
            cell = row.createCell(cellNum++);
            cell.setCellValue(scoreboardState.getProblemAliases().get(i));
            cell = row.createCell(cellNum++);
            cell.setCellValue(JidCacheServiceImpl.getInstance().getDisplayName(scoreboardState.getProblemJids().get(i)));
        }

        sheet = workbook.createSheet(Messages.get("team.teams"));

        List<ContestTeam> contestTeams = contestTeamService.getTeamsInContest(contest.getJid());
        rowNum = 0;
        cellNum = 0;
        row = sheet.createRow(rowNum++);
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("team.name"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("team.coach.name"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("team.member.name"));
        for (ContestTeam contestTeam : contestTeams) {
            cellNum = 0;
            row = sheet.createRow(rowNum++);
            cell = row.createCell(cellNum++);
            cell.setCellValue(contestTeam.getName());

            List<ContestTeamCoach> contestTeamCoaches = contestTeamService.getCoachesOfTeam(contestTeam.getJid());
            List<ContestTeamMember> contestTeamMembers = contestTeamService.getMembersOfTeam(contestTeam.getJid());
            if (contestTeamCoaches.size() > 0) {
                cell = row.createCell(1);
                cell.setCellValue(JidCacheServiceImpl.getInstance().getDisplayName(contestTeamCoaches.get(0).getCoachJid()));
            }
            if (contestTeamMembers.size() > 0) {
                cell = row.createCell(2);
                cell.setCellValue(JidCacheServiceImpl.getInstance().getDisplayName(contestTeamMembers.get(0).getMemberJid()));
            }
            int max = Math.max(contestTeamCoaches.size(), contestTeamMembers.size());
            for (int i = 1; i < max; ++i) {
                row = sheet.createRow(rowNum++);
                if (contestTeamCoaches.size() > i) {
                    cell = row.createCell(1);
                    cell.setCellValue(JidCacheServiceImpl.getInstance().getDisplayName(contestTeamCoaches.get(i).getCoachJid()));
                }
                if (contestTeamMembers.size() > i) {
                    cell = row.createCell(2);
                    cell.setCellValue(JidCacheServiceImpl.getInstance().getDisplayName(contestTeamMembers.get(i).getMemberJid()));
                }
            }
        }

        sheet = workbook.createSheet("Rank");
        if (contest.isIOI()) {
            IOIScoreboardContent ioiScoreboardContent = (IOIScoreboardContent) contestScoreboard.getScoreboard().getContent();
            rowNum = 0;
            row = sheet.createRow(rowNum++);

            cellNum = 0;
            cell = row.createCell(cellNum++);
            cell.setCellValue("Rank");
            cell = row.createCell(cellNum++);
            cell.setCellValue("Contestant");
            cell = row.createCell(cellNum++);
            cell.setCellValue("Total");
            for (String s : scoreboardState.getProblemAliases()) {
                cell = row.createCell(cellNum++);
                cell.setCellValue(s);
            }

            for (IOIScoreboardEntry entry : ioiScoreboardContent.getEntries()) {
                row = sheet.createRow(rowNum++);
                cellNum = 0;

                cell = row.createCell(cellNum++);
                cell.setCellValue(entry.rank);
                cell = row.createCell(cellNum++);
                cell.setCellValue(JidCacheServiceImpl.getInstance().getDisplayName(entry.contestantJid));
                cell = row.createCell(cellNum++);
                cell.setCellValue(entry.totalScores);
                for (Integer score : entry.scores) {
                    cell = row.createCell(cellNum++);
                    if (score != null) {
                        cell.setCellValue(score);
                    }
                }
            }

        } else {
            ICPCScoreboardContent icpcScoreboardContent = (ICPCScoreboardContent) contestScoreboard.getScoreboard().getContent();

            rowNum = 0;
            row = sheet.createRow(rowNum++);

            cellNum = 0;
            cell = row.createCell(cellNum++);
            cell.setCellValue("Rank");
            cell = row.createCell(cellNum++);
            cell.setCellValue("Contestant");
            cell = row.createCell(cellNum++);
            cell.setCellValue("Score");
            cell = row.createCell(cellNum++);
            cell.setCellValue("Penalty");
            for (String s : scoreboardState.getProblemAliases()) {
                cell = row.createCell(cellNum++);
                cell.setCellValue(s);
                cell = row.createCell(cellNum++);
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, cellNum - 2, cellNum - 1));
            }

            for (ICPCScoreboardEntry entry : icpcScoreboardContent.getEntries()) {
                row = sheet.createRow(rowNum++);
                cellNum = 0;

                cell = row.createCell(cellNum++);
                cell.setCellValue(entry.rank);
                cell = row.createCell(cellNum++);
                cell.setCellValue(JidCacheServiceImpl.getInstance().getDisplayName(entry.contestantJid));
                cell = row.createCell(cellNum++);
                cell.setCellValue(entry.totalAccepted);
                cell = row.createCell(cellNum++);
                cell.setCellValue(entry.totalPenalties);
                for (int i = 0; i < entry.attemptsList.size(); i++) {
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(entry.attemptsList.get(i));
                    cell = row.createCell(cellNum++);

                    if (entry.problemStateList.get(i) != ICPCScoreboardEntry.State.NOT_ACCEPTED.ordinal()) {
                        cell.setCellValue(entry.penaltyList.get(i));
                    }
                }
            }
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            baos.close();
            response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response().setHeader("Content-Disposition", "attachment; filename=\"" + contest.getName() + ".xls\"");
            return ok(baos.toByteArray());
        } catch (IOException e) {
            return internalServerError();
        }
    }

    private void appendSubtabsLayout(LazyHtml content, Contest contest) {
        content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestScoreboardController.viewScoreboard(contest.getId()), routes.ContestScoreboardController.viewOfficialScoreboard(contest.getId()), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Contest contest, InternalLink... lastLinks) {
        UrielControllerUtils.getInstance().appendBreadcrumbsLayout(content,
                ContestControllerUtils.getInstance().getContestBreadcrumbsBuilder(contest)
                        .add(new InternalLink(Messages.get("scoreboard.scoreboard"), routes.ContestController.jumpToScoreboards(contest.getId())))
                        .addAll(Arrays.asList(lastLinks))
                        .build()
        );
    }

    private boolean isAllowedToSuperviseScoreboard(Contest contest) {
        return ContestControllerUtils.getInstance().isPermittedToSupervise(contest, ContestPermissions.SCOREBOARD, IdentityUtils.getUserJid());
    }
}
