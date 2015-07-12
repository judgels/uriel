package org.iatoki.judgels.uriel;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class ContestSupervisorNotFoundException extends EntityNotFoundException {

    public ContestSupervisorNotFoundException() {
        super();
    }

    public ContestSupervisorNotFoundException(String s) {
        super(s);
    }

    public ContestSupervisorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContestSupervisorNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Contest Supervisor";
    }
}
