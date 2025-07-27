package com.algaworks.algadelivery.delivery.tracking.domain.repository;

import com.algaworks.algadelivery.delivery.tracking.domain.model.ContactPoint;
import com.algaworks.algadelivery.delivery.tracking.domain.model.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeliveryRepositoryTest {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @org.junit.jupiter.api.Test
    void shouldSaveDeliveryWithMultipleItems() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createValidationDetails());
        delivery.addItem("Livro", 2);
        delivery.addItem("Notebook", 1);
        Delivery saved = deliveryRepository.save(delivery);

        Delivery found = deliveryRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(2, found.getItems().size());
        assertTrue(found.getItems().stream().anyMatch(item -> item.getName().equals("Livro") && item.getQuantity() == 2));
        assertTrue(found.getItems().stream().anyMatch(item -> item.getName().equals("Notebook") && item.getQuantity() == 1));
    }

    @org.junit.jupiter.api.Test
    void shouldAllowAddingItemsWithSameNameButDifferentIds() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createValidationDetails());
        delivery.addItem("Camiseta", 1); // Supondo que cada addItem gera um id diferente
        delivery.addItem("Camiseta", 1);
        Delivery saved = deliveryRepository.save(delivery);

        Delivery found = deliveryRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(2, found.getItems().size());
        assertTrue(found.getItems().stream().allMatch(item -> item.getName().equals("Camiseta")));
        assertNotEquals(found.getItems().get(0).getId(), found.getItems().get(1).getId());
    }

    @org.junit.jupiter.api.Test
    void shouldAllowAddingNoItemsToDelivery() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createValidationDetails());
        Delivery saved = deliveryRepository.save(delivery);

        Delivery found = deliveryRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertTrue(found.getItems().isEmpty());
    }

    private Delivery.PreparationDetails createValidationDetails() {
        return Delivery.PreparationDetails.builder()
                .sender(new ContactPoint("12345-678", "Street A", "100", "Apt 1", "Sender Name", "123456789"))
                .recipient(new ContactPoint("87654-321", "Street B", "200", "Apt 2", "Recipient Name", "987654321"))
                .distanceFee(BigDecimal.valueOf(10))
                .courierPayout(BigDecimal.valueOf(5))
                .expectedDeliveryTime(Duration.ofHours(5))
                .build();
    }
}