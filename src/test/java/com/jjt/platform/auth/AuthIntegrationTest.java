package com.jjt.platform.auth;

import com.jjt.platform.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthIntegrationTest {

    // Credentials created by AdminUserInitializer on first context startup
    private static final String ADMIN_EMAIL = "admin@jjt.org";
    private static final String ADMIN_PASSWORD = "Admin@JJT2024!";

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    @Autowired
    TestRestTemplate rest;

    @Test
    void login_validCredentials_returns200WithTokens() {
        ResponseEntity<Map<String, Object>> response = post("/api/auth/login",
                Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKeys("accessToken", "refreshToken", "expiresIn", "tokenType");
        assertThat(response.getBody()).extractingByKey("accessToken").isNotNull();
    }

    @Test
    void login_wrongPassword_returns401() {
        ResponseEntity<Map<String, Object>> response = post("/api/auth/login",
                Map.of("email", ADMIN_EMAIL, "password", "wrong-password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_unknownEmail_returns401() {
        ResponseEntity<Map<String, Object>> response = post("/api/auth/login",
                Map.of("email", "nobody@example.com", "password", "irrelevant"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void me_validToken_returns200WithUserDetails() {
        String token = loginAndGetAccessToken();

        ResponseEntity<Map<String, Object>> response = get("/api/auth/me", token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("email", ADMIN_EMAIL);
        assertThat(response.getBody()).containsEntry("role", "JJT_ADMIN");
    }

    @Test
    void me_noToken_returns401() {
        ResponseEntity<Map<String, Object>> response = get("/api/auth/me", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void me_invalidToken_returns401() {
        ResponseEntity<Map<String, Object>> response = get("/api/auth/me", "not.a.valid.jwt");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refresh_validRefreshToken_returnsNewTokens() {
        Map<String, Object> loginBody = loginAndGetFullResponse();
        String refreshToken = (String) loginBody.get("refreshToken");

        ResponseEntity<Map<String, Object>> response = post("/api/auth/refresh",
                Map.of("refreshToken", refreshToken));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKeys("accessToken", "refreshToken");
    }

    @Test
    void refresh_invalidToken_returns401() {
        ResponseEntity<Map<String, Object>> response = post("/api/auth/refresh",
                Map.of("refreshToken", "00000000-0000-0000-0000-000000000000"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void sponsorEndpoint_asAdmin_returns403() {
        // JJT_ADMIN does not have SPONSOR role — must be rejected
        ResponseEntity<Map<String, Object>> response = get("/api/sponsor/children", loginAndGetAccessToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // -------------------------------------------------------------------------

    private String loginAndGetAccessToken() {
        return (String) loginAndGetFullResponse().get("accessToken");
    }

    private Map<String, Object> loginAndGetFullResponse() {
        ResponseEntity<Map<String, Object>> response = post("/api/auth/login",
                Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private ResponseEntity<Map<String, Object>> post(String url, Map<String, ?> body) {
        return rest.exchange(url, HttpMethod.POST, new HttpEntity<>(body), MAP_TYPE);
    }

    private ResponseEntity<Map<String, Object>> get(String url, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        if (bearerToken != null) {
            headers.setBearerAuth(bearerToken);
        }
        return rest.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), MAP_TYPE);
    }
}
