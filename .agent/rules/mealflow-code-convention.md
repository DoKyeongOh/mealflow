# Mealflow 코드 컨벤션

Mealflow 프로젝트에서 Java 코드를 작성할 때 기본적으로 따르는 형식, 네이밍, 가독성 기준을 정리한다.

이 문서는 코드의 **모양, 이름, 읽기 방식**에 대한 기준을 다룬다.  
서비스 구현 책임, 도메인 검증, 이벤트 처리 정책은 `mealflow-coding-rules.md`에서 관리한다.

---

## 1. 제어문은 항상 중괄호를 사용한다

`if`, `for`, `while` 등의 제어문은 한 줄짜리 로직이라도 항상 중괄호 `{ }`를 사용한다.

```java
if (item == null) {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND);
}
```

아래 형태는 지양한다.

```java
if (item == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
```

중괄호를 항상 사용하면 이후 로직이 추가될 때 실수를 줄일 수 있고, 코드 스타일도 일관된다.

---

## 2. 블록 다음에는 논리 단락을 구분한다

닫는 중괄호 `}` 다음에는 같은 메서드 안에서도 논리 흐름이 바뀌는 경우 빈 줄을 둔다.

```java
if (request.quantity() == null) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
}

InventoryItem item = request.toInventoryItem(userId);
inventoryItemRepository.save(item);
```

단, 모든 블록 뒤에 기계적으로 빈 줄을 넣지는 않는다.  
검증, 조회, 변경, 저장처럼 의미 단위가 바뀔 때만 구분한다.

---

## 3. DTO 변환 메서드 이름은 일관되게 사용한다

요청 DTO에서 엔티티를 만들거나 기존 엔티티에 값을 반영하는 메서드는 프로젝트 안에서 이름을 통일한다.

생성 DTO는 엔티티를 새로 만드는 의미가 강하므로 아래 이름을 사용할 수 있다.

```java
toEntity()
toInventoryItem()
```

도메인 엔티티 이름이 분명한 경우에는 `toInventoryItem()`처럼 구체적인 이름을 사용해도 된다.

```java
public InventoryItem toInventoryItem(Long userId) {
    return new InventoryItem(...);
}
```

부분 수정 DTO는 기존 엔티티에 값을 반영하는 의미가 강하므로 아래 이름이 적합하다.

```java
applyTo(InventoryItem item)
```

```java
public void applyTo(InventoryItem item) {
    ...
}
```

프로젝트 내에서는 같은 의미에 대해 여러 이름을 섞어 쓰지 않는다.

---

## 4. 더 이상 사용하지 않는 도메인 용어는 제거한다

현재 도메인에서 사용하지 않는 개념은 메서드명, 변수명, 주석에서 제거한다.

예를 들어 `ACTIVE`, `DEPLETED` 같은 상태 구분을 더 이상 쓰지 않는다면 아래와 같은 이름은 피한다.

```java
findActiveItems()
markAsDepleted()
```

실제 정책에 맞는 이름을 사용한다.

```java
findItemsByUserId()
deleteWhenQuantityIsZero()
```

사용하지 않는 용어가 남아 있으면 현재 정책을 오해하게 만들 수 있다.

---

## 5. API 의미와 메서드명을 맞춘다

하나의 메서드 이름에 여러 의미가 섞이지 않도록 한다.

예를 들어 `stockIn`이 아래 의미를 모두 포함하면 모호하다.

- 신규 재고 생성
- 기존 재고 병합
- 수량 증가
- 이벤트 기록

가능하면 생성, 수정, 삭제, 사용 처리의 의미를 분리한다.

```java
createInventoryItem()
updateInventoryItem()
useInventoryItem()
deleteInventoryItem()
```

메서드명은 내부 구현 방식보다 외부에서 이해해야 하는 행위 기준으로 정한다.

---

## 6. 문서는 실제 코드와 함께 유지한다

컨벤션은 코드 작성 방식의 기준이므로 실제 코드베이스와 달라지면 문서도 함께 수정한다.

컨벤션 문서에는 포맷팅, 네이밍, 가독성 기준만 남긴다.  
도메인 정책이나 서비스 책임 기준은 `mealflow-coding-rules.md`로 분리한다.
