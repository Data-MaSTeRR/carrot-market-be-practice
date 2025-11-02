# 당근마켓 클론 백엔드 서버

Spring Boot와 MySQL을 사용한 당근마켓 클론 프로젝트입니다.

## 기술 스택

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Web**
- **Spring Data JPA**
- **Spring Security + JWT**
- **MySQL 8.0+**
- **Gradle**
- **Lombok**
- **SpringDoc (Swagger)**

## 주요 기능

### 1. 사용자 인증
- 회원가입 / 로그인
- JWT 기반 인증
- Spring Security를 통한 보안 처리

### 2. 상품 관리
- 상품 등록/수정/삭제
- 상품 상태 관리 (판매중/예약중/거래완료)
- 상품 검색 (키워드 검색)
- 지역별 상품 조회
- 상품 좋아요 기능
- 내가 등록한 상품 조회
- 관심 상품 조회

### 3. 채팅 (준비)
- 1:1 채팅방 생성
- 실시간 메시지 전송

## API 문서

프로젝트 실행 후, 아래 주소로 접속하여 API 문서를 확인할 수 있습니다.

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 데이터베이스 ERD

```
User (사용자)
├── id (PK)
├── email (unique)
├── password
├── nickname
├── location (지역)
└── createdAt

Product (상품)
├── id (PK)
├── sellerId (FK -> User)
├── title
├── description
├── price
├── location
├── status (판매중/예약중/거래완료)
├── viewCount
├── likeCount
└── createdAt

ProductLike (상품 좋아요)
├── id (PK)
├── userId (FK -> User)
├── productId (FK -> Product)
└── createdAt

ChatRoom (채팅방)
├── id (PK)
├── productId (FK -> Product)
├── buyerId (FK -> User)
├── sellerId (FK -> User)
└── createdAt

ChatMessage (채팅 메시지)
├── id (PK)
├── chatRoomId (FK -> ChatRoom)
├── senderId (FK -> User)
├── message
└── createdAt
```

## 설치 및 실행 방법

### 1. MySQL 데이터베이스 생성

```sql
CREATE DATABASE carrot_market;
```

### 2. application.yml 설정

`src/main/resources/application.yml` 파일에서 자신의 데이터베이스 정보를 수정하세요:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/carrot_market?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: your_username
    password: your_password
```

### 3. 프로젝트 빌드 및 실행

```bash
./gradlew bootRun
```

서버가 `http://localhost:8080`에서 실행됩니다.

## 프로젝트 구조

```
src/
├── main/
│   ├── java/com/carrot/market/
│   │   ├── config/              # 설정 파일
│   │   │   └── SecurityConfig.java
│   │   ├── controller/          # REST API 컨트롤러
│   │   │   ├── AuthController.java
│   │   │   └── ProductController.java
│   │   ├── dto/                 # 데이터 전송 객체
│   │   │   ├── LoginRequest.java
│   │   │   ├── SignupRequest.java
│   │   │   ├── JwtResponse.java
│   │   │   ├── ProductRequest.java
│   │   │   ├── ProductResponse.java
│   │   │   └── UserResponse.java
│   │   ├── entity/              # JPA 엔티티
│   │   │   ├── User.java
│   │   │   ├── Product.java
│   │   │   ├── ProductLike.java
│   │   │   ├── ChatRoom.java
│   │   │   ├── ChatMessage.java
│   │   │   ├── UserRole.java
│   │   │   └── ProductStatus.java
│   │   ├── repository/          # JPA 레포지토리
│   │   │   ├── UserRepository.java
│   │   │   ├── ProductRepository.java
│   │   │   ├── ProductLikeRepository.java
│   │   │   ├── ChatRoomRepository.java
│   │   │   └── ChatMessageRepository.java
│   │   ├── security/            # 보안 관련
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── CustomUserDetailsService.java
│   │   ├── service/             # 비즈니스 로직
│   │   │   ├── AuthService.java
│   │   │   └── ProductService.java
│   │   ├── util/                # 유틸리티
│   │   │   └── JwtUtils.java
│   │   └── CarrotMarketApplication.java  # 메인 클래스
│   └── resources/
│       ├── application.yml      # 애플리케이션 설정
│       └── schema.sql          # 데이터베이스 스키마
```

## 추가 개발 예정 기능

- [ ] 이미지 업로드 기능
- [ ] WebSocket을 통한 실시간 채팅
- [ ] 알림 기능
- [ ] 사용자 프로필 관리
- [ ] 거래 후기 시스템
- [ ] 카테고리 관리
- [ ] 신고 기능

## 라이선스

MIT License
