package org.iatoki.judgels.uriel.modules.contest.clarificationtimelimit;

import play.data.validation.Constraints;

public final class ContestClarificationTimeLimitConfigForm {

    @Constraints.Required
    public long clarificationDuration;
}
