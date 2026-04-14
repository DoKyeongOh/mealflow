# Mealflow 서비스 레이어·DTO 규칙 (InventoryItemService 정리 기준)

`InventoryItemService` 및 인접 DTO를 다루면서 중요하게 본 원칙과, 함께 두면 좋은 관례를 정리한다.

---

## 1. 요청 DTO → 엔티티 조립은 DTO 메서드로 (서비스 단순화)

- **생성(create)** 같은 “한 번에 엔티티를 채우는” 흐름은 `CreateInventoryItemRequest`에 `toInventoryItem(Long userId)`처럼 **엔티티를 만드는 메서드**를 두고, 서비스에서는 검증 후 `repository.save(request.toInventoryItem(userId))` 수준으로 둔다.
- **수정(patch)** 은 필드가 부분 갱신이므로 `UpdateItemRequest.toInventoryItem(InventoryItem item)`처럼 **기존 엔티티에 반영**하는 형태가 맞다.
- 메서드 이름은 `toEntity()` / `toInventoryItem()` 등 팀에서 통일하면 된다. (도메인 엔티티 이름이 분명하면 `toInventoryItem`처럼 구체적으로도 무방하다.)

---

## 2. 참조 필드·필수 입력이 비어 있으면 “기본값 채우기” 대신 예외

- 저장소(`storageLocationId`), 유통기한(`expirationDate`) 등 **비즈니스상 반드시 있어야 하는 값**은, 다른 엔티티(예: 식료품 마스터의 기본 저장소·기본 유통기한)로 **조용히 보정하지 않는다.**
- 누락 시에는 **400 Bad Request** 등으로 명시적으로 실패시키고, 기본값이 필요하면 **프론트가 `suggested-defaults` 등으로 받아 폼에 채운 뒤** 요청 본문에 실어 보낸다.
- FK 존재·소유 범위 검증은 서비스의 `requireGroceryType`, `requireStorageLocation` 같은 **private 헬퍼**로 모은다.

---

## 3. 제어문 스타일: 중괄호 사용, 블록 다음 한 줄 공백

- `if`, `for`, `while` 등은 **항상 중괄호 `{ }`** 로 블록을 감싼다. (한 줄짜리 `if` 생략 지양.)
- **닫는 중괄호 `}` 다음**에는 가독성을 위해 **빈 줄 한 줄**을 두는 것을 선호한다. (같은 메서드 안에서 논리 단락 구분.)

---

## 4. 그 밖에 같이 두면 좋은 관례 (코드베이스에서 드러난 것)

| 항목 | 설명 |
|------|------|
| **검증의 층** | Bean Validation(`@NotNull`, `@DecimalMin` 등)으로 형식·필수값을 막고, **도메인 규칙**(예: 수량 양수, PATCH 수량 음수 불가)은 서비스에서 `ResponseStatusException`으로 처리한다. |
| **사용자 스코프** | `userId`와 함께 `findByIdAndUserId` 패턴으로 **다른 사용자 데이터 접근**을 막는다. |
| **이벤트 기록** | 재고 변화 이력은 `InventoryChangeEventService`에 위임하고, 엔티티 스냅샷은 `snapshotForEvent` 등으로 분리한다. |
| **수량 0과 행 존재** | “0개 줄”을 남기지 않고 **삭제**하는 정책이면, `use`/`update` 후 수량 0은 이벤트 기록 후 **물리 삭제** 등 규칙을 서비스에 일관되게 둔다. |
| **용어 정리** | 도메인에서 더 이상 쓰지 않는 개념(예: 예전 `ACTIVE`/`DEPLETED` 구분)은 **메서드·주석 이름**에서도 제거해 혼동을 줄인다. |
| **API 의미와 메서드명** | `stockIn`처럼 “입고 + 신규 생성 + 병합”이 한데 섞이면 의미가 모호해질 수 있으므로, **생성 / 수정 / 삭제**를 나누는 쪽이 이해하기 쉽다. |

---

## 5. 생성 시각 `now`를 DTO에 둘 때

- `toInventoryItem` 안에서 `Instant.now()`로 `createdAt`/`updatedAt`을 넣을 수 있다.
- 테스트·일관된 시각이 필요하면 나중에 `(Long userId, Instant now)`처럼 **시각을 인자로 받는 오버로드**를 고려할 수 있다.

---

이 문서는 팀 합의에 따라 갱신한다.
