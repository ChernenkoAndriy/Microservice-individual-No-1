package com.epam.java.specialization.gym_crm.controller;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.service.interfaces.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/trainings")
@RequiredArgsConstructor
@Tag(name = "Training Operations API")
public class TrainingController {

    private final TrainingService trainingService;

    @GetMapping("/trainee")
    @PreAuthorize("@userSecurity.isOwner(authentication, #username)")
    @Operation(summary = "Get Trainee Trainings List")
    public ResponseEntity<List<TraineeTrainingResponseDto>> getTraineeTrainings(
            @RequestParam String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date periodFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        return ResponseEntity.ok(trainingService.getTraineeTrainings(username, periodFrom, periodTo, trainerName, trainingType));
    }

    @GetMapping("/trainer")
    @PreAuthorize("@userSecurity.isOwner(authentication, #username)")
    @Operation(summary = "Get Trainer Trainings List")
    public ResponseEntity<List<TrainerTrainingResponseDto>> getTrainerTrainings(
            @RequestParam String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date periodFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date periodTo,
            @RequestParam(required = false) String traineeName) {
        return ResponseEntity.ok(trainingService.getTrainerTrainings(username, periodFrom, periodTo, traineeName));
    }

    @PostMapping
    @Operation(summary = "Add Training")
    public ResponseEntity<Void> addTraining(@Valid @RequestBody TrainingAddRequestDto request) {
        trainingService.addTraining(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/types")
    @Operation(summary = "Get Training types")
    public ResponseEntity<List<TrainingTypeResponseDto>> getTrainingTypes() {
        return ResponseEntity.ok(trainingService.getTrainingTypes());
    }
}