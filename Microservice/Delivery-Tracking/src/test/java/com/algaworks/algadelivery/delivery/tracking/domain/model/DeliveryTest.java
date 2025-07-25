package com.algaworks.algadelivery.delivery.tracking.domain.model;

import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryTest {

    @Test
    void shouldCreateDraftDeliveryWithDefaultValues() {
        Delivery delivery = Delivery.draft();

        assertNotNull(delivery.getId());
        assertEquals(DeliveryStatus.DRAFT, delivery.getStatus());
        assertEquals(0, delivery.getTotalItems());
        assertEquals(BigDecimal.ZERO, delivery.getTotalCost());
        assertEquals(BigDecimal.ZERO, delivery.getCourierPayout());
        assertEquals(BigDecimal.ZERO, delivery.getDistanceFee());
        assertTrue(delivery.getItems().isEmpty());
    }

    @Test
    void shouldAddItemAndUpdateTotalItems() {
        Delivery delivery = Delivery.draft();
        UUID itemId = delivery.addItem("Pizza", 2);

        assertEquals(2, delivery.getTotalItems());
        assertEquals(1, delivery.getItems().size());
        assertEquals(itemId, delivery.getItems().get(0).getId());
    }

    @Test
    void shouldRemoveItemAndUpdateTotalItems() {
        Delivery delivery = Delivery.draft();
        UUID itemId = delivery.addItem("Pizza", 2);
        delivery.removeItem(itemId);

        assertEquals(0, delivery.getTotalItems());
        assertTrue(delivery.getItems().isEmpty());
    }

    @Test
    void shouldChangeItemQuantityAndUpdateTotalItems() {
        Delivery delivery = Delivery.draft();
        UUID itemId = delivery.addItem("Pizza", 2);
        delivery.changeItemQuantity(itemId, 5);

        assertEquals(5, delivery.getTotalItems());
        assertEquals(5, delivery.getItems().get(0).getQuantity());
    }

    @Test
    void shouldRemoveAllItemsAndUpdateTotalItems() {
        Delivery delivery = Delivery.draft();
        delivery.addItem("Pizza", 2);
        delivery.addItem("Soda", 1);
        delivery.removeItems();

        assertEquals(0, delivery.getTotalItems());
        assertTrue(delivery.getItems().isEmpty());
    }

    @Test
    void shouldEditPreparationDetailsWhenDraft() {
        Delivery delivery = Delivery.draft();
        Delivery.PreparationDetails details = Delivery.PreparationDetails.builder()
                .sender(new ContactPoint("11111-222", "Street X", "500", "Apt 43", "Sender Update Name", "8198888-9999"))
                .recipient(new ContactPoint("22222-333", "Street Z", "900", "Apt 89", "Recipient Update Name", "8593333-4444"))
                .distanceFee(BigDecimal.valueOf(21))
                .courierPayout(BigDecimal.valueOf(9))
                .expectedDeliveryTime(Duration.ofHours(3))
                .build();

        delivery.editPreparationDetails(details);

        assertEquals(details.getSender(), delivery.getSender());
        assertEquals(details.getRecipient(), delivery.getRecipient());
        assertEquals(details.getDistanceFee(), delivery.getDistanceFee());
        assertEquals(details.getCourierPayout(), delivery.getCourierPayout());
        assertEquals(details.getDistanceFee().add(details.getCourierPayout()), delivery.getTotalCost());
        assertNotNull(delivery.getExpectedDeliveryAt());
    }

    @Test
    void shouldThrowExceptionWhenEditPreparationDetailsNotDraft() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createValidationDetails());
        delivery.place();

        assertThrows(DomainException.class, () -> delivery.editPreparationDetails(createValidationDetails()));
    }

    @Test
    void shouldPlaceDeliveryWhenFilledAndDraft() {
        Delivery delivery = Delivery.draft();

        delivery.editPreparationDetails(createValidationDetails());

        delivery.place();

        assertEquals(DeliveryStatus.WAITING_FOR_COURIER, delivery.getStatus());
        assertNotNull(delivery.getPlacedAt());
    }

    @Test
    void shouldThrowExceptionWhenPlaceDeliveryNotFilled() {
        Delivery delivery = Delivery.draft();

        assertThrows(DomainException.class, delivery::place);
    }

    @Test
    void shouldThrowExceptionWhenPlaceDeliveryNotDraft() {
        Delivery delivery = Delivery.draft();

        delivery.editPreparationDetails(createValidationDetails());
        delivery.place();

        assertThrows(DomainException.class, delivery::place);
    }

    @Test
    void shouldPickUpDeliveryAndSetCourierIdAndAssignedAt() {
        Delivery delivery = Delivery.draft();

        delivery.editPreparationDetails(createValidationDetails());
        delivery.place();

        UUID courierId = UUID.randomUUID();
        delivery.pickUp(courierId);

        assertEquals(courierId, delivery.getCourierId());
        assertEquals(DeliveryStatus.IN_TRANSIT, delivery.getStatus());
        assertNotNull(delivery.getAssignedAt());
    }

    @Test
    void shouldMarkAsDeliveredAndSetFulfilledAt() {
        Delivery delivery = Delivery.draft();

        delivery.editPreparationDetails(createValidationDetails());
        delivery.place();
        delivery.pickUp(UUID.randomUUID());

        delivery.markAsDelivered();

        assertEquals(DeliveryStatus.DELIVERY, delivery.getStatus());
        assertNotNull(delivery.getFulfilledAt());
    }

    @Test
    void shouldReturnUnmodifiableItemsList() {
        Delivery delivery = Delivery.draft();
        delivery.addItem("Pizza", 2);

        List<Item> items = delivery.getItems();
        assertThrows(UnsupportedOperationException.class, () -> items.add(Item.brandNew("Soda", 1)));
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