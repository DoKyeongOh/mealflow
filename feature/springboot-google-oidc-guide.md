# Spring Boot Google OIDC 연동 간단 정리

## 1. Maven 의존성

Spring Boot에서 Google OIDC 로그인을 붙일 때 최소 기준으로 많이 쓰는 의존성은 아래와 같다.

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
</dependencies>
```

핵심은 `spring-boot-starter-oauth2-client` 이다.  
이 의존성이 OAuth2 / OIDC 로그인 기능의 중심이다.

---

## 2. application.yml 예시

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_CLIENT_ID
            client-secret: YOUR_CLIENT_SECRET
            scope:
              - openid
              - profile
              - email
```

- `openid`: OIDC 사용을 위한 필수 scope
- `profile`: 이름, 사진 등의 기본 프로필 정보
- `email`: 이메일 정보

---

## 3. 핵심 구조

전체 흐름은 아래처럼 보면 된다.

```text
Google 로그인
  -> Spring Security가 OidcUser 생성
  -> provider + providerUserId(sub) 추출
  -> SocialAccount 조회 또는 생성
  -> 내부 User 조회 또는 생성
  -> 이후 서비스는 내부 User 기준으로 처리
```

즉, 외부 로그인 결과를 바로 서비스 회원으로 쓰는 게 아니라,  
중간에 `SocialAccount` 같은 연결 구조를 두고 내부 `User`와 매핑한다.

---

## 4. 핵심 클래스

### 4-1. SecurityConfig

로그인 방식과 인증 정책을 설정하는 클래스다.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService())
                )
            );

        return http.build();
    }

    @Bean
    OidcUserService customOidcUserService() {
        return new OidcUserService();
    }
}
```

여기서 핵심은 `oauth2Login()` 이다.

---

### 4-2. OidcUser

로그인 성공 후 Spring Security가 만들어주는 사용자 객체다.

```java
OidcUser oidcUser = ...;

String providerUserId = oidcUser.getSubject();
String email = oidcUser.getEmail();
String name = oidcUser.getFullName();
String picture = oidcUser.getPicture();
```

주요 의미:

- `getSubject()` -> `sub`, 공급자 내부 고유 식별자
- `getEmail()` -> 이메일
- `getFullName()` -> 표시용 이름
- `getPicture()` -> 프로필 이미지

즉, `OidcUser`는 외부 인증 결과를 담고 있는 principal 객체다.

---

### 4-3. SocialAccount

외부 로그인 계정과 내부 회원을 연결하는 구조다.

```java
public class SocialAccount {
    private Long id;
    private Long userId;
    private String provider;
    private String providerUserId;
    private String email;
    private String name;
    private String picture;
}
```

보통 핵심 식별자는 아래 조합이다.

- `provider`
- `providerUserId`

예를 들면:

- provider = `google`
- providerUserId = `sub 값`

이 구조를 쓰면 나중에 다른 소셜 로그인 공급자가 추가되어도 확장하기 쉽다.

---

### 4-4. User

서비스 내부 회원 본체다.

```java
public class User {
    private Long id;
    private String status;
    private String role;
}
```

실제 서비스의 권한 처리, 세션, JWT 등은 이 내부 `User` 기준으로 가는 경우가 많다.

---

## 5. 왜 이렇게 나누는가

구글 로그인만 보고 바로 엔티티를 만들면 나중에 구조가 금방 구글 종속적으로 바뀐다.

예를 들어 이런 식은 피하는 편이 낫다.

- `googleId`
- `googleEmail`

대신 아래처럼 가는 게 공통 구조다.

- `provider`
- `providerUserId`
- `email`
- `name`

이렇게 해두면 Google 외에 다른 OIDC 공급자를 붙여도 큰 수정 없이 확장 가능하다.

---

## 6. 최소 구현 관점에서 기억할 것

- 의존성은 `spring-boot-starter-security`, `spring-boot-starter-oauth2-client`가 핵심
- 설정은 `spring.security.oauth2.client.registration.google`
- 로그인 성공 후 받는 객체는 `OidcUser`
- 외부 계정 연결용 구조는 `SocialAccount`
- 실제 서비스 회원은 내부 `User`

---

## 7. 한 줄 정리

Spring Boot에서 Google OIDC를 붙일 때는  
**`OidcUser`로 외부 인증 정보를 받고, `SocialAccount`로 연결한 뒤, 내부 `User` 기준으로 서비스 로직을 처리하는 구조**로 보면 된다.
