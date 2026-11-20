package com.industrial.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteServiceRequest(

        @NotBlank(message = "Resolution notes are required")
        @Size(max = 2000, message = "Resolution notes must be at most 2000 characters")
        String resolutionNotes
) {}