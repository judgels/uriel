package org.iatoki.judgels.uriel;

import play.data.validation.Constraints;

import java.io.File;

public final class ContestTeamUpsertForm {

    public ContestTeamUpsertForm() {
    }

    public ContestTeamUpsertForm(ContestTeam contestTeam) {
        this.name = contestTeam.getName();
    }

    @Constraints.Required
    public String name;

    public File teamImage;

}
