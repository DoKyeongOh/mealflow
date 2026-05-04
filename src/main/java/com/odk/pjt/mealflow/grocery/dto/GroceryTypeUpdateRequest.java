package com.odk.pjt.mealflow.grocery.dto;

import com.odk.pjt.mealflow.grocery.model.GroceryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record GroceryTypeUpdateRequest(
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
