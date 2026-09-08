package com.epam.java.specialization.gym_crm.controller;

import com.epam.java.specialization.gym_crm.dto.*;
import com.epam.java.specialization.gym_crm.service.interfaces.TrainerService;
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

@RestController
@RequestMapping("/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainer Management API")
public class TrainerController {

    private final TrainerService trainerService;

    @PostMapping
    @Operation(summary = "Trainer Registration (returns generated credentials and JWT Token)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer registered successfully",
                    content = @Content(schema = @Schema(implementation = RegistrationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<RegistrationResponseDto> registerTrainer(@Valid @RequestBody TrainerRegisterRequestDto request) {
        return ResponseEntity.ok(trainerService.register(request));
    }

    @GetMapping("/{username}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #username)")
    @Operation(summary = "Get Trainer Profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<TrainerProfileResponseDto> getTrainerProfile(@PathVariable String username) {
        return ResponseEntity.ok(trainerService.getProfile(username));
    }

    @PutMapping("/{username}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #username)")
    @Operation(summary = "Update Trainer Profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = TrainerUpdateResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<TrainerUpdateResponseDto> updateTrainerProfile(
            @PathVariable String username,
            @Valid @RequestBody TrainerUpdateRequestDto request) {
        return ResponseEntity.ok(trainerService.updateProfile(username, request));
    }

    @PatchMapping("/{username}/activation")
    @PreAuthorize("@userSecurity.isOwner(authentication, #username)")
    @Operation(summary = "Activate/De-Activate Trainer Profile")
    public ResponseEntity<Void> toggleTrainerActivation(
            @PathVariable String username,
            @Valid @RequestBody ActivationRequestDto request) {
        trainerService.toggleActivation(username, request);
        return ResponseEntity.ok().build();
    }
}