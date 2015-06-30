package org.iatoki.judgels.uriel.services;

import java.util.Collection;
import java.util.Map;

public interface ContestPasswordService {

    Map<String, String> getContestantPasswordsMap(String contestJid, Collection<String> contestantJids);

    String getContestantPassword(String contestJid, String contestantJid);

    void generateContestantPasswordForAllContestants(String contestJid);

    void generateContestantPassword(String contestJid, String contestantJid);
}
