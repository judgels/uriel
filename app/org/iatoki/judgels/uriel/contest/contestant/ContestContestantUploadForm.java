package org.iatoki.judgels.uriel.contest.contestant;

import play.data.validation.Constraints;

import java.io.File;

public final class ContestContestantUploadForm {

    @Constraints.Required
    public File usernames;
}
