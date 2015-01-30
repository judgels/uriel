package org.iatoki.judgels.uriel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractJudgelsModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_submission")
public final class SubmissionModel extends AbstractJudgelsModel {

    public String userJid;

    public String problemJid;

    public String contestJid;

    public String language;

    public long score;

    public String verdict;

    @Column(columnDefinition = "TEXT")
    public String gradingResult;

}
