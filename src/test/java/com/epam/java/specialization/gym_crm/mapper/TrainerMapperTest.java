package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.dto.TrainerProfileResponseDto;
import com.epam.java.specialization.gym_crm.dto.TrainerRegisterRequestDto;
import com.epam.java.specialization.gym_crm.model.Trainer;
import com.epam.java.specialization.gym_crm.model.TrainingType;
import com.epam.java.specialization.gym_crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerMapperTest {

    private TrainerMapper trainerMapper;

    @BeforeEach
    void setUp() {
        trainerMapper = new TrainerMapperImpl();
    }

    @Test
    @DisplayName("Should map TrainerRegisterRequestDto to Trainer entity (ignoring specialization ID)")
    void toEntity_ShouldMapDtoToEntity() {
        TrainerRegisterRequestDto dto = TrainerRegisterRequestDto.builder()
                .firstName("Mike")
                .lastName("Tyson")
                .specializationId(1L)
                .build();

        Trainer entity = trainerMapper.toEntity(dto);

        assertThat(entity.getUser().getFirstName()).isEqualTo("Mike");
        assertThat(entity.getUser().getLastName()).isEqualTo("Tyson");
        assertThat(entity.getUser().getIsActive()).isTrue();
        assertThat(entity.getSpecialization()).isNull();
    }

    @Test
    @DisplayName("Should map Trainer entity to ProfileResponse with specialization name")
    void toProfileResponse_ShouldMapSpecializationName() {
        TrainingType type = TrainingType.builder().trainingTypeName("Boxing").build();
        User user = User.builder().firstName("Mike").lastName("Tyson").isActive(true).build();
        Trainer entity = Trainer.builder()
                .user(user)
                .specialization(type)
                .build();

        TrainerProfileResponseDto response = trainerMapper.toProfileResponse(entity);

        assertThat(response.getFirstName()).isEqualTo("Mike");
        assertThat(response.getSpecialization()).isEqualTo("Boxing");
        assertThat(response.getIsActive()).isTrue();
    }
}