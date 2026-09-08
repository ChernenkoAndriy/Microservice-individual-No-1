package com.epam.java.specialization.gym_crm.mapper;

import com.epam.java.specialization.gym_crm.dto.TraineeProfileResponseDto;
import com.epam.java.specialization.gym_crm.dto.TraineeRegisterRequestDto;
import com.epam.java.specialization.gym_crm.dto.TraineeUpdateRequestDto;
import com.epam.java.specialization.gym_crm.model.Trainee;
import com.epam.java.specialization.gym_crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeMapperTest {

    private TraineeMapper traineeMapper;

    @BeforeEach
    void setUp() {
        traineeMapper = new TraineeMapperImpl();
    }

    @Test
    @DisplayName("Should map TraineeRegisterRequestDto to Trainee entity")
    void toEntity_ShouldMapRegisterDtoToEntity() {
        TraineeRegisterRequestDto dto = TraineeRegisterRequestDto.builder()
                .firstName("John")
                .lastName("Doe")
                .address("Main St 123")
                .dateOfBirth(new Date())
                .build();

        Trainee entity = traineeMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getUser()).isNotNull();
        assertThat(entity.getUser().getFirstName()).isEqualTo(dto.getFirstName());
        assertThat(entity.getUser().getLastName()).isEqualTo(dto.getLastName());
        assertThat(entity.getUser().getIsActive()).isTrue(); 
        assertThat(entity.getAddress()).isEqualTo(dto.getAddress());
        assertThat(entity.getDateOfBirth()).isEqualTo(dto.getDateOfBirth());
    }

    @Test
    @DisplayName("Should map Trainee entity to TraineeProfileResponseDto")
    void toProfileResponse_ShouldMapEntityToDto() {
        User user = User.builder()
                .firstName("Alice")
                .lastName("Wonderland")
                .isActive(true)
                .build();
        Trainee entity = Trainee.builder()
                .user(user)
                .address("Wonderland St")
                .dateOfBirth(new Date())
                .build();

        TraineeProfileResponseDto response = traineeMapper.toProfileResponse(entity);

        assertThat(response.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(response.getLastName()).isEqualTo(user.getLastName());
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getAddress()).isEqualTo(entity.getAddress());
    }

    @Test
    @DisplayName("Should update existing Trainee entity from UpdateDto")
    void updateEntityFromDto_ShouldUpdateFields() {
        Trainee entity = Trainee.builder()
                .user(User.builder().firstName("Old").lastName("Name").isActive(true).build())
                .address("Old Address")
                .build();

        TraineeUpdateRequestDto updateDto = TraineeUpdateRequestDto.builder()
                .firstName("New")
                .lastName("Lastname")
                .isActive(false)
                .address("New Address")
                .build();

        traineeMapper.updateEntityFromDto(updateDto, entity);

        assertThat(entity.getUser().getFirstName()).isEqualTo("New");
        assertThat(entity.getUser().getLastName()).isEqualTo("Lastname");
        assertThat(entity.getUser().getIsActive()).isFalse();
        assertThat(entity.getAddress()).isEqualTo("New Address");
    }
}