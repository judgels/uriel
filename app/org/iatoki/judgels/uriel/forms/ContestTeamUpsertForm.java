package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

import java.io.File;

public final class ContestTeamUpsertForm {

    @Constraints.Required
    public String name;

    public File teamImage;
}
