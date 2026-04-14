package com.odk.pjt.mealflow.grocery;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroceryTypeRepository extends JpaRepository<GroceryType, Long> {

    List<GroceryType> findByUserIdOrderByUpdatedAtAsc(Long userId);

    Optional<GroceryType> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);

    boolean existsByUserIdAndNameIgnoreCaseAndIdNot(Long userId, String name, Long id);

    boolean existsByUserIdAndDefaultStorageLocationId(Long userId, Long defaultStorageLocationId);
}
