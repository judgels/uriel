package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.EntityNotFoundException;

public final class ContestNotFoundException extends EntityNotFoundException {

    public ContestNotFoundException() {
        super();
    }

    public ContestNotFoundException(String s) {
        super(s);
    }

    public ContestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContestNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Contest";
    }
}
