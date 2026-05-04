package com.odk.pjt.mealflow.inventoryevent;

import java.util.List;
import org.springframework.data.domain.Pageable;
import com.odk.pjt.mealflow.inventoryevent.model.InventoryChangeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryChangeEventRepository extends JpaRepository<InventoryChangeEvent, Long> {

    List<InventoryChangeEvent> findByUserIdAndInventoryItemIdOrderByOccurredAtDescIdDesc(
            Long userId, Long inventoryItemId, Pageable pageable);

    List<InventoryChangeEvent> findByUserIdAndGroceryTypeIdOrderByOccurredAtDescIdDesc(
            Long userId, Long groceryTypeId, Pageable pageable);

    List<InventoryChangeEvent> findByUserIdOrderByOccurredAtDescIdDesc(Long userId, Pageable pageable);
}
