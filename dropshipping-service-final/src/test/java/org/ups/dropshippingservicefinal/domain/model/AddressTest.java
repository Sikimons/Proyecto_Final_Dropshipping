package org.ups.dropshippingservicefinal.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

    @Test
    void given_allFields_when_construct_then_addressCreated() {
        Address address = new Address("Calle 1", "Quito", "Pichincha", "170501", "Ecuador");
        assertThat(address.getStreet()).isEqualTo("Calle 1");
        assertThat(address.getCity()).isEqualTo("Quito");
        assertThat(address.getState()).isEqualTo("Pichincha");
        assertThat(address.getPostalCode()).isEqualTo("170501");
        assertThat(address.getCountry()).isEqualTo("Ecuador");
    }

    @Test
    void given_twoAddressesWithSameFields_when_equals_then_areEqual() {
        Address a1 = new Address("Calle 1", "Quito", "Pichincha", "170501", "Ecuador");
        Address a2 = new Address("Calle 1", "Quito", "Pichincha", "170501", "Ecuador");
        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    void given_nullPostalCode_when_construct_then_addressCreated() {
        Address address = new Address("Calle 2", "Guayaquil", "Guayas", null, "Ecuador");
        assertThat(address.getPostalCode()).isNull();
    }
}
