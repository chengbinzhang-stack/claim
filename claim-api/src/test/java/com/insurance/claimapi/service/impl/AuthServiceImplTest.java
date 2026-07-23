package com.insurance.claimapi.service.impl;

import com.insurance.claimapi.dto.*;
import com.insurance.claimapi.entity.*;
import com.insurance.claimapi.exception.AuthenticationException;
import com.insurance.claimapi.mapper.UserMapper;
import com.insurance.claimapi.repository.RoleRepository;
import com.insurance.claimapi.repository.UserRepository;
import com.insurance.claimapi.security.JwtTokenProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserDto testUserDto;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = Role.builder().id(1L).name("CUSTOMER").build();

        testUser = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .fullName("Test User").password("$2a$10$encoded")
                .role(customerRole).enabled(true)
                .build();

        testUserDto = UserDto.builder()
                .id(1L).username("testuser").email("test@example.com")
                .fullName("Test User").roleName("CUSTOMER")
                .build();
    }

    @Test
    @DisplayName("login - success")
    void login_success() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser").password("password123")
                .build();

        when(userRepository.findByUsernameWithRole("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser)).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("testuser", response.getUser().getUsername());
    }

    @Test
    @DisplayName("login - user not found throws AuthenticationException")
    void login_userNotFound() {
        LoginRequest request = LoginRequest.builder()
                .username("unknown").password("password")
                .build();

        when(userRepository.findByUsernameWithRole("unknown")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("login - wrong password throws AuthenticationException")
    void login_wrongPassword() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser").password("wrongpassword")
                .build();

        when(userRepository.findByUsernameWithRole("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("login - disabled account throws AuthenticationException")
    void login_disabledAccount() {
        testUser.setEnabled(false);
        LoginRequest request = LoginRequest.builder()
                .username("testuser").password("password123")
                .build();

        when(userRepository.findByUsernameWithRole("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("register - success")
    void register_success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser").password("password123")
                .email("new@example.com").fullName("New User")
                .build();

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodednew");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("new-jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);
        when(userMapper.toDto(any(User.class))).thenReturn(
                UserDto.builder().id(2L).username("newuser").email("new@example.com")
                        .fullName("New User").roleName("CUSTOMER").build());

        LoginResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("new-jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - duplicate username throws AuthenticationException")
    void register_duplicateUsername() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser").password("password123")
                .email("new@example.com")
                .build();

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(testUser));

        assertThrows(AuthenticationException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("register - duplicate email throws AuthenticationException")
    void register_duplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser").password("password123")
                .email("test@example.com")
                .build();

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(AuthenticationException.class, () -> authService.register(request));
    }
}
