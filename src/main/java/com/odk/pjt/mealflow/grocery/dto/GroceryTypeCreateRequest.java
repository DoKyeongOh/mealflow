package com.odk.pjt.mealflow.grocery.dto;

import com.odk.pjt.mealflow.grocery.model.GroceryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record GroceryTypeCreateRequest(
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
