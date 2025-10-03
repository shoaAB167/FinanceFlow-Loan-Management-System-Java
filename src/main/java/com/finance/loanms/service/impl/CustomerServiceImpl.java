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

import java.util.List;
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
            if (request == null) {
                throw new IllegalArgumentException("Customer request cannot be null");
            }
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Customer name is required");
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Customer email is required");
            }
            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            
            // Auto-generate unique customer ID
            String generatedCustomerId = customerIdGenerator.generateCustomerId();
            
            // Ensure uniqueness (in case of rare collisions)
            int maxAttempts = 5;
            int attempts = 0;
            while (customerRepository.existsByCustomerId(generatedCustomerId) && attempts < maxAttempts) {
                generatedCustomerId = customerIdGenerator.generateCustomerId();
                attempts++;
            }
            
            if (attempts >= maxAttempts) {
                throw new IllegalStateException("Failed to generate a unique customer ID after " + maxAttempts + " attempts");
            }
            
            Customer customer = Customer.builder()
                    .customerId(generatedCustomerId)
                    .name(request.getName().trim())
                    .email(request.getEmail().toLowerCase().trim())
                    .build();

            Customer savedCustomer = customerRepository.save(customer);
            CustomerResponse response = mapToResponse(savedCustomer);

            return ApiResponse.ok("Customer created successfully", response);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email already exists");
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create customer: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<CustomerResponse> getCustomerById(Long customerId) {
        try {
            if (customerId == null || customerId <= 0) {
                throw new IllegalArgumentException("Invalid customer ID");
            }
            
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
            
            CustomerResponse response = mapToResponse(customer);
            return ApiResponse.ok("Customer retrieved successfully", response);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customer: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<CustomerResponse> getCustomerByCustomerId(String customerId) {
        try {
            if (customerId == null || customerId.trim().isEmpty()) {
                throw new IllegalArgumentException("Customer ID cannot be empty");
            }
            
            Customer customer = customerRepository.findByCustomerId(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with Customer ID: " + customerId));
                    
            CustomerResponse response = mapToResponse(customer);
            return ApiResponse.ok("Customer retrieved successfully", response);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customer by customer ID: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<CustomerResponse> updateCustomer(Long customerId, CustomerRequest request) {
        try {
            if (customerId == null || customerId <= 0) {
                throw new IllegalArgumentException("Invalid customer ID");
            }
            if (request == null) {
                throw new IllegalArgumentException("Customer request cannot be null");
            }
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Customer name is required");
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Customer email is required");
            }
            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

            customer.setName(request.getName().trim());
            customer.setEmail(request.getEmail().toLowerCase().trim());
            // Note: customerId is typically not updatable for consistency
            
            Customer updatedCustomer = customerRepository.save(customer);
            CustomerResponse response = mapToResponse(updatedCustomer);

            return ApiResponse.ok("Customer updated successfully", response);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email already exists");
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update customer: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteCustomer(Long customerId) {
        try {
            if (customerId == null || customerId <= 0) {
                throw new IllegalArgumentException("Invalid customer ID");
            }
            
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

            // Check if customer has active loans
            if (!customer.getLoanAccounts().isEmpty()) {
                throw new IllegalStateException("Cannot delete customer with active loans. Please close all loans before deleting the customer.");
            }

            customerRepository.delete(customer);
            return ApiResponse.ok("Customer deleted successfully", "Customer with ID " + customerId + " has been deleted");
            
        } catch (IllegalArgumentException | IllegalStateException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete customer: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<Page<CustomerResponse>> getAllCustomers(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }
            
            Page<Customer> customers = customerRepository.findAll(pageable);
            Page<CustomerResponse> response = customers.map(this::mapToResponse);
            return ApiResponse.ok("Customers retrieved successfully", response);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers: " + e.getMessage(), e);
        }
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .email(customer.getEmail())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .loans(customer.getLoanAccounts() != null
                        ? customer.getLoanAccounts().stream()
                        .map(loan -> CustomerResponse.LoanSummary.builder()
                                .loanId(loan.getId())
                                .loanAccountId(loan.getLoanId())
                                .principal(loan.getPrincipal())
                                .status(loan.getStatus().toString())
                                .tenureMonths(loan.getTenureMonths())
                                .build())
                        .collect(Collectors.toList())
                        : List.of()
                ).build();
    }
}
