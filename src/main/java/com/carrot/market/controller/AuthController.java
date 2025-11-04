package com.carrot.market.controller;

import com.carrot.market.dto.JwtResponse;
import com.carrot.market.dto.LoginRequest;
import com.carrot.market.dto.SignupRequest;
import com.carrot.market.dto.UserResponse;
import com.carrot.market.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 인증 관련 HTTP 요청을 처리하는 컨트롤러
 * 회원가입, 로그인, 프로필 관리 등의 엔드포인트를 제공합니다.
 *
 * REST API 설계:
 * - 모든 응답은 JSON 형식
 * - HTTP 상태 코드를 통한 결과 표현
 * - RESTful 원칙 준수
 */
@RestController // @Controller + @ResponseBody의 조합
// 모든 메서드의 반환값이 자동으로 JSON으로 변환되어 HTTP 응답 본문에 포함됨
@RequestMapping("/api/auth") // 이 컨트롤러의 모든 엔드포인트는 /api/auth로 시작
// 예: /api/auth/signup, /api/auth/login
@RequiredArgsConstructor // Lombok: final 필드에 대한 생성자 자동 생성 (생성자 주입 방식의 DI)
@Tag(name = "인증", description = "회원가입, 로그인 등 인증 관련 API") // Swagger UI 문서화용 태그
public class AuthController {

    // 실제 비즈니스 로직을 처리하는 서비스 계층
    // 컨트롤러는 HTTP 요청/응답만 처리하고, 핵심 로직은 서비스에 위임
    private final AuthService authService;

    /**
     * 회원가입 엔드포인트
     *
     * HTTP 요청 예시:
     * POST /api/auth/signup
     * Content-Type: application/json
     * {
     *   "username": "홍길동",
     *   "email": "hong@example.com",
     *   "password": "Password123!",
     *   "phoneNumber": "010-1234-5678",
     *   "location": "서울시 강남구"
     * }
     *
     * @param request 회원가입 요청 데이터를 담은 DTO
     * @return 생성된 사용자 정보와 HTTP 200 OK 상태
     *
     * @Valid 어노테이션:
     * - DTO 클래스에 선언된 검증 규칙을 자동으로 실행
     * - SignupRequest 내의 @NotNull, @NotBlank, @Size, @Email, @Pattern 등이 검사됨
     * - 검증 실패 시 MethodArgumentNotValidException 발생 (400 Bad Request)
     *
     * 검증 예시 (SignupRequest.java):
     * @NotBlank(message = "사용자명은 필수입니다")
     * private String username;
     *
     * @Email(message = "올바른 이메일 형식이 아닙니다")
     * @NotBlank(message = "이메일은 필수입니다")
     * private String email;
     *
     * @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
     *          message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다")
     * private String password;
     */
    @PostMapping("/signup") // POST 메서드로 /api/auth/signup 엔드포인트 매핑
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        // @RequestBody: HTTP 요청 본문(JSON)을 SignupRequest 객체로 자동 변환
        // @Valid: SignupRequest의 검증 규칙 실행

        // 1. 검증된 요청을 서비스 계층으로 전달하여 회원가입 처리
        UserResponse response = authService.signup(request);

        // 2. ResponseEntity.ok()로 HTTP 200 OK 응답 생성
        // UserResponse 객체는 자동으로 JSON으로 변환됨 (@RestController 덕분)
        return ResponseEntity.ok(response);

        /**
         * 응답 JSON 예시:
         * {
         *   "id": 1,
         *   "username": "홍길동",
         *   "email": "hong@example.com",
         *   "phoneNumber": "010-1234-5678",
         *   "location": "서울시 강남구",
         *   "mannerTemperature": 36.5,
         *   "profileImageUrl": null,
         *   "createdAt": "2025-11-04T13:50:00"
         * }
         */
    }

    /**
     * 로그인 엔드포인트
     *
     * HTTP 요청 예시:
     * POST /api/auth/login
     * Content-Type: application/json
     * {
     *   "email": "hong@example.com",
     *   "password": "Password123!"
     * }
     *
     * @param request 로그인 요청 데이터 (email, password)
     * @return JWT 토큰과 사용자 기본 정보, HTTP 200 OK 상태
     *
     * 로그인 프로세스:
     * 1. @Valid로 email, password 형식 검증
     * 2. AuthService에서 인증 처리 (이메일/비밀번호 확인)
     * 3. 인증 성공 시 JWT 토큰 생성
     * 4. 토큰과 사용자 정보를 클라이언트에 반환
     */
    @PostMapping("/login") // POST 메서드로 /api/auth/login 엔드포인트 매핑
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        // @Valid로 LoginRequest의 검증 규칙 실행
        // 예: @Email, @NotBlank 등

        // 1. 서비스 계층에서 로그인 인증 처리
        JwtResponse response = authService.login(request);

        // 2. JWT 토큰과 사용자 정보를 JSON으로 반환
        return ResponseEntity.ok(response);

        /**
         * 응답 JSON 예시:
         * {
         *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
         *   "userId": 1,
         *   "username": "홍길동",
         *   "email": "hong@example.com"
         * }
         *
         * 클라이언트는 이 토큰을 저장하고, 이후 요청 시
         * Authorization: Bearer {token} 헤더에 포함하여 인증
         */
    }

    /**
     * 현재 로그인한 사용자 정보 조회 엔드포인트
     *
     * HTTP 요청 예시:
     * GET /api/auth/me
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     *
     * @return 현재 인증된 사용자의 정보
     *
     * 동작 원리:
     * 1. 클라이언트가 JWT 토큰을 Authorization 헤더에 포함하여 요청
     * 2. JwtAuthenticationFilter가 토큰을 검증하고 SecurityContext에 인증 정보 저장
     * 3. AuthService가 SecurityContext에서 현재 사용자 정보를 추출
     * 4. 사용자 정보를 DB에서 조회하여 반환
     */
    @GetMapping("/me") // GET 메서드로 /api/auth/me 엔드포인트 매핑
    public ResponseEntity<UserResponse> getCurrentUser() {
        // 별도의 매개변수 없음
        // JWT 토큰에서 자동으로 사용자 식별

        // 1. 현재 인증된 사용자 정보 조회
        UserResponse response = authService.getCurrentUser();

        // 2. 사용자 정보를 JSON으로 반환
        return ResponseEntity.ok(response);

        /**
         * 응답 JSON 예시:
         * {
         *   "id": 1,
         *   "username": "홍길동",
         *   "email": "hong@example.com",
         *   "phoneNumber": "010-1234-5678",
         *   "location": "서울시 강남구",
         *   "mannerTemperature": 36.5,
         *   "profileImageUrl": "http://localhost:8080/profile-images/abc-123.jpg",
         *   "createdAt": "2025-11-04T13:50:00"
         * }
         */
    }

    /**
     * 프로필 사진 업로드 엔드포인트
     *
     * HTTP 요청 예시:
     * POST /api/auth/profile-image
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * Content-Type: multipart/form-data
     *
     * file: [이미지 파일 바이너리 데이터]
     *
     * @param file 업로드할 프로필 이미지 파일
     * @return 프로필 이미지가 업데이트된 사용자 정보
     *
     * @RequestParam("file"):
     * - multipart/form-data 형식의 요청에서 "file" 이름의 파라미터를 받음
     * - HTML form에서 <input type="file" name="file">과 매핑됨
     *
     * 파일 업로드 프로세스:
     * 1. 클라이언트가 이미지 파일을 multipart/form-data로 전송
     * 2. Spring이 자동으로 MultipartFile 객체로 변환
     * 3. AuthService에서 파일 검증 및 저장 처리
     * 4. 저장된 파일의 URL을 DB에 업데이트
     */
    @PostMapping("/profile-image") // POST 메서드로 /api/auth/profile-image 엔드포인트 매핑
    public ResponseEntity<UserResponse> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        // @RequestParam("file"): HTTP 요청의 "file" 파라미터를 MultipartFile로 받음

        // 1. 서비스 계층에서 프로필 이미지 업데이트 처리
        //    - 기존 이미지 삭제
        //    - 새 이미지 저장
        //    - DB에 URL 업데이트
        UserResponse response = authService.updateProfileImage(file);

        // 2. 업데이트된 사용자 정보를 JSON으로 반환
        return ResponseEntity.ok(response);

        /**
         * 응답 JSON 예시:
         * {
         *   "id": 1,
         *   "username": "홍길동",
         *   "email": "hong@example.com",
         *   "profileImageUrl": "http://localhost:8080/profile-images/a1b2c3d4-e5f6-7890.jpg",
         *   ...
         * }
         */
    }

    /**
     * 프로필 사진 삭제 엔드포인트
     *
     * HTTP 요청 예시:
     * DELETE /api/auth/profile-image
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     *
     * @return 프로필 이미지가 삭제된 사용자 정보
     *
     * 삭제 프로세스:
     * 1. 현재 사용자의 프로필 이미지 URL 조회
     * 2. 서버 디스크에서 실제 파일 삭제
     * 3. DB의 profileImageUrl을 null로 업데이트
     */
    @DeleteMapping("/profile-image") // DELETE 메서드로 /api/auth/profile-image 엔드포인트 매핑
    public ResponseEntity<UserResponse> deleteProfileImage() {
        // 별도의 매개변수 없음
        // JWT 토큰에서 현재 사용자 식별

        // 1. 서비스 계층에서 프로필 이미지 삭제 처리
        UserResponse response = authService.deleteProfileImage();

        // 2. 업데이트된 사용자 정보를 JSON으로 반환
        return ResponseEntity.ok(response);

        /**
         * 응답 JSON 예시:
         * {
         *   "id": 1,
         *   "username": "홍길동",
         *   "email": "hong@example.com",
         *   "profileImageUrl": null,  // null로 변경됨
         *   ...
         * }
         */
    }
}
