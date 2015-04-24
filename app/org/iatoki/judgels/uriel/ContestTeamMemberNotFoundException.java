package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.EntityNotFoundException;

public final class ContestTeamMemberNotFoundException extends EntityNotFoundException {

    public ContestTeamMemberNotFoundException() {
        super();
    }

    public ContestTeamMemberNotFoundException(String s) {
        super(s);
    }

    public ContestTeamMemberNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContestTeamMemberNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Contest Team Member";
    }
}
