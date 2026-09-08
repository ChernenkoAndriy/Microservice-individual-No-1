package com.epam.java.specialization.gym_crm.trainee.internal;

import com.epam.java.specialization.gym_crm.trainee.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {
    Optional<Trainee> findByUserUsername(String username);
}