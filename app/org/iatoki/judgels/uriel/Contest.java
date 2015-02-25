package org.iatoki.judgels.uriel;

import java.util.Date;

public final class Contest {

    private long id;

    private String jid;

    private String name;

    private String description;

    private ContestType type;

    private ContestScope scope;

    private ContestStyle style;

    private Date startTime;

    private Date endTime;

    private Date clarificationEndTime;

    private boolean isIncognitoScoreboard;

    public Contest(long id, String jid, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime, Date clarificationEndTime, boolean isIncognitoScoreboard) {
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
        this.isIncognitoScoreboard = isIncognitoScoreboard;
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

    public boolean isIncognitoScoreboard() {
        return isIncognitoScoreboard;
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
}
