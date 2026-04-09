# 프론트 선구성용 폴더 구조 제안

## 추천 방향
백엔드가 아직 없을 때는 **기능 기준(feature-based)** 구조로 시작하는 것이 가장 무난하다.

기준은 아래처럼 잡으면 된다.

- `pages`: 라우트 단위 화면 조립
- `features`: 기능별 UI, 상태, API, mock 관리
- `shared`: 여러 기능에서 공통으로 쓰는 것만 보관
- `app`: 전역 설정, 라우터, provider 관리

---

## 추천 폴더 구조

```txt
src/
  app/
    router/
    providers/
    styles/

  pages/
    HomePage/
    UserPage/

  features/
    auth/
      api/
      model/
      ui/
      mock/
    user/
      api/
      model/
      ui/
      mock/

  shared/
    ui/
    lib/
    hooks/
    types/
    constants/
```

---

## 왜 이 구조가 적합한가

- 백엔드가 없어도 `mock` 기반으로 화면을 먼저 만들 수 있음
- 이후 실제 API가 나오면 `api` 내부만 교체하면 됨
- 페이지와 기능 로직이 분리되어 유지보수가 쉬움
- 파일이 기능 단위로 묶여 있어서 찾기 편함

---

## 기술 스택 추천

초기 약식 프론트 구성 기준으로는 아래 조합이 가장 무난하다.

- **React**
- **TypeScript**
- **Vite**
- **React Router**
- **TanStack Query**
- **Redux Toolkit** (전역 UI·클라이언트 상태)
- UI 라이브러리: **Ant Design**

---

## 상태 관리 기준

- 서버 데이터: **TanStack Query**
- 전역 UI·클라이언트 상태: **Redux Toolkit** (`createSlice`, `configureStore`)
- 페이지 내부 상태: **useState**

전역 슬라이스는 실제로 공유되는 도메인만 두고, 처음부터 과하게 쪼개지 않는다.

---

## 구현 시 핵심 원칙

- mock 데이터도 실제 API 응답 형태를 가정해서 만든다
- 컴포넌트 안에 mock 데이터를 직접 박지 않는다
- 공통화는 처음부터 과하게 하지 않는다
- `shared`에는 진짜 공통 요소만 둔다

---

## 한 줄 결론
지금 단계에서는 **feature-based 구조 + mock 분리 + TanStack Query와 Redux Toolkit의 역할 분리**로 시작하는 것이 가장 실용적이다.
