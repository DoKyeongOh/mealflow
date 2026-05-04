package com.odk.pjt.mealflow.grocery.dto;

import com.odk.pjt.mealflow.grocery.model.GroceryType;
import java.time.Instant;

public record GroceryTypeResponse(
        Long id,
        String name,
        Long defaultStorageLocationId,
        Integer defaultShelfLifeDays,
        Instant createdAt,
        Instant updatedAt) {

    public static GroceryTypeResponse from(GroceryType e) {
        return new GroceryTypeResponse(
                e.getId(),
                e.getName(),
                e.getDefaultStorageLocationId(),
                e.getDefaultShelfLifeDays(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }
}
