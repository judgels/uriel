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

    public String style;

    public boolean locked;

    public ContestModel() {
    }

    public ContestModel(String name, String description, String style) {
        this.name = name;
        this.description = description;
        this.style = style;
    }
}
