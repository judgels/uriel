package org.iatoki.judgels.uriel.contest.team.member;

import play.data.validation.Constraints;

import java.io.File;

public final class ContestTeamMemberUploadForm {

    @Constraints.Required
    public File usernames;
}
