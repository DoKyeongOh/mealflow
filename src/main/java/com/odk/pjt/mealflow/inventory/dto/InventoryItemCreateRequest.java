package com.odk.pjt.mealflow.inventory.dto;

import com.odk.pjt.mealflow.inventory.model.InventoryItem;
import com.odk.pjt.mealflow.inventory.model.InventoryItemUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record InventoryItemCreateRequest(
                @NotNull Long groceryTypeId,
                @NotNull Long storageLocationId,
                @NotNull @DecimalMin("0.0001") BigDecimal quantity,
                @NotNull InventoryItemUnit unit,
                @NotNull LocalDate expirationDate) {

        /** 영속 전 엔티티 — {@code createdAt}/{@code updatedAt}에 {@code now}를 넣는다. */
        public InventoryItem toInventoryItem(Long userId) {
                InventoryItem e = new InventoryItem();
                e.setUserId(userId);
                e.setGroceryTypeId(groceryTypeId);
                e.setStorageLocationId(storageLocationId);
                e.setQuantity(quantity);
                e.setUnit(unit);
                e.setExpirationDate(expirationDate);
                e.setCreatedAt(Instant.now());
                e.setUpdatedAt(Instant.now());
                return e;
        }
}
