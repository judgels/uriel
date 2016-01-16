package org.iatoki.judgels.uriel.contest.team;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.contest.team.coach.ContestTeamCoach;
import org.iatoki.judgels.uriel.contest.team.coach.ContestTeamCoachNotFoundException;
import org.iatoki.judgels.uriel.contest.team.member.ContestTeamMember;
import org.iatoki.judgels.uriel.contest.team.member.ContestTeamMemberNotFoundException;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestantDao;
import org.iatoki.judgels.uriel.contest.ContestDao;
import org.iatoki.judgels.uriel.contest.team.coach.ContestTeamCoachDao;
import org.iatoki.judgels.uriel.contest.team.member.ContestTeamMemberDao;
import org.iatoki.judgels.uriel.contest.contestant.ContestContestantModel;
import org.iatoki.judgels.uriel.contest.ContestModel;
import org.iatoki.judgels.uriel.contest.team.coach.ContestTeamCoachModel;
import org.iatoki.judgels.uriel.contest.team.coach.ContestTeamCoachModel_;
import org.iatoki.judgels.uriel.contest.team.member.ContestTeamMemberModel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public final class ContestTeamServiceImpl implements ContestTeamService {

    private final ContestContestantDao contestContestantDao;
    private final ContestDao contestDao;
    private final ContestTeamCoachDao contestTeamCoachDao;
    private final ContestTeamDao contestTeamDao;
    private final ContestTeamMemberDao contestTeamMemberDao;
    private final FileSystemProvider teamAvatarFileSystemProvider;

    @Inject
    public ContestTeamServiceImpl(ContestContestantDao contestContestantDao, ContestDao contestDao, ContestTeamCoachDao contestTeamCoachDao, ContestTeamDao contestTeamDao, ContestTeamMemberDao contestTeamMemberDao, @TeamAvatarFileSystemProvider FileSystemProvider teamAvatarFileSystemProvider) {
        this.contestContestantDao = contestContestantDao;
        this.contestDao = contestDao;
        this.contestTeamCoachDao = contestTeamCoachDao;
        this.contestTeamDao = contestTeamDao;
        this.contestTeamMemberDao = contestTeamMemberDao;
        this.teamAvatarFileSystemProvider = teamAvatarFileSystemProvider;
        if (!teamAvatarFileSystemProvider.fileExists(ImmutableList.of("team-default.png"))) {
            try {
                teamAvatarFileSystemProvider.uploadFileFromStream(ImmutableList.of(), getClass().getResourceAsStream("/public/images/team/team-default.png"), "team-default.png");
                teamAvatarFileSystemProvider.makeFilePublic(ImmutableList.of("team-default.png"));
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create default avatar.");
            }
        }
    }

    @Override
    public boolean isUserPartOfAnyTeamInContest(String contestJid, String userJid) {
        List<String> teamJids = contestTeamDao.getJidsInContest(contestJid);

        return contestTeamCoachDao.isUserRegisteredAsCoachInTeams(userJid, teamJids) || contestTeamMemberDao.isUserRegisteredAsMemberInAnyTeam(userJid, teamJids);
    }

    @Override
    public boolean isUserACoachOfAnyTeamInContest(String contestJid, String coachJid) {
        List<String> teamJids = contestTeamDao.getJidsInContest(contestJid);

        return contestTeamCoachDao.isUserRegisteredAsCoachInTeams(coachJid, teamJids);
    }

    @Override
    public boolean isUserACoachInTeam(String userJid, String teamJid) {
        return contestTeamCoachDao.isUserCoachByUserJidAndTeamJid(userJid, teamJid);
    }

    @Override
    public ContestTeam findContestTeamById(long contestTeamId) throws ContestTeamNotFoundException {
        ContestTeamModel contestTeamModel = contestTeamDao.findById(contestTeamId);
        if (contestTeamModel == null) {
            throw new ContestTeamNotFoundException("Contest Team not found");
        }

        List<ContestTeamCoachModel> contestTeamCoachesModel = contestTeamCoachDao.getAllByTeamJid(contestTeamModel.jid);
        List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.getAllInTeam(contestTeamModel.jid);

        return createContestTeamFromModel(contestTeamModel, contestTeamCoachesModel, contestTeamMemberModels);
    }

    @Override
    public ContestTeamCoach findContestTeamCoachById(long contestTeamCoachId) throws ContestTeamCoachNotFoundException {
        ContestTeamCoachModel contestTeamCoachModel = contestTeamCoachDao.findById(contestTeamCoachId);
        if (contestTeamCoachModel == null) {
            throw new ContestTeamCoachNotFoundException("Contest Team Coach not found.");
        }

        return createContestTeamCoachFromModel(contestTeamCoachModel);
    }

    @Override
    public ContestTeamMember findContestTeamMemberById(long contestTeamMemberId) throws ContestTeamMemberNotFoundException {
        ContestTeamMemberModel contestTeamMemberModel = contestTeamMemberDao.findById(contestTeamMemberId);
        if (contestTeamMemberModel == null) {
            throw new ContestTeamMemberNotFoundException("Contest Team Member not found.");
        }

        return createContestTeamMemberFromModel(contestTeamMemberModel);
    }

    @Override
    public Page<ContestTeam> getPageOfTeamsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = contestTeamDao.countByFiltersEq(filterString, ImmutableMap.of(ContestTeamModel_.contestJid, contestJid));
        List<ContestTeamModel> contestTeamModels = contestTeamDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(ContestTeamModel_.contestJid, contestJid), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<ContestTeam> contestTeamBuilder = ImmutableList.builder();
        for (ContestTeamModel contestTeamModel : contestTeamModels) {
            List<ContestTeamCoachModel> contestTeamCoachesModel = contestTeamCoachDao.getAllByTeamJid(contestTeamModel.jid);
            List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.getAllInTeam(contestTeamModel.jid);

            contestTeamBuilder.add(createContestTeamFromModel(contestTeamModel, contestTeamCoachesModel, contestTeamMemberModels));
        }
        return new Page<>(contestTeamBuilder.build(), totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<ContestTeam> getPageOfTeamsInContestByCoachJid(String contestJid, String coachJid, long pageIndex, long pageSize, String orderBy, String orderDir) {
        List<String> teamJidsInContest = contestTeamDao.getJidsInContest(contestJid);

        Map<SingularAttribute<? super ContestTeamCoachModel, ? extends Object>, String> filterColumns = ImmutableMap.of(ContestTeamCoachModel_.coachJid, coachJid);
        Map<SingularAttribute<? super ContestTeamCoachModel, String>, List<String>> filterColumnsIn = ImmutableMap.of(ContestTeamCoachModel_.teamJid, teamJidsInContest);

        long totalRows = contestTeamCoachDao.countByFilters("", filterColumns, filterColumnsIn);
        List<ContestTeamCoachModel> contestTeamCoachModels = contestTeamCoachDao.findSortedByFilters(orderBy, orderDir, "", filterColumns, filterColumnsIn, pageIndex * pageSize, pageSize);

        List<String> teamJids = Lists.transform(contestTeamCoachModels, m -> m.teamJid);
        List<ContestTeamModel> contestTeamModels = contestTeamDao.getByJids(teamJids);

        ImmutableList.Builder<ContestTeam> contestTeamBuilder = ImmutableList.builder();
        for (ContestTeamModel contestTeamModel : contestTeamModels) {
            List<ContestTeamCoachModel> contestTeamCoachesModel = contestTeamCoachDao.getAllByTeamJid(contestTeamModel.jid);
            List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.getAllInTeam(contestTeamModel.jid);

            contestTeamBuilder.add(createContestTeamFromModel(contestTeamModel, contestTeamCoachesModel, contestTeamMemberModels));
        }
        return new Page<>(contestTeamBuilder.build(), totalRows, pageIndex, pageSize);
    }

    @Override
    public List<ContestTeam> getTeamsInContest(String contestJid) {
        List<ContestTeamModel> contestTeamModels = contestTeamDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ContestTeamModel_.contestJid, contestJid), 0, -1);

        ImmutableList.Builder<ContestTeam> contestTeamBuilder = ImmutableList.builder();
        for (ContestTeamModel contestTeamModel : contestTeamModels) {
            List<ContestTeamCoachModel> contestTeamCoachesModel = contestTeamCoachDao.getAllByTeamJid(contestTeamModel.jid);
            List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.getAllInTeam(contestTeamModel.jid);

            contestTeamBuilder.add(createContestTeamFromModel(contestTeamModel, contestTeamCoachesModel, contestTeamMemberModels));
        }

        return contestTeamBuilder.build();
    }

    @Override
    public List<ContestTeam> getTeamsInContestByCoachJid(String contestJid, String coachJid) {
        List<String> teamJidsInContest = contestTeamDao.getJidsInContest(contestJid);
        List<ContestTeamCoachModel> coaches = contestTeamCoachDao.getAllInTeamsByJid(coachJid, teamJidsInContest);

        List<ContestTeamModel> teamModels = contestTeamDao.getByJids(Lists.transform(coaches, m -> m.teamJid));

        ImmutableList.Builder<ContestTeam> teams = ImmutableList.builder();
        for (ContestTeamModel teamModel : teamModels) {
            List<ContestTeamCoachModel> contestTeamCoachesModel = contestTeamCoachDao.getAllByTeamJid(teamModel.jid);
            List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.getAllInTeam(teamModel.jid);

            teams.add(createContestTeamFromModel(teamModel, contestTeamCoachesModel, contestTeamMemberModels));
        }

        return teams.build();
    }

    @Override
    public List<ContestTeamCoach> getCoachesOfTeam(String contestTeamJid) {
        List<ContestTeamCoachModel> contestTeamCoachModels = contestTeamCoachDao.getAllByTeamJid(contestTeamJid);

        return Lists.transform(contestTeamCoachModels, m -> createContestTeamCoachFromModel(m));
    }

    @Override
    public List<ContestTeamMember> getCoachedMembersInContest(String contestJid, String coachJid) {
        List<String> teamJids = contestTeamDao.getJidsInContest(contestJid);
        List<ContestTeamCoachModel> contestTeamCoachModels = contestTeamCoachDao.getAllInTeamsByJid(coachJid, teamJids);

        List<ContestTeamModel> contestTeamModels = contestTeamDao.getByJids(Lists.transform(contestTeamCoachModels, m -> m.teamJid));

        List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.getAllInTeams(Lists.transform(contestTeamModels, m -> m.jid));

        return Lists.transform(contestTeamMemberModels, m -> createContestTeamMemberFromModel(m));
    }

    @Override
    public List<ContestTeamMember> getMembersOfTeam(String contestTeamJid) {
        List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.getAllInTeam(contestTeamJid);

        return Lists.transform(contestTeamMemberModels, m -> createContestTeamMemberFromModel(m));
    }

    @Override
    public String getTeamAvatarImageURL(String imageName) {
        return teamAvatarFileSystemProvider.getURL(ImmutableList.of(imageName));
    }

    @Override
    public ContestTeam createContestTeam(String contestJid, String name, String userJid, String userIpAddress) {
        ContestModel contestModel = contestDao.findByJid(contestJid);

        ContestTeamModel contestTeamModel = new ContestTeamModel();
        contestTeamModel.contestJid = contestModel.jid;
        contestTeamModel.name = name;
        contestTeamModel.teamImageName = "team-default.png";

        contestTeamDao.persist(contestTeamModel, userJid, userIpAddress);

        contestDao.edit(contestModel, userJid, userIpAddress);

        return createContestTeamFromModel(contestTeamModel, ImmutableList.of(), ImmutableList.of());
    }

    @Override
    public ContestTeam createContestTeam(String contestJid, String name, File teamImage, String extension, String userJid, String userIpAddress) throws IOException {
        ContestModel contestModel = contestDao.findByJid(contestJid);

        ContestTeamModel contestTeamModel = new ContestTeamModel();
        contestTeamModel.contestJid = contestModel.jid;
        contestTeamModel.name = name;
        contestTeamModel.teamImageName = "team-default.png";

        contestTeamDao.persist(contestTeamModel, userJid, userIpAddress);

        String newImageName = contestTeamModel.jid + "-" + JudgelsPlayUtils.hashMD5(UUID.randomUUID().toString()) + "." + extension;
        teamAvatarFileSystemProvider.uploadFile(ImmutableList.of(), teamImage, newImageName);
        teamAvatarFileSystemProvider.makeFilePublic(ImmutableList.of(newImageName));

        contestTeamModel.teamImageName = newImageName;

        contestTeamDao.edit(contestTeamModel, userJid, userIpAddress);

        contestDao.edit(contestModel, userJid, userIpAddress);

        return createContestTeamFromModel(contestTeamModel, ImmutableList.of(), ImmutableList.of());
    }

    @Override
    public void updateContestTeam(String contestTeamJid, String name, String userJid, String userIpAddress) {
        ContestTeamModel contestTeamModel = contestTeamDao.findByJid(contestTeamJid);
        contestTeamModel.name = name;

        contestTeamDao.edit(contestTeamModel, userJid, userIpAddress);

        ContestModel contestModel = contestDao.findByJid(contestTeamModel.contestJid);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void updateContestTeam(String contestTeamJid, String name, File teamImage, String extension, String userJid, String userIpAddress) throws IOException {
        ContestTeamModel contestTeamModel = contestTeamDao.findByJid(contestTeamJid);
        String newImageName = contestTeamModel.jid + "-" + JudgelsPlayUtils.hashMD5(UUID.randomUUID().toString()) + "." + extension;
        teamAvatarFileSystemProvider.uploadFile(ImmutableList.of(), teamImage, newImageName);
        teamAvatarFileSystemProvider.makeFilePublic(ImmutableList.of(newImageName));

        contestTeamModel.name = name;
        contestTeamModel.teamImageName = newImageName;

        contestTeamDao.edit(contestTeamModel, userJid, userIpAddress);

        ContestModel contestModel = contestDao.findByJid(contestTeamModel.contestJid);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void createContestTeamCoach(String contestTeamJid, String coachJid, String userJid, String userIpAddress) {
        ContestTeamCoachModel contestTeamCoachModel = new ContestTeamCoachModel();
        contestTeamCoachModel.teamJid = contestTeamJid;
        contestTeamCoachModel.coachJid = coachJid;

        contestTeamCoachDao.persist(contestTeamCoachModel, userJid, userIpAddress);

        ContestTeamModel contestTeamModel = contestTeamDao.findByJid(contestTeamJid);

        contestTeamDao.edit(contestTeamModel, userJid, userIpAddress);

        ContestModel contestModel = contestDao.findByJid(contestTeamModel.contestJid);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void removeContestTeamCoachById(long contestTeamCoachId) {
        ContestTeamCoachModel contestTeamCoachModel = contestTeamCoachDao.findById(contestTeamCoachId);
        contestTeamCoachDao.remove(contestTeamCoachModel);
    }

    @Override
    public void createContestTeamMember(String contestTeamJid, String memberJid, String userJid, String userIpAddress) {
        ContestTeamMemberModel contestTeamMemberModel = new ContestTeamMemberModel();
        contestTeamMemberModel.teamJid = contestTeamJid;
        contestTeamMemberModel.memberJid = memberJid;

        contestTeamMemberDao.persist(contestTeamMemberModel, userJid, userIpAddress);

        ContestTeamModel contestTeamModel = contestTeamDao.findByJid(contestTeamJid);

        contestTeamDao.edit(contestTeamModel, userJid, userIpAddress);

        ContestModel contestModel = contestDao.findByJid(contestTeamModel.contestJid);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void removeContestTeamMemberById(long contestTeamMemberId) {
        ContestTeamMemberModel contestTeamMemberModel = contestTeamMemberDao.findById(contestTeamMemberId);
        contestTeamMemberDao.remove(contestTeamMemberModel);
    }

    @Override
    public void startTeamAsCoach(String contestJid, String teamJid, String userJid, String userIpAddress) {
        long now = System.currentTimeMillis();
        ContestTeamModel contestTeamModel = contestTeamDao.findByJid(teamJid);
        contestTeamModel.contestStartTime = now;
        contestTeamDao.edit(contestTeamModel, userJid, userIpAddress);

        List<ContestTeamMemberModel> contestTeamMemberModels = contestTeamMemberDao.getAllInTeam(teamJid);

        for (ContestTeamMemberModel contestTeamMemberModel : contestTeamMemberModels) {
            ContestContestantModel contestContestantModel = contestContestantDao.findInContestByContestantJid(contestJid, contestTeamMemberModel.memberJid);
            contestContestantModel.contestStartTime = now;

            contestContestantDao.edit(contestContestantModel, userJid, userIpAddress);
        }

        ContestModel contestModel = contestDao.findByJid(contestTeamModel.contestJid);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    private static ContestTeam createContestTeamFromModel(ContestTeamModel contestTeamModel, List<ContestTeamCoachModel> contestTeamCoachModels, List<ContestTeamMemberModel> contestTeamMemberModels) {
        return new ContestTeam(contestTeamModel.id, contestTeamModel.jid, contestTeamModel.contestJid, contestTeamModel.name, getTeamImageURLFromImageName(contestTeamModel.teamImageName), new Date(contestTeamModel.contestStartTime), contestTeamCoachModels.stream().map(ctc -> createContestTeamCoachFromModel(ctc)).collect(Collectors.toList()), contestTeamMemberModels.stream().map(ctm -> createContestTeamMemberFromModel(ctm)).collect(Collectors.toList()));
    }

    private static ContestTeamCoach createContestTeamCoachFromModel(ContestTeamCoachModel contestTeamCoachModel) {
        return new ContestTeamCoach(contestTeamCoachModel.id, contestTeamCoachModel.teamJid, contestTeamCoachModel.coachJid);
    }

    private static ContestTeamMember createContestTeamMemberFromModel(ContestTeamMemberModel contestTeamMemberModel) {
        return new ContestTeamMember(contestTeamMemberModel.id, contestTeamMemberModel.teamJid, contestTeamMemberModel.memberJid);
    }

    private static URL getTeamImageURLFromImageName(String imageName) {
        try {
            return new URL(UrielProperties.getInstance().getUrielBaseUrl() + org.iatoki.judgels.uriel.controllers.api.internal.routes.InternalContestTeamAPIController.renderTeamAvatarImage(imageName));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
