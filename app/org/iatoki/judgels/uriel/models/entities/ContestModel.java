package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest")
@JidPrefix("CONT")
public final class ContestModel extends AbstractJudgelsModel {

    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    public String type;

    public String scope;

    public String style;

    public long startTime;

    public long endTime;

    public long clarificationEndTime;

    public boolean isExclusive;

    public boolean isUsingScoreboard;

    public boolean isIncognitoScoreboard;

    public boolean requiresPassword;

    public ContestModel() {
    }

    public ContestModel(long id, String name, String type, String scope, String style, long startTime, long endTime) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.style = style;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
