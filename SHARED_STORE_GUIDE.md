# Shared Customer & Sales Store Implementation

## Overview
This implementation provides a unified, synchronized customer and sales database that works seamlessly between customer and owner sides of your application. All data is automatically synchronized across network nodes and sorted by timestamp.

## Key Features

### 1. **Unified Customer Store (Sob Store)**
- All customers are stored in a centralized, synchronized database
- Whether a customer registers on the customer side or owner side, they appear in both
- Customers are sorted by creation time (ascending - oldest first)

### 2. **Bi-directional Sales Synchronization**
- When a **customer purchases** from the customer side → sale is recorded in the owner's database
- When the **owner makes a sale** from the owner side → sale is recorded in the customer's database
- All sales are automatically synced across network nodes via `NetworkManager`

### 3. **Timestamp-based Sorting**
- All sales records are sorted by timestamp
- Newest sales appear first by default
- Methods available for both ascending and descending sorts

### 4. **Cross-node Visibility**
- Each machine maintains identical customer and sales data
- Sales include the source node (owner/customer machine) for audit trail
- Seller information is tracked for each transaction

## Usage Guide

### Basic Usage

#### Getting All Data (Sorted)

```java
// Get all customers sorted by registration time
List<Customer> customers = SharedStore.getAllCustomersSorted();

// Get all sales sorted by timestamp (newest first)
List<SaleRecord> sales = SharedStore.getAllSalesSorted();

// Get all sales sorted by timestamp (oldest first)
List<SaleRecord> salesAsc = SharedStore.getAllSalesSortedAscending();
```

#### Customer Management

```java
// Find customer by ID
Customer customer = SharedStore.getCustomerById(customerId);

// Find customer by phone number
Customer customer = SharedStore.getCustomerByPhone("+8801712345678");

// Add or update a customer (broadcasts to other nodes)
Customer newCustomer = new Customer("Rahim", "0171234567", "rahim@email.com", "Dhaka", "Retail", 0);
SharedStore.addOrUpdateCustomer(newCustomer);
```

#### Sales Recording

```java
// Record a sale (broadcasts to other nodes)
SaleRecord sale = new SaleRecord(
    "BILL-001",
    LocalDateTime.now().toString(),
    "Salesman Name",
    "machine-owner-node",
    5,           // quantity
    2500.00,     // amount
    "Item1 x2 | Item2 x3",
    "[{...}]",   // JSON items
    "Customer Name",
    "+8801712345678",
    "customer@email.com",
    "Customer Address"
);
SharedStore.recordSharedSale(sale);
```

#### Getting Filtered Sales

```java
// Get all sales by a specific customer
List<SaleRecord> customerSales = SharedStore.getCustomerSalesSorted(customerId);

// Get all sales by a specific seller
List<SaleRecord> sellerSales = SharedStore.getSellerSalesSorted("Salesman Name");

// Get all sales from a specific node (machine)
List<SaleRecord> nodeSales = SharedStore.getNodeSalesSorted("machine-owner-node");

// Get all unique source nodes
Set<String> nodes = SharedStore.getAllSourceNodes();
```

#### Analytics & Reports

```java
// Get sales summary with grouped statistics
Map<String, Object> summary = SharedStore.getSalesSummary();
// Returns: totalSales, totalAmount, totalQuantity, totalCustomers, salesByNode, salesBySeller, latestSale

// Get top customers by purchase amount (descending)
List<Map<String, Object>> topCustomers = SharedStore.getTopCustomers();
// Each entry: phone, name, totalAmount, totalQuantity, purchaseCount

// Get complete purchase history for a customer
Map<String, Object> history = SharedStore.getCustomerPurchaseHistory(customerId);
// Returns: customer details + list of all purchases + summary stats
```

### Integration with Existing Code

The SharedStore layer sits on top of existing managers:

```
SharedStore (Unified API)
    ↓
CustomerManager + SalesManager + NetworkManager (Existing Infrastructure)
    ↓
File Storage + Network Broadcasting
```

### Example: Complete Customer Purchase Flow

```java
public class CheckoutController {
    public void processCheckout(List<CartItem> items, Customer customer) {
        // 1. Ensure customer is in shared store
        if (customer.getId() == null) {
            customer = new Customer(...);
        }
        SharedStore.addOrUpdateCustomer(customer);
        
        // 2. Create sale record
        SaleRecord sale = NetworkManager.getInstance()
            .buildSaleRecord(
                items, 
                totalQty, 
                totalAmount,
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress()
            );
        
        // 3. Record sale (broadcasts to owner/other nodes)
        SharedStore.recordSharedSale(sale);
        
        // 4. Update customer due balance if on credit
        customer.setDueBalance(customer.getDueBalance() + sale.getTotalAmount());
        SharedStore.addOrUpdateCustomer(customer);
    }
}
```

### Example: Display All Sales Timeline

```java
public void displaySalesTimeline() {
    List<SaleRecord> sales = SharedStore.getAllSalesSorted(); // Newest first
    
    for (SaleRecord sale : sales) {
        System.out.println(
            sale.getTimestamp() + 
            " | " + sale.getCustomerName() + 
            " | ₹" + sale.getTotalAmount() +
            " | from: " + sale.getSourceNode()
        );
    }
}
```

### Example: Customer Profile View

```java
public void showCustomerProfile(String customerId) {
    Map<String, Object> history = SharedStore.getCustomerPurchaseHistory(customerId);
    
    Customer cust = (Customer) // retrieve from history
    System.out.println("Name: " + cust.getName());
    System.out.println("Phone: " + cust.getPhone());
    System.out.println("Type: " + cust.getType());
    System.out.println("Due Balance: " + cust.getDueBalance());
    
    List<SaleRecord> purchases = (List<SaleRecord>) history.get("purchases");
    System.out.println("Total Purchases: " + purchases.size());
    System.out.println("Total Spent: " + history.get("totalAmount"));
    
    // Display each purchase
    for (SaleRecord sale : purchases) {
        System.out.println("  " + sale.getTimestamp() + " - ₹" + sale.getTotalAmount());
    }
}
```

## Data Synchronization Flow

### When Networking is Enabled:

1. **Customer Side Makes Purchase**
   - Customer saves customer info locally
   - Customer records sale locally
   - `NetworkManager.broadcastCustomer()` sends to Owner
   - `NetworkManager.broadcastSaleRecord()` sends to Owner
   - Owner receives and saves to their database automatically

2. **Owner Side Makes Sale**
   - Owner saves customer info locally
   - Owner records sale locally
   - `NetworkManager.broadcastCustomer()` sends to Customer
   - `NetworkManager.broadcastSaleRecord()` sends to Customer
   - Customer receives and saves to their database automatically

3. **New Node Connects**
   - Server sends full `CustomerManager` data
   - Server sends full `SalesManager` data
   - New node replaces local data with complete sync

## File Storage

### Files Used:
- **Customers**: `~/.shopapp_customers.dat` (one line per customer, Base64 encoded)
- **Sales**: `~/.shopapp_sales.dat` (one line per sale, Base64 encoded)

### Auto-detection:
- Both managers watch their files every 2 seconds
- If external changes detected, data reloads automatically
- Useful for multi-instance same-machine scenarios

## Important Notes

1. **Thread Safety**: All SharedStore methods are `synchronized` for thread-safe access
2. **Null Handling**: Methods gracefully handle null values
3. **Broadcasting**: All add/record operations automatically broadcast (if networking active)
4. **Sorting**: Latest data appears first by default (descending timestamp)
5. **Persistent**: All data survives app restart (stored in home directory files)

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    SharedStore (This Layer)                  │
│  - Unified API for customer & sales data                    │
│  - Automatic sorting & filtering                            │
│  - Analytics & reporting                                    │
└──────────────┬──────────────────────────────────────────────┘
               │
    ┌──────────┼──────────────┐
    ▼          ▼              ▼
┌─────────┐ ┌──────────┐ ┌──────────────┐
│Customer │ │  Sales   │ │   Network    │
│Manager  │ │ Manager  │ │   Manager    │
│  (.dat) │ │  (.dat)  │ │ (Broadcast)  │
└────┬────┘ └────┬─────┘ └──────┬───────┘
     │           │              │
     │           │    ┌─────────┴────────────┐
     │           │    ▼                      ▼
     │           │ ┌────────────────────────────────┐
     └───────────┼─►  StockServer / StockClient     │
                 │  (Network Synchronization)       │
                 └────────────────────────────────┘
```

## Troubleshooting

### Issue: Data not syncing between nodes
**Solution**: Ensure NetworkManager is in SERVER or CLIENT mode (not OFFLINE)
```java
NetworkManager.getInstance().getMode() // Should be SERVER or CLIENT
```

### Issue: Old data not appearing
**Solution**: Call syncData() to reload from disk
```java
SharedStore.syncData();
```

### Issue: Can't find customer
**Solution**: Check if customer exists and use correct identifiers
```java
Customer c = SharedStore.getCustomerByPhone(phone); // More reliable than ID
if (c == null) System.out.println("Customer not found");
```

## Future Enhancements

1. Add date range filtering for sales queries
2. Add customer status tracking (active, inactive, blacklisted)
3. Add inventory tracking per customer
4. Add payment history/reconciliation
5. Add customer loyalty points system

