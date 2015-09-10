package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.uriel.ContestContestantStatus;
import org.iatoki.judgels.uriel.ContestScoreboard;
import org.iatoki.judgels.uriel.ContestScoreboardType;
import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.Scoreboard;
import org.iatoki.judgels.uriel.ScoreboardState;
import org.iatoki.judgels.uriel.adapters.impls.ScoreboardAdapters;
import org.iatoki.judgels.uriel.models.daos.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.ContestProblemDao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantModel_;
import org.iatoki.judgels.uriel.models.entities.ContestProblemModel;
import org.iatoki.judgels.uriel.models.entities.ContestScoreboardModel;

import java.util.Date;
import java.util.List;

final class ContestScoreboardServiceUtils {

    private ContestScoreboardServiceUtils() {
        // prevent instantiation
    }

    static ContestScoreboard createContestScoreboardFromModel(ContestScoreboardModel contestScoreboardModel, ContestStyle style) {
        Scoreboard scoreboard = ScoreboardAdapters.fromContestStyle(style).parseScoreboardFromJson(contestScoreboardModel.scoreboard);

        return new ContestScoreboard(contestScoreboardModel.id, contestScoreboardModel.contestJid, ContestScoreboardType.valueOf(contestScoreboardModel.type), scoreboard, new Date(contestScoreboardModel.timeUpdate));
    }

    static ScoreboardState getScoreboardStateInContest(ContestProblemDao contestProblemDao, ContestContestantDao contestContestantDao, String contestJid) {
        List<ContestProblemModel> contestProblemModels = contestProblemDao.getUsedInContest(contestJid);
        List<ContestContestantModel> contestContestantModels = contestContestantDao.findSortedByFilters("id", "asc", "", ImmutableMap.of(ContestContestantModel_.contestJid, contestJid, ContestContestantModel_.status, ContestContestantStatus.APPROVED.name()), ImmutableMap.of(), 0, -1);

        List<String> problemJids = Lists.transform(contestProblemModels, m -> m.problemJid);
        List<String> problemAliases = Lists.transform(contestProblemModels, m -> m.alias);
        List<String> contestantJids = Lists.transform(contestContestantModels, m -> m.userJid);

        return new ScoreboardState(problemJids, problemAliases, contestantJids);
    }
}
