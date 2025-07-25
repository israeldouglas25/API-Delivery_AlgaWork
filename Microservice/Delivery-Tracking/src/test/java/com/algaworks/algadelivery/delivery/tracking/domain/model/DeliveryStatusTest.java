package com.algaworks.algadelivery.delivery.tracking.domain.model;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryStatusTest {

    @org.junit.jupiter.api.Test
    void canChangeToReturnsTrueForValidTransitions() {
        assertTrue(DeliveryStatus.DRAFT.canChangeTo(DeliveryStatus.WAITING_FOR_COURIER));
        assertTrue(DeliveryStatus.WAITING_FOR_COURIER.canChangeTo(DeliveryStatus.IN_TRANSIT));
        assertTrue(DeliveryStatus.IN_TRANSIT.canChangeTo(DeliveryStatus.DELIVERY));
    }

    @org.junit.jupiter.api.Test
    void canChangeToReturnsFalseForInvalidTransitions() {
        assertFalse(DeliveryStatus.DRAFT.canChangeTo(DeliveryStatus.IN_TRANSIT));
        assertFalse(DeliveryStatus.WAITING_FOR_COURIER.canChangeTo(DeliveryStatus.DELIVERY));
        assertFalse(DeliveryStatus.DELIVERY.canChangeTo(DeliveryStatus.DRAFT));
    }

    @org.junit.jupiter.api.Test
    void canNotChangeToReturnsTrueForInvalidTransitions() {
        assertTrue(DeliveryStatus.DRAFT.canNotChangeTo(DeliveryStatus.IN_TRANSIT));
        assertTrue(DeliveryStatus.WAITING_FOR_COURIER.canNotChangeTo(DeliveryStatus.DELIVERY));
        assertTrue(DeliveryStatus.DELIVERY.canNotChangeTo(DeliveryStatus.DRAFT));
    }

    @org.junit.jupiter.api.Test
    void canNotChangeToReturnsFalseForValidTransitions() {
        assertFalse(DeliveryStatus.DRAFT.canNotChangeTo(DeliveryStatus.WAITING_FOR_COURIER));
        assertFalse(DeliveryStatus.WAITING_FOR_COURIER.canNotChangeTo(DeliveryStatus.IN_TRANSIT));
        assertFalse(DeliveryStatus.IN_TRANSIT.canNotChangeTo(DeliveryStatus.DELIVERY));
    }

    @org.junit.jupiter.api.Test
    void canChangeToReturnsFalseWhenTransitionToSameStatus() {
        assertFalse(DeliveryStatus.DRAFT.canChangeTo(DeliveryStatus.DRAFT));
        assertFalse(DeliveryStatus.IN_TRANSIT.canChangeTo(DeliveryStatus.IN_TRANSIT));
    }

}