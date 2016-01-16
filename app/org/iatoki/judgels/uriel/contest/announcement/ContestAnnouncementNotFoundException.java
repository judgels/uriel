package org.iatoki.judgels.uriel.contest.announcement;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class ContestAnnouncementNotFoundException extends EntityNotFoundException {

    public ContestAnnouncementNotFoundException() {
        super();
    }

    public ContestAnnouncementNotFoundException(String s) {
        super(s);
    }

    public ContestAnnouncementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContestAnnouncementNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Contest Announcement";
    }
}
