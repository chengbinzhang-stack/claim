package com.insurance.claimapi.service;

import com.insurance.claimapi.dto.*;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
