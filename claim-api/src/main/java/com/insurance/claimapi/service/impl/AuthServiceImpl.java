package com.insurance.claimapi.service.impl;

import com.insurance.claimapi.dto.*;
import com.insurance.claimapi.entity.User;
import com.insurance.claimapi.exception.AuthenticationException;
import com.insurance.claimapi.exception.ResourceNotFoundException;
import com.insurance.claimapi.mapper.UserMapper;
import com.insurance.claimapi.repository.UserRepository;
import com.insurance.claimapi.security.JwtTokenProvider;
import com.insurance.claimapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());

        User user = userRepository.findByUsernameWithRole(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }

        if (!user.getEnabled()) {
            throw new AuthenticationException("Account is disabled");
        }

        String token = jwtTokenProvider.generateToken(user);
        long expiresIn = jwtTokenProvider.getExpirationTime();

        log.info("User {} logged in successfully", request.getUsername());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(userMapper.toDto(user))
                .build();
    }
}
