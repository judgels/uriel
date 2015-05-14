package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestContestantPasswordModel;

import java.util.Collection;
import java.util.Map;

public interface ContestContestantPasswordDao extends Dao<Long, ContestContestantPasswordModel> {
    boolean existsByContestJidAndContestantJid(String contestJid, String contestantJid);

    ContestContestantPasswordModel findByContestJidAndContestantJid(String contestJid, String contestantJid);

    Map<String, String> getContestantPasswordsByContestJidAndContestantJids(String contestJid, Collection<String> contestantJids);
}
