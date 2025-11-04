package com.carrot.market.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    // Validation 제약 조건 상수
    private static final int USERNAME_MIN_LENGTH = 1;
    private static final int USERNAME_MAX_LENGTH = 10;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 40;
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final String PHONE_NUMBER_PATTERN = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$";
    private static final int LOCATION_MIN_LENGTH = 2;
    private static final int LOCATION_MAX_LENGTH = 100;
    private static final String LOCATION_PATTERN = "^[가-힣A-Za-z0-9\\s.,-]{" + LOCATION_MIN_LENGTH + "," + LOCATION_MAX_LENGTH + "}$";


    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = USERNAME_MIN_LENGTH, max = USERNAME_MAX_LENGTH,
          message = "사용자명은 " + USERNAME_MIN_LENGTH + "~" + USERNAME_MAX_LENGTH + "자여야 합니다")
    private String username;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH,
          message = "비밀번호는 " + PASSWORD_MIN_LENGTH + "~" + PASSWORD_MAX_LENGTH + "자여야 합니다")
    @Pattern(
            regexp = PASSWORD_PATTERN,
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자(@$!%*?&)를 각각 최소 1개 이상 포함해야 합니다"
    )
    private String password;

    @NotBlank(message = "휴대폰 번호는 필수입니다")
    @Pattern(regexp = PHONE_NUMBER_PATTERN, message = "휴대폰 번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
    private String phoneNumber;

    @NotBlank(message = "위치 정보는 필수입니다")
    @Size(min = LOCATION_MIN_LENGTH, max = LOCATION_MAX_LENGTH,
          message = "위치 정보는 " + LOCATION_MIN_LENGTH + "~" + LOCATION_MAX_LENGTH + "자여야 합니다")
    @Pattern(regexp = LOCATION_PATTERN, message = "위치 정보 형식이 올바르지 않습니다")
    private String location;
}
