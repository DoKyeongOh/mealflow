package com.odk.pjt.mealflow.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "inventory_items",
        indexes = {
            @Index(name = "idx_inventory_items_user_grocery", columnList = "user_id,grocery_type_id"),
            @Index(name = "idx_inventory_items_user_storage", columnList = "user_id,storage_location_id"),
            @Index(name = "idx_inventory_items_user_expiration", columnList = "user_id,expiration_date")
        })
@Getter
@Setter
@NoArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "grocery_type_id", nullable = false)
    private Long groceryTypeId;

    @Column(name = "storage_location_id", nullable = false)
    private Long storageLocationId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GroceryUnit unit;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
