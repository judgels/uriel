package org.iatoki.judgels.uriel.contest.contestant;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class ContestContestantNotFoundException extends EntityNotFoundException {

    public ContestContestantNotFoundException() {
        super();
    }

    public ContestContestantNotFoundException(String s) {
        super(s);
    }

    public ContestContestantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContestContestantNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Contest Contestant";
    }
}
