package org.iatoki.judgels.uriel;

public final class Contest {

    private final long id;

    private final String jid;

    private final String name;

    private final String description;

    private final ContestStyle style;

    private final ContestStyleConfig styleConfig;

    public Contest(long id, String jid, String name, String description, ContestStyle style, ContestStyleConfig styleConfig) {
        this.id = id;
        this.jid = jid;
        this.name = name;
        this.description = description;
        this.style = style;
        this.styleConfig = styleConfig;
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

    public ContestStyle getStyle() {
        return style;
    }

    public ContestStyleConfig getStyleConfig() {
        return styleConfig;
    }

    public boolean isICPC() {
        return style.equals(ContestStyle.ICPC);
    }

    public boolean isIOI() {
        return style.equals(ContestStyle.IOI);
    }
}
