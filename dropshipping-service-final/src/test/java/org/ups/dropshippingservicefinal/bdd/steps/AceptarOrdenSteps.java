package org.ups.dropshippingservicefinal.bdd.steps;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.ups.dropshippingservicefinal.bdd.ScenarioState;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AceptarOrdenSteps {

    private static final Long PROVIDER_ID = 42L;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioState state;

    @Dado("la orden con código {string} está en estado PENDING")
    public void given_ordenEnPending(String orderCode) {
        state.setOrderId(findOrderIdByCode(orderCode));
    }

    @Dado("la orden con código {string} está en estado ACCEPTED")
    public void given_ordenEnAccepted(String orderCode) {
        state.setOrderId(findOrderIdByCode(orderCode));
    }

    @Cuando("el proveedor acepta la orden con fecha estimada {string}")
    public void when_aceptaOrden(String date) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        state.setResponse(restTemplate.postForEntity(
                "/api/v1/providers/{providerId}/orders/{orderId}/accept",
                new HttpEntity<>("{\"estimatedDispatchDate\": \"" + date + "\"}", headers),
                String.class,
                PROVIDER_ID,
                state.getOrderId()
        ));
    }

    @Entonces("el campo newStatus de la respuesta es {string}")
    public void then_newStatus(String expectedStatus) {
        assertThat(state.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(JsonPath.read(state.getResponse().getBody(), "$.newStatus").toString()).isEqualTo(expectedStatus);
    }

    @Entonces("el campo estimatedDispatchDate de la respuesta es {string}")
    public void then_dispatchDate(String expectedDate) {
        assertThat(JsonPath.read(state.getResponse().getBody(), "$.estimatedDispatchDate").toString()).isEqualTo(expectedDate);
    }

    private Long findOrderIdByCode(String orderCode) {
        ResponseEntity<String> list =
                restTemplate.getForEntity("/api/v1/providers/{providerId}/orders", String.class, PROVIDER_ID);
        List<Map<String, Object>> orders = JsonPath.parse(list.getBody()).read("$");
        return orders.stream()
                .filter(order -> orderCode.equals(order.get("orderCode")))
                .findFirst()
                .map(order -> ((Number) order.get("orderId")).longValue())
                .orElseThrow();
    }
}
