package org.iatoki.judgels.uriel.forms;

import org.iatoki.judgels.uriel.Contest;
import play.data.validation.Constraints;

public final class ContestUpsertForm {

    public ContestUpsertForm() {
    }

    public ContestUpsertForm(Contest contest) {
        this.name = contest.getName();
        this.description = contest.getDescription();
        this.style = contest.getStyle().name();
    }

    @Constraints.Required
    public String name;

    public String description;

    @Constraints.Required
    public String style;
}
