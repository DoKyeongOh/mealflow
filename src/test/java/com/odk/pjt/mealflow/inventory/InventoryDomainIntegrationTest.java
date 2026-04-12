package com.odk.pjt.mealflow.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.odk.pjt.mealflow.grocery.GroceryTypeService;
import com.odk.pjt.mealflow.storage.StorageLocationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InventoryDomainIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:h2:mem:mealflow_inv_it;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
    }

    private static final long USER = 9001L;

    @Autowired
    private StorageLocationService storageLocationService;

    @Autowired
    private GroceryTypeService groceryTypeService;

    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    private InventoryChangeEventRepository inventoryChangeEventRepository;

    private Long storageId;
    private Long groceryTypeId;

    @BeforeEach
    void setUp() {
        storageId = storageLocationService.create(USER, "Fridge").getId();
        groceryTypeId = groceryTypeService
                .create(USER, "Milk", storageId, 5)
                .getId();
    }

    @Test
    void stockIn_mergeAndEventsAreTransactional() {
        InventoryItem first =
                inventoryItemService.stockIn(USER, groceryTypeId, null, new BigDecimal("2"), GroceryUnit.L, null);
        assertThat(first.getQuantity()).isEqualByComparingTo(new BigDecimal("2"));
        assertThat(inventoryChangeEventRepository.findAll()).hasSize(1);

        InventoryItem merged =
                inventoryItemService.stockIn(USER, groceryTypeId, null, new BigDecimal("1"), GroceryUnit.L, null);
        assertThat(merged.getId()).isEqualTo(first.getId());
        assertThat(merged.getQuantity()).isEqualByComparingTo(new BigDecimal("3"));
        assertThat(inventoryChangeEventRepository.findAll()).hasSize(2);

        List<InventoryChangeEvent> events =
                inventoryItemService.listEventsForItem(USER, first.getId(), 10);
        assertThat(events).hasSize(2);
        assertThat(events.getFirst().getEventType()).isEqualTo(InventoryEventType.STOCK_IN);
    }

    @Test
    void use_reducesQuantityAndWritesUsedEvent() {
        InventoryItem item =
                inventoryItemService.stockIn(USER, groceryTypeId, null, new BigDecimal("3"), GroceryUnit.L, null);
        inventoryItemService.use(USER, item.getId(), new BigDecimal("1"), "cooking");
        InventoryItem updated = inventoryItemService.get(USER, item.getId());
        assertThat(updated.getQuantity()).isEqualByComparingTo(new BigDecimal("2"));
        List<InventoryChangeEvent> events =
                inventoryItemService.listEventsForItem(USER, item.getId(), 10);
        assertThat(events.stream().anyMatch(e -> e.getEventType() == InventoryEventType.USED))
                .isTrue();
    }

    @Test
    void expiringQuery_findsItemsWithinWindow() {
        LocalDate soon = LocalDate.now().plusDays(3);
        inventoryItemService.stockIn(
                USER, groceryTypeId, storageId, BigDecimal.ONE, GroceryUnit.COUNT, soon);
        List<InventoryItem> expiring = inventoryItemService.listExpiringWithin(USER, 7);
        assertThat(expiring).isNotEmpty();
    }

    @Test
    void userScope_listActiveOnlySeesSameUser() {
        inventoryItemService.stockIn(USER, groceryTypeId, null, BigDecimal.ONE, GroceryUnit.COUNT, null);
        List<InventoryItem> otherUser = inventoryItemService.listActive(USER + 1);
        assertThat(otherUser).isEmpty();
    }
}
