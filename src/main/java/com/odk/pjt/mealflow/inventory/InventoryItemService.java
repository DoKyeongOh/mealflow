package com.odk.pjt.mealflow.inventory;

import com.odk.pjt.mealflow.grocery.GroceryType;
import com.odk.pjt.mealflow.grocery.GroceryTypeRepository;
import com.odk.pjt.mealflow.inventory.dto.InventoryDtos;
import com.odk.pjt.mealflow.storage.StorageLocation;
import com.odk.pjt.mealflow.storage.StorageLocationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
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

    private final InventoryChangeEventService inventoryChangeEventService;
    private final InventoryItemRepository inventoryItemRepository;
    private final GroceryTypeRepository groceryTypeRepository;
    private final StorageLocationRepository storageLocationRepository;

    @Transactional(readOnly = true)
    public List<InventoryItem> list(Long userId) {
        return inventoryItemRepository.findByUserIdOrderByExpirationDateAscIdAsc(userId);
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
        return inventoryItemRepository.findExpiringSoon(userId, until);
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> listRecent(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), 100));
        return inventoryItemRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId, pageable);
    }

    /**
     * 신규 보관 행 생성(입고). 수량 증가·감소는 {@link #updateDetails}로 처리.
     */
    @Transactional
    public InventoryItem create(Long userId, InventoryDtos.CreateInventoryItemRequest request) {
        if (request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
        }

        requireGroceryType(userId, request.groceryTypeId());
        if (request.storageLocationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "storage location is required");
        }

        requireStorageLocation(userId, request.storageLocationId());
        if (request.expirationDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "expiration date is required");
        }

        InventoryItem saved = inventoryItemRepository.save(request.toInventoryItem(userId));
        inventoryChangeEventService.appendEvent(userId, null, saved);
        return saved;
    }

    /** 폐기 이벤트(after 없음) 기록 후 행 삭제. */
    @Transactional
    public InventoryItem delete(Long userId, Long id) {
        InventoryItem item = requireLineItem(userId, id);
        inventoryChangeEventService.appendEvent(userId, item, null);
        inventoryItemRepository.delete(item);
        return item;
    }

    @Transactional
    public InventoryItem updateDetails(Long userId, Long id, InventoryDtos.UpdateItemRequest request) {
        InventoryItem item = requireLineItem(userId, id);
        InventoryItem before = snapshotForEvent(item);
        if (request.quantity() != null && request.quantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be non-negative");
        }

        if (request.storageLocationId() != null
                && !Objects.equals(item.getStorageLocationId(), request.storageLocationId())) {
            requireStorageLocation(userId, request.storageLocationId());
        }

        if (request.groceryTypeId() != null && !Objects.equals(item.getGroceryTypeId(), request.groceryTypeId())) {
            requireGroceryType(userId, request.groceryTypeId());
        }

        boolean modified = request.toInventoryItem(item);
        if (!modified) {
            return item;
        }

        Instant now = Instant.now();
        item.setUpdatedAt(now);
        if (item.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            inventoryChangeEventService.appendEvent(userId, before, item);
            inventoryItemRepository.delete(item);
            return item;
        }

        inventoryItemRepository.save(item);
        inventoryChangeEventService.appendEvent(userId, before, snapshotForEvent(item));
        return item;
    }

    /** 영속되지 않는 스냅샷 — 이벤트의 before/after 비교용. */
    private static InventoryItem snapshotForEvent(InventoryItem src) {
        InventoryItem s = new InventoryItem();
        s.setId(src.getId());
        s.setUserId(src.getUserId());
        s.setGroceryTypeId(src.getGroceryTypeId());
        s.setStorageLocationId(src.getStorageLocationId());
        s.setQuantity(src.getQuantity());
        s.setUnit(src.getUnit());
        s.setExpirationDate(src.getExpirationDate());
        s.setCreatedAt(src.getCreatedAt());
        s.setUpdatedAt(src.getUpdatedAt());
        return s;
    }

    private InventoryItem requireLineItem(Long userId, Long id) {
        InventoryItem item = inventoryItemRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inventory line has no quantity");
        }
        return item;
    }

    private GroceryType requireGroceryType(Long userId, Long id) {
        return groceryTypeRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid grocery type"));
    }

    private StorageLocation requireStorageLocation(Long userId, Long id) {
        return storageLocationRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storage location"));
    }
}
