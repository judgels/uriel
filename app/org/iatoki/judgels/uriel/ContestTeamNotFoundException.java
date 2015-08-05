package org.iatoki.judgels.uriel;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class ContestTeamNotFoundException extends EntityNotFoundException {

    public ContestTeamNotFoundException() {
        super();
    }

    public ContestTeamNotFoundException(String s) {
        super(s);
    }

    public ContestTeamNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContestTeamNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Contest Team";
    }
}
