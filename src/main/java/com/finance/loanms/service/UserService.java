package com.finance.loanms.service;

import com.finance.loanms.dto.ApiResponse;

import com.finance.loanms.dto.request.LoginRequest;
import com.finance.loanms.dto.request.RegisterRequest;

import java.util.Map;

public interface UserService {
    ApiResponse<String> register(RegisterRequest request);

    ApiResponse<Map<String, String>> login(LoginRequest request);

    ApiResponse<Map<String, String>> refresh(String refreshToken);
}
