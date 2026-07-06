package org.ups.dropshippingservicefinal.bdd.steps;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.ups.dropshippingservicefinal.bdd.ScenarioState;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VerOrdenesSteps {

    private static final Long PROVIDER_ID = 42L;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioState state;

    @Dado("el proveedor {long} tiene órdenes asignadas en el sistema")
    public void given_proveedorConOrdenes(Long providerId) {
        assertThat(providerId).isEqualTo(PROVIDER_ID);
    }

    @Cuando("el proveedor consulta sus órdenes")
    public void when_consultaOrdenes() {
        state.setResponse(restTemplate.getForEntity("/api/v1/providers/{providerId}/orders", String.class, PROVIDER_ID));
        List<?> orders = JsonPath.read(state.getResponse().getBody(), "$");
        if (!orders.isEmpty()) {
            Number orderIdValue = JsonPath.read(state.getResponse().getBody(), "$[0].orderId");
            state.setFirstOrderId(orderIdValue.longValue());
        }
    }

    @Cuando("el proveedor filtra sus órdenes por estado {string}")
    public void when_filtraOrdenes(String status) {
        state.setResponse(restTemplate.getForEntity("/api/v1/providers/{providerId}/orders?status={status}",
                String.class, PROVIDER_ID, status));
    }

    @Cuando("el proveedor solicita el detalle de la primera orden")
    public void when_solicitaDetalle() {
        ResponseEntity<String> listResponse =
                restTemplate.getForEntity("/api/v1/providers/{providerId}/orders", String.class, PROVIDER_ID);
        Number orderIdValue = JsonPath.read(listResponse.getBody(), "$[0].orderId");
        state.setFirstOrderId(orderIdValue.longValue());
        state.setResponse(restTemplate.getForEntity("/api/v1/providers/{providerId}/orders/{orderId}",
                String.class, PROVIDER_ID, state.getFirstOrderId()));
    }

    @Entonces("la respuesta tiene código HTTP {int}")
    public void then_httpStatus(int expectedStatus) {
        assertThat(state.getResponse().getStatusCode()).isEqualTo(HttpStatus.valueOf(expectedStatus));
    }

    @Entonces("la lista contiene al menos una orden con productCode y productDescription")
    public void then_listaContieneOrdenes() {
        List<String> productCodes = JsonPath.read(state.getResponse().getBody(), "$[*].productCode");
        List<String> productDescriptions = JsonPath.read(state.getResponse().getBody(), "$[*].productDescription");
        assertThat(productCodes).isNotEmpty();
        assertThat(productDescriptions).isNotEmpty();
    }

    @Entonces("todas las órdenes en la respuesta tienen estado {string}")
    public void then_todasTienenEstado(String status) {
        List<String> statuses = JsonPath.read(state.getResponse().getBody(), "$[*].status");
        assertThat(statuses).allMatch(status::equals);
    }

    @Entonces("el detalle incluye deliveryAddress con street, city, state y country")
    public void then_detalleContieneAddress() {
        assertThat(JsonPath.read(state.getResponse().getBody(), "$.deliveryAddress.street").toString()).isNotBlank();
        assertThat(JsonPath.read(state.getResponse().getBody(), "$.deliveryAddress.city").toString()).isNotBlank();
        assertThat(JsonPath.read(state.getResponse().getBody(), "$.deliveryAddress.state").toString()).isNotBlank();
        assertThat(JsonPath.read(state.getResponse().getBody(), "$.deliveryAddress.country").toString()).isNotBlank();
    }

    @Entonces("el detalle incluye customerName y customerContact")
    public void then_detalleContieneContacto() {
        assertThat(JsonPath.read(state.getResponse().getBody(), "$.customerName").toString()).isNotBlank();
        assertThat(JsonPath.read(state.getResponse().getBody(), "$.customerContact").toString()).isNotBlank();
    }

    @Entonces("el detalle incluye expectedDeliveryDate")
    public void then_detalleContieneFecha() {
        assertThat(JsonPath.read(state.getResponse().getBody(), "$.expectedDeliveryDate").toString()).isNotBlank();
    }
}
