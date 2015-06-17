package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.sandalphon.models.entities.AbstractGradingModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_programming_grading")
public final class GradingModel extends AbstractGradingModel {
}
