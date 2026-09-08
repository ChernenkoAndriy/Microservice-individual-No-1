package com.epam.java.specialization.gym_crm.repository.specification;

import com.epam.java.specialization.gym_crm.model.*;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;
import java.util.Date;

public class TraineeSpecifications {

    public static Specification<Training> hasTraineeUsername(String username) {
        return (root, query, cb) -> cb.equal(
                root.join(Training_.trainee).join(Trainee_.user).get(User_.username),
                username
        );
    }

    public static Specification<Training> dateGreaterThanOrEqualTo(Date fromDate) {
        return (root, query, cb) -> fromDate == null ? null :
                cb.greaterThanOrEqualTo(root.get(Training_.trainingDate), fromDate);
    }

    public static Specification<Training> dateLessThanOrEqualTo(Date toDate) {
        return (root, query, cb) -> toDate == null ? null :
                cb.lessThanOrEqualTo(root.get(Training_.trainingDate), toDate);
    }

    public static Specification<Training> hasTrainerUsername(String trainerName) {
        return (root, query, cb) -> (trainerName == null || trainerName.trim().isEmpty()) ? null :
                cb.equal(root.join(Training_.trainer).join(Trainer_.user).get(User_.username), trainerName);
    }

    public static Specification<Training> hasTrainingType(String trainingType) {
        return (root, query, cb) -> (trainingType == null || trainingType.trim().isEmpty()) ? null :
                cb.equal(root.join(Training_.trainingType).get(TrainingType_.trainingTypeName), trainingType);
    }
}