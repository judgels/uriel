package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantNotFoundException;
import org.iatoki.judgels.uriel.ContestContestantStatus;

import java.util.List;

public interface ContestContestantService {

    boolean isContestantInContest(String contestJid, String contestContestantJid);

    boolean hasContestantStartContest(String contestJid, String contestContestantJid);

    ContestContestant findContestantInContestById(long contestContestantId) throws ContestContestantNotFoundException;

    ContestContestant findContestantInContestAndJid(String contestJid, String contestContestantJid);

    List<ContestContestant> getContestantsInContest(String contestJid);

    Page<ContestContestant> getPageOfContestantsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    long countContestantsInContest(String contestJid);

    void createContestContestant(long contestId, String userJid, ContestContestantStatus status);

    void updateContestContestant(long contestContestantId, ContestContestantStatus status);

    void deleteContestContestant(long contestContestantId);

    void startContestAsContestant(String contestJid, String userJid);
}
