package org.iatoki.judgels.uriel.contest;

import org.iatoki.judgels.play.EntityNotFoundException;

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
