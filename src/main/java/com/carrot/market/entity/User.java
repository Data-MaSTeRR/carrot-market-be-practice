package com.carrot.market.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity  // JPA 엔티티 선언 - DB 테이블과 매핑
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_location", columnList = "location"),
                @Index(name = "idx_phone", columnList = "phoneNumber")
        }
)
@Getter @Setter  // getter/setter 자동 생성
@NoArgsConstructor  // 기본 생성자 (JPA 필수)
@AllArgsConstructor  // 모든 필드 생성자
@Builder  // 빌더 패턴 (User.builder().name("").build())
public class User {

    @Id  // 기본 키
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto Increment
    private Long id;

    @Column(length = 50)  // VARCHAR(50)
    private String username;

    @Column(nullable = false, unique = true, length = 255) // NOT NULL, UNIQUE 제약조건, VARCHAR(255)
    private String email;

    @Column(nullable = false, length = 255) // VARCHAR(255) NOT NULL
    private String password;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "profile_image_url", length = 200)
    private String profileImageUrl;

    @Column(length = 255) // VARCHAR(255)
    private String location; // 동네 정보

    @Column(name = "manner_temperature", columnDefinition = "DOUBLE DEFAULT 36.5")
    private Double mannerTemperature; // 매너온도

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
