package org.iatoki.judgels.uriel.contest;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.contest.style.ContestStyle;
import org.iatoki.judgels.uriel.contest.style.ContestStyleConfig;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardState;
import org.iatoki.judgels.uriel.contest.module.ContestModule;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@ImplementedBy(ContestServiceImpl.class)
public interface ContestService {

    Contest findContestById(long contestId) throws ContestNotFoundException;

    Contest findContestByJid(String contestJid);

    ScoreboardState getScoreboardStateInContest(String contestJid);

    Page<Contest> getPageOfContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<Contest> getPageOfAllowedContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String userJid);

    List<Contest> getRunningContestsWithScoreboardModule(Date timeNow);

    Contest createContest(String name, String description, ContestStyle style, String userJid, String userIpAddress);

    void updateContest(String contestJid, String name, String description, ContestStyle style, String userJid, String userIpAddress);

    void updateContestStyleConfiguration(String contestJid, ContestStyleConfig styleConfig, String userJid, String userIpAddress);

    void updateContestModuleConfiguration(String contestJid, Collection<ContestModule> contestModules, String userJid, String userIpAddress);

    void lockContest(String contestJid, String userJid, String userIpAddress);

    void unlockContest(String contestJid, String userJid, String userIpAddress);
}
