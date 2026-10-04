package com.example.apicustomer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @Size(max = 20, message = "Phone must be less than 20 characters")
    String phone,

    @Size(max = 500, message = "Address must be less than 500 characters")
    String address
) {}
