package com.odk.pjt.mealflow.inventoryevent.dto;

import com.odk.pjt.mealflow.inventoryevent.model.InventoryChangeEvent;
import com.odk.pjt.mealflow.inventory.model.InventoryItemUnit;
import java.math.BigDecimal;
import java.time.Instant;

public record InventoryEventResponse(
                Long id,
                Long inventoryItemId,
                Long groceryTypeId,
                BigDecimal countBefore,
                BigDecimal countDiff,
                BigDecimal countAfter,
                InventoryItemUnit unit,
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
