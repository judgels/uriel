package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.EntityNotFoundException;

public final class ContestProblemNotFoundException extends EntityNotFoundException {

    public ContestProblemNotFoundException() {
        super();
    }

    public ContestProblemNotFoundException(String s) {
        super(s);
    }

    public ContestProblemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContestProblemNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Contest Problem";
    }
}
