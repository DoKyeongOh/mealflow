package com.odk.pjt.mealflow.inventory.dto;

import com.odk.pjt.mealflow.inventory.GroceryUnit;
import com.odk.pjt.mealflow.inventory.InventoryChangeEvent;
import com.odk.pjt.mealflow.inventory.InventoryEventType;
import com.odk.pjt.mealflow.inventory.InventoryItem;
import com.odk.pjt.mealflow.inventory.InventoryItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class InventoryDtos {

    private InventoryDtos() {}

    public record StockInRequest(
            @NotNull Long groceryTypeId,
            Long storageLocationId,
            @NotNull @DecimalMin("0.0001") BigDecimal quantity,
            @NotNull GroceryUnit unit,
            LocalDate expirationDate) {}

    public record UseRequest(
            @NotNull @DecimalMin("0.0001") BigDecimal amount, String memo) {}

    public record DisposeRequest(String memo) {}

    public record UpdateItemRequest(
            BigDecimal quantity,
            GroceryUnit unit,
            LocalDate expirationDate,
            Long storageLocationId,
            Long groceryTypeId) {}

    public record InventoryItemResponse(
            Long id,
            Long groceryTypeId,
            Long storageLocationId,
            BigDecimal quantity,
            GroceryUnit unit,
            LocalDate expirationDate,
            InventoryItemStatus status,
            Instant depletedAt,
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
                    e.getStatus(),
                    e.getDepletedAt(),
                    e.getCreatedAt(),
                    e.getUpdatedAt());
        }
    }

    public record InventoryEventResponse(
            Long id,
            Long inventoryItemId,
            Long groceryTypeId,
            InventoryEventType eventType,
            BigDecimal countBefore,
            BigDecimal countDiff,
            BigDecimal countAfter,
            GroceryUnit unit,
            Instant occurredAt,
            String memo,
            String changedFieldsJson) {

        public static InventoryEventResponse from(InventoryChangeEvent e) {
            return new InventoryEventResponse(
                    e.getId(),
                    e.getInventoryItemId(),
                    e.getGroceryTypeId(),
                    e.getEventType(),
                    e.getCountBefore(),
                    e.getCountDiff(),
                    e.getCountAfter(),
                    e.getUnit(),
                    e.getOccurredAt(),
                    e.getMemo(),
                    e.getChangedFieldsJson());
        }
    }

    public record SuggestedDefaultsResponse(LocalDate suggestedExpirationDate, Long suggestedStorageLocationId) {}
}
