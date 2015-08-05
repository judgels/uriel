package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

import java.io.File;

public final class ContestContestantUploadForm {

    @Constraints.Required
    public File usernames;
}
