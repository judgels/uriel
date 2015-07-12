package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.domains.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_read")
public final class ContestReadModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String userJid;

    public String type;

    public Long readId;

}
