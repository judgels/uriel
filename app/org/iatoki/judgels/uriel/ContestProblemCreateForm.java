package org.iatoki.judgels.uriel;

import play.data.validation.Constraints;

public final class ContestProblemCreateForm {

    @Constraints.Required
    public String problemJid;

    @Constraints.Required
    public String problemSecret;

    @Constraints.Required
    public String alias;

    @Constraints.Required
    public long submissionsLimit;

    @Constraints.Required
    public String status;
}
