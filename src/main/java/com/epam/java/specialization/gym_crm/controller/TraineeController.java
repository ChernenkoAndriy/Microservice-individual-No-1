package com.epam.java.specialization.gym_crm.controller;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.service.interfaces.TraineeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/trainees")
@RequiredArgsConstructor
@Tag(name = "Trainee Management API")
public class TraineeController {

    private final TraineeService traineeService;

    @PostMapping
    @Operation(summary = "Trainee Registration (returns generated credentials and JWT Token)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee registered successfully",
                    content = @Content(schema = @Schema(implementation = RegistrationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<RegistrationResponseDto> registerTrainee(@Valid @RequestBody TraineeRegisterRequestDto request) {
        return ResponseEntity.ok(traineeService.register(request));
    }

    @GetMapping("/{username}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #username)")
    @Operation(summary = "Get Trainee Profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not profile owner)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<TraineeProfileResponseDto> getTraineeProfile(@PathVariable String username) {
        return ResponseEntity.ok(traineeService.getProfile(username));
    }

    @PutMapping("/{username}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #username)")
    @Operation(summary = "Update Trainee Profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = TraineeUpdateResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<TraineeUpdateResponseDto> updateTraineeProfile(
            @PathVariable String username,
            @Valid @RequestBody TraineeUpdateRequestDto request) {
        return ResponseEntity.ok(traineeService.updateProfile(username, request));
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #username)")
    @Operation(summary = "Delete Trainee Profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable String username) {
        traineeService.deleteProfile(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/unassigned-trainers")
    @PreAuthorize("@userSecurity.isOwner(authentication, #username)")
    @Operation(summary = "Get active trainers not assigned on trainee")
    public ResponseEntity<List<TrainerShortResponseDto>> getUnassignedActiveTrainers(@PathVariable String username) {
        return ResponseEntity.ok(traineeService.getUnassignedActiveTrainers(username));
    }

    @PutMapping("/{username}/trainers")
    @PreAuthorize("@userSecurity.isOwner(authentication, #username)")
    @Operation(summary = "Update Trainee's Trainer List")
    public ResponseEntity<List<TrainerShortResponseDto>> updateTraineesTrainersList(
            @PathVariable String username,
            @Valid @RequestBody List<TrainerUsernameRequestDto> request) {
        return ResponseEntity.ok(traineeService.updateTrainersList(username, request));
    }

    @PatchMapping("/{username}/activation")
    @PreAuthorize("@userSecurity.isOwner(authentication, #username)")
    @Operation(summary = "Activate/De-Activate Trainee Profile")
    public ResponseEntity<Void> toggleTraineeActivation(
            @PathVariable String username,
            @Valid @RequestBody ActivationRequestDto request) {
        traineeService.toggleActivation(username, request);
        return ResponseEntity.ok().build();
    }
}