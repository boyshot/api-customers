package com.example.apicustomer.service;

import com.example.apicustomer.dto.CustomerRequest;
import com.example.apicustomer.dto.CustomerResponse;
import com.example.apicustomer.entity.Customer;
import com.example.apicustomer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    public Optional<CustomerResponse> findById(UUID id) {
        return customerRepository.findById(id)
            .map(this::toResponse);
    }

    public Optional<CustomerResponse> findByEmail(String email) {
        return customerRepository.findByEmail(email)
            .map(this::toResponse);
    }

    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Customer with email " + request.email() + " already exists");
        }
        
        Customer customer = new Customer();
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());
        
        return toResponse(customerRepository.save(customer));
    }

    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        if (!customer.getEmail().equals(request.email()) &&
            customerRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Customer with email " + request.email() + " already exists");
        }

        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());

        return toResponse(customerRepository.save(customer));
    }

    public void delete(UUID id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        customerRepository.delete(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
            customer.getId(),
            customer.getName(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getAddress(),
            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }
}
