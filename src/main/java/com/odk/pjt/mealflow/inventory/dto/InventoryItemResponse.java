package com.odk.pjt.mealflow.inventory.dto;

import com.odk.pjt.mealflow.inventory.model.InventoryItem;
import com.odk.pjt.mealflow.inventory.model.InventoryItemUnit;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record InventoryItemResponse(
                Long id,
                Long groceryTypeId,
                Long storageLocationId,
                BigDecimal quantity,
                InventoryItemUnit unit,
                LocalDate expirationDate,
                Instant createdAt,
                Instant updatedAt) {

        public static InventoryItemResponse from(InventoryItem e) {
                return new InventoryItemResponse(
                                e.getId(),
                                e.getGroceryTypeId(),
                                e.getStorageLocationId(),
                                e.getQuantity(),
                                e.getUnit(),
                                e.getExpirationDate(),
                                e.getCreatedAt(),
                                e.getUpdatedAt());
        }
}
