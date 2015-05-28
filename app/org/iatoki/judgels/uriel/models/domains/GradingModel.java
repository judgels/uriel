package org.iatoki.judgels.uriel.models.domains;

import org.iatoki.judgels.sandalphon.commons.models.domains.AbstractGradingModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_programming_grading")
public final class GradingModel extends AbstractGradingModel {
}
