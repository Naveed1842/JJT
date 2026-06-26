package com.jjt.platform.api;

import com.jjt.platform.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class PublicSponsorshipIntegrationTest {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final String ADMIN_EMAIL = "admin@jjt.org";
    private static final String ADMIN_PASSWORD = "Admin@JJT2024!";

    @Autowired
    TestRestTemplate rest;

    private UUID childId;

    @BeforeEach
    void createChild() {
        // Each test gets a fresh child so sponsorship state does not bleed between tests
        String token = loginAndGetAccessToken();
        Map<String, String> childRequest = Map.of(
                "rollNumber", "TC-" + UUID.randomUUID().toString().substring(0, 8),
                "fullName", "Test Child",
                "city", "Lahore",
                "campusName", "Test Campus",
                "schoolName", "Test School",
                "educationAmount", "5000",
                "educationCurrency", "PKR"
        );
        ResponseEntity<Map<String, Object>> response = adminPost("/api/admin/children", childRequest, token);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        childId = UUID.fromString((String) body.get("childId"));
    }

    @Test
    void commitPublicSponsorship_validRequest_returns201() {
        ResponseEntity<Map<String, Object>> response = post("/api/public/sponsorships", buildSponsorshipRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("childId");
        assertThat(response.getBody()).containsKey("startMonth");
    }

    @Test
    void commitPublicSponsorship_duplicatePending_returns409() {
        // First request succeeds
        assertThat(post("/api/public/sponsorships", buildSponsorshipRequest()).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        // Second request for the same child must be rejected
        ResponseEntity<Map<String, Object>> duplicate = post("/api/public/sponsorships", buildSponsorshipRequest());
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void commitPublicSponsorship_missingEmail_returns400() {
        Map<String, Object> body = Map.of(
                "childId", childId.toString(),
                "commitmentType", "FULL",
                "sponsor", Map.of("name", "A Sponsor", "phone", "01234567890")
                // email deliberately absent
        );
        assertThat(post("/api/public/sponsorships", body).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void commitPublicSponsorship_malformedEmail_returns400() {
        Map<String, Object> body = Map.of(
                "childId", childId.toString(),
                "commitmentType", "FULL",
                "sponsor", Map.of("name", "A Sponsor", "email", "not-an-email", "phone", "01234567890")
        );
        assertThat(post("/api/public/sponsorships", body).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void commitPublicSponsorship_missingName_returns400() {
        Map<String, Object> body = Map.of(
                "childId", childId.toString(),
                "commitmentType", "FULL",
                "sponsor", Map.of("email", "sponsor@example.com")
                // name deliberately absent
        );
        assertThat(post("/api/public/sponsorships", body).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void commitPublicSponsorship_nonExistentChild_returns400() {
        Map<String, Object> body = Map.of(
                "childId", UUID.randomUUID().toString(),
                "commitmentType", "FULL",
                "sponsor", Map.of("name", "A Sponsor", "email", "sponsor@example.com")
        );
        assertThat(post("/api/public/sponsorships", body).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // -------------------------------------------------------------------------

    private Map<String, Object> buildSponsorshipRequest() {
        return Map.of(
                "childId", childId.toString(),
                "commitmentType", "FULL",
                "sponsor", Map.of("name", "A Sponsor", "email", "sponsor@example.com", "phone", "01234567890")
        );
    }

    private ResponseEntity<Map<String, Object>> post(String url, Map<String, ?> body) {
        HttpEntity<Map<String, ?>> entity = new HttpEntity<>(body);
        return rest.exchange(url, HttpMethod.POST, entity, MAP_TYPE);
    }

    private ResponseEntity<Map<String, Object>> adminPost(String url, Map<String, ?> body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Map<String, ?>> entity = new HttpEntity<>(body, headers);
        return rest.exchange(url, HttpMethod.POST, entity, MAP_TYPE);
    }

    private String loginAndGetAccessToken() {
        Map<String, String> credentials = Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(credentials);
        ResponseEntity<Map<String, Object>> response = rest.exchange(
                "/api/auth/login", HttpMethod.POST, entity, MAP_TYPE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        return (String) body.get("accessToken");
    }
}
