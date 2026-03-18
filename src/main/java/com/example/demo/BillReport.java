package com.example.demo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Bill Report System - Generates shop-style bills with IDs and complete details
 */
public class BillReport {
    
    /**
     * Represents a single bill/transaction group
     */
    public static class Bill {
        public String billId;
        public String date;
        public LocalDateTime billTime;
        public List<BillItem> items;
        public double totalAmount;
        public int totalItems;
        
        public Bill(String billId, LocalDateTime billTime, List<BillItem> items) {
            this.billId = billId;
            this.billTime = billTime;
            this.date = billTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            this.items = items;
            this.totalAmount = items.stream().mapToDouble(i -> i.amount).sum();
            this.totalItems = items.stream().mapToInt(i -> i.quantity).sum();
        }
        
        public String getFormattedDate() {
            return billTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
    
    /**
     * Individual item in a bill
     */
    public static class BillItem {
        public String productName;
        public String category;
        public double price;
        public int quantity;
        public double amount;
        public LocalDateTime timestamp;
        
        public BillItem(String productName, String category, double price, int quantity, LocalDateTime timestamp) {
            this.productName = productName;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.amount = price * quantity;
            this.timestamp = timestamp;
        }
    }
    
    private static int billCounter = 0;
    private static final List<Bill> bills = new ArrayList<>();
    
    /**
     * Generate a bill from sales records (typically daily)
     */
    public static void generateBillFromSales(String dateStr) {
        List<SalesTracker.SaleRecord> dailySales = SalesTracker.getSalesByDate(dateStr);
        
        if (dailySales.isEmpty()) {
            return;
        }
        
        billCounter++;
        String billId = String.format("BILL-%05d", billCounter);
        List<BillItem> items = new ArrayList<>();
        
        for (SalesTracker.SaleRecord sale : dailySales) {
            items.add(new BillItem(
                sale.productName,
                sale.category,
                sale.price,
                sale.quantity,
                sale.saleTime
            ));
        }
        
        Bill bill = new Bill(billId, dailySales.get(0).saleTime, items);
        bills.add(bill);
    }
    
    /**
     * Get all bills
     */
    public static List<Bill> getAllBills() {
        return new ArrayList<>(bills);
    }
    
    /**
     * Get bill by ID
     */
    public static Bill getBillById(String billId) {
        return bills.stream()
                .filter(b -> b.billId.equals(billId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get bills for a specific date
     */
    public static List<Bill> getBillsByDate(String date) {
        return bills.stream()
                .filter(b -> b.date.equals(date))
                .toList();
    }
    
    /**
     * Generate professional bill format for printing
     */
    public static String formatBillProfessional(Bill bill) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    🛍️ SHOP BILL REPORT                       ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════╝\n\n");
        
        sb.append("Bill ID:  ").append(bill.billId).append("\n");
        sb.append("Date:     ").append(bill.getFormattedDate()).append("\n");
        sb.append("─────────────────────────────────────────────────────────────\n\n");
        
        sb.append("ITEMS:\n");
        sb.append("┌──┬────────────────────────┬──────┬────────┬──────────────┐\n");
        sb.append("│No│ Product Name           │ Qty  │ Price  │ Amount       │\n");
        sb.append("├──┼────────────────────────┼──────┼────────┼──────────────┤\n");
        
        for (int i = 0; i < bill.items.size(); i++) {
            BillItem item = bill.items.get(i);
            sb.append(String.format("│%2d│ %-22s │ %4d │ Tk.%6.2f│ Tk.%10.2f │\n",
                    i + 1,
                    item.productName.substring(0, Math.min(22, item.productName.length())),
                    item.quantity,
                    item.price,
                    item.amount));
        }
        
        sb.append("└──┴────────────────────────┴──────┴────────┴──────────────┘\n\n");
        
        sb.append(String.format("Total Items: %d\n", bill.totalItems));
        sb.append(String.format("Total Amount: Tk.%.2f\n\n", bill.totalAmount));
        
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("              Thank you for your purchase! 🙏\n");
        sb.append("═══════════════════════════════════════════════════════════════\n");
        
        return sb.toString();
    }
    
    /**
     * Clear all bills (for testing)
     */
    public static void clearAllBills() {
        bills.clear();
        billCounter = 0;
    }
}
