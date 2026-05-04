---
name: dto-fullstack-impact-check
description: Controller나 API에서 사용되는 DTO가 수정될 때, 연관된 Backend와 Frontend의 변경 사항을 모두 추적하고 동기화하는 가이드라인입니다. 특히 API 엔드포인트와 입출력 데이터명의 일치 여부를 집중적으로 점검합니다.
---

# DTO Full-stack Impact Check Skill

이 스킬은 API 계약(Contract)의 핵심인 DTO(Data Transfer Object)가 변경될 때, 시스템 전체의 일관성을 유지하기 위해 Backend와 Frontend 양쪽의 영향도를 분석하고 수정하는 절차를 정의합니다. **특히 API 엔드포인트 URL과 입출력 데이터 필드명(Naming)의 정확한 일치를 보장하는 가드레일을 제공합니다.**

## 핵심 원칙

1.  **계약 우선 (Contract-First)**: DTO의 변경은 곧 API 계약의 변경입니다. Backend와 Frontend가 공유하는 '약속'이 깨지지 않도록 입출력 데이터의 구조와 이름을 최우선으로 관리합니다.
2.  **명명 규칙 준수 (Naming Consistency)**: Java DTO의 필드명과 Frontend TypeScript의 속성명은 반드시 1:1로 매핑되어야 합니다. (예: camelCase 일치 여부, `@JsonProperty` 사용 시 실제 직렬화되는 이름 확인)
3.  **엔드포인트 동기화**: DTO 변경이 API 경로(`@RequestMapping`)나 HTTP Method의 변경을 수반하는지 확인하고, 이를 Frontend API Client에 즉시 반영합니다.
4.  **영향도 전파**: DTO 변경은 단순한 클래스 수정을 넘어 Controller, Service, API Client, 그리고 UI Component까지 영향을 미칩니다.

## API 계약 가드레일 (Checkpoints)

| 항목 | 점검 내용 | 확인 방법 |
| :--- | :--- | :--- |
| **API Endpoint** | `@RequestMapping`, `@PostMapping` 등의 경로가 Frontend의 API 호출 URL과 일치하는가? | Controller의 `@RequestMapping` 경로와 Frontend API service 파일 대조 |
| **Field Naming** | JSON 직렬화/역직렬화 시 사용되는 필드명이 TypeScript interface와 정확히 일치하는가? | Java의 `@JsonProperty`, `@JsonAlias` 등 확인 및 TS 인터페이스 필드명 대조 |
| **Data Types** | Java의 타입(Long, LocalDateTime 등)이 TypeScript 타입(number, string 등)과 적절히 매핑되었는가? | 특히 날짜/시간 포맷(ISO-8601) 일치 여부 확인 |
| **Constraints** | `@Valid`, `@NotBlank` 등의 제약 조건이 Frontend Validation(Schema)에도 반영되었는가? | Zod/Yup 스키마 또는 UI 에러 핸들링 로직 확인 |

## 영향도 분석 및 수정 절차

### 1. Backend (Java) 체크리스트

-   **Controller**: 해당 DTO를 사용하는 API 엔드포인트 경로와 HTTP Method를 확인합니다.
-   **Validation**: `@Valid`, `@NotBlank`, `@Size` 등 제약 조건이 추가되거나 변경되었는지 확인하고, 관련 테스트 코드를 업데이트합니다.
-   **Mapper/Converter**: Entity와 DTO 간의 변환 로직에서 필드 누락이나 오기입이 없는지 확인합니다.

### 2. Frontend (TypeScript) 체크리스트

-   **Type Definition**: Java DTO에서 정의한 **최종 데이터명(JSON Key)**을 기준으로 TypeScript `interface`를 수정합니다.
-   **API Client**: 엔드포인트 URL이 변경되었다면 이를 반영하고, 파라미터 전달 방식(`@RequestBody` vs `@RequestParam`)에 맞게 호출부를 수정합니다.
-   **Form & UI**: UI에서 입력받는 데이터명이 DTO 필드명과 일치하는지(`name` attribute 등) 확인합니다.

## Agent 가이드라인 (동작 지침)

DTO 수정 시 다음 과정을 통해 "데이터명 불일치"를 원천 차단하십시오:

1.  **계약 추출**: Java DTO 파일에서 실제 JSON으로 나가는 필드명 목록을 추출합니다. (이때 `@JsonProperty`가 있다면 해당 값을 우선함)
2.  **엔드포인트 추적**: 해당 DTO를 사용하는 Controller를 찾아 전체 API URL(Base Path + Method Path)을 도출합니다.
3.  **Frontend 동기화**:
    -   도출된 API URL을 사용하는 Frontend API 호출 코드를 찾아 URL을 동기화합니다.
    -   도출된 필드명 목록을 바탕으로 TypeScript 인터페이스를 업데이트합니다.
4.  **교차 검증**: `grep`을 통해 이전 필드명이 여전히 Frontend 코드에 남아있는지 검색하여 잔재를 제거합니다.

## 주의 사항

-   **필드 삭제/이름 변경**: 이는 Breaking Change입니다. 이 작업을 수행하기 전 반드시 "Frontend 사용처 없음"을 확인하거나, 동시 수정을 완료해야 합니다.
-   **Snake Case vs Camel Case**: 프로젝트 표준이 Camel Case라면 양쪽 모두 엄격히 준수하는지 확인하십시오.
-   **Enum 동기화**: Java Enum 값이 변경(Upper Case 등)되면 Frontend의 타입 정의도 동일한 문자열 세트로 업데이트해야 합니다.
