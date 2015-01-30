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

    public Contest(long id, String jid, String name, String description, ContestType type, ContestScope scope, ContestStyle style, Date startTime, Date endTime) {
        this.id = id;
        this.jid = jid;
        this.name = name;
        this.description = description;
        this.type = type;
        this.scope = scope;
        this.style = style;
        this.startTime = startTime;
        this.endTime = endTime;
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
}
