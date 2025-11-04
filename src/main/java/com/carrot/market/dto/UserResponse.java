package com.carrot.market.dto;

import com.carrot.market.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // 빌더 패턴 -> 가독성 up
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private String location;
    private Double mannerTemperature;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .location(user.getLocation())
                .mannerTemperature(user.getMannerTemperature())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
