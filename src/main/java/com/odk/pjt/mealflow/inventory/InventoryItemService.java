package com.odk.pjt.mealflow.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odk.pjt.mealflow.grocery.GroceryType;
import com.odk.pjt.mealflow.grocery.GroceryTypeRepository;
import com.odk.pjt.mealflow.storage.StorageLocation;
import com.odk.pjt.mealflow.storage.StorageLocationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryChangeEventRepository inventoryChangeEventRepository;
    private final GroceryTypeRepository groceryTypeRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<InventoryItem> listActive(Long userId) {
        return inventoryItemRepository.findByUserIdAndStatusOrderByExpirationDateAscIdAsc(
                userId, InventoryItemStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public InventoryItem get(Long userId, Long id) {
        return inventoryItemRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> listExpiringWithin(Long userId, int daysInclusive) {
        if (daysInclusive < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "days must be non-negative");
        }
        LocalDate until = LocalDate.now().plusDays(daysInclusive);
        return inventoryItemRepository.findExpiringSoon(userId, InventoryItemStatus.ACTIVE, until);
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> listRecent(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), 100));
        return inventoryItemRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<InventoryChangeEvent> listEventsForItem(Long userId, Long inventoryItemId, int limit) {
        get(userId, inventoryItemId);
        Pageable pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), 200));
        return inventoryChangeEventRepository.findByUserIdAndInventoryItemIdOrderByOccurredAtDescIdDesc(
                userId, inventoryItemId, pageable);
    }

    @Transactional(readOnly = true)
    public List<InventoryChangeEvent> listEventsForGroceryType(Long userId, Long groceryTypeId, int limit) {
        groceryTypeRepository
                .findByIdAndUserId(groceryTypeId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Pageable pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), 200));
        return inventoryChangeEventRepository.findByUserIdAndGroceryTypeIdOrderByOccurredAtDescIdDesc(
                userId, groceryTypeId, pageable);
    }

    @Transactional(readOnly = true)
    public List<InventoryChangeEvent> listEventsForUser(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), 200));
        return inventoryChangeEventRepository.findByUserIdOrderByOccurredAtDescIdDesc(userId, pageable);
    }

    /**
     * C-01/C-02: when storageLocationId / expirationDate are null, applies grocery type defaults.
     */
    @Transactional
    public InventoryItem stockIn(
            Long userId,
            Long groceryTypeId,
            Long storageLocationId,
            BigDecimal quantity,
            GroceryUnit unit,
            LocalDate expirationDate) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
        }
        GroceryType groceryType = requireActiveGroceryType(userId, groceryTypeId);
        Long resolvedStorageId = storageLocationId != null ? storageLocationId : groceryType.getDefaultStorageLocationId();
        if (resolvedStorageId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "storage location is required");
        }
        requireActiveStorage(userId, resolvedStorageId);
        LocalDate resolvedExpiration = expirationDate;
        if (resolvedExpiration == null && groceryType.getDefaultShelfLifeDays() != null) {
            resolvedExpiration = LocalDate.now().plusDays(groceryType.getDefaultShelfLifeDays());
        }

        Optional<InventoryItem> merge =
                findMergeCandidate(userId, groceryTypeId, resolvedStorageId, resolvedExpiration, unit);
        Instant now = Instant.now();
        if (merge.isPresent()) {
            InventoryItem item = merge.get();
            BigDecimal before = item.getQuantity();
            BigDecimal after = before.add(quantity);
            item.setQuantity(after);
            item.setUpdatedAt(now);
            inventoryItemRepository.save(item);
            appendEvent(
                    userId,
                    item.getId(),
                    groceryTypeId,
                    InventoryEventType.STOCK_IN,
                    before,
                    quantity,
                    after,
                    unit,
                    null,
                    null,
                    null);
            return item;
        }

        InventoryItem created = new InventoryItem();
        created.setUserId(userId);
        created.setGroceryTypeId(groceryTypeId);
        created.setStorageLocationId(resolvedStorageId);
        created.setQuantity(quantity);
        created.setUnit(unit);
        created.setExpirationDate(resolvedExpiration);
        created.setStatus(InventoryItemStatus.ACTIVE);
        created.setCreatedAt(now);
        created.setUpdatedAt(now);
        InventoryItem saved = inventoryItemRepository.save(created);
        appendEvent(
                userId,
                saved.getId(),
                groceryTypeId,
                InventoryEventType.STOCK_IN,
                null,
                quantity,
                quantity,
                unit,
                null,
                null,
                null);
        return saved;
    }

    @Transactional
    public InventoryItem use(Long userId, Long id, BigDecimal amount, String memo) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be positive");
        }
        InventoryItem item = requireActiveItem(userId, id);
        BigDecimal before = item.getQuantity();
        if (before.compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient quantity");
        }
        BigDecimal after = before.subtract(amount);
        Instant now = Instant.now();
        item.setQuantity(after);
        item.setUpdatedAt(now);
        if (after.compareTo(BigDecimal.ZERO) == 0) {
            item.setStatus(InventoryItemStatus.DEPLETED);
            item.setDepletedAt(now);
        }
        inventoryItemRepository.save(item);
        appendEvent(
                userId,
                id,
                item.getGroceryTypeId(),
                InventoryEventType.USED,
                before,
                amount.negate(),
                after,
                item.getUnit(),
                memo,
                null,
                null);
        return item;
    }

    @Transactional
    public InventoryItem dispose(Long userId, Long id, String memo) {
        InventoryItem item = requireActiveItem(userId, id);
        BigDecimal before = item.getQuantity();
        Instant now = Instant.now();
        item.setQuantity(BigDecimal.ZERO);
        item.setStatus(InventoryItemStatus.DEPLETED);
        item.setDepletedAt(now);
        item.setUpdatedAt(now);
        inventoryItemRepository.save(item);
        appendEvent(
                userId,
                id,
                item.getGroceryTypeId(),
                InventoryEventType.DISPOSED,
                before,
                before.negate(),
                BigDecimal.ZERO,
                item.getUnit(),
                memo,
                null,
                null);
        return item;
    }

    @Transactional
    public InventoryItem updateDetails(
            Long userId,
            Long id,
            BigDecimal quantity,
            GroceryUnit unit,
            LocalDate expirationDate,
            Long storageLocationId,
            Long groceryTypeId) {
        InventoryItem item = requireActiveItem(userId, id);
        Map<String, Object> changed = new HashMap<>();
        if (quantity != null && item.getQuantity().compareTo(quantity) != 0) {
            changed.put("quantity", Map.of("from", item.getQuantity(), "to", quantity));
            item.setQuantity(quantity);
        }
        if (unit != null && item.getUnit() != unit) {
            changed.put("unit", Map.of("from", item.getUnit().name(), "to", unit.name()));
            item.setUnit(unit);
        }
        if (expirationDate != null && !Objects.equals(item.getExpirationDate(), expirationDate)) {
            changed.put(
                    "expirationDate",
                    Map.of("from", item.getExpirationDate() != null ? item.getExpirationDate().toString() : null, "to", expirationDate.toString()));
            item.setExpirationDate(expirationDate);
        }
        if (storageLocationId != null && !Objects.equals(item.getStorageLocationId(), storageLocationId)) {
            requireActiveStorage(userId, storageLocationId);
            changed.put("storageLocationId", Map.of("from", item.getStorageLocationId(), "to", storageLocationId));
            item.setStorageLocationId(storageLocationId);
        }
        if (groceryTypeId != null && !Objects.equals(item.getGroceryTypeId(), groceryTypeId)) {
            requireActiveGroceryType(userId, groceryTypeId);
            changed.put("groceryTypeId", Map.of("from", item.getGroceryTypeId(), "to", groceryTypeId));
            item.setGroceryTypeId(groceryTypeId);
        }
        if (changed.isEmpty()) {
            return item;
        }
        Instant now = Instant.now();
        item.setUpdatedAt(now);
        inventoryItemRepository.save(item);
        String json;
        try {
            json = objectMapper.writeValueAsString(changed);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        appendEvent(
                userId,
                id,
                item.getGroceryTypeId(),
                InventoryEventType.MODIFIED,
                null,
                null,
                null,
                item.getUnit(),
                null,
                json,
                null);
        return item;
    }

    /** S-03: remove from active list — marks depleted without full dispose semantics if already zero. */
    @Transactional
    public InventoryItem completeOrRemove(Long userId, Long id) {
        InventoryItem item = get(userId, id);
        if (item.getStatus() == InventoryItemStatus.DEPLETED) {
            return item;
        }
        return dispose(userId, id, "removed");
    }

    private Optional<InventoryItem> findMergeCandidate(
            Long userId, Long groceryTypeId, Long storageLocationId, LocalDate expirationDate, GroceryUnit unit) {
        List<InventoryItem> candidates =
                inventoryItemRepository.findByUserIdAndGroceryTypeIdAndStorageLocationIdAndStatus(
                        userId, groceryTypeId, storageLocationId, InventoryItemStatus.ACTIVE);
        return candidates.stream()
                .filter(i -> i.getUnit() == unit)
                .filter(i -> Objects.equals(i.getExpirationDate(), expirationDate))
                .findFirst();
    }

    private InventoryItem requireActiveItem(Long userId, Long id) {
        InventoryItem item = inventoryItemRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (item.getStatus() != InventoryItemStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inventory item is not active");
        }
        return item;
    }

    private GroceryType requireActiveGroceryType(Long userId, Long id) {
        return groceryTypeRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid grocery type"));
    }

    private StorageLocation requireActiveStorage(Long userId, Long id) {
        return storageLocationRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storage location"));
    }

    private void appendEvent(
            Long userId,
            Long inventoryItemId,
            Long groceryTypeId,
            InventoryEventType eventType,
            BigDecimal countBefore,
            BigDecimal countDiff,
            BigDecimal countAfter,
            GroceryUnit unit,
            String memo,
            String changedFieldsJson,
            String payloadJson) {
        InventoryChangeEvent ev = new InventoryChangeEvent();
        ev.setUserId(userId);
        ev.setInventoryItemId(inventoryItemId);
        ev.setGroceryTypeId(groceryTypeId);
        ev.setEventType(eventType);
        ev.setCountBefore(countBefore);
        ev.setCountDiff(countDiff);
        ev.setCountAfter(countAfter);
        ev.setUnit(unit);
        ev.setOccurredAt(Instant.now());
        ev.setMemo(memo);
        ev.setChangedFieldsJson(changedFieldsJson);
        ev.setPayloadJson(payloadJson);
        inventoryChangeEventRepository.save(ev);
    }
}
