package com.odk.pjt.mealflow.storage.dto;

import com.odk.pjt.mealflow.storage.StorageLocation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class StorageLocationDtos {

    private StorageLocationDtos() {}

    public record CreateRequest(
            @NotBlank @Size(max = 255) String name) {}

    public record UpdateRequest(
            @NotBlank @Size(max = 255) String name) {}

    public record Response(
            Long id, String name, Instant createdAt, Instant updatedAt) {

        public static Response from(StorageLocation e) {
            return new Response(e.getId(), e.getName(), e.getCreatedAt(), e.getUpdatedAt());
        }
    }

    /** {@code true} if any inventory item or active grocery type default points at this storage. */
    public record ReferenceStatusResponse(boolean referenced) {}
}
