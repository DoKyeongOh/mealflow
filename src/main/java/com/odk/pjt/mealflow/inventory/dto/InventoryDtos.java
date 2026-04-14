package com.odk.pjt.mealflow.inventory.dto;

import com.odk.pjt.mealflow.inventory.GroceryUnit;
import com.odk.pjt.mealflow.inventory.InventoryChangeEvent;
import com.odk.pjt.mealflow.inventory.InventoryItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public final class InventoryDtos {

        private InventoryDtos() {
        }

        public record CreateInventoryItemRequest(
                        @NotNull Long groceryTypeId,
                        @NotNull Long storageLocationId,
                        @NotNull @DecimalMin("0.0001") BigDecimal quantity,
                        @NotNull GroceryUnit unit,
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

        public record UpdateItemRequest(
                        BigDecimal quantity,
                        GroceryUnit unit,
                        LocalDate expirationDate,
                        Long storageLocationId,
                        Long groceryTypeId) {

                /** 비-null 필드만 엔티티에 반영. 값이 바뀌었으면 {@code true}. */
                public boolean toInventoryItem(InventoryItem item) {
                        boolean modified = false;
                        if (quantity != null && item.getQuantity().compareTo(quantity) != 0) {
                                item.setQuantity(quantity);
                                modified = true;
                        }
                        if (unit != null && item.getUnit() != unit) {
                                item.setUnit(unit);
                                modified = true;
                        }
                        if (expirationDate != null && !Objects.equals(item.getExpirationDate(), expirationDate)) {
                                item.setExpirationDate(expirationDate);
                                modified = true;
                        }
                        if (storageLocationId != null
                                        && !Objects.equals(item.getStorageLocationId(), storageLocationId)) {
                                item.setStorageLocationId(storageLocationId);
                                modified = true;
                        }
                        if (groceryTypeId != null && !Objects.equals(item.getGroceryTypeId(), groceryTypeId)) {
                                item.setGroceryTypeId(groceryTypeId);
                                modified = true;
                        }
                        return modified;
                }
        }

        public record InventoryItemResponse(
                        Long id,
                        Long groceryTypeId,
                        Long storageLocationId,
                        BigDecimal quantity,
                        GroceryUnit unit,
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

        public record InventoryEventResponse(
                        Long id,
                        Long inventoryItemId,
                        Long groceryTypeId,
                        BigDecimal countBefore,
                        BigDecimal countDiff,
                        BigDecimal countAfter,
                        GroceryUnit unit,
                        Instant occurredAt) {

                public static InventoryEventResponse from(InventoryChangeEvent e) {
                        return new InventoryEventResponse(
                                        e.getId(),
                                        e.getInventoryItemId(),
                                        e.getGroceryTypeId(),
                                        e.getCountBefore(),
                                        e.getCountDiff(),
                                        e.getCountAfter(),
                                        e.getUnit(),
                                        e.getOccurredAt());
                }
        }

        public record SuggestedDefaultsResponse(LocalDate suggestedExpirationDate, Long suggestedStorageLocationId) {
        }
}
