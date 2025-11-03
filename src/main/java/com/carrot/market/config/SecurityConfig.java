package com.carrot.market.config;

import com.carrot.market.security.UserDetailsService;
import com.carrot.market.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 설정 클래스
 * - JWT 기반 인증/인가
 * - Stateless 세션 관리
 * - CORS 설정
 */
@Configuration // Spring 설정 클래스
@EnableWebSecurity // Spring Security 활성화
@EnableMethodSecurity // @PreAuthorize 같은 메서드 레벨 보안 활성화
@RequiredArgsConstructor // final 필드 생성자 주입
public class SecurityConfig {

    // @RequiredArgsConstructor가 생성자 주입 처리
    private final UserDetailsService userDetailsService;

    // ---------------------------------------------------------------

    // 인증 없이 접근 가능한 공개 엔드포인트
    private static final String[] PUBLIC_ENDPOINTS = {
            "/",
            "/api/auth/**",
            "/api/products",
            "/api/products/*",
            "/api/products/search",
            "/api/products/location"
    };

    // Swagger UI 엔드포인트
    private static final String[] SWAGGER_ENDPOINTS = {
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    // 정적 리소스 엔드포인트
    private static final String[] STATIC_RESOURCES = {
            "/css/**",
            "/js/**",
            "/images/**"
    };

    // ---------------------------------------------------------------

    /**
     * 직접 구현한 jwt 인증필터
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * 비밀번호 암호화하는 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 매니저 Bean 등록
     * - 로그인 시 사용자 인증 처리
     * - UserDetailsService와 PasswordEncoder를 자동으로 사용하여 인증 수행
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ---------------------------------------------------------------

    /**
     * Spring Security 필터 체인 설정
     * - CSRF 비활성화 (JWT 사용)
     * - CORS 설정
     * - Stateless 세션 관리
     * - 엔드포인트별 인증/인가 규칙
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // jwt는 session을 stateless로 관리하여 csrf가 필요없다.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless: 서버에 세션을 저장하지 않고 매 요청마다 JWT로 인증
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 엔드포인트
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                        .requestMatchers(STATIC_RESOURCES).permitAll()
                        // 나머지는 인증이 필요한 엔드포인트
                        .anyRequest().authenticated()
                )
                // `UsernamePasswordAuthenticationFilter` 전에 `JwtAuthenticationFilter` 적용
                // -> 가장 먼저 jwt토큰 검증
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정
     * - 허용할 Origin, Method, Header 정의
     * - Credentials 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
