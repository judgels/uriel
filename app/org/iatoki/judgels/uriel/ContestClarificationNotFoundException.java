package org.iatoki.judgels.uriel;

import org.iatoki.judgels.commons.EntityNotFoundException;

public final class ContestClarificationNotFoundException extends EntityNotFoundException {

    public ContestClarificationNotFoundException() {
        super();
    }

    public ContestClarificationNotFoundException(String s) {
        super(s);
    }

    public ContestClarificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContestClarificationNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Contest Clarification";
    }
}
