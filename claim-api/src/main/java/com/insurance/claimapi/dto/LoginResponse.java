package com.insurance.claimapi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserDto user;
}
