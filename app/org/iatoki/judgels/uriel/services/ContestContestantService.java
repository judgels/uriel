package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantNotFoundException;
import org.iatoki.judgels.uriel.ContestContestantStatus;

import java.util.List;

public interface ContestContestantService {

    boolean isContestContestantInContestByUserJid(String contestJid, String contestContestantJid);

    boolean isContestStarted(String contestJid, String contestContestantJid);

    ContestContestant findContestContestantByContestContestantId(long contestContestantId) throws ContestContestantNotFoundException;

    ContestContestant findContestContestantByContestJidAndContestContestantJid(String contestJid, String contestContestantJid);

    List<ContestContestant> findAllContestContestantsByContestJid(String contestJid);

    Page<ContestContestant> pageContestContestantsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    long getContestContestantCount(String contestJid);

    void createContestContestant(long contestId, String userJid, ContestContestantStatus status);

    void updateContestContestant(long contestContestantId, ContestContestantStatus status);

    void deleteContestContestant(long contestContestantId);

    void startContestAsContestant(String contestJid, String userJid);
}
