package com.odk.pjt.mealflow.grocery.dto;

import com.odk.pjt.mealflow.grocery.GroceryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class GroceryTypeDtos {

    private GroceryTypeDtos() {}

    public record CreateRequest(
            @NotBlank @Size(max = 255) String name,
            Long defaultStorageLocationId,
            Integer defaultShelfLifeDays) {}

    public record UpdateRequest(
            @NotBlank @Size(max = 255) String name,
            Long defaultStorageLocationId,
            Integer defaultShelfLifeDays) {}

    public record Response(
            Long id,
            String name,
            Long defaultStorageLocationId,
            Integer defaultShelfLifeDays,
            Instant createdAt,
            Instant updatedAt) {

        public static Response from(GroceryType e) {
            return new Response(
                    e.getId(),
                    e.getName(),
                    e.getDefaultStorageLocationId(),
                    e.getDefaultShelfLifeDays(),
                    e.getCreatedAt(),
                    e.getUpdatedAt());
        }
    }

    /** {@code true} if any inventory item uses this grocery type. */
    public record ReferenceStatusResponse(boolean referenced) {}
}
