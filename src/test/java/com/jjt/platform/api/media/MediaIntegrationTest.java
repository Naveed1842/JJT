package com.jjt.platform.api.media;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class MediaIntegrationTest {

    private static final String ADMIN_EMAIL    = "admin@jjt.org";
    private static final String ADMIN_PASSWORD = "Admin@JJT2024!";

    private static final ParameterizedTypeReference<Map<String, Object>>        MAP_TYPE  = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<Map<String, Object>>>  LIST_TYPE = new ParameterizedTypeReference<>() {};

    @Autowired
    TestRestTemplate rest;

    private String adminToken;

    @BeforeEach
    void login() {
        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                "/api/auth/login", HttpMethod.POST,
                new HttpEntity<>(Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD)),
                MAP_TYPE);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        adminToken = (String) resp.getBody().get("accessToken");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private UUID createTestChild() {
        UUID childId  = UUID.randomUUID();
        UUID ledgerId = UUID.randomUUID();
        String roll   = "TEST-" + childId.toString().substring(0, 6).toUpperCase();

        Map<String, Object> body = Map.of(
                "rollNumber", roll,
                "fullName", "Integration Test Child",
                "city", "Karachi",
                "campusName", "Test Campus",
                "schoolName", "Test School",
                "educationAmount", "5000",
                "educationCurrency", "PKR",
                "childId", childId.toString(),
                "ledgerId", ledgerId.toString());

        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                "/api/admin/children", HttpMethod.POST,
                new HttpEntity<>(body, authHeaders()),
                MAP_TYPE);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return childId;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(adminToken);
        return h;
    }

    private HttpHeaders authJsonHeaders() {
        HttpHeaders h = authHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    void uploadIntent_missingMimeType_returns400() {
        UUID childId = createTestChild();
        Map<String, Object> intent = Map.of(
                "ownerType", "CHILD",
                "ownerId", childId.toString(),
                "attachmentRole", "PROFILE_PHOTO",
                "originalName", "photo.jpg",
                "mimeType", "application/x-unknown",
                "sizeBytes", 1024);

        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                "/api/media/upload-intent", HttpMethod.POST,
                new HttpEntity<>(intent, authJsonHeaders()),
                MAP_TYPE);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void fullUploadFlow_createsAttachmentAndDeletesIt() {
        UUID childId = createTestChild();

        // Step 1: Upload intent
        Map<String, Object> intentBody = Map.of(
                "ownerType", "CHILD",
                "ownerId", childId.toString(),
                "attachmentRole", "PROFILE_PHOTO",
                "originalName", "photo.jpg",
                "mimeType", "image/jpeg",
                "sizeBytes", 64,
                "visibility", "PUBLIC",
                "sortOrder", 0);

        ResponseEntity<Map<String, Object>> intentResp = rest.exchange(
                "/api/media/upload-intent", HttpMethod.POST,
                new HttpEntity<>(intentBody, authJsonHeaders()),
                MAP_TYPE);

        assertThat(intentResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(intentResp.getBody()).containsKeys("mediaId", "uploadUrl", "storageRef");

        String mediaId  = (String) intentResp.getBody().get("mediaId");
        String uploadUrl = (String) intentResp.getBody().get("uploadUrl");

        // Step 2: PUT bytes to upload URL (local provider uses /api/media/upload/{mediaId})
        // Strip base URL — TestRestTemplate uses the server's base automatically.
        String uploadPath = uploadUrl.replaceFirst("http://[^/]+", "");
        byte[] fakeJpegBytes = new byte[64];
        fakeJpegBytes[0] = (byte) 0xFF; // JPEG magic bytes
        fakeJpegBytes[1] = (byte) 0xD8;

        HttpHeaders putHeaders = new HttpHeaders();
        putHeaders.setContentType(MediaType.IMAGE_JPEG);
        ResponseEntity<Void> putResp = rest.exchange(
                uploadPath, HttpMethod.PUT,
                new HttpEntity<>(fakeJpegBytes, putHeaders),
                Void.class);

        assertThat(putResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Step 3: Confirm upload
        ResponseEntity<Map<String, Object>> confirmResp = rest.exchange(
                "/api/media/" + mediaId + "/confirm", HttpMethod.POST,
                new HttpEntity<>(Map.of(), authJsonHeaders()),
                MAP_TYPE);

        assertThat(confirmResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirmResp.getBody()).containsKey("id");
        assertThat(confirmResp.getBody().get("id")).isEqualTo(mediaId);

        // Step 4: Verify attachment exists
        ResponseEntity<List<Map<String, Object>>> attResp = rest.exchange(
                "/api/media/attachments?ownerType=CHILD&ownerId=" + childId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                LIST_TYPE);

        assertThat(attResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(attResp.getBody()).isNotEmpty();
        Map<String, Object> att = attResp.getBody().get(0);
        assertThat(att.get("attachmentRole")).isEqualTo("PROFILE_PHOTO");
        assertThat(att.get("mediaId")).isEqualTo(mediaId);

        String attachmentId = (String) att.get("id");

        // Step 5: Delete the attachment
        ResponseEntity<Void> delResp = rest.exchange(
                "/api/media/attachments/" + attachmentId,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()),
                Void.class);

        assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Step 6: Verify attachment is gone
        ResponseEntity<List<Map<String, Object>>> afterDelResp = rest.exchange(
                "/api/media/attachments?ownerType=CHILD&ownerId=" + childId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                LIST_TYPE);

        assertThat(afterDelResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(afterDelResp.getBody()).isEmpty();
    }

    @Test
    void confirmUpload_wrongStatus_returns409() {
        UUID childId = createTestChild();

        // Request intent
        Map<String, Object> intentBody = Map.of(
                "ownerType", "CHILD",
                "ownerId", childId.toString(),
                "attachmentRole", "GALLERY",
                "originalName", "doc.jpg",
                "mimeType", "image/jpeg",
                "sizeBytes", 32,
                "visibility", "PUBLIC",
                "sortOrder", 1);

        ResponseEntity<Map<String, Object>> intentResp = rest.exchange(
                "/api/media/upload-intent", HttpMethod.POST,
                new HttpEntity<>(intentBody, authJsonHeaders()),
                MAP_TYPE);

        assertThat(intentResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String mediaId = (String) intentResp.getBody().get("mediaId");

        // Confirm BEFORE uploading bytes (status still UPLOADING but file not in storage)
        // Then confirm a second time → PROCESSING/READY state → 409
        // First confirm (skip the PUT — confirm on UPLOADING state should succeed or fail gracefully)
        // Actually: confirm twice — second time should 409 since status != UPLOADING
        rest.exchange(
                "/api/media/" + mediaId + "/confirm", HttpMethod.POST,
                new HttpEntity<>(Map.of(), authJsonHeaders()),
                MAP_TYPE);

        // Status code 200 or 500 (storage file missing, handled gracefully by processing service)
        // What matters is the second call returns 409
        ResponseEntity<Map<String, Object>> secondConfirm = rest.exchange(
                "/api/media/" + mediaId + "/confirm", HttpMethod.POST,
                new HttpEntity<>(Map.of(), authJsonHeaders()),
                MAP_TYPE);

        assertThat(secondConfirm.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void listAttachments_unauthenticated_returns401() {
        ResponseEntity<Void> resp = rest.exchange(
                "/api/media/attachments?ownerType=CHILD&ownerId=" + UUID.randomUUID(),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
