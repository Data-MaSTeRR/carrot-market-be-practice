package com.carrot.market.service;

import com.carrot.market.dto.JwtResponse;
import com.carrot.market.dto.LoginRequest;
import com.carrot.market.dto.SignupRequest;
import com.carrot.market.dto.UserResponse;
import com.carrot.market.entity.User;
import com.carrot.market.entity.UserRole;
import com.carrot.market.exception.EmailAlreadyExistsException;
import com.carrot.market.exception.UsernameAlreadyExistsException;
import com.carrot.market.repository.UserRepository;
import com.carrot.market.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    // from SecurityConfig
    // -> 비밀번호 암호화
    private final PasswordEncoder passwordEncoder;

    // from SecurityConfig
    // -> 로그인 인증 매니저(암호화된 비밀번호와 상세정보 비교하여 일치여부 확인)
    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    @Transactional
    public UserResponse signup(SignupRequest request) { // 요청DTO

        // Validation
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("이미 존재하는 사용자명입니다");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다");
        }

        // Entity 내 객체(인스턴스) 생성
        User user = User.builder() // builder -> 객체 생성 가독성 up 도구
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 -> 암호화 후에 저장
                .nickname(request.getNickname())
                .phoneNumber(request.getPhoneNumber())
                .location(request.getLocation())
                .mannerTemperature(36.5)
                .role(UserRole.USER)
                .isActive(true)
                .build();

        /**
         * 그 user 인스턴스를 저장 -> JpaRepository (super class 내 메서드)
         * ↓
         * JPA가 자동으로 SQL 생성 & 실행:
         * INSERT INTO users (username, email, password, ...)
         * VALUES ('홍길동', 'test@example.com', 'encrypted...', ...)
         *
         * DB에 저장 완료!
         * DB가 생성한 ID를 user 객체에 자동으로 넣어줌
         */
        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    public JwtResponse login(LoginRequest request) {
        // AuthenticationManager가 인증 실패 시 BadCredentialsException 발생 (일반적 메시지)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 인증 성공 후에는 사용자가 반드시 존재하므로 User Enumeration 위험 없음
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("인증 처리 중 오류가 발생했습니다"));

        return new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getNickname());
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        // 인증 정보 가져오기 from SecurityContextHolder `JwtAuthenticationFilter`
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("인증 처리 중 오류가 발생했습니다"));

        return UserResponse.from(user);
    }
}
