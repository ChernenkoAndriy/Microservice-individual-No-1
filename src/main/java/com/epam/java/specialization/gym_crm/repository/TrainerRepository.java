package com.epam.java.specialization.gym_crm.repository;

import com.epam.java.specialization.gym_crm.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUserUsername(String username);

    List<Trainer> findByUserUsernameIn(List<String> usernames);

    @Query("SELECT t FROM Trainer t WHERE t.user.isActive = true AND NOT EXISTS " +
            "(SELECT 1 FROM Trainee tr JOIN tr.trainers trt WHERE tr.user.username = :username AND trt.id = t.id)")
    List<Trainer> findAvailableTrainersNotAssignedToTrainee(@Param("username") String traineeUsername);
}