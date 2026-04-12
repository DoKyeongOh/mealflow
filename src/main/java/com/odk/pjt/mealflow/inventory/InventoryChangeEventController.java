package com.odk.pjt.mealflow.inventory;

import com.odk.pjt.mealflow.inventory.dto.InventoryDtos;
import com.odk.pjt.mealflow.security.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory-change-events")
@RequiredArgsConstructor
public class InventoryChangeEventController {

    private final InventoryItemService inventoryItemService;

    @GetMapping
    public List<InventoryDtos.InventoryEventResponse> list(
            @RequestParam(required = false) Long groceryTypeId, @RequestParam(defaultValue = "50") int limit) {
        Long userId = SecurityUtils.requireCurrentUserId();
        List<InventoryChangeEvent> events;
        if (groceryTypeId != null) {
            events = inventoryItemService.listEventsForGroceryType(userId, groceryTypeId, limit);
        } else {
            events = inventoryItemService.listEventsForUser(userId, limit);
        }
        return events.stream().map(InventoryDtos.InventoryEventResponse::from).toList();
    }
}
