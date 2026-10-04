package com.example.apicustomer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
    UUID id,
    String name,
    String email,
    String phone,
    String address,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
