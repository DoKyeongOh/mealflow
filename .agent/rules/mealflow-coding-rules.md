# Mealflow 코딩 규칙

Mealflow 서비스 레이어에서 DTO 처리, 검증, 사용자 스코프, 이벤트 기록, 재고 정책을 구현할 때 따르는 기준을 정리한다.

이 문서는 코드의 형식보다 **서비스 구현 판단 기준**을 다룬다.  
제어문 스타일, 네이밍, 가독성 기준은 `mealflow-code-convention.md`에서 관리한다.

---

## 1. 요청 DTO에서 엔티티 조립을 담당한다

서비스는 비즈니스 흐름을 읽기 쉽게 유지하고, 요청 DTO는 엔티티 생성 또는 변경에 필요한 값 조립을 담당한다.

생성 요청은 DTO에서 엔티티를 만들어 반환한다.

```java
InventoryItem item = request.toInventoryItem(userId);
inventoryItemRepository.save(item);
```

예시:

```java
public InventoryItem toInventoryItem(Long userId) {
    Instant now = Instant.now();

    return InventoryItem.builder()
            .userId(userId)
            .groceryTypeId(groceryTypeId)
            .storageLocationId(storageLocationId)
            .quantity(quantity)
            .expirationDate(expirationDate)
            .createdAt(now)
            .updatedAt(now)
            .build();
}
```

부분 수정 요청은 기존 엔티티에 값을 반영한다.

```java
request.applyTo(item);
```

생성은 새 엔티티를 만드는 흐름이고, 수정은 기존 엔티티를 변경하는 흐름이므로 처리 방식을 구분한다.

---

## 2. 필수 비즈니스 값은 조용히 기본값으로 보정하지 않는다

비즈니스상 반드시 필요한 값이 요청에 없으면 서버에서 임의로 채우지 않고 명시적으로 실패시킨다.

대상 예시는 다음과 같다.

| 값 | 설명 |
|---|---|
| `storageLocationId` | 재고가 저장될 위치 |
| `expirationDate` | 유통기한 또는 소비기한 |
| `groceryTypeId` | 식료품 종류 |
| `quantity` | 재고 수량 |

잘못된 방식:

```java
if (request.storageLocationId() == null) {
    storageLocationId = groceryType.getDefaultStorageLocationId();
}
```

권장 방식:

```java
if (request.storageLocationId() == null) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "storageLocationId is required");
}
```

기본값 추천이 필요하면 서버가 별도 API로 추천값을 제공하고, 프론트가 폼에 채운 뒤 요청 본문에 포함해서 보낸다.

예시:

```http
GET /api/grocery-types/{id}/suggested-defaults
```

---

## 3. FK 존재 여부와 사용자 소유 범위는 서비스에서 검증한다

외래키로 참조하는 엔티티는 존재 여부뿐 아니라 현재 사용자의 소유 범위까지 확인한다.

```java
private GroceryType requireGroceryType(Long groceryTypeId, Long userId) {
    return groceryTypeRepository.findByIdAndUserId(groceryTypeId, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid groceryTypeId"));
}
```

```java
private StorageLocation requireStorageLocation(Long storageLocationId, Long userId) {
    return storageLocationRepository.findByIdAndUserId(storageLocationId, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storageLocationId"));
}
```

서비스 메서드 본문에 검증 로직을 반복해서 늘어놓기보다 `requireXxx` 형태의 private 헬퍼로 모은다.

---

## 4. 검증 책임을 계층별로 나눈다

단순 형식과 필수값은 DTO의 Bean Validation에서 처리한다.

```java
@NotNull
private Long groceryTypeId;

@NotNull
@DecimalMin("0.01")
private BigDecimal quantity;
```

도메인 흐름에 따라 판단해야 하는 규칙은 서비스에서 처리한다.

예시:

- PATCH에서 수량을 음수로 변경할 수 없음
- 다른 사용자의 저장 위치를 사용할 수 없음
- 사용 처리 후 수량이 0이면 삭제
- 이미 삭제된 재고를 다시 수정할 수 없음

```java
if (request.quantity() != null && request.quantity().compareTo(BigDecimal.ZERO) < 0) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity cannot be negative");
}
```

---

## 5. 사용자 스코프는 항상 repository 조회 조건에 포함한다

사용자별 데이터는 단순 `findById`로 조회하지 않는다.

지양:

```java
inventoryItemRepository.findById(itemId);
```

권장:

```java
inventoryItemRepository.findByIdAndUserId(itemId, userId);
```

이 규칙은 다른 사용자 데이터 접근을 막기 위한 기본 원칙이다.

---

## 6. 재고 변화 이벤트 기록은 별도 서비스에 위임한다

재고 생성, 수정, 사용, 삭제 등 이력이 필요한 동작은 `InventoryChangeEventService`에 위임한다.

```java
inventoryChangeEventService.recordCreated(item);
```

```java
inventoryChangeEventService.recordUsed(item, usedQuantity);
```

서비스 레이어는 재고 상태 변경을 담당하고, 이벤트 서비스는 이력 기록을 담당한다.

---

## 7. 이벤트용 스냅샷 생성은 별도 메서드로 분리한다

이벤트에 저장할 엔티티 상태가 필요하면 서비스 흐름 안에 직접 조립하지 않고 별도 메서드로 분리한다.

```java
private InventoryItemSnapshot snapshotForEvent(InventoryItem item) {
    return new InventoryItemSnapshot(
            item.getId(),
            item.getGroceryTypeId(),
            item.getStorageLocationId(),
            item.getQuantity(),
            item.getExpirationDate()
    );
}
```

이렇게 하면 이벤트 저장 구조가 바뀌어도 서비스의 핵심 흐름이 덜 흔들린다.

---

## 8. 수량이 0이 된 재고는 정책에 따라 일관되게 처리한다

Mealflow에서는 “0개 재고 행”을 남기지 않는 정책을 기본으로 한다.

따라서 사용 또는 수정 후 수량이 0이 되면 이벤트를 먼저 기록한 뒤 물리 삭제한다.

```java
item.decreaseQuantity(usedQuantity);
inventoryChangeEventService.recordUsed(item, usedQuantity);

if (item.isZeroQuantity()) {
    inventoryItemRepository.delete(item);
}
```

중요한 순서는 다음과 같다.

1. 재고 변경
2. 이벤트 기록
3. 수량 0 여부 확인
4. 삭제

이벤트 기록 전에 삭제하면 변경 당시의 상태를 남기기 어려워질 수 있다.

---

## 9. 생성 시각은 DTO에서 만들 수 있지만 필요하면 외부에서 주입한다

단순한 생성 흐름에서는 DTO 변환 메서드 안에서 `Instant.now()`를 사용할 수 있다.

```java
public InventoryItem toInventoryItem(Long userId) {
    Instant now = Instant.now();
    ...
}
```

테스트나 여러 엔티티 간 시각 일관성이 중요해지면 `now`를 인자로 받는 형태를 추가한다.

```java
public InventoryItem toInventoryItem(Long userId, Instant now) {
    ...
}
```

서비스에서는 다음처럼 사용할 수 있다.

```java
Instant now = Instant.now();
InventoryItem item = request.toInventoryItem(userId, now);
```

---

## 10. 이 문서는 서비스 구현 기준으로 유지한다

이 문서에는 Mealflow 서비스 레이어의 구현 판단 기준만 남긴다.

다음 내용은 이 문서에 둔다.

- DTO와 엔티티의 책임 분리
- 필수값 누락 처리
- FK 존재 여부와 사용자 소유 범위 검증
- Bean Validation과 서비스 검증의 역할 분리
- 이벤트 기록 방식
- 수량 0 처리 정책
- 생성/수정 시각 처리 방식

코드 스타일에 대한 내용은 `mealflow-code-convention.md`에서 관리한다.
