package org.ups.dropshippingservicefinal.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.ups.dropshippingservicefinal.application.port.out.LoadOrderPort;
import org.ups.dropshippingservicefinal.application.port.out.SaveOrderPort;
import org.ups.dropshippingservicefinal.application.port.out.SaveOrderStatusEventPort;
import org.ups.dropshippingservicefinal.application.port.out.SendNotificationPort;
import org.ups.dropshippingservicefinal.application.service.AcceptOrderService;
import org.ups.dropshippingservicefinal.application.service.GetOrderDetailService;
import org.ups.dropshippingservicefinal.application.service.GetProviderOrdersService;
import org.ups.dropshippingservicefinal.application.service.RejectOrderService;

@Configuration
@EnableRetry
public class ApplicationConfig {

    @Bean
    public GetProviderOrdersService getProviderOrdersService(LoadOrderPort loadOrderPort) {
        return new GetProviderOrdersService(loadOrderPort);
    }

    @Bean
    public GetOrderDetailService getOrderDetailService(LoadOrderPort loadOrderPort) {
        return new GetOrderDetailService(loadOrderPort);
    }

    @Bean
    public AcceptOrderService acceptOrderService(LoadOrderPort loadOrderPort,
                                                 SaveOrderPort saveOrderPort,
                                                 SaveOrderStatusEventPort saveEventPort,
                                                 SendNotificationPort notificationPort) {
        return new AcceptOrderService(loadOrderPort, saveOrderPort, saveEventPort, notificationPort);
    }

    @Bean
    public RejectOrderService rejectOrderService(LoadOrderPort loadOrderPort,
                                                 SaveOrderPort saveOrderPort,
                                                 SaveOrderStatusEventPort saveEventPort,
                                                 SendNotificationPort notificationPort) {
        return new RejectOrderService(loadOrderPort, saveOrderPort, saveEventPort, notificationPort);
    }
}
