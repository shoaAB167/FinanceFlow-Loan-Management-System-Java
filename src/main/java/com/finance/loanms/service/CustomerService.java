package com.finance.loanms.service;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.CustomerRequest;
import com.finance.loanms.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    ApiResponse<CustomerResponse> createCustomer(CustomerRequest request);

    ApiResponse<CustomerResponse> getCustomerById(Long customerId);

    ApiResponse<CustomerResponse> getCustomerByCustomerId(String customerId);

    ApiResponse<CustomerResponse> updateCustomer(Long customerId, CustomerRequest request);

    ApiResponse<String> deleteCustomer(Long customerId);

    ApiResponse<Page<CustomerResponse>> getAllCustomers(Pageable pageable);
}
