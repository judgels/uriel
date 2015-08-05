package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantPasswordModel;

import java.util.Collection;
import java.util.Map;

public interface ContestContestantPasswordDao extends Dao<Long, ContestContestantPasswordModel> {
    boolean existsByContestJidAndContestantJid(String contestJid, String contestantJid);

    ContestContestantPasswordModel findByContestJidAndContestantJid(String contestJid, String contestantJid);

    Map<String, String> getContestantPasswordsByContestJidAndContestantJids(String contestJid, Collection<String> contestantJids);
}
