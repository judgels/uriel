package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.EntityNotFoundException;

public final class ContestTeamCoachNotFoundException extends EntityNotFoundException {

    public ContestTeamCoachNotFoundException() {
        super();
    }

    public ContestTeamCoachNotFoundException(String s) {
        super(s);
    }

    public ContestTeamCoachNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContestTeamCoachNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Contest Team Coach";
    }
}