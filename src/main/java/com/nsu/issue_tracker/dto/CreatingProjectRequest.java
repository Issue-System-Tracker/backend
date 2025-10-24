package com.nsu.issue_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CreatingProjectRequest(

        @NotBlank
        @Size(min = 3, max = 100)
        String name


        // TODO ..
) {}