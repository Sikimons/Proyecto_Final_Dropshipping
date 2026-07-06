package org.ups.dropshippingservicefinal.adapter.out.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.ups.dropshippingservicefinal.application.port.out.SendNotificationPort;
import org.ups.dropshippingservicefinal.domain.model.OrderStatus;
import org.ups.dropshippingservicefinal.domain.model.OrderStatusEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ExtendWith(OutputCaptureExtension.class)
class InternalNotificationAdapterRetryTest {

    @Autowired
    private SendNotificationPort notificationPort;

    @Autowired
    private FailingAdapter failingAdapter;

    @BeforeEach
    void setUp() {
        failingAdapter.resetCounts();
    }

    @Test
    void given_sendFails_when_notifyOrderAccepted_then_retriedAndFailureEventLogged(CapturedOutput output) {
        OrderStatusEvent event = new OrderStatusEvent(
                1L, 1L, OrderStatus.PENDING, OrderStatus.ACCEPTED,
                "42", LocalDateTime.now(), LocalDate.now().plusDays(1), null);

        assertDoesNotThrow(() -> notificationPort.notifyOrderAccepted(event));

        assertThat(failingAdapter.getAcceptedCallCount()).isEqualTo(3);
        assertThat(output.getAll()).contains("NOTIFICATION_FAILURE_EVENT");
    }

    @Test
    void given_sendFails_when_notifyOrderRejected_then_retriedAndFailureEventLogged(CapturedOutput output) {
        OrderStatusEvent event = new OrderStatusEvent(
                2L, 2L, OrderStatus.PENDING, OrderStatus.REJECTED,
                "42", LocalDateTime.now(), null, "Sin stock disponible");

        assertDoesNotThrow(() -> notificationPort.notifyOrderRejected(event));

        assertThat(failingAdapter.getRejectedCallCount()).isEqualTo(3);
        assertThat(output.getAll()).contains("NOTIFICATION_FAILURE_EVENT");
    }

    @TestConfiguration
    static class FailingAdapterConfig {

        @Bean
        @Primary
        public FailingAdapter failingAdapter() {
            return new FailingAdapter();
        }
    }

    static class FailingAdapter extends InternalNotificationAdapter {

        private final AtomicInteger acceptedCallCount = new AtomicInteger(0);
        private final AtomicInteger rejectedCallCount = new AtomicInteger(0);

        @Override
        void doSend(String channel, String payload) {
            if ("ANALYST".equals(channel)) {
                acceptedCallCount.incrementAndGet();
            } else {
                rejectedCallCount.incrementAndGet();
            }
            throw new RuntimeException("Simulated failure channel=" + channel);
        }

        int getAcceptedCallCount() {
            return acceptedCallCount.get();
        }

        int getRejectedCallCount() {
            return rejectedCallCount.get();
        }

        void resetCounts() {
            acceptedCallCount.set(0);
            rejectedCallCount.set(0);
        }
    }
}
