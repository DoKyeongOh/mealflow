package com.odk.pjt.mealflow.inventory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByIdAndUserId(Long id, Long userId);

    List<InventoryItem> findByUserIdOrderByExpirationDateAscIdAsc(Long userId);

    List<InventoryItem> findByUserIdAndGroceryTypeIdAndStorageLocationId(
            Long userId, Long groceryTypeId, Long storageLocationId);

    @Query(
            """
            SELECT i FROM InventoryItem i
            WHERE i.userId = :userId
            AND i.quantity > 0
            AND i.expirationDate IS NOT NULL AND i.expirationDate <= :untilDate
            ORDER BY i.expirationDate ASC, i.id ASC
            """)
    List<InventoryItem> findExpiringSoon(@Param("userId") Long userId, @Param("untilDate") LocalDate untilDate);

    /** Any inventory row referencing this storage blocks physical delete. */
    boolean existsByUserIdAndStorageLocationId(Long userId, Long storageLocationId);

    /** Any inventory row referencing this grocery type blocks physical delete. */
    boolean existsByUserIdAndGroceryTypeId(Long userId, Long groceryTypeId);

    List<InventoryItem> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, Pageable pageable);
}
