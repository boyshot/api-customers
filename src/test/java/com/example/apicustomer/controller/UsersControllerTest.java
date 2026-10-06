package com.example.apicustomer.controller;

import com.example.apicustomer.dto.UserResponse;
import com.example.apicustomer.service.UsersService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersControllerTest {

    private final UsersService usersService = mock(UsersService.class);
    private final UsersController usersController = new UsersController(usersService);

    @Test
    void getUsersReturnsUsersFromService() {
        UserResponse user = new UserResponse(
            UUID.fromString("92d4670e-bc6b-4c18-820e-7e4a1f4c5c7e"),
            "Paulo Rocha"
        );
        List<UserResponse> users = List.of(user);
        when(usersService.findAll()).thenReturn(users);

        ResponseEntity<List<UserResponse>> response = usersController.getUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        verify(usersService).findAll();
    }
}
