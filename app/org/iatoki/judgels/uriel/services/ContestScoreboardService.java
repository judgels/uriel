package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.Scoreboard;

import java.net.URL;
import java.util.Map;

public interface ContestScoreboardService {

    boolean isContestScoreboardExistByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type);

    ContestScoreboard findContestScoreboardByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type);

    Map<String, URL> getMapContestantJidToImageUrlInContest(String contestJid);

    void upsertFrozenScoreboard(long contestScoreboardId);

    void updateContestScoreboardByContestJidAndScoreboardType(String contestJid, ContestScoreboardType type, Scoreboard scoreboard);
}
