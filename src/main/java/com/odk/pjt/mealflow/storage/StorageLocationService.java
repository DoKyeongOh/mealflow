package com.odk.pjt.mealflow.storage;

import com.odk.pjt.mealflow.grocery.GroceryTypeRepository;
import com.odk.pjt.mealflow.inventory.InventoryItemRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StorageLocationService {

    private final StorageLocationRepository storageLocationRepository;
    private final GroceryTypeRepository groceryTypeRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Transactional(readOnly = true)
    public List<StorageLocation> list(Long userId) {
        return storageLocationRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional(readOnly = true)
    public StorageLocation get(Long userId, Long id) {
        return requireStorageLocation(userId, id);
    }

    /**
     * 보관 항목·식료품 종류(기본 저장소)가 이 저장소를 참조하는지 — 삭제 가능 여부 등에 사용.
     */
    @Transactional(readOnly = true)
    public boolean isReferenced(Long userId, Long storageLocationId) {
        requireStorageLocation(userId, storageLocationId);
        if (inventoryItemRepository.existsByUserIdAndStorageLocationId(userId, storageLocationId)) {
            return true;
        }
        return groceryTypeRepository.existsByUserIdAndDefaultStorageLocationId(userId, storageLocationId);
    }

    @Transactional
    public StorageLocation create(Long userId, String name) {
        String trimmed = name.trim();
        if (storageLocationRepository.existsByUserIdAndNameIgnoreCase(userId, trimmed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate storage location name");
        }
        Instant now = Instant.now();
        StorageLocation entity = new StorageLocation();
        entity.setUserId(userId);
        entity.setName(trimmed);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return storageLocationRepository.save(entity);
    }

    @Transactional
    public StorageLocation update(Long userId, Long id, String name) {
        StorageLocation entity = requireStorageLocation(userId, id);
        String trimmed = name.trim();
        if (storageLocationRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, trimmed, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate storage location name");
        }
        entity.setName(trimmed);
        entity.setUpdatedAt(Instant.now());
        return storageLocationRepository.save(entity);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        if (inventoryItemRepository.existsByUserIdAndStorageLocationId(userId, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cannot delete: inventory items reference this location");
        }
        if (groceryTypeRepository.existsByUserIdAndDefaultStorageLocationId(userId, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cannot delete: a grocery type uses this location as default");
        }
        if (storageLocationRepository.findByIdAndUserId(id, userId).isPresent()) {
            storageLocationRepository.deleteById(id);
        }
    }

    private StorageLocation requireStorageLocation(Long userId, Long id) {
        return storageLocationRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
