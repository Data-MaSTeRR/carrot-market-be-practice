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

@RestController // RestAPI용
@RequestMapping("/api/auth") // 공통 엔드포인트 시작
@RequiredArgsConstructor // 생성자 DI
@Tag(name = "인증", description = "회원가입, 로그인 등 인증 관련 API") // Swagger용
public class AuthController {

    private final AuthService authService;

    /**
     * @Valid ???
     * DTO(요청 바디로 받는 객체)에 @Valid를 붙이면,
     * 그 DTO에 선언한 제약(예: @NotNull, @NotBlank, @Size, @Email, @Pattern 등)이 자동으로 검사됩니다.
     * SignupRequest, LoginRequest .java 파일 참고
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse response = authService.signup(request); // 받은 json body를 유효성 검사 후, Service로 넘김(DB에 저장)
        return ResponseEntity.ok(response);
        // ↓
        // Spring이 자동으로 UserResponse 객체를 JSON으로 변환!
        // -> RestController 덕분. @ResponseBody 포함
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
}
