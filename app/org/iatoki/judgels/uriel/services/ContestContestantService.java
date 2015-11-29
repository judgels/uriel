package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestContestant;
import org.iatoki.judgels.uriel.ContestContestantNotFoundException;
import org.iatoki.judgels.uriel.ContestContestantOrganization;
import org.iatoki.judgels.uriel.ContestContestantStatus;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ContestContestantService {

    boolean hasRegisteredToContest(String contestJid, String contestContestantJid);

    boolean isContestantInContest(String contestJid, String contestContestantJid);

    boolean hasContestantStartContest(String contestJid, String contestContestantJid);

    ContestContestant findContestantInContestById(long contestContestantId) throws ContestContestantNotFoundException;

    ContestContestant findContestantInContestAndJid(String contestJid, String contestContestantJid);

    ContestContestantOrganization findContestantOrganizationInContestAndJid(String contestJid, String contestandJid);

    List<ContestContestant> getContestantsInContest(String contestJid);

    Map<String, Date> getContestantStartTimes(String contestJid);

    Page<ContestContestant> getPageOfContestantsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    long countContestantsInContest(String contestJid);

    void createContestContestant(String contestJid, String userJid, ContestContestantStatus status, String createUserJid, String createUserIpAddress);

    void createContestContestantOrganization(String contestJid, String userJid, String organization, String createUserJid, String createUserIpAddress);

    void updateContestContestant(long contestContestantId, ContestContestantStatus status, String updateUserJid, String updateUserIpAddress);

    void deleteContestContestant(long contestContestantId);

    void deleteContestContestantOrganization(long contestContestantOrganizationId);

    void startContestAsContestant(String contestJid, String userJid, String starterUserJid, String starterUserIpAddress);
}
