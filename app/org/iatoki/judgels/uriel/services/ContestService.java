package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.ContestStyleConfig;
import org.iatoki.judgels.uriel.ScoreboardState;
import org.iatoki.judgels.uriel.modules.ContestModule;

import java.util.Date;
import java.util.List;

public interface ContestService {

    Contest findContestById(long contestId) throws ContestNotFoundException;

    Contest findContestByJid(String contestJid);

    ScoreboardState getContestStateByJid(String contestJid);

    Page<Contest> pageAllowedContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String userJid, boolean isAdmin);

    List<Contest> getRunningContests(Date timeNow);

    Contest createContest(String name, String description, ContestStyle style);

    void updateContest(long contestId, String name, String description, ContestStyle style);

    void updateContestStyleConfigurationByContestJid(String contestJid, ContestStyleConfig styleConfig);

    void updateContestModuleConfigurationByContestJid(String contestJid, List<ContestModule> contestModules);
}
