package com.odk.pjt.mealflow.grocery.dto;

import com.odk.pjt.mealflow.grocery.GroceryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class GroceryTypeDtos {

    private GroceryTypeDtos() {
    }

    public record CreateRequest(
            @NotBlank @Size(max = 255) String name,
            Long defaultStorageLocationId,
            Integer defaultShelfLifeDays) {

        public GroceryType toGroceryType(Long userId, Instant now) {
            GroceryType entity = new GroceryType();
            entity.setUserId(userId);
            entity.setName(name.trim());
            entity.setDefaultStorageLocationId(defaultStorageLocationId);
            entity.setDefaultShelfLifeDays(defaultShelfLifeDays);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            return entity;
        }
    }

    public record UpdateRequest(
            @NotBlank @Size(max = 255) String name,
            Long defaultStorageLocationId,
            Integer defaultShelfLifeDays) {

        public GroceryType toGroceryType(GroceryType entity) {
            entity.setName(name.trim());
            entity.setDefaultStorageLocationId(defaultStorageLocationId);
            entity.setDefaultShelfLifeDays(defaultShelfLifeDays);
            entity.setUpdatedAt(Instant.now());
            return entity;
        }
    }

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

    /** {@code true} if any inventory item references this grocery type. */
    public record ReferenceStatusResponse(boolean referenced) {}
}
