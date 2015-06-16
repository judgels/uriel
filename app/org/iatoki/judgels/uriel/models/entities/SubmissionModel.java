package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.sandalphon.commons.models.domains.AbstractSubmissionModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_programming_submission")
public final class SubmissionModel extends AbstractSubmissionModel {
}
