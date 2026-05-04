package com.odk.pjt.mealflow.storage.dto;

import com.odk.pjt.mealflow.storage.model.StorageLocation;
import java.time.Instant;

public record StorageLocationResponse(
        Long id, String name, Instant createdAt, Instant updatedAt) {

    public static StorageLocationResponse from(StorageLocation e) {
        return new StorageLocationResponse(e.getId(), e.getName(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
