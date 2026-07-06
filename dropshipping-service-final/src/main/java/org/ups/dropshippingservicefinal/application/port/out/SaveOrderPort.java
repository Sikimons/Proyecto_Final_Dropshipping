package org.ups.dropshippingservicefinal.application.port.out;

import org.ups.dropshippingservicefinal.domain.model.DropshippingOrder;

public interface SaveOrderPort {

    DropshippingOrder saveOrder(DropshippingOrder order);
}
