package org.iatoki.judgels.uriel.contest;

import org.iatoki.judgels.play.jid.JidPrefix;
import org.iatoki.judgels.play.model.AbstractJudgelsModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

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
