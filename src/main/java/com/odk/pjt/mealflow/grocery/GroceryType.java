package com.odk.pjt.mealflow.grocery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "grocery_types",
        uniqueConstraints = @UniqueConstraint(name = "uk_grocery_types_user_name", columnNames = {"user_id", "name"}),
        indexes = {
            @Index(name = "idx_grocery_types_user_id", columnList = "user_id"),
            @Index(name = "idx_grocery_types_default_storage", columnList = "default_storage_location_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class GroceryType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "default_storage_location_id")
    private Long defaultStorageLocationId;

    @Column(name = "default_shelf_life_days")
    private Integer defaultShelfLifeDays;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
