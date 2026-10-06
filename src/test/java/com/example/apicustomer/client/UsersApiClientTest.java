package com.example.apicustomer.client;

import com.example.apicustomer.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UsersApiClientTest {

    @Test
    void fetchUsersCallsUsersEndpointAndDeserializesResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        UsersApiClient client = new UsersApiClient(
            builder.baseUrl("http://localhost:5000").build()
        );
        server.expect(requestTo("http://localhost:5000/users"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(
                "[{\"id\":\"92d4670e-bc6b-4c18-820e-7e4a1f4c5c7e\",\"name\":\"Paulo Rocha\"},"
                    + "{\"id\":\"753c6648-1a6d-4c26-be57-bc1d454b518f\",\"name\":\"Ash\"}]",
                MediaType.APPLICATION_JSON
            ));

        List<UserResponse> users = client.fetchUsers();

        assertEquals(2, users.size());
        assertEquals(UUID.fromString("92d4670e-bc6b-4c18-820e-7e4a1f4c5c7e"), users.get(0).id());
        assertEquals("Paulo Rocha", users.get(0).name());
        assertEquals(UUID.fromString("753c6648-1a6d-4c26-be57-bc1d454b518f"), users.get(1).id());
        assertEquals("Ash", users.get(1).name());
        server.verify();
    }
}
