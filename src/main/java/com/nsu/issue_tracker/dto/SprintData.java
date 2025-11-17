package com.nsu.issue_tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SprintData(
        @NotBlank
        String name,

        @NotNull
        Long spintId,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate
)
{ }
