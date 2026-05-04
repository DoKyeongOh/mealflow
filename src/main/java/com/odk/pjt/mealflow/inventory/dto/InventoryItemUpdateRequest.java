package com.odk.pjt.mealflow.inventory.dto;

import com.odk.pjt.mealflow.inventory.model.InventoryItem;
import com.odk.pjt.mealflow.inventory.model.InventoryItemUnit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record InventoryItemUpdateRequest(
                BigDecimal quantity,
                InventoryItemUnit unit,
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
