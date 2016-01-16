package org.iatoki.judgels.uriel.contest.manager;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class ContestManagerNotFoundException extends EntityNotFoundException {

    public ContestManagerNotFoundException() {
        super();
    }

    public ContestManagerNotFoundException(String s) {
        super(s);
    }

    public ContestManagerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContestManagerNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Contest Manager";
    }
}
