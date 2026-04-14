package com.odk.pjt.mealflow.grocery;

import com.odk.pjt.mealflow.grocery.dto.GroceryTypeDtos;
import com.odk.pjt.mealflow.inventory.InventoryItemRepository;
import com.odk.pjt.mealflow.storage.StorageLocation;
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
        return groceryTypeRepository.findByUserIdOrderByUpdatedAtAsc(userId);
    }

    @Transactional(readOnly = true)
    public GroceryType get(Long userId, Long id) {
        return requireGroceryType(userId, id);
    }

    /**
     * 보관 항목이 이 식료품 종류를 참조하는지 — 삭제 전 확인 등.
     */
    @Transactional(readOnly = true)
    public boolean isReferenced(Long userId, Long groceryTypeId) {
        requireGroceryType(userId, groceryTypeId);
        return inventoryItemRepository.existsByUserIdAndGroceryTypeId(userId, groceryTypeId);
    }

    @Transactional
    public GroceryType create(Long userId, GroceryTypeDtos.CreateRequest request) {
        if (groceryTypeRepository.existsByUserIdAndNameIgnoreCase(userId, request.name().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate grocery type name");
        }

        requireStorageLocation(userId, request.defaultStorageLocationId());
        Instant now = Instant.now();
        return groceryTypeRepository.save(request.toGroceryType(userId, now));
    }

    @Transactional
    public GroceryType update(Long userId, Long id, GroceryTypeDtos.UpdateRequest request) {
        GroceryType entity = requireGroceryType(userId, id);

        if (groceryTypeRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, request.name().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate grocery type name");
        }

        requireStorageLocation(userId, request.defaultStorageLocationId());
        return groceryTypeRepository.save(request.toGroceryType(entity));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        if (inventoryItemRepository.existsByUserIdAndGroceryTypeId(userId, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cannot delete: inventory items reference this grocery type");
        }

        if (groceryTypeRepository.findByIdAndUserId(id, userId).isPresent()) {
            groceryTypeRepository.deleteById(id);
        }
    }

    private GroceryType requireGroceryType(Long userId, Long id) {
        return groceryTypeRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private StorageLocation requireStorageLocation(Long userId, Long storageLocationId) {
        return storageLocationRepository
                .findByIdAndUserId(storageLocationId, userId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid default storage location"));
    }
}
