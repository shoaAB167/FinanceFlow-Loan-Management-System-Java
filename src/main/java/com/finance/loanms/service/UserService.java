package com.finance.loanms.service;

import com.finance.loanms.dto.ApiResponse;

import java.util.Map;

public interface UserService {
    ApiResponse<String> register(String username, String password);
    ApiResponse<Map<String, String>> login(String username, String password);
    ApiResponse<Map<String, String>> refresh(String refreshToken);
}
