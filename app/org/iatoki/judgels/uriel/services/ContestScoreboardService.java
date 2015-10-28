package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.Scoreboard;

import java.net.URL;
import java.util.Map;

public interface ContestScoreboardService {

    boolean scoreboardExistsInContestByType(String contestJid, ContestScoreboardType scoreboardType);

    ContestScoreboard findScoreboardInContestByType(String contestJid, ContestScoreboardType scoreboardType);

    Map<String, URL> getMappedContestantJidToImageUrlInContest(String contestJid);

    void upsertContestScoreboard(String contestJid, ContestScoreboardType scoreboardType, Scoreboard scoreboard, long time, String userJid, String ipAddress);
}
