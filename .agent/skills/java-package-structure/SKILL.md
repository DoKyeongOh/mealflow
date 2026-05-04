---
name: java-package-structure
description: 특정 패키지를 Mealflow 프로젝트의 표준 구조(도메인 중심 + dto/model 하위 패키지)로 재구성하는 가이드라인 및 도구입니다.
---

# Java Package Structure Skill

이 스킬은 Mealflow 프로젝트의 Java 패키지 구조를 일관되게 유지하고 재구성하는 방법을 정의합니다.

## 표준 구조 정의

모든 Java 소스 코드는 다음 원칙에 따라 배치되어야 합니다:

1.  **최상위 패키지**: `com.odk.pjt.mealflow`
2.  **도메인 관리 포인트**: 최상위 패키지 바로 하위에 도메인별 패키지를 둡니다.
    - 예: `com.odk.pjt.mealflow.user`, `com.odk.pjt.mealflow.auth`, `com.odk.pjt.mealflow.order`
3.  **하위 패키지 (제한적)**: 도메인 패키지 하위에는 오직 다음 두 가지 패키지만 가질 수 있습니다.
    - `dto`: 데이터 전달 객체 (Request, Response 등)
    - `model`: 도메인 엔티티, 영속성 모델 등
4.  **기타 클래스**: Service, Repository, Controller, Validator, Utility 등은 도메인 패키지 **바로 하위**에 위치합니다.

5.  **개별 파일 원칙**: 각 DTO나 Model 클래스는 개별적인 Java 파일로 관리합니다. 하나의 클래스 내부에 여러 DTO/Model을 **이너 클래스(Inner Class)로 묶어서 관리하지 마십시오.**

### 구조 예시
```
com.odk.pjt.mealflow.user
├── UserMapper.java          (기타 클래스)
├── UserRepository.java      (기타 클래스)
├── UserService.java         (기타 클래스)
├── UserController.java      (기타 클래스)
├── dto
│   ├── UserJoinRequest.java  (DTO - 개별 파일)
│   └── UserResponse.java     (DTO - 개별 파일)
└── model
    └── User.java             (Model/Entity - 개별 파일)
```

## 패키지 재구성 절차 (Agent 가이드라인)

이 스킬을 사용하여 패키지를 재구성할 때는 다음 단계를 따르십시오:

1.  **분석**: 대상 도메인 패키지의 파일들을 확인하고 `dto`, `model`, `기타`로 분류합니다. 만약 여러 DTO가 한 파일에 묶여 있다면 이를 분리할 계획을 세웁니다.
2.  **디렉토리 생성**: 필요한 경우 `dto` 및 `model` 디렉토리를 생성합니다.
3.  **파일 이동 및 패키지 수정**:
    - 파일을 적절한 위치로 이동하거나 이너 클래스를 개별 파일로 추출합니다.
    - 이동/추출된 파일의 `package` 선언을 수정합니다.
4.  **참조 업데이트**:
    - 프로젝트 전체에서 해당 클래스를 참조하는 모든 `import` 문을 찾아 수정합니다.
5.  **검증**: 컴파일 오류가 없는지 확인하고, 필요시 테스트를 수행합니다.

## 주의 사항
- `dto`와 `model` 외의 임의의 하위 패키지(예: `service`, `repository`, `controller`)를 생성하지 마십시오.
- 패키지 이동 시 Java 파일 내부의 `package` 선언과 파일 시스템 경로가 일치해야 함을 명심하십시오.
- **이너 클래스 지양**: 코드 가독성과 유지보수를 위해 하나의 파일에 여러 DTO/Model을 몰아넣지 마십시오.
