# 페이즈 1 엔티티 모델 및 관계 정의

## 1. 문서 정보

| 항목 | 내용 |
|------|------|
| 문서명 | 페이즈 1 엔티티 모델 및 관계 정의 |
| 버전 | 2.0 |
| 기준 문서 | `docs/phase1/phase1_functional_specification.md`, ER 보완안(외부 리뷰) |
| 목적 | 페이즈 1 구현 시 필요한 도메인 엔티티, 속성, 관계, 정책·인덱스를 한곳에 정리한다 |
| 필드 표기 | **속성(컬럼)명**은 **camelCase** 영문. 엔티티·타입명은 **PascalCase**. DB 테이블명 예시는 기존처럼 `snake_case`를 쓸 수 있으며, DDL에서는 팀 규칙에 맞게 매핑한다 |

---

## 2. 개요

기능 명세의 데이터 구조 개요(§7)에 따라 페이즈 1은 아래 개념을 영속화한다. **식료품 종류는 사용자 개인 마스터**이며, **보관 항목은 물리 삭제보다 종료(비활성) 상태 관리**를 우선한다(ER 보완안과 정합).

| 개념 | 역할 | 명세 대응 |
|------|------|-----------|
| 사용자 | **도메인 주체** — 보관·이력·종류 마스터의 소속(`userId`); `Account.userId`로 계정과 1:1 | §5, §6.1 M-04 |
| 계정 | **계정성** 정보 — `userId`로 `User` 참조, 로그인 제공자·외부 주체 ID | §6.1 M-02, M-03 |
| 저장 장소 | **사용자별** 마스터 — **한 행이 보관 위치 하나**(이름만 두고, 별도 분류 enum은 두지 않음) | §6.2 T-05, §6.3 S-06 |
| 식료품 종류 | **사용자별** 마스터 — 이름, **기본 저장 장소(FK)**, 평균 유통기한 기본값 | §6.2 |
| 식료품 보관 항목 | 현재 보유 스냅샷; **동일 (사용자·종류·보관·유통기한)** 이면 병합 가능 | §6.3 |
| 식료품 변경 이력 | 입고·사용·폐기·수정의 시간순 이벤트; **핵심 값은 컬럼**, 수정 상세는 JSON 보조 | §6.4 |

**원칙:** 보관 항목은 **현재 상태**를, 이력은 **변화의 누적**을 담는다. 상태 변경과 이력 삽입은 **동일 트랜잭션**으로 처리한다(명세 §8, §9, §10).

**인증·도메인 분리:** **`User`(사용자)** 를 도메인 주체로 두고, **`Account`(계정)** 은 로그인 식별자를 보관하며 **`userId`로 `User`를 참조**한다(일반적인 관례: FK는 계정 쪽에 둠). 식료품 도메인의 FK는 모두 **`userId`** 로 통일한다(Spring Security·OIDC는 `Account` 행과 매핑).

---

## 3. 도메인 상수·표현 규칙

### 3.1 저장 장소 마스터 (`StorageLocation`)

`GroceryType`·`InventoryItem`의 보관은 **`storageLocationId`** 로만 가리킨다. **저장 “종류”를 enum으로 또 박지 않는다** — 이 테이블의 **각 행이 곧 사용자 입장에서의 보관 위치 하나**이고, 그걸 구분하는 값은 행에 담는 **`name`**(표시명)뿐이면 된다.

| 필드(개념) | 설명 |
|------------|------|
| `name` | 사용자가 부르는 보관 위치 이름(예: “메인 냉장고”, “실온 선반”). **이 마스터가 존재하는 이유**가 되는 필드 |

동일 사용자 안에서 `name`이 겹치지 않게 할지(예: `(userId, name)` unique)는 제품 정책으로 정한다.

### 3.2 단위 (`unit`)

수량과 분리해 둔다. `quantity`는 **decimal**, `unit`은 **enum 또는 짧은 코드 문자열**로 통일한다.

**예시 값:** `COUNT`, `G`, `KG`, `ML`, `L`, `PACK`, `BOTTLE` 등(구현에서 목록 확정).

### 3.3 `InventoryItem` 한 줄의 의미(병합 기준)

**한 행**은 아래 조합의 **현재 상태**를 나타낸다.

- 소속 사용자(`userId`)
- 식료품 종류(`groceryTypeId`)
- 저장 장소(`storageLocationId` — §3.1 마스터)
- 유통기한(`expirationDate`; 동일 일자면 동일 행으로 볼지 시간 단위는 구현에서 결정)

**같은 사용자·같은 종류·같은 저장 장소(`storageLocationId`)·같은 유통기한**이면 **동일 `InventoryItem`으로 병합**하고 수량만 가산하는 방식이 가능하도록 설계한다.  
저장 장소나 유통기한이 다르면 **별도 행**으로 분리한다.

---

## 4. 엔티티 정의

### 4.1 사용자 (`User`)

| 항목 | 설명 |
|------|------|
| 책임 | **도메인 주체** — 식료품 데이터의 소속·등록 맥락(M-04). `StorageLocation`·`GroceryType`·`InventoryItem`·`InventoryChangeEvent`의 `userId`가 가리키는 대상 |
| 명세 근거 | §5, §6.1 |

**권장 속성**

| 속성 | 필수 | 설명 |
|------|------|------|
| `id` | 예 | 기본 키 |
| `createdAt` | 권장 | 생성 시각 |
| `displayName` | 선택 | 화면·프로필 표시명(향후 확장) |

인증·로그인 정보는 **`User`에 두지 않고** 별도 `Account`에 둔다. 가입 흐름에서는 보통 **`User`를 먼저 만들고** 이어서 `Account`에 `userId`를 넣는 식으로 맞춘다.

---

### 4.2 계정 (`Account`)

| 항목 | 설명 |
|------|------|
| 책임 | **계정성** — 로그인 제공자·외부 주체 ID. 토큰·OIDC와의 매핑 대상 |
| 명세 근거 | §6.1 M-02, M-03 |

**권장 속성**

| 속성 | 필수 | 설명 |
|------|------|------|
| `id` | 예 | 기본 키 |
| `userId` | 예 | **`User` 참조**. 페이즈 1에서 **1 : 1** 이면 `userId` **unique** |
| `authProvider` | 예 | OIDC issuer 또는 `google` 등 제공자 식별 |
| `authSubject` | 예 | 해당 제공자 기준 외부 주체 ID(`sub`) |
| `createdAt` | 권장 | 생성 시각 |

**제약:** `(authProvider, authSubject)` **unique** — 동일 외부 계정 중복 방지.

이메일·계정 잠금 등은 정책에 따라 추가한다.

**구현 참고:** `User.id` = `Account.userId` 로 **동일 값**을 쓰는 공유 키 스타일도 가능하지만, 관례적으로는 **`Account`에 `userId` FK** 를 두는 편이 읽기 쉽다.

---

### 4.3 저장 장소 (`StorageLocation`)

| 항목 | 설명 |
|------|------|
| 책임 | **사용자 개인 마스터** — **한 행 = 보관 위치 하나**. 분류용 enum은 두지 않고, 위치를 구분하는 **`name`** 만 둔다. `GroceryType`·`InventoryItem`은 이 테이블을 `storageLocationId`로 참조한다 |
| 명세 근거 | §6.2 T-05, §6.3 S-06 |

**권장 속성**

| 속성 | 필수 | 설명 |
|------|------|------|
| `id` | 예 | 기본 키 |
| `userId` | 예 | M-04; 소속 사용자 |
| `name` | 예 | 보관 위치 표시명(§3.1) |
| `createdAt` / `updatedAt` | 권장 | |
| `archivedAt` | 선택 | 미사용 시 비노출 등 |

**제약:** 필요 시 동일 사용자 내 `name` 유일(예: `(userId, name)` unique).

**삭제:** `GroceryType`·`InventoryItem`이 참조 중이면 **RESTRICT** 또는, 정책상 **소프트 아카이브**(`archivedAt`) 우선.

---

### 4.4 식료품 종류 (`GroceryType`)

| 항목 | 설명 |
|------|------|
| 책임 | **사용자 개인 마스터**. 이름·**기본 저장 장소(FK)**·평균 유통기한 기본값. 다른 사용자와 공유하지 않는다 |
| 명세 근거 | §6.2 T-01~T-06, §7 |

**권장 속성**

| 속성 | 필수 | 명세 ID | 설명 |
|------|------|---------|------|
| `id` | 예 | — | 기본 키 |
| `userId` | 예 | M-04 | 소속 사용자; **개인 마스터** |
| `name` | 예 | T-04 | 표시명 |
| `defaultStorageLocationId` | 선택 | T-05, C-02 | **`StorageLocation.id`**. 신규 보관 항목 등록 시 기본값 제안; null이면 앱에서 안내 |
| `defaultShelfLifeDays` | 선택 | T-06, C-01 | 일수 |
| `createdAt` / `updatedAt` | 권장 | — | |
| `archivedAt` | 선택 | T-03 | 비노출·아카이브 시각; 물리 삭제 대안 |

**무결성:** `defaultStorageLocationId`가 있으면 **같은 `userId`를 가진 `StorageLocation`** 만 가리키도록 애플리케이션 또는 복합 FK로 맞춘다.

**제약·정책:** 다른 엔티티에서 이 종류를 참조할 때는 FK **`groceryTypeId`**(`GroceryType.id`)를 쓴다. 동일 사용자 안에서 `name` 문자열을 유일하게 강제할지(예: `(userId, name)` unique)는 제품 정책으로 정한다.

**삭제(T-03):** 물리 삭제보다 **`archivedAt` 설정(소프트 아카이브)** 을 우선한다. 하위 `InventoryItem`이 남아 있으면 **RESTRICT**로 삭제를 막거나, 정책상 연쇄 처리 시에만 하위와 함께 제거한다.

---

### 4.5 식료품 보관 항목 (`InventoryItem`)

| 항목 | 설명 |
|------|------|
| 책임 | **현재** 보유 스냅샷. **물리 삭제를 기본으로 하지 않고** 수량 0·폐기 등 시 **종료(비활성) 상태**로 전환한다 |
| 명세 근거 | §6.3 S-01~S-07, §7 |

**권장 속성**

| 속성 | 필수 | 명세 ID | 설명 |
|------|------|---------|------|
| `id` | 예 | — | 기본 키 |
| `userId` | 예 | M-04 | 소속·등록 맥락의 기준 |
| `groceryTypeId` | 예 | S-01 | 사용자 개인 마스터 참조 |
| `quantity` | 예 | S-04 | decimal |
| `unit` | 예 | S-04 | §3.2 |
| `expirationDate` | 선택 | S-05, C-03 | nullable; 임박 조회에 사용 |
| `storageLocationId` | 예 | S-06 | **`StorageLocation.id`**. 같은 사용자 소속 행만 참조 |
| `status` | 예 | — | 예: `ACTIVE`, `DEPLETED` 등 — **활성/종료** |
| `depletedAt` | 선택 | — | 종료 시각(null이면 미종료) |
| `createdAt` / `updatedAt` | 권장 | S-02 | |

**라이프사이클:** 폐기·소진 등으로 목록에서 “없애는” 동작은 **행 삭제 대신 `status`·`depletedAt` 갱신**을 우선한다. 이력의 `inventoryItemId` FK가 안정적으로 유지된다.

명세 S-03 “삭제”는 구현에서 **종료 처리**로 충족할 수 있다(물리 삭제는 예외 정책).

**무결성:** `storageLocationId`는 **`StorageLocation.userId` = `InventoryItem.userId`** 인 행만 허용.

---

### 4.6 식료품 변경 이력 (`InventoryChangeEvent`)

| 항목 | 설명 |
|------|------|
| 책임 | 불변 이벤트 로그. 입고·사용·폐기는 **정형 컬럼** 중심, **수정(MODIFIED)** 은 `changedFieldsJson` 보조 |
| 명세 근거 | §6.4 H-01~H-06, §10 |

**이벤트 유형:** `STOCK_IN` · `USED` · `DISPOSED` · `MODIFIED`(명세: 입고·사용·폐기·수정에 대응; 코드명은 구현에서 통일)

**권장 속성**

| 속성 | 필수 | 설명 |
|------|------|------|
| `id` | 예 | 기본 키 |
| `userId` | 예 | H-06·M-04. **비정규화 유지 권장**(조인·권한 필터 단순화) |
| `inventoryItemId` | 예 | 대상 보관 행 |
| `groceryTypeId` | 예 | 종류별 이력 조회(H-05); 항목 비활성 후에도 조회 안정 |
| `eventType` | 예 | 위 유형 |
| `countBefore` | 선택 | 일이 일어나기 **직전** 수량(넣을 수 있으면) |
| `countDiff` | 선택 | 이번에 **늘거나 줄어든** 양(늘리면 +, 쓰거나 버리면 −) |
| `countAfter` | 선택 | 일이 끝난 **뒤** 수량(넣을 수 있으면) |
| `unit` | 선택 | 해당 시점 단위 |
| `occurredAt` | 예 | 발생 시각 |
| `memo` | 선택 | 메모 |
| `payloadJson` | 선택 | 부가 정보(JSON, 필요 시) |

**수정 이벤트(H-04):** 유형은 `MODIFIED` 하나로 두고, 수량·유통기한·`storageLocationId`·단위·`groceryTypeId` 변경 등은 **`changedFieldsJson`**에 남긴다.

**원칙:** 상태 갱신과 이벤트 삽입은 **동일 트랜잭션**에서 수행한다.

---

## 5. 엔티티 간 관계

### 5.1 관계 요약

```text
User (1) ── (1) Account          [FK: Account.userId → User.id]

User (1) ──< (N) StorageLocation
User (1) ──< (N) GroceryType
User (1) ──< (N) InventoryItem
User (1) ──< (N) InventoryChangeEvent

StorageLocation (1) ──< (0..N) GroceryType     [optional FK: defaultStorageLocationId]
StorageLocation (1) ──< (N) InventoryItem      [FK: storageLocationId]

GroceryType (1) ──< (N) InventoryItem

InventoryItem (1) ──< (N) InventoryChangeEvent

GroceryType (1) ──< (N) InventoryChangeEvent   [groceryTypeId; H-05 조회용]
```

- **사용자 ↔ 계정:** **1 : 1** — FK는 **`Account.userId` → `User`** (도메인 중심은 `User`).
- **사용자 → 저장 장소·종류·보관·이력:** 각각 **1 : N** (M-04).
- **저장 장소 → 종류(기본값)·보관 항목:** 한 `StorageLocation`이 여러 종류의 기본값·여러 보관 행에 쓰일 수 있다.
- **종류 → 보관 항목:** **1 : N** (개인 마스터).
- **보관 항목 → 이력:** **1 : N**. 비활성 항목도 이력 조회를 위해 행을 유지한다.

### 5.2 카디널리티 표

| From | To | 카디널리티 | 비고 |
|------|-----|------------|------|
| User | Account | 1 : 1 | `Account.userId` FK |
| User | StorageLocation | 1 : N | 저장 장소 마스터 |
| User | GroceryType | 1 : N | 개인 마스터 |
| User | InventoryItem | 1 : N | M-04 |
| User | InventoryChangeEvent | 1 : N | H-06, `userId` 유지 |
| StorageLocation | GroceryType | 1 : N | optional `defaultStorageLocationId` |
| StorageLocation | InventoryItem | 1 : N | `storageLocationId` |
| GroceryType | InventoryItem | 1 : N | |
| InventoryItem | InventoryChangeEvent | 1 : N | |

---

## 6. 인덱스 초안

실제 조회·권한 필터를 고려한 **초안**이다. DB 제품에 맞게 조정한다.

| 테이블 | 인덱스 | 용도 |
|--------|--------|------|
| `account` | unique `(authProvider, authSubject)` | 로그인 조회·중복 방지 |
| `account` | unique `userId` | User당 로그인 연동 1건(1:1 가정 시) |
| `grocery_type` | `(userId)` | 사용자별 종류 목록 |
| `grocery_type` | `(defaultStorageLocationId)` | 기본 저장 장소 역참조(선택) |
| `storage_location` | `(userId)` | 사용자별 저장 장소 목록 |
| `inventory_item` | `(userId, groceryTypeId)` | 종류별 보유 |
| `inventory_item` | `(userId, storageLocationId)` | 장소별 보유 |
| `inventory_item` | `(userId, expirationDate)` | 유통기한 임박(C-03) |
| `inventory_item` | `(userId, status)` | 활성/종료 필터 |
| `inventory_change_event` | `(userId, occurredAt DESC)` | 전체 이력(H-06) |
| `inventory_change_event` | `(inventoryItemId, occurredAt DESC)` | 항목별 이력(H-05) |
| `inventory_change_event` | `(groceryTypeId, occurredAt DESC)` | 종류별 이력(H-05) |

---

## 7. 페이즈 1에서 닫아 둔 결정 요약

ER 보완안에서 권장한 대로, 아래는 **페이즈 1에서 기본 정책으로 확정**한다.

| # | 결정 |
|---|------|
| 1 | `GroceryType`은 **사용자 개인 마스터**; 식별·참조는 **`id`** (이름 정규화 컬럼 없음) |
| 2 | `InventoryItem`은 **물리 삭제보다 종료(`status`·`depletedAt`) 우선** |
| 3 | `InventoryChangeEvent`는 **핵심 조회 컬럼 분리** + 수정 상세는 `changedFieldsJson` |
| 4 | 보관 위치는 **`StorageLocation` 한 행 = 한 곳**으로만 정의하고, enum 분류는 두지 않으며 **`storageLocationId`** 로만 참조 |
| 5 | 수량은 **decimal**, 단위는 **코드화된 `unit`** |
| 6 | 동일 **사용자·종류·보관·유통기한**이면 **동일 행 병합 가능**(§3.3) |
| 7 | 이벤트에 **`userId` 유지**(비정규화) |
| 8 | **`Account` / `User` 분리** — 인증은 `Account`, FK는 **`Account.userId` → `User`**, 도메인 테이블은 `userId` |

---

## 8. 남은 구현 선택 사항

| 주제 | 비고 |
|------|------|
| `User`·`Account` 키 설계 | `Account.userId` FK(권장) vs 동일 값 공유 등 §4.2 |
| 신규 사용자 `StorageLocation` | 가입 시 “냉장고”, “냉동고” 등 **`name`만 넣은 행**을 미리 만들지 여부 |
| `status` 값 집합 | `ACTIVE`/`DEPLETED` 외 세분화 여부 |
| 물리 삭제 | 운영·GDPR 등으로 필요 시 별도 배치·정책 |

---

## 9. 페이즈 1 범위에서 두지 않는 것

- 레시피·장보기·식단 엔티티
- OCR·영수증 원본
- 복잡한 알림 규칙 엔티티(유통기한 **임박**은 `expirationDate` + 애플리케이션 로직)

---

## 10. 구현 시 교차 검증 체크리스트

1. 모든 식료품 관련 조회·변경에 **`userId`** 조건이 적용되는가(M-04). 로그인 세션은 **`Account`** 로 식별되고 **`Account.userId`** 로 **`User`** 와 연결되는가.
2. 상태 변경과 이벤트 기록이 **동일 트랜잭션**인가(§9-3).
3. 항목별·전체·종류별 이력 조회(H-05, H-06)가 **인덱스·컬럼**으로 가능한가.
4. 보관 항목 **종료 후에도** 항목별 이력이 끊기지 않는가(`inventoryItemId` 유지).
5. `GroceryType` **아카이브·삭제**가 하위 `InventoryItem` 무결성과 모순되지 않는가.
6. `StorageLocation` 참조 시 **`userId` 일치**(같은 사용자의 마스터만)가 보장되는가.

---

## 11. 스키마 초안 요약표 (필드 목록)

편의상 필드를 요약한다. 이름은 프로젝트 컨벤션에 맞게 매핑한다.

**`user`** — `id`, `createdAt`, `displayName`(선택)

**`account`** — `id`, `userId`(FK → `user`, 1:1 시 unique), `authProvider`, `authSubject`, `createdAt`

**`storage_location`** — `id`, `userId`, `name`, `createdAt`, `updatedAt`, `archivedAt`

**`grocery_type`** — `id`, `userId`, `name`, `defaultStorageLocationId`(FK → `storage_location`, 선택), `defaultShelfLifeDays`, `createdAt`, `updatedAt`, `archivedAt`

**`inventory_item`** — `id`, `userId`, `groceryTypeId`, `storageLocationId`(FK → `storage_location`), `quantity`, `unit`, `expirationDate`, `status`, `depletedAt`, `createdAt`, `updatedAt`

**`inventory_change_event`** — `id`, `userId`, `inventoryItemId`, `groceryTypeId`, `eventType`, `qtyBefore`, `qtyChange`, `qtyAfter`, `unit`, `occurredAt`, `memo`, `changedFieldsJson`, `payloadJson`

---

## 12. 문서 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 1.0 | 2026-04-11 | 초안: `phase1_functional_specification.md` 기반 엔티티·관계 정리 |
| 1.1 | 2026-04-11 | Account/User 분리; `GroceryType` 공용 마스터화; 등록자 표현; 종류 삭제 무결성 |
| 1.2 | 2026-04-11 | ER 보완안 반영: 개인 마스터·병합 기준·종료 상태·이벤트 컬럼·보관/단위·인덱스·`User` 단순화안 |
| 1.3 | 2026-04-11 | `Account` / `User` 재분리 — 계정성 vs 도메인 주체, 관계·인덱스·스키마 요약 반영 |
| 1.4 | 2026-04-11 | FK 방향 정리: **`Account.userId` → `User`**(계정에 userId), §4 순서·다이어그램·인덱스·스키마 반영 |
| 1.5 | 2026-04-11 | `GroceryType.normalized_name` 제거; 식별은 `id` 중심, `(userId, name)` unique는 선택 정책으로 이동 |
| 1.6 | 2026-04-11 | **`StorageLocation` 마스터** 도입 — `kind`+`label`; `GroceryType`·`InventoryItem`은 `storageLocationId` 참조; 이벤트 스냅샷·관계·인덱스 반영 |
| 1.7 | 2026-04-11 | `InventoryChangeEvent` 스냅샷 컬럼 제거; 수량 컬럼 설명을 쉬운 말로 정리 |
| 1.8 | 2026-04-11 | 이력 수량 필드명을 `qty_before` / `qty_change` / `qty_after` 로 단순화 |
| 1.9 | 2026-04-11 | `StorageLocation`에서 `kind`·`label` 제거 — **행 자체가 보관 위치**, `name`만 유지 |
| 2.0 | 2026-04-11 | 문서 내 **속성(필드) 이름**을 **camelCase 영문**으로 통일; 물리 DB·테이블명은 프로젝트에서 매핑 |
