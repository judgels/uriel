package org.iatoki.judgels.uriel;

import java.util.Date;

public final class Contest implements Comparable<Contest> {

    private final long id;

    private final String jid;

    private final String name;

    private final String description;

    private final ContestType type;

    private final ContestScope scope;

    private final ContestStyle style;

    private final Date startTime;

    private final Date endTime;

    private final Date clarificationEndTime;

    private final boolean isExclusive;

    private final boolean isUsingScoreboard;

    private final boolean isIncognitoScoreboard;

    private final boolean requiresPassword;

    public Contest(long id, String jid, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isExclusive, boolean isUsingScoreboard, boolean isIncognitoScoreboard, boolean requiresPassword) {
        this.id = id;
        this.jid = jid;
        this.name = name;
        this.description = description;
        this.type = type;
        this.scope = scope;
        this.style = style;
        this.startTime = startTime;
        this.endTime = endTime;
        this.clarificationEndTime = clarificationEndTime;
        this.isExclusive = isExclusive;
        this.isUsingScoreboard = isUsingScoreboard;
        this.isIncognitoScoreboard = isIncognitoScoreboard;
        this.requiresPassword = requiresPassword;
    }

    public long getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ContestType getType() {
        return type;
    }

    public ContestScope getScope() {
        return scope;
    }

    public ContestStyle getStyle() {
        return style;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getClarificationEndTime() {
        return clarificationEndTime;
    }

    public boolean isClarificationTimeValid() {
        return clarificationEndTime.after(new Date());
    }

    public boolean isExclusive() {
        return isExclusive;
    }

    public boolean isUsingScoreboard() {
        return isUsingScoreboard;
    }

    public boolean isIncognitoScoreboard() {
        return isIncognitoScoreboard;
    }

    public boolean requiresPassword() {
        return requiresPassword;
    }

    public boolean isStandard() {
        return type.equals(ContestType.STANDARD);
    }

    public boolean isVirtual() {
        return type.equals(ContestType.VIRTUAL);
    }

    public boolean isPrivate() {
        return scope.equals(ContestScope.PRIVATE);
    }

    public boolean isPublic() {
        return scope.equals(ContestScope.PUBLIC);
    }

    public boolean isICPC() {
        return style.equals(ContestStyle.ICPC);
    }

    public boolean isIOI() {
        return style.equals(ContestStyle.IOI);
    }

    @Override
    public int compareTo(Contest o) {
        return o.getStartTime().compareTo(this.getStartTime());
    }
}
