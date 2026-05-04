package com.odk.pjt.mealflow.inventoryevent;

import com.odk.pjt.mealflow.inventoryevent.dto.InventoryEventResponse;
import com.odk.pjt.mealflow.inventoryevent.model.InventoryChangeEvent;
import com.odk.pjt.mealflow.security.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory-change-events")
@RequiredArgsConstructor
public class InventoryChangeEventController {

    private final InventoryChangeEventService inventoryChangeEventService;

    @GetMapping
    public List<InventoryEventResponse> list(
            @RequestParam(required = false) Long groceryTypeId, @RequestParam(defaultValue = "50") int limit) {
        Long userId = SecurityUtils.requireCurrentUserId();
        List<InventoryChangeEvent> events;
        if (groceryTypeId != null) {
            events = inventoryChangeEventService.listEventsForGroceryType(userId, groceryTypeId, limit);
        } else {
            events = inventoryChangeEventService.listEventsForUser(userId, limit);
        }
        return events.stream().map(InventoryEventResponse::from).toList();
    }

    @GetMapping("/inventory-item/{id}")
    public List<InventoryEventResponse> eventsForItem(
            @PathVariable Long id, @RequestParam(defaultValue = "50") int limit) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return inventoryChangeEventService.listEventsForItem(userId, id, limit).stream()
                .map(InventoryEventResponse::from)
                .toList();
    }
}
