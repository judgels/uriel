package org.iatoki.judgels.uriel.models.domains;

import org.iatoki.judgels.commons.models.JidPrefix;
import org.iatoki.judgels.commons.models.domains.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_clarification")
public final class ContestClarificationModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String contestJid;

    public String topicJid;

    public String title;

    public String question;

    public String answer;

    public String status;

}
