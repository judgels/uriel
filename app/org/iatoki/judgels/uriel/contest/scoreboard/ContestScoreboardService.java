package org.iatoki.judgels.uriel.contest.scoreboard;

import com.google.inject.ImplementedBy;

import java.net.URL;
import java.util.Map;

@ImplementedBy(ContestScoreboardServiceImpl.class)
public interface ContestScoreboardService {

    boolean scoreboardExistsInContestByType(String contestJid, ContestScoreboardType scoreboardType);

    ContestScoreboard findScoreboardInContestByType(String contestJid, ContestScoreboardType scoreboardType);

    Map<String, URL> getMappedContestantJidToImageUrlInContest(String contestJid);

    void upsertContestScoreboard(String contestJid, ContestScoreboardType scoreboardType, Scoreboard scoreboard, long time, String userJid, String ipAddress);
}
