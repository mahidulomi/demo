package com.example.demo;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Small helper for serialising network payloads without extra dependencies.
 */
final class NetworkCodec {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private NetworkCodec() {}

    static String encodeText(String value) {
        String safe = value == null ? "" : value;
        return ENCODER.encodeToString(safe.getBytes(StandardCharsets.UTF_8));
    }

    static String decodeText(String encoded) {
        if (encoded == null || encoded.isEmpty()) return "";
        return new String(DECODER.decode(encoded), StandardCharsets.UTF_8);
    }

    static String encodeStockItem(StockItem item) {
        return String.join(";",
                encodeText(item.getProductId()),
                encodeText(item.getProductName()),
                encodeText(item.getCategory()),
                encodeText(item.getSubCategory()),
                Integer.toString(item.getQuantity()),
                Double.toString(item.getPrice()),
                encodeText(item.getImagePath())
        );
    }

    static StockItem decodeStockItem(String payload) {
        String[] parts = payload.split(";", -1);
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid stock payload: " + payload);
        }
        String productId = decodeText(parts[0]);
        String productName = decodeText(parts[1]);
        String category = decodeText(parts[2]);
        String subCategory = decodeText(parts[3]);
        int quantity = Integer.parseInt(parts[4]);
        double price = Double.parseDouble(parts[5]);
        String imagePath = parts.length >= 7 ? decodeText(parts[6]) : "";
        return new StockItem(productId, productName, category, subCategory, quantity, price, imagePath);
    }

    static String encodeSaleRecord(SaleRecord sale) {
        return String.join(";",
                encodeText(sale.getSaleId()),
                encodeText(sale.getTimestamp()),
                encodeText(sale.getSoldBy()),
                encodeText(sale.getSourceNode()),
                Integer.toString(sale.getTotalQuantity()),
                Double.toString(sale.getTotalAmount()),
                encodeText(sale.getLineItemsSummary()),
                encodeText(sale.getItemsJson())
        );
    }

    static SaleRecord decodeSaleRecord(String payload) {
        String[] parts = payload.split(";", -1);
        if (parts.length < 7) {
            throw new IllegalArgumentException("Invalid sale payload: " + payload);
        }
        String itemsJson = parts.length >= 8 ? decodeText(parts[7]) : "[]";
        
        return new SaleRecord(
                decodeText(parts[0]),
                decodeText(parts[1]),
                decodeText(parts[2]),
                decodeText(parts[3]),
                Integer.parseInt(parts[4]),
                Double.parseDouble(parts[5]),
                decodeText(parts[6]),
                itemsJson
        );
    }

    static String encodeCustomer(Customer customer) {
        return String.join(";",
                encodeText(customer.getId()),
                encodeText(customer.getName()),
                encodeText(customer.getPhone()),
                encodeText(customer.getEmail()),
                encodeText(customer.getAddress()),
                encodeText(customer.getType()),
                Double.toString(customer.getDueBalance())
        );
    }

    static Customer decodeCustomer(String payload) {
        String[] parts = payload.split(";", -1);
        if (parts.length < 7) {
            throw new IllegalArgumentException("Invalid customer payload: " + payload);
        }
        return new Customer(
                decodeText(parts[0]),
                decodeText(parts[1]),
                decodeText(parts[2]),
                decodeText(parts[3]),
                decodeText(parts[4]),
                decodeText(parts[5]),
                Double.parseDouble(parts[6])
        );
    }

    static String joinRecords(Collection<String> records) {
        return String.join("|", records);
    }

    static List<String> splitRecords(String payload) {
        if (payload == null || payload.isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = payload.split("\\|");
        List<String> records = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                records.add(part);
            }
        }
        return records;
    }
}

