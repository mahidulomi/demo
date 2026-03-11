package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartTest {

    @AfterEach
    void cleanup() {
        Cart.clearCart();
        Cart.setLastVisitedPage("home-view.fxml");
    }

    @Test
    void addItem_mergesQuantityForSameProduct() {
        Cart.addItem("P1", "Phone", "Electronics", 100.0, 1, "", 0);
        Cart.addItem("P1", "Phone", "Electronics", 100.0, 2, "", 0);

        assertEquals(1, Cart.getItemCount());
        assertEquals(3, Cart.getTotalQuantity());
        assertEquals(300.0, Cart.getTotalPrice(), 0.001);
    }

    @Test
    void updateQuantity_zeroRemovesItem() {
        Cart.addItem("P2", "Watch", "Accessories", 50.0, 2, "", 0);

        Cart.updateQuantity("P2", 0);

        assertTrue(Cart.isEmpty());
        assertFalse(Cart.containsItem("P2"));
    }

    @Test
    void lastVisitedPage_ignoresInvalidValues() {
        Cart.setLastVisitedPage("electronics-view.fxml");
        Cart.setLastVisitedPage("");
        Cart.setLastVisitedPage(null);

        assertEquals("electronics-view.fxml", Cart.getLastVisitedPage());
    }
}

