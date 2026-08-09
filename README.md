<div align="center">
  <img width="166" height="101" alt="image" src="https://github.com/user-attachments/assets/60f80bb0-17a4-429a-8c0e-dc7abf863000" />

</div>

<p align="center">
  질문을 게시하고 다양한 의견을 투표로 확인하는 커뮤니티 서비스<br/>
  Votle 백엔드 레포지토리입니다.
</p>


> [서비스 바로가기](http://votle.kro.kr/posts)

---

# Overview
<div align="center">
  <img height="300" alt="image" src="https://github.com/user-attachments/assets/c3e319f6-09c9-4e78-b7f8-5e943c156a75" />
  <img height="300" alt="image" src="https://github.com/user-attachments/assets/81f32a22-bb05-47a3-8d27-ea7652c21713" />
  <img height="300" alt="image" src="https://github.com/user-attachments/assets/6ac80eaf-3062-4976-81bd-c6e3df5583ef" />
</div>

**Votle**은 게시글에 투표를 더해 다양한 의견을 쉽고 빠르게 모을 수 있는 커뮤니티 서비스입니다.<br/>
사용자는 게시글을 작성하고 투표를 생성하거나, 다른 사용자의 투표에 참여하고 댓글로 의견을 나눌 수 있습니다.

- 게시글과 투표를 한 화면에서 확인
- 투표 참여 후 항목별 결과와 참여 인원 확인
- 댓글과 좋아요를 통한 게시글 상호작용
- 마이페이지에서 작성·참여·좋아요 활동 관리
- 세션 기반 인증과 CSRF 보호를 적용한 API 연동

# Tech Stack

### Backend

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Security
- Spring Data JPA / Hibernate
- JWT (`jjwt`)

### Database

- MySQL
- Flyway

### Test & Infrastructure

- Gradle Wrapper
- JUnit 5
- H2
- Testcontainers
- Docker

# Core Features

| 기능 | 설명 |
|------|------|
| 🔐 인증 | 회원가입, 로그인, 로그아웃, 세션 갱신 |
| 📝 게시글 | 게시글 작성·조회·수정·삭제 및 이미지 첨부 |
| 💾 임시 저장 | 작성 중인 게시글과 투표 항목 임시 저장 |
| 🗳️ 투표 | 투표 생성·참여·결과 확인, 2~5개 항목 지원 |
| ❤️ 좋아요 | 게시글 좋아요 등록·취소 및 개수 확인 |
| 💬 댓글 | 댓글 작성·수정·삭제 |
| 👤 마이페이지 | 작성한 글, 참여한 투표, 좋아요한 글과 사용자 통계 확인 |
| ⚙️ 계정 관리 | 프로필 수정, 비밀번호 변경, 회원 탈퇴 |

# Architecture

<img width="3685" height="1559" alt="image" src="https://github.com/user-attachments/assets/8c3fba84-11fb-47cd-b583-f4b13c847938" />


### 주요 경로

| 경로 | 설명 |
|------|------|
| `/posts` | 게시글 목록 |
| `/posts/:postId` | 게시글 상세, 투표, 댓글 |
| `/posts/new` | 게시글 작성 |
| `/posts/:postId/edit` | 게시글 수정 |
| `/login` | 로그인 |
| `/signup` | 회원가입 |
| `/mypage` | 마이페이지 |
| `/profile/edit` | 프로필 수정 및 회원 탈퇴 |
| `/password/edit` | 비밀번호 변경 |


## 🤔 Retrospective

- JPA가 스키마를 임의로 변경하지 않도록 Flyway와 `ddl-auto: validate`를 함께 사용했습니다.
- 쿠키 기반 인증을 적용하면서 CORS credential과 CSRF 보호를 함께 고려했습니다.
- 게시글 목록과 마이페이지 조회에는 커서 기반 페이지네이션을 적용해 데이터 증가에
  대응할 수 있도록 구성했습니다.
- 도메인 예외를 전역 예외 처리기로 일관된 `message`·`data` 응답 형식으로 반환하도록
  구성했습니다.
