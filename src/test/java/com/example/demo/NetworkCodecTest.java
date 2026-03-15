package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkCodecTest {

    @AfterEach
    void cleanup() {
        Session.logout();
    }

    @Test
    void stockItem_roundTrip_preservesMetadata() {
        StockItem item = new StockItem(
                "E_custom_100",
                "Gaming Mouse Pro",
                "Electronics",
                "Accessories",
                8,
                2499.5,
                "file:/tmp/mouse.png"
        );

        String encoded = NetworkCodec.encodeStockItem(item);
        StockItem decoded = NetworkCodec.decodeStockItem(encoded);

        assertEquals(item.getProductId(), decoded.getProductId());
        assertEquals(item.getProductName(), decoded.getProductName());
        assertEquals(item.getCategory(), decoded.getCategory());
        assertEquals(item.getSubCategory(), decoded.getSubCategory());
        assertEquals(item.getQuantity(), decoded.getQuantity());
        assertEquals(item.getPrice(), decoded.getPrice(), 0.0001);
        assertEquals(item.getImagePath(), decoded.getImagePath());
    }

    @Test
    void saleRecord_roundTrip_preservesFields() {
        SaleRecord sale = new SaleRecord(
                "sale-1",
                "2026-03-15T10:30:00",
                "Mahidul",
                "SERVER-PC",
                4,
                5500.0,
                "Mouse x1 | Serum x3"
        );

        String encoded = NetworkCodec.encodeSaleRecord(sale);
        SaleRecord decoded = NetworkCodec.decodeSaleRecord(encoded);

        assertEquals(sale.getSaleId(), decoded.getSaleId());
        assertEquals(sale.getTimestamp(), decoded.getTimestamp());
        assertEquals(sale.getSoldBy(), decoded.getSoldBy());
        assertEquals(sale.getSourceNode(), decoded.getSourceNode());
        assertEquals(sale.getTotalQuantity(), decoded.getTotalQuantity());
        assertEquals(sale.getTotalAmount(), decoded.getTotalAmount(), 0.0001);
        assertEquals(sale.getLineItemsSummary(), decoded.getLineItemsSummary());
    }

    @Test
    void buildSaleRecord_usesSessionUserAndItemSummary() {
        Session.login("Omi");
        CartItem mouse = new CartItem("E_Mouse", "Mouse", "Electronics", 5000, 2, "", 10);
        CartItem serum = new CartItem("B_Serum", "Hyaluronic Acid Serum", "Beauty", 2000, 1, "", 0);

        SaleRecord sale = NetworkManager.getInstance().buildSaleRecord(List.of(mouse, serum), 3, 11000.0);

        assertEquals("Omi", sale.getSoldBy());
        assertEquals(3, sale.getTotalQuantity());
        assertEquals(11000.0, sale.getTotalAmount(), 0.0001);
        assertFalse(sale.getSaleId().isBlank());
        assertTrue(sale.getSourceNode().contains("OFFLINE"));
        assertTrue(sale.getLineItemsSummary().contains("Mouse x2"));
        assertTrue(sale.getLineItemsSummary().contains("Hyaluronic Acid Serum x1"));
    }
}

