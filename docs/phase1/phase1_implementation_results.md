# 페이즈 1 구현 결과

## 1. 문서 정보

| 항목 | 내용 |
|------|------|
| 문서명 | 페이즈 1 구현 결과 |
| 기준 명세 | `docs/phase1/phase1_functional_specification.md` |
| 대상 코드 | `src/main/java/com/odk/pjt/mealflow` (및 연동 설정) |

본 문서는 명세 항목 ID별로 **현재 코드에 구현된 내용**을 정리한다.

---

## 2. 구현 요약

- **인증·계정**: Spring Security OAuth2 로그인(Google OIDC), 세션 기반. 최초 로그인 시 `User`·`Account` 자동 생성 및 `userId`를 API에 전달.
- **도메인 API**: 보관 장소(`storage-locations`), 식료품 종류(`grocery-types`), 보관 항목(`inventory-items`), 변경 이력(`inventory-change-events`) REST 엔드포인트.
- **데이터 격리**: 모든 조회·변경이 `SecurityUtils.requireCurrentUserId()`로 얻은 사용자 ID로 스코프된다.
- **이력**: 보관 항목 생성·수정·삭제(및 수량 0 처리) 시 `InventoryChangeEvent`에 수량 스냅샷·차이가 트랜잭션 내 기록된다.

---

## 3. 회원 관리 (`SecurityConfig`, `AccountProvisioningService`, `AccountLinkageOidcUserService`, `LoginRedirectController`)

| ID | 구현 결과 |
|----|-----------|
| **M-01** 회원 가입 | Google OIDC로 **최초 인증 시** `AccountProvisioningService.findOrCreateForGoogle`가 `User`와 `Account`를 생성·저장한다. 별도 이메일/비밀번호 폼 가입 API는 없다. |
| **M-02** 로그인 | `oauth2Login`으로 Google 로그인. 로그인 페이지는 `mealflow.frontend.login-url`로 위임한다. `/login` GET은 `LoginRedirectController`가 동일 URL로 리다이렉트한다. |
| **M-03** 로그아웃 | `SecurityConfig`에서 `logoutSuccessUrl("/")`로 세션 종료 후 루트로 이동한다. |
| **M-04** 데이터 분리 | API는 `MealflowOidcUser`의 `userId`만 사용하고, 리포지토리는 `userId`·`findByIdAndUserId` 등으로 **타 사용자 리소스 접근을 구조적으로 차단**한다. 통합 테스트 `InventoryDomainIntegrationTest#userScope_listOnlySeesSameUser`로 타 사용자 목록 공백을 검증한다. |

---

## 4. 식료품 종류 (`GroceryType`, `GroceryTypeController`, `GroceryTypeService`)

| ID | 구현 결과 |
|----|-----------|
| **T-01** 등록 | `POST /api/v1/grocery-types`. 이름 중복 시 409. |
| **T-02** 수정 | `PUT /api/v1/grocery-types/{id}`. |
| **T-03** 삭제 | `DELETE /api/v1/grocery-types/{id}`. **보관 항목이 해당 종류를 참조하면 409**로 삭제 거부. `GET .../referenced`로 사전 확인 가능. |
| **T-04** 이름 | 엔티티·DTO에 `name`, 사용자별 유니크 제약(`uk_grocery_types_user_name`). |
| **T-05** 기본 보관 장소 | `defaultStorageLocationId` 컬럼·API 필드. 생성·수정 시 서비스에서 **해당 사용자 소유 저장소 ID**로 검증한다. **null이면 검증 실패로 거부**되어, 운영상 생성·수정 요청에는 유효한 ID가 필요하다. |
| **T-06** 평균 유통기한 기본값 | `defaultShelfLifeDays`(일수, nullable). |

---

## 5. 보관 장소 (`StorageLocation`, `StorageLocationController`, `StorageLocationService`)

명세의 「기본 보관 장소」 선택지는 **사용자 정의 저장소 이름**으로 충족한다. CRUD는 `GET/POST/PUT/DELETE /api/v1/storage-locations`. 삭제 시 보관 항목·종류 기본값 참조가 있으면 서비스 레벨에서 막는다(`referenced` 조회 엔드포인트 제공).

---

## 6. 식료품 보관 상태 (`InventoryItem`, `InventoryItemController`, `InventoryItemService`)

| ID | 구현 결과 |
|----|-----------|
| **S-01** 등록 | `POST /api/v1/inventory-items`. 수량은 양수 필수, 보관 장소·유통기한 필수. 성공 시 이력 1건 append. |
| **S-02** 수정 | `PATCH /api/v1/inventory-items/{id}`. 수량·단위·유통기한·보관 장소·식료품 종류(선택 필드) 부분 갱신. 변경 시 이력 append. 수량이 0이 되면 이벤트 기록 후 **행 삭제**. |
| **S-03** 삭제 | `DELETE /api/v1/inventory-items/{id}`. 폐기로 간주하는 이벤트(이후 스냅샷 없음) 기록 후 행 삭제. |
| **S-04** 수량·단위 | `BigDecimal quantity`, `GroceryUnit` enum(COUNT, G, KG, ML, L, PACK, BOTTLE). |
| **S-05** 유통기한 | `LocalDate expirationDate`. |
| **S-06** 보관 장소 | `storageLocationId`. |
| **S-07** 목록 조회 | `GET /api/v1/inventory-items` — 유통기한·ID 순. 개별 `GET .../{id}`. |

---

## 7. 식료품 변경 이력 (`InventoryChangeEvent`, `InventoryChangeEventService`, 컨트롤러)

| ID | 구현 결과 |
|----|-----------|
| **H-01** 입고 | 신규 행 `create` 시 이벤트: `countBefore == null`, `countAfter == 입고 수량`, `countDiff` 동일. |
| **H-02** 사용 | `updateDetails`로 수량 감소 시 `countDiff` 음수, 전후 수량 필드 기록. |
| **H-03** 폐기 | 행 `delete` 또는 수량 0으로 소진 시 `countAfter == 0` 이벤트 후 행 제거. |
| **H-04** 수정 | 비수량 필드만 바뀌어도 변경이 감지되면 이벤트가 쌓인다. 이벤트 엔티티는 **수량 필드 중심**이라, 유형 구분 필드(입고/사용/폐기/수정)는 없고 **수량 변화·0 여부·삭제 흐름으로 해석**한다. |
| **H-05** 항목별 조회 | `GET /api/v1/inventory-items/{id}/events?limit=`. 최신순 페이지(상한 200). |
| **H-06** 전체 조회 | `GET /api/v1/inventory-change-events?limit=`. 선택적으로 `groceryTypeId`로 **종류 단위** 필터. |

이력 저장은 `InventoryItemService`의 상태 변경과 **동일 트랜잭션**에서 `appendEvent`로 수행된다.

---

## 8. 편의 기능 (`InventoryItemController`, `InventoryItemService`)

| ID | 구현 결과 |
|----|-----------|
| **C-01** 평균 유통기한 자동 입력 | `GET /api/v1/inventory-items/suggested-defaults?groceryTypeId=` — 종류의 `defaultShelfLifeDays`가 있으면 `오늘 + 일수`를 `suggestedExpirationDate`로 반환. |
| **C-02** 기본 보관 장소 자동 선택 | 동일 응답의 `suggestedStorageLocationId`에 종류의 `defaultStorageLocationId`를 담는다. |
| **C-03** 유통기한 임박 표시 | `GET /api/v1/inventory-items/expiring?withinDays=7`(기본 7). `expirationDate <= 오늘+withinDays` 이고 수량>0인 항목. 클라이언트가 배지 등으로 표시하면 된다. |
| **C-04** 최근 등록 재사용 | `GET /api/v1/inventory-items/recent?limit=10`(최대 100) — `createdAt` 내림차순. |

---

## 9. 명세 완료 기준(§9) 대응

| 기준 | 구현 상태 |
|------|-----------|
| 로그인 후 자신의 식료품만 관리 | API 전 구간 `userId` 스코프 + OIDC 연동 사용자 ID. |
| 현재 상태 조회 | `inventory-items` 목록·단건. |
| 변경 시 이력 자동 기록 | 생성·수정·삭제·수량0 처리 시 `InventoryChangeEvent` 기록. |
| 유통기한 임박 확인 | `expiring` API + 목록 정렬(유통기한 순). |

---

## 10. 비기능(명세 §10) 요약

| 영역 | 구현 내용 |
|------|-----------|
| 보안 | API 기본 인증 필요(`ApiAuthenticationIntegrationTest` 미인증 거부). |
| 일관성 | 서비스 메서드 `@Transactional`로 상태 변경과 이력 기록을 묶음. |
| 추적성 | 이벤트에 `occurredAt`, `groceryTypeId`, `inventoryItemId`, 단위, 수량 전·후·차이 저장. **행위 유형(enum)은 없음.** |

---

## 11. 테스트

| 테스트 | 내용 |
|--------|------|
| `InventoryDomainIntegrationTest` | 이력 누적·수량 증감·임박 조회·사용자 스코프. |
| `ApiAuthenticationIntegrationTest` | 미인증 API 접근 시 200 아님. |

---

## 12. 명세 대비 참고 (한 줄)

- **제외 범위**(OCR, 레시피·장보기 추천 등)는 구현 없음 — 명세와 동일.
- 이력은 **수량 중심 이벤트 로그**이며, 명세 문구의 「입고·사용·폐기·수정」은 **API 상 별도 타입으로 노출되지 않고** 동작·수량 변화로 구분한다.
