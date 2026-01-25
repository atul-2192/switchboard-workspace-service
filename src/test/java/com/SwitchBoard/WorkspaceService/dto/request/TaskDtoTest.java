package com.SwitchBoard.WorkspaceService.dto.request;

import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TaskDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validTaskDto_passesValidation() {
        // Arrange
        TaskDto dto = new TaskDto();
        dto.setTitle("Valid Task Title");
        dto.setDescription("Description");
        dto.setPriority(3);
        dto.setRewardPoints(100);
        dto.setEstimatedHours(5.5);
        dto.setTitleColor("#FFFFFF");
        dto.setDeadline(Instant.now().plusSeconds(86400));

        // Act
        Set<ConstraintViolation<TaskDto>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankTitle_failsValidation() {
        // Arrange
        TaskDto dto = new TaskDto();
        dto.setTitle("");

        // Act
        Set<ConstraintViolation<TaskDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("required")));
    }

    @Test
    void invalidPriority_failsValidation() {
        // Arrange
        TaskDto dto = new TaskDto();
        dto.setTitle("Valid Title");
        dto.setPriority(10); // Invalid, must be 1-5

        // Act
        Set<ConstraintViolation<TaskDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void invalidHexColor_failsValidation() {
        // Arrange
        TaskDto dto = new TaskDto();
        dto.setTitle("Valid Title");
        dto.setTitleColor("red"); // Invalid, must be hex

        // Act
        Set<ConstraintViolation<TaskDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        // Arrange
        TaskDto dto = new TaskDto();
        Instant deadline = Instant.now();

        // Act
        dto.setTitle("Task");
        dto.setDescription("Desc");
        dto.setTaskTypeKey("type");
        dto.setStatusKey(TaskStatus.ONGOING);
        dto.setPriority(2);
        dto.setRewardPoints(50);
        dto.setEstimatedHours(3.0);
        dto.setTitleColor("#ABC123");
        dto.setOrderNumber(1);
        dto.setDeadline(deadline);

        // Assert
        assertEquals("Task", dto.getTitle());
        assertEquals("Desc", dto.getDescription());
        assertEquals("type", dto.getTaskTypeKey());
        assertEquals(TaskStatus.ONGOING, dto.getStatusKey());
        assertEquals(2, dto.getPriority());
        assertEquals(50, dto.getRewardPoints());
        assertEquals(3.0, dto.getEstimatedHours());
        assertEquals("#ABC123", dto.getTitleColor());
        assertEquals(1, dto.getOrderNumber());
        assertEquals(deadline, dto.getDeadline());
    }
}
