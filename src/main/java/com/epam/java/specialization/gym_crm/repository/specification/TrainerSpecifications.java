package com.epam.java.specialization.gym_crm.repository.specification;

import com.epam.java.specialization.gym_crm.model.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.Date;

public class TrainerSpecifications {

    public static Specification<Training> hasTrainerUsername(String username) {
        return (root, query, cb) -> cb.equal(
                root.join(Training_.trainer).join(Trainer_.user).get(User_.username),
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

    public static Specification<Training> hasTraineeUsername(String traineeName) {
        return (root, query, cb) -> (traineeName == null || traineeName.trim().isEmpty()) ? null :
                cb.equal(root.join(Training_.trainee).join(Trainee_.user).get(User_.username), traineeName);
    }
}