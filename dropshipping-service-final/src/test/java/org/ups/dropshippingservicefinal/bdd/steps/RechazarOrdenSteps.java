package org.ups.dropshippingservicefinal.bdd.steps;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.ups.dropshippingservicefinal.bdd.ScenarioState;

import static org.assertj.core.api.Assertions.assertThat;

public class RechazarOrdenSteps {

    private static final Long PROVIDER_ID = 42L;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioState state;

    @Dado("la orden pendiente para rechazo está disponible")
    public void given_ordenPendienteParaRechazo() {
        ResponseEntity<String> list =
                restTemplate.getForEntity("/api/v1/providers/{providerId}/orders?status=PENDING",
                        String.class, PROVIDER_ID);
        Number orderIdValue = JsonPath.read(list.getBody(), "$[0].orderId");
        state.setOrderId(orderIdValue.longValue());
    }

    @Cuando("el proveedor rechaza la orden con motivo {string}")
    public void when_rechazaOrden(String reason) {
        state.setLastReason(reason);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"reason\": \"" + reason.replace("\"", "\\\"") + "\"}";
        state.setResponse(restTemplate.postForEntity(
                "/api/v1/providers/{providerId}/orders/{orderId}/reject",
                new HttpEntity<>(body, headers),
                String.class,
                PROVIDER_ID,
                state.getOrderId()
        ));
    }

    @Entonces("el campo newStatus de la respuesta de rechazo es {string}")
    public void then_newStatus(String expectedStatus) {
        assertThat(JsonPath.read(state.getResponse().getBody(), "$.newStatus").toString()).isEqualTo(expectedStatus);
    }

    @Entonces("el campo rejectionReason contiene el motivo enviado")
    public void then_rejectionReason() {
        assertThat(JsonPath.read(state.getResponse().getBody(), "$.rejectionReason").toString()).isEqualTo(state.getLastReason());
    }
}
