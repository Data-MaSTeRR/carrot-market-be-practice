# JWT (JSON Web Token) 핵심 개념

## JWT란?

JSON 기반의 **자체 포함형(self-contained)** 인증 토큰으로, 디지털 서명을 통해 신뢰성을 보장합니다.

### 핵심 특징
- **Stateless**: 서버에 세션 저장 불필요
- **자체 포함**: 토큰에 사용자 정보 포함
- **디지털 서명**: 변조 방지

## JWT 구조

```
Header.Payload.Signature
xxxxx.yyyyy.zzzzz
```

### 1. Header (헤더)
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```
- `alg`: 서명 알고리즘 (HS256, RS256 등)
- `typ`: 토큰 타입

### 2. Payload (페이로드)
```json
{
  "sub": "1234567890",
  "name": "John Doe",
  "iat": 1516239022,
  "exp": 1516242622
}
```

**표준 클레임 (Registered Claims)**:
- `sub`: 주제 (사용자 ID)
- `iat`: 발급 시간
- `exp`: 만료 시간
- `iss`: 발급자
- `aud`: 대상자

### 3. Signature (서명)
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

## JWT vs 세션

| 특징 | 세션 | JWT |
|------|------|-----|
| 저장 위치 | 서버 (메모리/DB/Redis) | 클라이언트 |
| 상태 관리 | Stateful | Stateless |
| 확장성 | 어려움 (세션 공유 필요) | 쉬움 |
| 즉시 무효화 | 가능 ✓ | 어려움 ✗ |
| 토큰 크기 | 작음 (Session ID) | 큼 (수백 bytes) |

## 동작 방식

```
1. 로그인
   Client → Server
   (username, password)

2. JWT 발급
   Server → Client
   {
     "token": "eyJhbGci...",
     "expiresIn": 3600
   }

3. 인증 요청
   Client → Server
   Authorization: Bearer eyJhbGci...

4. 토큰 검증
   Server
   - 서명 검증
   - 만료 시간 확인
   → 사용자 정보 추출
```

## 보안 고려사항

### ⚠️ 주요 위험

1. **XSS 공격**
   - localStorage 사용 시 토큰 탈취 위험
   - **대책**: HttpOnly 쿠키 사용

2. **토큰 무효화 불가**
   - 발급된 토큰은 만료 전까지 유효
   - **대책**: 짧은 만료 시간 + Refresh Token

3. **Payload 노출**
   - Base64 인코딩만 됨 (암호화 아님)
   - **대책**: 민감한 정보 제외

### ✅ 안전한 사용

```javascript
// 1. 짧은 만료 시간
const accessToken = jwt.sign(payload, secret, { expiresIn: '15m' });

// 2. HTTPS 필수
// HTTP는 토큰 탈취 위험

// 3. HttpOnly 쿠키
res.cookie('token', jwt, {
  httpOnly: true,    // JavaScript 접근 불가
  secure: true,      // HTTPS만
  sameSite: 'strict' // CSRF 방어
});

// 4. 최소한의 정보만 포함
const payload = {
  userId: 123,
  role: 'user'
  // ❌ password, ssn 등 민감정보 제외
};

// 5. 강력한 Secret Key
// 최소 256비트, 환경 변수 관리
```

## Refresh Token 패턴

```
Access Token: 15분 (짧게)
Refresh Token: 7일 (DB 저장)

┌─────────────┐
│ Access      │ 만료 시
│ Token       │────────→ Refresh Token으로 재발급
│ 만료        │          (Refresh Token도 검증)
└─────────────┘
```

**장점**:
- Access Token 탈취 시 피해 최소화 (15분)
- Refresh Token은 DB 저장으로 즉시 무효화 가능

## 언제 JWT를 사용할까?

### ✅ 적합한 경우
- 마이크로서비스 아키텍처
- 모바일 앱 인증
- SPA (Single Page Application)
- 확장성이 중요한 서비스

### ❌ 부적합한 경우
- 즉시 토큰 무효화가 필수 (금융 서비스)
- 단일 서버 소규모 서비스
- 매우 민감한 정보 다룰 때

## 핵심 요약

1. **JWT = Stateless 인증**
   - 서버에 세션 저장 불필요
   - 확장성 우수

2. **보안 = 구현 방법**
   - 짧은 만료 시간
   - HTTPS 필수
   - HttpOnly 쿠키
   - 민감정보 제외

3. **Refresh Token 권장**
   - Access Token: 짧게 (15분)
   - Refresh Token: DB 관리

## 참고 자료

- [JWT.io](https://jwt.io) - JWT 디버거
- [RFC 7519](https://tools.ietf.org/html/rfc7519) - JWT 표준
- [OWASP JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)