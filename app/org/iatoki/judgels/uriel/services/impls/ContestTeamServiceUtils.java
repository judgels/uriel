package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.uriel.ContestTeam;
import org.iatoki.judgels.uriel.ContestTeamCoach;
import org.iatoki.judgels.uriel.ContestTeamMember;
import org.iatoki.judgels.uriel.UrielProperties;
import org.iatoki.judgels.uriel.controllers.api.routes;
import org.iatoki.judgels.uriel.models.entities.ContestTeamCoachModel;
import org.iatoki.judgels.uriel.models.entities.ContestTeamMemberModel;
import org.iatoki.judgels.uriel.models.entities.ContestTeamModel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

final class ContestTeamServiceUtils {

    private ContestTeamServiceUtils() {
        // prevent instantiation
    }

    static ContestTeam createContestTeamFromModel(ContestTeamModel contestTeamModel, List<ContestTeamCoachModel> contestTeamCoachModels, List<ContestTeamMemberModel> contestTeamMemberModels) {
        return new ContestTeam(contestTeamModel.id, contestTeamModel.jid, contestTeamModel.contestJid, contestTeamModel.name, getTeamImageURLFromImageName(contestTeamModel.teamImageName), new Date(contestTeamModel.contestStartTime), contestTeamCoachModels.stream().map(ctc -> createContestTeamCoachFromModel(ctc)).collect(Collectors.toList()), contestTeamMemberModels.stream().map(ctm -> createContestTeamMemberFromModel(ctm)).collect(Collectors.toList()));
    }

    static ContestTeamCoach createContestTeamCoachFromModel(ContestTeamCoachModel contestTeamCoachModel) {
        return new ContestTeamCoach(contestTeamCoachModel.id, contestTeamCoachModel.teamJid, contestTeamCoachModel.coachJid);
    }

    static ContestTeamMember createContestTeamMemberFromModel(ContestTeamMemberModel contestTeamMemberModel) {
        return new ContestTeamMember(contestTeamMemberModel.id, contestTeamMemberModel.teamJid, contestTeamMemberModel.memberJid);
    }

    static URL getTeamImageURLFromImageName(String imageName) {
        try {
            return new URL(UrielProperties.getInstance().getUrielBaseUrl() + routes.ContestAPIController.renderTeamAvatarImage(imageName));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
