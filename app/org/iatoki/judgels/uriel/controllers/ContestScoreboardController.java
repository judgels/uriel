package org.iatoki.judgels.uriel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.views.html.layouts.heading3WithActionsLayout;
import org.iatoki.judgels.gabriel.commons.GabrielUtils;
import org.iatoki.judgels.gabriel.commons.Submission;
import org.iatoki.judgels.gabriel.commons.SubmissionService;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.ContestService;
import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.ContestTeamCoach;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.ContestTypeConfigStandard;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtual;
import org.iatoki.judgels.uriel.ContestTypeConfigVirtualStartTrigger;
import org.iatoki.judgels.uriel.JidCacheService;
import org.iatoki.judgels.uriel.ScoreAdapter;
import org.iatoki.judgels.uriel.ScoreAdapters;
import org.iatoki.judgels.uriel.commons.ContestScoreState;
import org.iatoki.judgels.uriel.commons.IOIScoreboardContent;
import org.iatoki.judgels.uriel.commons.IOIScoreboardEntry;
import org.iatoki.judgels.uriel.commons.Scoreboard;
import org.iatoki.judgels.uriel.commons.ScoreboardContent;
import org.iatoki.judgels.uriel.controllers.security.Authenticated;
import org.iatoki.judgels.uriel.controllers.security.HasRole;
import org.iatoki.judgels.uriel.controllers.security.LoggedIn;
import org.iatoki.judgels.uriel.views.html.layouts.accessTypeByStatusLayout;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public class ContestScoreboardController extends Controller {

    private static final long PAGE_SIZE = 20;

    private final ContestService contestService;
    private final SubmissionService submissionService;

    public ContestScoreboardController(ContestService contestService, SubmissionService submissionService) {
        this.contestService = contestService;
        this.submissionService = submissionService;
    }

    public Result viewScoreboard(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if ((contest.isUsingScoreboard()) && (isAllowedToEnterContest(contest))) {
            ContestScoreboard contestScoreboard;
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            if ((contest.isStandard()) && ((new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigStandard.class)).getScoreboardFreezeTime() < System.currentTimeMillis()) && (!(new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigStandard.class)).isOfficialScoreboardAllowed())) {
                if (contestService.isContestScoreboardExistByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.FROZEN)) {
                    contestScoreboard = contestService.findContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.FROZEN);
                } else {
                    contestScoreboard = null;
                }
            } else {
                contestScoreboard = contestService.findContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL);
            }
            ScoreAdapter adapter = ScoreAdapters.fromContestStyle(contest.getStyle());
            LazyHtml content;
            if (contestScoreboard == null) {
                content = new LazyHtml(adapter.renderScoreboard(null, null, JidCacheService.getInstance(), IdentityUtils.getUserJid(), false, ImmutableSet.of()));
            } else {
                Scoreboard scoreboard = contestScoreboard.getScoreboard();
                if (contest.isIncognitoScoreboard()) {
                    if (isCoach(contest)) {
                        List<ContestTeamMember> contestTeamMembers = contestService.findContestTeamMembersByContestJidAndCoachJid(contest.getJid(), IdentityUtils.getUserJid());
                        content = new LazyHtml(adapter.renderScoreboard(scoreboard, contestScoreboard.getLastUpdateTime(), JidCacheService.getInstance(), IdentityUtils.getUserJid(), true, contestTeamMembers.stream().map(ct -> ct.getMemberJid()).collect(Collectors.toSet())));
                    } else {
                        content = new LazyHtml(adapter.renderScoreboard(scoreboard, contestScoreboard.getLastUpdateTime(), JidCacheService.getInstance(), IdentityUtils.getUserJid(), true, ImmutableSet.of(IdentityUtils.getUserJid())));
                    }
                } else {
                    content = new LazyHtml(adapter.renderScoreboard(scoreboard, contestScoreboard.getLastUpdateTime(), JidCacheService.getInstance(), IdentityUtils.getUserJid(), false, scoreboard.getState().getContestantJids().stream().collect(Collectors.toSet())));
                }
            }

            if (isAllowedToSuperviseScoreboard(contest)) {
                content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestScoreboardController.viewScoreboard(contest.getId()), routes.ContestScoreboardController.viewOfficialScoreboard(contest.getId()), c));
            }

            appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                    new InternalLink(Messages.get("scoreboard.scoreboard"), routes.ContestScoreboardController.viewScoreboard(contest.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Public Scoreboard");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result viewOfficialScoreboard(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if ((contest.isUsingScoreboard()) && (isAllowedToSuperviseScoreboard(contest))) {
            ContestScoreboard contestScoreboard = contestService.findContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL);
            ScoreAdapter adapter = ScoreAdapters.fromContestStyle(contest.getStyle());
            Scoreboard scoreboard = contestScoreboard.getScoreboard();
            LazyHtml content = new LazyHtml(adapter.renderScoreboard(scoreboard, contestScoreboard.getLastUpdateTime(), JidCacheService.getInstance(), IdentityUtils.getUserJid(), false, scoreboard.getState().getContestantJids().stream().collect(Collectors.toSet())));

            content.appendLayout(c -> heading3WithActionsLayout.render(Messages.get("scoreboard.scoreboard"), new InternalLink[]{new InternalLink(Messages.get("scoreboard.refresh"), routes.ContestScoreboardController.refreshAllScoreboard(contest.getId())), new InternalLink(Messages.get("data.download"), routes.ContestScoreboardController.downloadContestDataAsXLS(contest.getId()))}, c));

            content.appendLayout(c -> accessTypeByStatusLayout.render(routes.ContestScoreboardController.viewScoreboard(contest.getId()), routes.ContestScoreboardController.viewOfficialScoreboard(contest.getId()), c));

            appendTabsLayout(content, contest);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("contest.contests"), routes.ContestController.index()),
                    new InternalLink(contest.getName(), routes.ContestController.viewContest(contest.getId())),
                    new InternalLink(Messages.get("scoreboard.scoreboard"), routes.ContestScoreboardController.viewScoreboard(contest.getId())),
                    new InternalLink(Messages.get("status.supervisor"), routes.ContestScoreboardController.viewOfficialScoreboard(contest.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Contest - Official Scoreboard");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result refreshAllScoreboard(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if ((contest.isUsingScoreboard()) && (isAllowedToSuperviseScoreboard(contest))) {
            try {
                if (GabrielUtils.getScoreboardLock().tryLock(10, TimeUnit.SECONDS)) {
                    ScoreAdapter adapter = ScoreAdapters.fromContestStyle(contest.getStyle());
                    ContestScoreState state = contestService.getContestStateByJid(contest.getJid());

                    List<Submission> submissions = submissionService.findAllSubmissionsByContestJid(contest.getJid());

                    ScoreboardContent content = adapter.computeScoreboardContent(state, submissions, contestService.getMapContestantJidToImageUrlInContest(contest.getJid()));
                    Scoreboard scoreboard = adapter.createScoreboard(state, content);
                    contestService.updateContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL, scoreboard);

                    if (contest.isStandard()) {
                        refreshFrozenScoreboard(contest, adapter, state);
                    }

                    GabrielUtils.getScoreboardLock().unlock();
                }
                return redirect(routes.ContestScoreboardController.viewOfficialScoreboard(contest.getId()));
            } catch (InterruptedException e) {
                return internalServerError();
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    public Result downloadContestDataAsXLS(long contestId) {
        Contest contest = contestService.findContestById(contestId);
        if ((contest.isUsingScoreboard()) && (isAllowedToSuperviseScoreboard(contest))) {
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestScoreboard contestScoreboard = contestService.findContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.OFFICIAL);
            ContestScoreState contestScoreState = contestScoreboard.getScoreboard().getState();

            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet(Messages.get("problem.problems"));

            int rowNum = 0;
            int cellNum = 0;
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(cellNum++);
            cell.setCellValue(Messages.get("problem.alias"));
            cell = row.createCell(cellNum++);
            cell.setCellValue(Messages.get("problem.name"));
            for (int i=0;i<contestScoreState.getProblemJids().size();++i) {
                row = sheet.createRow(rowNum++);
                cellNum = 0;
                cell = row.createCell(cellNum++);
                cell.setCellValue(contestScoreState.getProblemAliases().get(i));
                cell = row.createCell(cellNum++);
                cell.setCellValue(JidCacheService.getInstance().getDisplayName(contestScoreState.getProblemJids().get(i)));
            }

            sheet = workbook.createSheet(Messages.get("team.teams"));

            List<ContestTeam> contestTeams = contestService.findAllContestTeams(contest.getJid());
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
                row = sheet.createRow(rowNum++);
                cell = row.createCell(cellNum++);
                cell.setCellValue(contestTeam.getName());

                List<ContestTeamCoach> contestTeamCoaches = contestService.findContestTeamCoachesByTeamJid(contestTeam.getContestJid());
                List<ContestTeamMember> contestTeamMembers = contestService.findContestTeamMembersByTeamJid(contestTeam.getJid());
                if (contestTeamCoaches.size() > 0) {
                    cell = row.createCell(1);
                    cell.setCellValue(JidCacheService.getInstance().getDisplayName(contestTeamCoaches.get(0).getCoachJid()));
                }
                if (contestTeamMembers.size() > 0) {
                    cell = row.createCell(2);
                    cell.setCellValue(JidCacheService.getInstance().getDisplayName(contestTeamMembers.get(0).getMemberJid()));
                }
                int max = Math.max(contestTeamCoaches.size(), contestTeamMembers.size());
                for (int i=1;i<max;++i) {
                    row = sheet.createRow(rowNum++);
                    if (contestTeamCoaches.size() > i) {
                        cell = row.createCell(1);
                        cell.setCellValue(JidCacheService.getInstance().getDisplayName(contestTeamCoaches.get(i).getCoachJid()));
                    }
                    if (contestTeamMembers.size() > i) {
                        cell = row.createCell(2);
                        cell.setCellValue(JidCacheService.getInstance().getDisplayName(contestTeamMembers.get(i).getMemberJid()));
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
                for (String s : contestScoreState.getProblemAliases()) {
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(s);
                }

                for (IOIScoreboardEntry entry : ioiScoreboardContent.getEntries()) {
                    row = sheet.createRow(rowNum++);
                    cellNum = 0;

                    cell = row.createCell(cellNum++);
                    cell.setCellValue(entry.rank);
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(JidCacheService.getInstance().getDisplayName(entry.contestantJid));
                    cell = row.createCell(cellNum++);
                    cell.setCellValue(entry.totalScores);
                    for (Integer score : entry.scores) {
                        cell = row.createCell(cellNum++);
                        cell.setCellValue(score);
                    }
                }
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    workbook.write(baos);
                    baos.close();
                    response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    response().setHeader("Content-Disposition", "attachment; filename=\"" + contest.getName()+ ".xls\"");
                    return ok(baos.toByteArray());
                } catch (IOException e) {
                    return internalServerError();
                }
            } else {
                // TODO FOR ACM ICPC
                return internalServerError();
            }
        } else {
            return tryEnteringContest(contest);
        }
    }

    private void refreshFrozenScoreboard(Contest contest, ScoreAdapter adapter, ContestScoreState state) {
        ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
        List<Submission> submissions = submissionService.findAllSubmissionsByContestJidBeforeTime(contest.getJid(), new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigStandard.class).getScoreboardFreezeTime());

        ScoreboardContent content = adapter.computeScoreboardContent(state, submissions, contestService.getMapContestantJidToImageUrlInContest(contest.getJid()));
        Scoreboard scoreboard = adapter.createScoreboard(state, content);
        contestService.updateContestScoreboardByContestJidAndScoreboardType(contest.getJid(), ContestScoreboardType.FROZEN, scoreboard);
    }

    private void appendTabsLayout(LazyHtml content, Contest contest) {
        Date contestEndTime = contest.getEndTime();
        if ((contest.isVirtual()) && (isContestant(contest))) {
            ContestContestant contestContestant = contestService.findContestContestantByContestJidAndContestContestantJid(contest.getJid(), IdentityUtils.getUserJid());
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
            contestEndTime = new Date(contestContestant.getContestEnterTime() + contestTypeConfigVirtual.getContestDuration());
        }
        ContestControllerUtils.getInstance().appendTabsLayout(content, contest, isSupervisorOrAbove(contest), contestEndTime);
    }

    private boolean isManager(Contest contest) {
        return contestService.isContestManagerInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isSupervisor(Contest contest) {
        return contestService.isContestSupervisorInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isCoach(Contest contest) {
        return contestService.isUserCoachInAnyTeamByContestJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isContestant(Contest contest) {
        return contestService.isContestContestantInContestByUserJid(contest.getJid(), IdentityUtils.getUserJid());
    }

    private boolean isContestStarted(Contest contest) {
        return (!new Date().before(contest.getStartTime()));
    }

    private boolean isAllowedToEnterContest(Contest contest) {
        if (ControllerUtils.getInstance().isAdmin() || isManager(contest) || isSupervisor(contest)) {
            return true;
        }
        if (contest.isStandard()) {
            return ((isContestant(contest) && isContestStarted(contest)) || (isCoach(contest)));
        } else {
            ContestConfiguration contestConfiguration = contestService.findContestConfigurationByContestJid(contest.getJid());
            ContestTypeConfigVirtual contestTypeConfigVirtual = new Gson().fromJson(contestConfiguration.getTypeConfig(), ContestTypeConfigVirtual.class);
            if (contestTypeConfigVirtual.getStartTrigger().equals(ContestTypeConfigVirtualStartTrigger.CONTESTANT)) {
                return (isContestant(contest) && (isContestStarted(contest)));
            } else {
                return ((isContestStarted(contest)) && (isCoach(contest) || (isContestant(contest) && (contestService.isContestEntered(contest.getJid(), IdentityUtils.getUserJid())))));
            }
        }
    }

    private boolean isSupervisorOrAbove(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest) || (isSupervisor(contest));
    }

    private boolean isAllowedToSuperviseScoreboard(Contest contest) {
        return ControllerUtils.getInstance().isAdmin() || isManager(contest) || isSupervisor(contest);
    }

    private Result tryEnteringContest(Contest contest) {
        if (isAllowedToEnterContest(contest)) {
            return redirect(routes.ContestAnnouncementController.viewPublishedAnnouncements(contest.getId()));
        } else {
            return redirect(routes.ContestController.viewContest(contest.getId()));
        }
    }
}
