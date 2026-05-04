package com.odk.pjt.mealflow.inventory;

import com.odk.pjt.mealflow.grocery.model.GroceryType;
import com.odk.pjt.mealflow.grocery.GroceryTypeService;
import com.odk.pjt.mealflow.inventory.dto.InventoryItemCreateRequest;
import com.odk.pjt.mealflow.inventory.dto.InventoryItemResponse;
import com.odk.pjt.mealflow.inventory.dto.InventoryItemUpdateRequest;

import com.odk.pjt.mealflow.security.SecurityUtils;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory-items")
@RequiredArgsConstructor
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;

    @GetMapping
    public List<InventoryItemResponse> list() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return inventoryItemService.list(userId).stream()
                .map(InventoryItemResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public InventoryItemResponse get(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return InventoryItemResponse.from(inventoryItemService.get(userId, id));
    }

    /** C-03: items expiring on or before today + withinDays. */
    @GetMapping("/expiring")
    public List<InventoryItemResponse> expiring(@RequestParam(defaultValue = "7") int withinDays) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return inventoryItemService.listExpiringWithin(userId, withinDays).stream()
                .map(InventoryItemResponse::from)
                .toList();
    }

    /** C-04: recently created items for reuse. */
    @GetMapping("/recent")
    public List<InventoryItemResponse> recent(@RequestParam(defaultValue = "10") int limit) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return inventoryItemService.listRecent(userId, limit).stream()
                .map(InventoryItemResponse::from)
                .toList();
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryItemResponse create(@Valid @RequestBody InventoryItemCreateRequest body) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return InventoryItemResponse.from(inventoryItemService.create(userId, body));
    }

    @PatchMapping("/{id}")
    public InventoryItemResponse patch(
            @PathVariable Long id, @Valid @RequestBody InventoryItemUpdateRequest body) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return InventoryItemResponse.from(inventoryItemService.updateDetails(userId, id, body));
    }

    @DeleteMapping("/{id}")
    public InventoryItemResponse delete(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return InventoryItemResponse.from(inventoryItemService.delete(userId, id));
    }
}
