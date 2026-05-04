package com.odk.pjt.mealflow.storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StorageLocationUpdateRequest(
        @NotBlank @Size(max = 255) String name) {}
