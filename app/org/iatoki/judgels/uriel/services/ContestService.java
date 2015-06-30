package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.uriel.Contest;
import org.iatoki.judgels.uriel.ContestConfiguration;
import org.iatoki.judgels.uriel.ContestNotFoundException;
import org.iatoki.judgels.uriel.ContestScope;
import org.iatoki.judgels.uriel.ContestScopeConfig;
import org.iatoki.judgels.uriel.ContestScoreState;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.ContestStyleConfig;
import org.iatoki.judgels.uriel.ContestType;
import org.iatoki.judgels.uriel.ContestTypeConfig;

import java.util.Date;
import java.util.List;

public interface ContestService {

    Contest findContestById(long contestId) throws ContestNotFoundException;

    Contest findContestByJid(String contestJid);

    ContestScoreState getContestStateByJid(String contestJid);

    ContestConfiguration findContestConfigurationByContestJid(String contestJid);

    Page<Contest> pageAllowedContests(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String userJid, boolean isAdmin);

    List<Contest> getRunningContests(Date timeNow);

    Contest createContest(String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isExclusive, boolean isUsingScoreboard, boolean isIncognitoScoreboard, boolean requiresPassword);

    void updateContest(long contestId, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isExclusive, boolean isUsingScoreboard, boolean isIncognitoScoreboard, boolean requiresPassword);

    void updateContestConfigurationByContestJid(String contestJid, ContestTypeConfig typeConfig, ContestScopeConfig scopeConfig, ContestStyleConfig styleConfig);
}
