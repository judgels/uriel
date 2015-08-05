package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.sandalphon.models.entities.AbstractSubmissionModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_programming_submission")
public final class SubmissionModel extends AbstractSubmissionModel {
}
