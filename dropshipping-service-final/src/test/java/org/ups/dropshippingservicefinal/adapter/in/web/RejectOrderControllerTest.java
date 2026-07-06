package org.ups.dropshippingservicefinal.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@Sql(scripts = {"classpath:db/cleanup.sql", "classpath:db/schema.sql", "classpath:db/data.sql"})
class RejectOrderControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void given_ordenPendiente_when_postReject_then_http200() {
        Long orderId = getFirstPendingOrderId();
        ResponseEntity<String> response =
                postReject(orderId, "{\"reason\": \"Sin stock del producto hasta agosto de 2026.\"}");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(JsonPath.read(response.getBody(), "$.newStatus").toString()).isEqualTo("REJECTED");
    }

    @Test
    void given_motivoVacio_when_postReject_then_http400() {
        Long orderId = getFirstPendingOrderId();
        ResponseEntity<String> response = postReject(orderId, "{\"reason\": \"\"}");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void given_ordenYaProcesada_when_postReject_then_http409() {
        Long acceptedId = getAcceptedOrderId();
        ResponseEntity<String> response = postReject(acceptedId, "{\"reason\": \"Sin stock del producto\"}");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    private Long getFirstPendingOrderId() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/v1/providers/42/orders?status=PENDING", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Number orderIdValue = JsonPath.read(response.getBody(), "$[0].orderId");
        Long orderId = orderIdValue.longValue();
        assertThat(orderId).isNotNull();
        return orderId;
    }

    private Long getAcceptedOrderId() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/v1/providers/42/orders?status=ACCEPTED", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Number orderIdValue = JsonPath.read(response.getBody(), "$[0].orderId");
        Long orderId = orderIdValue.longValue();
        assertThat(orderId).isNotNull();
        return orderId;
    }

    private ResponseEntity<String> postReject(Long orderId, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(
                "/api/v1/providers/42/orders/{orderId}/reject",
                new HttpEntity<>(body, headers),
                String.class,
                orderId
        );
    }
}
