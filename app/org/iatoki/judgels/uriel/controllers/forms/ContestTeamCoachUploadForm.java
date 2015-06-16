package org.iatoki.judgels.uriel.controllers.forms;

import play.data.validation.Constraints;

import java.io.File;

public final class ContestTeamCoachUploadForm {

    @Constraints.Required
    public File usernames;
}
