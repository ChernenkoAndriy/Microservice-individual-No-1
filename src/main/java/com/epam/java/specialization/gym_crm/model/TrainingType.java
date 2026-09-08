package com.epam.java.specialization.gym_crm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "training_types")
public class TrainingType extends AbstractEntity<Long> {

    @Column(name = "training_type_name", nullable = false, unique = true)
    private String trainingTypeName;
}