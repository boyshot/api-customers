package com.example.apicustomer.service;

import com.example.apicustomer.dto.CustomerRequest;
import com.example.apicustomer.dto.CustomerResponse;
import com.example.apicustomer.entity.Customer;
import com.example.apicustomer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService - Testes Unitários")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerRequest request;
    private CustomerResponse response;

    @BeforeEach
    void setUp() {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        LocalDateTime now = LocalDateTime.now();

        customer = new Customer();
        customer.setId(id);
        customer.setName("João Silva");
        customer.setEmail("joao@email.com");
        customer.setPhone("11999999999");
        customer.setAddress("Rua A, 123");
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        request = new CustomerRequest("João Silva", "joao@email.com", "11999999999", "Rua A, 123");

        response = new CustomerResponse(
            id,
            "João Silva",
            "joao@email.com",
            "11999999999",
            "Rua A, 123",
            now,
            now
        );
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void testFindAll() {
        // Arrange
        List<Customer> customers = List.of(customer);
        when(customerRepository.findAll()).thenReturn(customers);

        // Act
        List<CustomerResponse> result = customerService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("João Silva", result.get(0).name());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve encontrar cliente por ID")
    void testFindById() {
        // Arrange
        UUID id = customer.getId();
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        // Act
        Optional<CustomerResponse> result = customerService.findById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("João Silva", result.get().name());
        verify(customerRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar cliente inexistente por ID")
    void testFindByIdNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<CustomerResponse> result = customerService.findById(id);

        // Assert
        assertFalse(result.isPresent());
        verify(customerRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve encontrar cliente por email")
    void testFindByEmail() {
        // Arrange
        String email = "joao@email.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));

        // Act
        Optional<CustomerResponse> result = customerService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("joao@email.com", result.get().email());
        verify(customerRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve criar novo cliente")
    void testCreateCustomer() {
        // Arrange
        when(customerRepository.existsByEmail(request.email())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        CustomerResponse result = customerService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals("João Silva", result.name());
        assertEquals("joao@email.com", result.email());
        verify(customerRepository, times(1)).existsByEmail(request.email());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com email duplicado")
    void testCreateCustomerWithDuplicateEmail() {
        // Arrange
        when(customerRepository.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.create(request);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(customerRepository, times(1)).existsByEmail(request.email());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Deve atualizar cliente")
    void testUpdateCustomer() {
        // Arrange
        UUID id = customer.getId();
        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(id);
        updatedCustomer.setName("João Silva Atualizado");
        updatedCustomer.setEmail("joao@email.com");
        updatedCustomer.setPhone("11888888888");
        updatedCustomer.setAddress("Rua B, 456");
        updatedCustomer.setCreatedAt(customer.getCreatedAt());
        updatedCustomer.setUpdatedAt(LocalDateTime.now());

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        // Act
        CustomerResponse result = customerService.update(id, request);

        // Assert
        assertNotNull(result);
        assertEquals("João Silva Atualizado", result.name());
        assertEquals("11888888888", result.phone());
        verify(customerRepository, times(1)).findById(id);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar cliente inexistente")
    void testUpdateCustomerNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.update(id, request);
        });

        assertTrue(exception.getMessage().contains("not found"));
        verify(customerRepository, times(1)).findById(id);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Deve deletar cliente")
    void testDeleteCustomer() {
        // Arrange
        UUID id = customer.getId();
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        // Act
        customerService.delete(id);

        // Assert
        verify(customerRepository, times(1)).findById(id);
        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar cliente inexistente")
    void testDeleteCustomerNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.delete(id);
        });

        assertTrue(exception.getMessage().contains("not found"));
        verify(customerRepository, times(1)).findById(id);
        verify(customerRepository, never()).delete(any(Customer.class));
    }
}
