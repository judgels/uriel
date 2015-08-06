package org.iatoki.judgels.uriel.modules.clarification;

import play.data.validation.Constraints;

public final class ContestClarificationConfigForm {

    @Constraints.Required
    public long clarificationDuration;
}
