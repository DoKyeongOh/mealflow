package com.odk.pjt.mealflow.inventory;

import com.odk.pjt.mealflow.grocery.GroceryTypeRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class InventoryChangeEventService {

    private final InventoryChangeEventRepository inventoryChangeEventRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final GroceryTypeRepository groceryTypeRepository;

    @Transactional(readOnly = true)
    public List<InventoryChangeEvent> listEventsForItem(Long userId, Long inventoryItemId, int limit) {
        inventoryItemRepository
                .findByIdAndUserId(inventoryItemId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Pageable pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), 200));
        return inventoryChangeEventRepository.findByUserIdAndInventoryItemIdOrderByOccurredAtDescIdDesc(
                userId, inventoryItemId, pageable);
    }

    @Transactional(readOnly = true)
    public List<InventoryChangeEvent> listEventsForGroceryType(Long userId, Long groceryTypeId, int limit) {
        groceryTypeRepository
                .findByIdAndUserId(groceryTypeId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Pageable pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), 200));
        return inventoryChangeEventRepository.findByUserIdAndGroceryTypeIdOrderByOccurredAtDescIdDesc(
                userId, groceryTypeId, pageable);
    }

    @Transactional(readOnly = true)
    public List<InventoryChangeEvent> listEventsForUser(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), 200));
        return inventoryChangeEventRepository.findByUserIdOrderByOccurredAtDescIdDesc(userId, pageable);
    }

    /**
     * @param beforeItem 변경 전 스냅샷. 신규 행 생성처럼 이전 상태가 없으면 {@code null}.
     * @param afterItem  반영 후 보관 항목. 행 삭제 직후는 {@code null}이며, 식별·단위는 {@code beforeItem}을 쓰고
     *                   수량은 0으로 기록한다.
     *                   <p>
     *                   입고/이동/폐기 같은 해석은 저장하지 않는다. 수량 변화는 {@code count_*}로만 남기고, 장소·유통기한
     *                   등 비수량 변경은 별도 확장(예: 변경 필드 스냅샷)으로 다루는 것을 권장한다.
     */
    @Transactional
    public void appendEvent(Long userId, InventoryItem beforeItem, InventoryItem afterItem) {
        InventoryItem anchor = afterItem != null ? afterItem : beforeItem;
        if (anchor == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "event requires at least one of before/after item");
        }
        InventoryChangeEvent ev = new InventoryChangeEvent();
        ev.setUserId(userId);
        ev.setInventoryItemId(anchor.getId());
        ev.setGroceryTypeId(anchor.getGroceryTypeId());
        ev.setUnit(anchor.getUnit());
        ev.setOccurredAt(Instant.now());
        BigDecimal countAfter;
        BigDecimal countBefore;
        if (afterItem == null) {
            countBefore = beforeItem != null ? beforeItem.getQuantity() : null;
            countAfter = BigDecimal.ZERO;
        } else {
            countAfter = afterItem.getQuantity();
            countBefore = beforeItem != null ? beforeItem.getQuantity() : null;
        }
        BigDecimal countDiff = countBefore != null ? countAfter.subtract(countBefore) : countAfter;
        ev.setCountBefore(countBefore);
        ev.setCountDiff(countDiff);
        ev.setCountAfter(countAfter);
        inventoryChangeEventRepository.save(ev);
    }
}
