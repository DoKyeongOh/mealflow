package com.odk.pjt.mealflow.grocery;

import com.odk.pjt.mealflow.inventory.InventoryItemRepository;
import com.odk.pjt.mealflow.storage.StorageLocationRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GroceryTypeService {

    private final GroceryTypeRepository groceryTypeRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Transactional(readOnly = true)
    public List<GroceryType> list(Long userId) {
        return groceryTypeRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional(readOnly = true)
    public GroceryType get(Long userId, Long id) {
        return groceryTypeRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * 보관 항목이 이 식료품 종류를 참조하는지 — 삭제 가능 여부 등에 사용.
     */
    @Transactional(readOnly = true)
    public boolean isReferenced(Long userId, Long groceryTypeId) {
        groceryTypeRepository
                .findByIdAndUserId(groceryTypeId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return inventoryItemRepository.existsByUserIdAndGroceryTypeId(userId, groceryTypeId);
    }

    @Transactional
    public GroceryType create(Long userId, String name, Long defaultStorageLocationId, Integer defaultShelfLifeDays) {
        String trimmed = name.trim();
        if (groceryTypeRepository.existsByUserIdAndNameIgnoreCase(userId, trimmed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate grocery type name");
        }
        validateDefaultStorage(userId, defaultStorageLocationId);
        Instant now = Instant.now();
        GroceryType entity = new GroceryType();
        entity.setUserId(userId);
        entity.setName(trimmed);
        entity.setDefaultStorageLocationId(defaultStorageLocationId);
        entity.setDefaultShelfLifeDays(defaultShelfLifeDays);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return groceryTypeRepository.save(entity);
    }

    @Transactional
    public GroceryType update(
            Long userId, Long id, String name, Long defaultStorageLocationId, Integer defaultShelfLifeDays) {
        GroceryType entity = groceryTypeRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String trimmed = name.trim();
        if (groceryTypeRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, trimmed, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate grocery type name");
        }
        validateDefaultStorage(userId, defaultStorageLocationId);
        entity.setName(trimmed);
        entity.setDefaultStorageLocationId(defaultStorageLocationId);
        entity.setDefaultShelfLifeDays(defaultShelfLifeDays);
        entity.setUpdatedAt(Instant.now());
        return groceryTypeRepository.save(entity);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        GroceryType entity = groceryTypeRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (inventoryItemRepository.existsByUserIdAndGroceryTypeId(userId, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cannot delete: inventory items reference this grocery type");
        }
        groceryTypeRepository.delete(entity);
    }

    private void validateDefaultStorage(Long userId, Long defaultStorageLocationId) {
        if (defaultStorageLocationId == null) {
            return;
        }
        storageLocationRepository
                .findByIdAndUserId(defaultStorageLocationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid default storage location"));
    }
}
