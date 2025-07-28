package com.finance.loanms.controller;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.CustomerRequest;
import com.finance.loanms.dto.response.CustomerResponse;
import com.finance.loanms.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }

    @GetMapping("/customer-id/{customerIdentifier}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByCustomerId(@PathVariable String customerIdentifier) {
        return ResponseEntity.ok(customerService.getCustomerByCustomerId(customerIdentifier));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<ApiResponse<String>> deleteCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.deleteCustomer(customerId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> getAllCustomers(Pageable pageable) {
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }
}
