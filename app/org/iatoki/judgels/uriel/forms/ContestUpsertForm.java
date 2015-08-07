package org.iatoki.judgels.uriel.forms;

import play.data.validation.Constraints;

public final class ContestUpsertForm {

    @Constraints.Required
    public String name;

    public String description;

    @Constraints.Required
    public String style;
}
