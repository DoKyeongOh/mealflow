package com.odk.pjt.mealflow.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "inventory_change_events",
        indexes = {
            @Index(name = "idx_ice_user_occurred", columnList = "user_id,occurred_at"),
            @Index(name = "idx_ice_item_occurred", columnList = "inventory_item_id,occurred_at"),
            @Index(name = "idx_ice_grocery_occurred", columnList = "grocery_type_id,occurred_at")
        })
@Getter
@Setter
@NoArgsConstructor
public class InventoryChangeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "inventory_item_id", nullable = false)
    private Long inventoryItemId;

    @Column(name = "grocery_type_id", nullable = false)
    private Long groceryTypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private InventoryEventType eventType;

    @Column(name = "count_before", precision = 19, scale = 4)
    private BigDecimal countBefore;

    @Column(name = "count_diff", precision = 19, scale = 4)
    private BigDecimal countDiff;

    @Column(name = "count_after", precision = 19, scale = 4)
    private BigDecimal countAfter;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private GroceryUnit unit;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
