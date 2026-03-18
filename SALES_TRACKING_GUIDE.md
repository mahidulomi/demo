# Dashboard Sales Tracking System - Implementation Guide

## Features Implemented

### 1. **Products Menu** 🛍️
- Click "📦 Products" button in sidebar
- Shows a context menu with 4 categories:
  - 💄 Beauty
  - 📱 Electronics
  - 🏠 Home & Living
  - 👗 Fashion
- Each category navigates to the respective product page

### 2. **Sales Section** 💰
- Click "💰 Sales" button in sidebar
- **If no sales:** Shows "No sales yet. Customers need to purchase products!"
- **If sales exist:** Displays:
  - Total number of transactions
  - Total revenue (₹ amount)
  - Last 5 recent sales with:
    - Product name
    - Category
    - Quantity sold
    - Total amount for that transaction
    - Date & time of purchase

### 3. **Reports Section** 📊
- Click "📊 Reports" button in sidebar
- **If no sales:** Shows "No sales data available yet."
- **If sales exist:** Displays comprehensive report with:
  - **Overall Statistics:**
    - Total transactions count
    - Total revenue amount
  
  - **Sales by Category:**
    - Each category shows:
      - Total units sold
      - Revenue for that category
  
  - **All Transactions (Date-wise):**
    - Complete table showing:
      - Product name
      - Category
      - Quantity sold
      - Amount (₹)
      - Exact date & time of purchase

### 4. **Auto-Add Sales** ✅
When a customer purchases products, sales are automatically added:

- **Cart Checkout:** Each item in cart is added to sales when "Buy Now" is clicked
- **Direct Purchase:** When "Buy Now" is used on product details page
- **Real-time Updates:** Sales appear immediately in Sales and Reports sections

---

## How It Works

### Sales Recording Flow:
```
Customer Adds Product to Cart
           ↓
Customer Clicks "Buy Now" / Checkout
           ↓
SalesTracker.addSale() is called for each item
           ↓
Sale appears in Dashboard:
  - Sales section shows latest sales
  - Reports show detailed breakdown by category and date
```

### SalesTracker Class Features:
```java
SalesTracker.addSale(
  String productName,     // e.g., "iPhone 15"
  String category,        // e.g., "Electronics"
  double unitPrice,       // e.g., 45000.00
  int quantity            // e.g., 2
);
```

---

## Example Usage

### Scenario 1: First Purchase
1. User clicks "📱 Electronics" from Products menu
2. User selects iPhone 15 (₹45,000)
3. User clicks "Buy Now" with qty=1
4. **Auto-recorded:** SalesTracker adds sale
5. Go to Dashboard → Sales section
6. **Shows:** "iPhone 15 (Electronics) - Qty: 1 @ ₹45,000.00 - 2026-03-18 14:30:22"
7. Go to Dashboard → Reports section
8. **Shows:** 
   - Total Transactions: 1
   - Total Revenue: ₹45,000.00
   - Electronics: 1 units - ₹45,000.00
   - Transaction table with date/time

### Scenario 2: Multiple Purchases
1. User buys: iPhone 15 (Electronics, ₹45,000 × 1)
2. User buys: Wireless Mouse (Electronics, ₹2,500 × 2)
3. User buys: Face Cream (Beauty, ₹800 × 3)
4. **Sales Section Shows:**
   - Total Sales: 3 transactions
   - Total Revenue: ₹50,400.00
   - Last 5 sales listed with all details

5. **Reports Section Shows:**
   - Overall: 3 transactions, ₹50,400.00 revenue
   - Electronics: 3 units - ₹50,000.00
   - Beauty: 3 units - ₹2,400.00
   - All transactions with timestamps

---

## Data Storage

- **In-Memory:** Sales stored during current session
- **Real-time:** Updates instantly as purchases happen
- **Persistent:** (Optional) Can be enhanced to save to disk via SalesTracker

---

## API Reference

### SalesTracker Methods:

```java
// Add a new sale
SalesTracker.addSale(productName, category, price, quantity);

// Get all sales
List<SaleRecord> sales = SalesTracker.getAllSales();

// Get total revenue
double revenue = SalesTracker.getTotalRevenue();

// Get sales count
int count = SalesTracker.getTotalSalesCount();

// Get sales by category
List<SaleRecord> categorySales = SalesTracker.getSalesByCategory("Electronics");

// Get sales by date
List<SaleRecord> dateSales = SalesTracker.getSalesByDate("2026-03-18");

// Get top selling products
List<SaleRecord> topSellers = SalesTracker.getTopSellingProducts(5);
```

---

## Files Modified/Created

### New Files:
- ✅ `SalesTracker.java` - Sales tracking system

### Updated Files:
- ✅ `HomeController.java` - Added Products menu, Sales, Reports methods
- ✅ `home-view.fxml` - Updated sidebar buttons with fx:id and action handlers
- ✅ `CartController.java` - Added SalesTracker.addSale() in checkout
- ✅ `ProductDetailsController.java` - Added SalesTracker.addSale() in Buy Now

---

## Testing Checklist

- [ ] Click Products → See 4 categories menu
- [ ] Click each category and verify navigation
- [ ] Click Sales → See "No sales" message initially
- [ ] Buy a product via cart checkout
- [ ] Click Sales → See purchase recorded with quantity and amount
- [ ] Buy more products from different categories
- [ ] Click Reports → See detailed breakdown by category
- [ ] Check timestamps are accurate
- [ ] Verify total revenue calculations are correct
- [ ] Check category-wise totals match

---

## Notes

- Sales are recorded automatically with product name, category, price, and quantity
- Each sale includes exact date and time of purchase
- Reports show both summary and detailed transaction history
- Dashboard dynamically updates as customers make purchases
- Empty states handled gracefully with appropriate messages


