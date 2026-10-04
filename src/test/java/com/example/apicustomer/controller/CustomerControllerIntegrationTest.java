package com.example.apicustomer.controller;

import com.example.apicustomer.dto.CustomerRequest;
import com.example.apicustomer.dto.CustomerResponse;
import com.example.apicustomer.entity.Customer;
import com.example.apicustomer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("CustomerController - Testes de Integração")
class CustomerControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer1;
    private Customer customer2;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/customers";
        restClient = RestClient.create();
        customerRepository.deleteAll();

        customer1 = new Customer();
        customer1.setName("João Silva");
        customer1.setEmail("joao@email.com");
        customer1.setPhone("11999999999");
        customer1.setAddress("Rua A, 123");
        customer1 = customerRepository.save(customer1);

        customer2 = new Customer();
        customer2.setName("Maria Santos");
        customer2.setEmail("maria@email.com");
        customer2.setPhone("11888888888");
        customer2.setAddress("Rua B, 456");
        customer2 = customerRepository.save(customer2);
    }

    @Test
    @DisplayName("GET /api/customers - Deve retornar todos os clientes")
    void testGetAllCustomers() {
        ResponseEntity<List> response = restClient.get()
            .uri(baseUrl)
            .retrieve()
            .toEntity(List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("GET /api/customers - Deve retornar lista vazia quando não há clientes")
    void testGetAllCustomersEmpty() {
        customerRepository.deleteAll();

        ResponseEntity<List> response = restClient.get()
            .uri(baseUrl)
            .retrieve()
            .toEntity(List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    @DisplayName("GET /api/customers/{id} - Deve retornar cliente por ID")
    void testGetCustomerById() {
        UUID id = customer1.getId();
        String url = baseUrl + "/" + id;

        ResponseEntity<CustomerResponse> response = restClient.get()
            .uri(url)
            .retrieve()
            .toEntity(CustomerResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
        assertEquals("João Silva", response.getBody().name());
        assertEquals("joao@email.com", response.getBody().email());
        assertEquals("11999999999", response.getBody().phone());
        assertEquals("Rua A, 123", response.getBody().address());
        assertNotNull(response.getBody().createdAt());
        assertNotNull(response.getBody().updatedAt());
    }

    @Test
    @DisplayName("GET /api/customers/{id} - Deve retornar 404 quando cliente não existe")
    void testGetCustomerByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        String url = baseUrl + "/" + nonExistentId;

        ResponseEntity<Void> response = restClient.get()
            .uri(url)
            .retrieve()
            .onStatus(status -> status.value() == 404, (request, resp) -> {})
            .toBodilessEntity();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /api/customers/email/{email} - Deve retornar cliente por email")
    void testGetCustomerByEmail() {
        String url = baseUrl + "/email/joao@email.com";

        ResponseEntity<CustomerResponse> response = restClient.get()
            .uri(url)
            .retrieve()
            .toEntity(CustomerResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("João Silva", response.getBody().name());
        assertEquals("joao@email.com", response.getBody().email());
        assertEquals("11999999999", response.getBody().phone());
    }

    @Test
    @DisplayName("GET /api/customers/email/{email} - Deve retornar 404 quando email não existe")
    void testGetCustomerByEmailNotFound() {
        String url = baseUrl + "/email/inexistente@email.com";

        ResponseEntity<Void> response = restClient.get()
            .uri(url)
            .retrieve()
            .onStatus(status -> status.value() == 404, (request, resp) -> {})
            .toBodilessEntity();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/customers - Deve criar novo cliente com sucesso")
    void testCreateCustomer() {
        CustomerRequest request = new CustomerRequest(
            "Carlos Oliveira",
            "carlos@email.com",
            "11777777777",
            "Rua C, 789"
        );

        ResponseEntity<CustomerResponse> response = restClient.post()
            .uri(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toEntity(CustomerResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().id());
        assertEquals("Carlos Oliveira", response.getBody().name());
        assertEquals("carlos@email.com", response.getBody().email());
        assertEquals("11777777777", response.getBody().phone());
        assertEquals("Rua C, 789", response.getBody().address());
        assertNotNull(response.getBody().createdAt());
        assertNotNull(response.getBody().updatedAt());
    }

    @Test
    @DisplayName("POST /api/customers - Deve retornar 409 ao criar cliente com email duplicado")
    void testCreateCustomerDuplicateEmail() {
        CustomerRequest request = new CustomerRequest(
            "João Outro",
            "joao@email.com", // Email já existe
            "11666666666",
            "Rua D, 101"
        );

        ResponseEntity<Void> response = restClient.post()
            .uri(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .onStatus(status -> status.value() == 409, (req, resp) -> {})
            .toBodilessEntity();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/customers - Deve retornar 400 quando dados inválidos")
    void testCreateCustomerInvalidData() {
        CustomerRequest request = new CustomerRequest(
            "", // Nome vazio
            "email-invalido", // Email inválido
            "",
            ""
        );

        ResponseEntity<Void> response = restClient.post()
            .uri(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .onStatus(status -> status.value() == 400, (req, resp) -> {})
            .toBodilessEntity();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("PUT /api/customers/{id} - Deve atualizar cliente com sucesso")
    void testUpdateCustomer() {
        UUID id = customer1.getId();
        String url = baseUrl + "/" + id;
        
        CustomerRequest request = new CustomerRequest(
            "João Silva Atualizado",
            "joao.novo@email.com",
            "11555555555",
            "Rua A, 999"
        );

        ResponseEntity<CustomerResponse> response = restClient.put()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toEntity(CustomerResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
        assertEquals("João Silva Atualizado", response.getBody().name());
        assertEquals("joao.novo@email.com", response.getBody().email());
        assertEquals("11555555555", response.getBody().phone());
        assertEquals("Rua A, 999", response.getBody().address());
    }

    @Test
    @DisplayName("PUT /api/customers/{id} - Deve retornar 404 quando cliente não existe")
    void testUpdateCustomerNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        String url = baseUrl + "/" + nonExistentId;
        
        CustomerRequest request = new CustomerRequest(
            "Nome Qualquer",
            "email@email.com",
            "11444444444",
            "Rua Qualquer"
        );

        ResponseEntity<Void> response = restClient.put()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .onStatus(status -> status.value() == 404, (req, resp) -> {})
            .toBodilessEntity();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("PUT /api/customers/{id} - Deve retornar 409 ao atualizar com email duplicado")
    void testUpdateCustomerDuplicateEmail() {
        UUID id = customer1.getId();
        String url = baseUrl + "/" + id;
        
        CustomerRequest request = new CustomerRequest(
            "João Silva",
            "maria@email.com", // Email já usado por customer2
            "11999999999",
            "Rua A, 123"
        );

        ResponseEntity<Void> response = restClient.put()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .onStatus(status -> status.value() == 409, (req, resp) -> {})
            .toBodilessEntity();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /api/customers/{id} - Deve deletar cliente com sucesso")
    void testDeleteCustomer() {
        UUID id = customer1.getId();
        String url = baseUrl + "/" + id;

        ResponseEntity<Void> deleteResponse = restClient.delete()
            .uri(url)
            .retrieve()
            .toBodilessEntity();

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // Verifica que o cliente foi realmente deletado
        ResponseEntity<Void> getResponse = restClient.get()
            .uri(url)
            .retrieve()
            .onStatus(status -> status.value() == 404, (req, resp) -> {})
            .toBodilessEntity();

        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /api/customers/{id} - Deve retornar 404 quando cliente não existe")
    void testDeleteCustomerNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        String url = baseUrl + "/" + nonExistentId;

        ResponseEntity<Void> response = restClient.delete()
            .uri(url)
            .retrieve()
            .onStatus(status -> status.value() == 404, (req, resp) -> {})
            .toBodilessEntity();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Cenário completo: criar, buscar, atualizar e deletar cliente")
    void testCompleteCustomerLifecycle() {
        // 1. Criar novo cliente
        CustomerRequest createRequest = new CustomerRequest(
            "Pedro Alves",
            "pedro@email.com",
            "11333333333",
            "Rua E, 202"
        );

        ResponseEntity<CustomerResponse> createResponse = restClient.post()
            .uri(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(createRequest)
            .retrieve()
            .toEntity(CustomerResponse.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        
        UUID id = createResponse.getBody().id();
        String customerUrl = baseUrl + "/" + id;

        // 2. Buscar cliente criado
        ResponseEntity<CustomerResponse> getResponse = restClient.get()
            .uri(customerUrl)
            .retrieve()
            .toEntity(CustomerResponse.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("Pedro Alves", getResponse.getBody().name());
        assertEquals("pedro@email.com", getResponse.getBody().email());

        // 3. Atualizar cliente
        CustomerRequest updateRequest = new CustomerRequest(
            "Pedro Alves Junior",
            "pedro.jr@email.com",
            "11222222222",
            "Rua F, 303"
        );

        ResponseEntity<CustomerResponse> updateResponse = restClient.put()
            .uri(customerUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(updateRequest)
            .retrieve()
            .toEntity(CustomerResponse.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("Pedro Alves Junior", updateResponse.getBody().name());
        assertEquals("pedro.jr@email.com", updateResponse.getBody().email());

        // 4. Deletar cliente
        ResponseEntity<Void> deleteResponse = restClient.delete()
            .uri(customerUrl)
            .retrieve()
            .toBodilessEntity();

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // 5. Verificar que foi deletado
        ResponseEntity<Void> verifyResponse = restClient.get()
            .uri(customerUrl)
            .retrieve()
            .onStatus(status -> status.value() == 404, (req, resp) -> {})
            .toBodilessEntity();

        assertEquals(HttpStatus.NOT_FOUND, verifyResponse.getStatusCode());
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios na criação")
    void testCreateCustomerValidation() {
        // Nome vazio
        CustomerRequest requestEmptyName = new CustomerRequest(
            "",
            "valido@email.com",
            "11999999999",
            "Rua A, 123"
        );

        ResponseEntity<Void> response1 = restClient.post()
            .uri(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestEmptyName)
            .retrieve()
            .onStatus(status -> status.value() == 400, (req, resp) -> {})
            .toBodilessEntity();

        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());

        // Email inválido
        CustomerRequest requestInvalidEmail = new CustomerRequest(
            "Nome Válido",
            "email-sem-arroba",
            "11999999999",
            "Rua A, 123"
        );

        ResponseEntity<Void> response2 = restClient.post()
            .uri(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestInvalidEmail)
            .retrieve()
            .onStatus(status -> status.value() == 400, (req, resp) -> {})
            .toBodilessEntity();

        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }
}
