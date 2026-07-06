package org.ups.dropshippingservicefinal.bdd;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class ScenarioState {

    private ResponseEntity<String> response;
    private Long orderId;
    private Long firstOrderId;
    private String lastReason;

    public ResponseEntity<String> getResponse() {
        return response;
    }

    public void setResponse(ResponseEntity<String> response) {
        this.response = response;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getFirstOrderId() {
        return firstOrderId;
    }

    public void setFirstOrderId(Long firstOrderId) {
        this.firstOrderId = firstOrderId;
    }

    public String getLastReason() {
        return lastReason;
    }

    public void setLastReason(String lastReason) {
        this.lastReason = lastReason;
    }
}
