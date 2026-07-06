package org.ups.dropshippingservicefinal.adapter.in.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.ups.dropshippingservicefinal.domain.exception.InvalidDispatchDateException;
import org.ups.dropshippingservicefinal.domain.exception.InvalidRejectionReasonException;
import org.ups.dropshippingservicefinal.domain.exception.OrderAlreadyProcessedException;
import org.ups.dropshippingservicefinal.domain.exception.OrderNotFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/providers/42/orders/1");
    }

    @Test
    void given_orderNotFoundException_then_404() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleOrderNotFound(new OrderNotFoundException("No encontrado"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
    }

    @Test
    void given_orderAlreadyProcessedException_then_409() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleOrderAlreadyProcessed(
                        new OrderAlreadyProcessedException("Ya procesada"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("status", 409);
    }

    @Test
    void given_invalidDispatchDateException_then_400() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleInvalidDispatchDate(
                        new InvalidDispatchDateException("Fecha inválida"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
    }

    @Test
    void given_invalidRejectionReasonException_then_400() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleInvalidRejectionReason(
                        new InvalidRejectionReasonException("Motivo requerido"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
    }

    @Test
    void given_genericException_then_500() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneric(new RuntimeException("Error inesperado"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
    }
}
