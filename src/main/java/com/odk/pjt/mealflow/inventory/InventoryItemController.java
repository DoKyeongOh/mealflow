package com.odk.pjt.mealflow.inventory;

import com.odk.pjt.mealflow.grocery.GroceryType;
import com.odk.pjt.mealflow.grocery.GroceryTypeService;
import com.odk.pjt.mealflow.inventory.dto.InventoryDtos;
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
    private final GroceryTypeService groceryTypeService;

    @GetMapping
    public List<InventoryDtos.InventoryItemResponse> list() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return inventoryItemService.listActive(userId).stream()
                .map(InventoryDtos.InventoryItemResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public InventoryDtos.InventoryItemResponse get(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return InventoryDtos.InventoryItemResponse.from(inventoryItemService.get(userId, id));
    }

    /** C-03: items expiring on or before today + withinDays. */
    @GetMapping("/expiring")
    public List<InventoryDtos.InventoryItemResponse> expiring(@RequestParam(defaultValue = "7") int withinDays) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return inventoryItemService.listExpiringWithin(userId, withinDays).stream()
                .map(InventoryDtos.InventoryItemResponse::from)
                .toList();
    }

    /** C-04: recently created items for reuse. */
    @GetMapping("/recent")
    public List<InventoryDtos.InventoryItemResponse> recent(@RequestParam(defaultValue = "10") int limit) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return inventoryItemService.listRecent(userId, limit).stream()
                .map(InventoryDtos.InventoryItemResponse::from)
                .toList();
    }

    /** C-01/C-02: suggested expiration and storage from grocery type defaults. */
    @GetMapping("/suggested-defaults")
    public InventoryDtos.SuggestedDefaultsResponse suggestedDefaults(@RequestParam Long groceryTypeId) {
        Long userId = SecurityUtils.requireCurrentUserId();
        GroceryType gt = groceryTypeService.get(userId, groceryTypeId);
        LocalDate suggestedExpiration = null;
        if (gt.getDefaultShelfLifeDays() != null) {
            suggestedExpiration = LocalDate.now().plusDays(gt.getDefaultShelfLifeDays());
        }
        return new InventoryDtos.SuggestedDefaultsResponse(suggestedExpiration, gt.getDefaultStorageLocationId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryDtos.InventoryItemResponse stockIn(@Valid @RequestBody InventoryDtos.StockInRequest body) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return InventoryDtos.InventoryItemResponse.from(inventoryItemService.stockIn(
                userId,
                body.groceryTypeId(),
                body.storageLocationId(),
                body.quantity(),
                body.unit(),
                body.expirationDate()));
    }

    @PostMapping("/{id}/use")
    public InventoryDtos.InventoryItemResponse use(
            @PathVariable Long id, @Valid @RequestBody InventoryDtos.UseRequest body) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return InventoryDtos.InventoryItemResponse.from(
                inventoryItemService.use(userId, id, body.amount(), body.memo()));
    }

    @PostMapping("/{id}/dispose")
    public InventoryDtos.InventoryItemResponse dispose(
            @PathVariable Long id, @RequestBody(required = false) InventoryDtos.DisposeRequest body) {
        Long userId = SecurityUtils.requireCurrentUserId();
        String memo = body != null ? body.memo() : null;
        return InventoryDtos.InventoryItemResponse.from(inventoryItemService.dispose(userId, id, memo));
    }

    @PatchMapping("/{id}")
    public InventoryDtos.InventoryItemResponse patch(
            @PathVariable Long id, @Valid @RequestBody InventoryDtos.UpdateItemRequest body) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return InventoryDtos.InventoryItemResponse.from(inventoryItemService.updateDetails(
                userId,
                id,
                body.quantity(),
                body.unit(),
                body.expirationDate(),
                body.storageLocationId(),
                body.groceryTypeId()));
    }

    @DeleteMapping("/{id}")
    public InventoryDtos.InventoryItemResponse remove(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return InventoryDtos.InventoryItemResponse.from(inventoryItemService.completeOrRemove(userId, id));
    }

    @GetMapping("/{id}/events")
    public List<InventoryDtos.InventoryEventResponse> eventsForItem(
            @PathVariable Long id, @RequestParam(defaultValue = "50") int limit) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return inventoryItemService.listEventsForItem(userId, id, limit).stream()
                .map(InventoryDtos.InventoryEventResponse::from)
                .toList();
    }
}
