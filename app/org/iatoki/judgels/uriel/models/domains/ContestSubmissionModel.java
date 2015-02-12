package org.iatoki.judgels.uriel.models.domains;

import org.iatoki.judgels.sandalphon.commons.models.domains.SubmissionModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_submission")
public final class ContestSubmissionModel extends SubmissionModel {
}
