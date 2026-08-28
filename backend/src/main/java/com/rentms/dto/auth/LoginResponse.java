package com.rentms.dto.auth;

import com.rentms.entity.User;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private UserDto user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDto {
        private Long id;
        private String name;
        private String mobileNumber;
        private String email;
        private User.Role role;

        public static UserDto from(User user) {
            return UserDto.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .mobileNumber(user.getMobileNumber())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();
        }
    }
}