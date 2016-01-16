package org.iatoki.judgels.uriel.contest.problem;

import org.iatoki.judgels.play.EntityNotFoundException;

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
