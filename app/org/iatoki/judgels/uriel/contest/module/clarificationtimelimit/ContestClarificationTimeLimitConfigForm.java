package org.iatoki.judgels.uriel.contest.module.clarificationtimelimit;

import play.data.validation.Constraints;

public final class ContestClarificationTimeLimitConfigForm {

    @Constraints.Required
    public long clarificationDuration;
}
