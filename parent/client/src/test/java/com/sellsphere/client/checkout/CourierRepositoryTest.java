package com.sellsphere.client.checkout;

import com.sellsphere.common.entity.Courier;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CourierRepositoryTest {

    @Autowired
    private CourierRepository courierRepository;

    @Test
    void givenCourierData_whenSave_thenShouldPersistAndAssignId() {

        Courier courier = Courier.builder()
                .name("DHL")
                .logoUrl("https://example.com/dhl-logo.png")
                .minDeliveryTime(1)
                .maxDeliveryTime(5)
                .build();

        Courier savedCourier = courierRepository.save(courier);

        assertThat(savedCourier).isNotNull();
        assertThat(savedCourier.getId()).isNotNull();
        assertThat(savedCourier.getName()).isEqualTo("DHL");
        assertThat(savedCourier.getLogoUrl()).isEqualTo("https://example.com/dhl-logo.png");
        assertThat(savedCourier.getMinDeliveryTime()).isEqualTo(1);
        assertThat(savedCourier.getMaxDeliveryTime()).isEqualTo(5);
    }

    @Test
    void givenCourier_whenFindByName_thenCorrectCourierIsReturned() {

        String courierName = "FedEx";
        Courier courier = Courier.builder()
                .name(courierName)
                .logoUrl("https://example.com/fedex-logo.png")
                .minDeliveryTime(2)
                .maxDeliveryTime(6)
                .build();
        courierRepository.save(courier);

        Optional<Courier> foundCourier = courierRepository.findByName(courierName);

        assertThat(foundCourier).isPresent();
        assertThat(foundCourier.get().getName()).isEqualTo(courierName);
        assertThat(foundCourier.get().getLogoUrl()).isEqualTo("https://example.com/fedex-logo.png");
        assertThat(foundCourier.get().getMinDeliveryTime()).isEqualTo(2);
        assertThat(foundCourier.get().getMaxDeliveryTime()).isEqualTo(6);
    }

}