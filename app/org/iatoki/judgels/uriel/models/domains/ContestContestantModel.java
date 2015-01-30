package org.iatoki.judgels.uriel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_contestant")
public final class ContestContestantModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String contestJid;

    public String userJid;

    public String status;

}
