package com.example.apicustomer.controller;

import com.example.apicustomer.dto.CustomerRequest;
import com.example.apicustomer.dto.CustomerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "Customer API", description = "API para gerenciamento de clientes")
public interface CustomerApi {

    @Operation(summary = "Listar todos os clientes", description = "Retorna a lista de todos os clientes cadastrados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class)))
    })
    ResponseEntity<List<CustomerResponse>> getAllCustomers();

    @Operation(summary = "Buscar cliente por ID", description = "Retorna um cliente específico pelo seu UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    ResponseEntity<CustomerResponse> getCustomerById(
        @Parameter(description = "UUID do cliente", required = true) UUID id);

    @Operation(summary = "Buscar cliente por email", description = "Retorna um cliente específico pelo seu email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    ResponseEntity<CustomerResponse> getCustomerByEmail(
        @Parameter(description = "Email do cliente", required = true) String email);

    @Operation(summary = "Criar novo cliente", description = "Cria um novo cliente no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    ResponseEntity<?> createCustomer(CustomerRequest request);

    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    ResponseEntity<?> updateCustomer(
        @Parameter(description = "UUID do cliente", required = true) UUID id,
        CustomerRequest request);

    @Operation(summary = "Deletar cliente", description = "Remove um cliente do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    ResponseEntity<?> deleteCustomer(
        @Parameter(description = "UUID do cliente", required = true) UUID id);
}
