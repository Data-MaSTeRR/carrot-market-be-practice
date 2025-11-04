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
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 인증 및 회원 관리를 담당하는 서비스 클래스
 * 회원가입, 로그인, 프로필 관리 등의 핵심 비즈니스 로직을 처리합니다.
 */
@Service  // Spring의 서비스 계층 컴포넌트로 등록
@RequiredArgsConstructor  // Lombok: final 필드에 대한 생성자 자동 생성 (의존성 주입용)
public class AuthService {

    // 사용자 데이터베이스 접근을 위한 Repository
    private final UserRepository userRepository;

    // SecurityConfig에서 주입됨
    // 비밀번호를 암호화(해시)하는 인코더 (평문 비밀번호를 암호화하여 DB에 저장)
    private final PasswordEncoder passwordEncoder;

    // SecurityConfig에서 주입됨
    // 로그인 시 사용자 인증을 처리하는 매니저
    // 입력받은 비밀번호와 DB에 저장된 암호화된 비밀번호를 비교하여 일치 여부 확인
    private final AuthenticationManager authenticationManager;

    // JWT 토큰 생성 및 검증을 담당하는 유틸리티 클래스
    private final JwtUtils jwtUtils;

    // 파일 업로드/삭제를 처리하는 서비스
    private final FileService fileService;

    /**
     * 회원가입 처리 메서드
     * @param request 회원가입 요청 데이터 (username, email, password 등)
     * @return 생성된 사용자 정보를 담은 응답 DTO
     * @throws UsernameAlreadyExistsException 중복된 사용자명이 존재할 경우
     * @throws EmailAlreadyExistsException 중복된 이메일이 존재할 경우
     */
    @Transactional  // 메서드 실행을 하나의 트랜잭션으로 묶음 (DB 작업의 원자성 보장)
    public UserResponse signup(SignupRequest request) { // 요청 DTO를 매개변수로 받음

        // 1. 사용자명 중복 검증
        // 동일한 username이 이미 DB에 존재하는지 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("이미 존재하는 사용자명입니다");
        }

        // 2. 이메일 중복 검증
        // 동일한 email이 이미 DB에 존재하는지 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다");
        }

        // 3. User 엔티티 객체(인스턴스) 생성
        // Builder 패턴을 사용하여 가독성 높게 객체 생성
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호를 암호화하여 저장
                .phoneNumber(request.getPhoneNumber())
                .location(request.getLocation())
                .mannerTemperature(36.5)  // 초기 매너온도 기본값 설정
                .role(UserRole.USER)       // 기본 권한을 일반 사용자로 설정
                .isActive(true)            // 활성 상태로 설정
                .build();

        /**
         * 4. DB에 사용자 정보 저장
         * userRepository.save(user)를 호출하면:
         *
         * JPA가 자동으로 SQL 생성 & 실행:
         * INSERT INTO users (username, email, password, phone_number, location,
         *                   manner_temperature, role, is_active, created_at)
         * VALUES ('홍길동', 'test@example.com', '$2a$10$encrypted...', '010-1234-5678',
         *         '서울시 강남구', 36.5, 'USER', true, '2025-11-04 13:50:00')
         *
         * 저장 완료 후:
         * - DB가 자동 생성한 ID(Primary Key)를 user 객체에 자동으로 할당
         * - 저장된 완전한 엔티티를 반환 (ID 포함)
         */
        user = userRepository.save(user);

        // 5. Entity를 DTO로 변환하여 반환
        // 민감한 정보(비밀번호 등)를 제외하고 필요한 정보만 클라이언트에 전달
        return UserResponse.from(user);
    }

    /**
     * 로그인 처리 메서드
     * @param request 로그인 요청 데이터 (email, password)
     * @return JWT 토큰과 사용자 기본 정보를 담은 응답 DTO
     * @throws BadCredentialsException 인증 실패 시 (이메일 또는 비밀번호 불일치)
     */
    public JwtResponse login(LoginRequest request) {
        // 1. 사용자 인증 시도
        // AuthenticationManager가 다음을 수행:
        // - DB에서 email로 사용자 조회
        // - 입력받은 password와 DB의 암호화된 password 비교
        // - 인증 실패 시 BadCredentialsException 자동 발생 (보안을 위해 일반적인 메시지 제공)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. 인증 성공 시 SecurityContext에 인증 정보 저장
        // 이후 요청에서 현재 로그인한 사용자 정보를 조회할 수 있게 됨
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. JWT 토큰 생성
        // 사용자 정보를 기반으로 JWT 토큰 생성 (보통 email, role 등 포함)
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 4. 사용자 정보 조회
        // 인증 성공 후에는 사용자가 반드시 존재하므로 User Enumeration 공격 위험 없음
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("인증 처리 중 오류가 발생했습니다"));

        // 5. JWT 토큰과 사용자 기본 정보를 담은 응답 반환
        return new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail());
    }

    /**
     * 현재 로그인한 사용자 정보 조회 메서드
     * @return 현재 인증된 사용자의 정보를 담은 응답 DTO
     * @throws RuntimeException 인증 정보가 유효하지 않을 경우
     */
    @Transactional(readOnly = true)  // 읽기 전용 트랜잭션 (성능 최적화)
    public UserResponse getCurrentUser() {
        // 1. SecurityContext에서 현재 인증 정보 가져오기
        // JwtAuthenticationFilter에서 JWT 토큰을 검증하고 설정한 인증 정보
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증 정보에서 이메일 추출
        // getName()은 일반적으로 사용자 식별자(여기서는 email)를 반환
        String email = authentication.getName();

        // 3. 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("인증 처리 중 오류가 발생했습니다"));

        // 4. Entity를 DTO로 변환하여 반환
        return UserResponse.from(user);
    }

    /**
     * 프로필 사진 업데이트 메서드
     * @param file 업로드할 프로필 사진 파일 (MultipartFile 형식)
     * @return 프로필 사진이 업데이트된 사용자 정보
     * @throws RuntimeException 유효하지 않은 이미지 파일이거나 인증 오류 시
     */
    @Transactional  // DB 작업(업데이트)을 트랜잭션으로 묶음
    public UserResponse updateProfileImage(MultipartFile file) {
        // 1. 파일 유효성 검증
        // 이미지 파일 형식, 크기 등을 체크 (FileService에서 구현)
        if (!fileService.isValidImageFile(file)) {
            throw new RuntimeException("유효하지 않은 이미지 파일입니다");
        }

        // 2. 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("인증 처리 중 오류가 발생했습니다"));

        // 3. 기존 프로필 이미지가 있다면 삭제
        // 서버 디스크 공간 관리를 위해 이전 파일 제거
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            fileService.deleteFile(user.getProfileImageUrl());
        }

        // 4. 새 파일을 서버에 저장
        // 파일이 실제 디스크에 저장되고, 접근 가능한 URL이 반환됨
        // 예: "http://localhost:8080/profile-images/a1b2c3d4-e5f6-7890.jpg"
        String fileUrl = fileService.saveFile(file);

        // 5. 사용자 엔티티의 프로필 이미지 URL 업데이트
        user.setProfileImageUrl(fileUrl);

        // 6. 변경사항을 DB에 저장
        user = userRepository.save(user);

        // 7. 업데이트된 사용자 정보를 DTO로 변환하여 반환
        return UserResponse.from(user);
    }

    /**
     * 프로필 사진 삭제 메서드
     * @return 프로필 사진이 삭제된 사용자 정보
     * @throws RuntimeException 인증 오류 시
     */
    @Transactional  // DB 작업(업데이트)을 트랜잭션으로 묶음
    public UserResponse deleteProfileImage() {
        // 1. 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("인증 처리 중 오류가 발생했습니다"));

        // 2. 프로필 이미지가 있는지 확인
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            // 3. 서버 디스크에서 실제 파일 삭제
            fileService.deleteFile(user.getProfileImageUrl());

            // 4. DB의 프로필 이미지 URL을 null로 설정
            user.setProfileImageUrl(null);

            // 5. 변경사항을 DB에 저장
            user = userRepository.save(user);
        }

        // 6. 업데이트된 사용자 정보를 DTO로 변환하여 반환
        return UserResponse.from(user);
    }
}
