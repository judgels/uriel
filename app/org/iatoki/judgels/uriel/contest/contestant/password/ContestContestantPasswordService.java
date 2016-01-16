package org.iatoki.judgels.uriel.contest.contestant.password;

import com.google.inject.ImplementedBy;

import java.util.Collection;
import java.util.Map;

@ImplementedBy(ContestContestantPasswordServiceImpl.class)
public interface ContestContestantPasswordService {

    Map<String, String> getContestantPasswordsMap(String contestJid, Collection<String> contestantJids);

    String getContestantPassword(String contestJid, String contestantJid);

    void generateContestantPasswordForAllContestants(String contestJid, String userJid, String userIpAddress);

    void generateContestantPassword(String contestJid, String contestantJid, String userJid, String userIpAddress);
}
