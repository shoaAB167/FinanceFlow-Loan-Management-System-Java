package com.finance.loanms.service.impl;

import com.finance.loanms.dto.ApiResponse;
import com.finance.loanms.dto.request.CustomerRequest;
import com.finance.loanms.dto.response.CustomerResponse;
import com.finance.loanms.exception.ResourceNotFoundException;
import com.finance.loanms.model.entity.Customer;
import com.finance.loanms.repository.CustomerRepository;
import com.finance.loanms.service.CustomerService;
import com.finance.loanms.util.CustomerIdGenerator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerIdGenerator customerIdGenerator;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerIdGenerator customerIdGenerator) {
        this.customerRepository = customerRepository;
        this.customerIdGenerator = customerIdGenerator;
    }

    @Override
    @Transactional
    public ApiResponse<CustomerResponse> createCustomer(CustomerRequest request) {
        try {
            // Auto-generate unique customer ID
            String generatedCustomerId = customerIdGenerator.generateCustomerId();
            
            // Ensure uniqueness (in case of rare collisions)
            while (customerRepository.existsByCustomerId(generatedCustomerId)) {
                generatedCustomerId = customerIdGenerator.generateCustomerId();
            }
            
            Customer customer = Customer.builder()
                    .customerId(generatedCustomerId)
                    .name(request.getName())
                    .email(request.getEmail())
                    .build();

            Customer savedCustomer = customerRepository.save(customer);
            CustomerResponse response = mapToResponse(savedCustomer);

            return ApiResponse.ok("Customer created successfully", response);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    @Override
    public ApiResponse<CustomerResponse> getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        CustomerResponse response = mapToResponse(customer);
        return ApiResponse.ok("Customer retrieved successfully", response);
    }

    @Override
    public ApiResponse<CustomerResponse> getCustomerByCustomerId(String customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with Customer ID: " + customerId));

        CustomerResponse response = mapToResponse(customer);
        return ApiResponse.ok("Customer retrieved successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<CustomerResponse> updateCustomer(Long customerId, CustomerRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        try {
            customer.setName(request.getName());
            customer.setEmail(request.getEmail());
            // Note: customerId is typically not updatable for consistency

            Customer updatedCustomer = customerRepository.save(customer);
            CustomerResponse response = mapToResponse(updatedCustomer);

            return ApiResponse.ok("Customer updated successfully", response);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        // Check if customer has active loans
        if (!customer.getLoanAccounts().isEmpty()) {
            throw new IllegalStateException("Cannot delete customer with existing loan accounts");
        }

        customerRepository.delete(customer);
        return ApiResponse.ok("Customer deleted successfully", "Customer with ID " + customerId + " has been deleted");
    }

    @Override
    public ApiResponse<Page<CustomerResponse>> getAllCustomers(Pageable pageable) {
        Page<Customer> customers = customerRepository.findAll(pageable);
        Page<CustomerResponse> response = customers.map(this::mapToResponse);

        return ApiResponse.ok("Customers retrieved successfully", response);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .email(customer.getEmail())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .loans(customer.getLoanAccounts().stream()
                        .map(loan -> CustomerResponse.LoanSummary.builder()
                                .loanId(loan.getId())
                                .loanAccountId(loan.getLoanId())
                                .principal(loan.getPrincipal())
                                .status(loan.getStatus().toString())
                                .tenureMonths(loan.getTenureMonths())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
