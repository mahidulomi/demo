# 🏪 E-Commerce Shopping Application - সম্পূর্ণ গাইড

## প্রজেক্ট সারসংক্ষেপ
এটি একটি **JavaFX ভিত্তিক ডেস্কটপ ই-কমার্স অ্যাপ্লিকেশন** যা একক মেশিন বা মাল্টি-মেশিন নেটওয়ার্কে চলতে পারে। স্টক, বিক্রয়, গ্রাহক ডেটা এবং ব্যবহারকারী অ্যাকাউন্ট স্থানীয় ফাইলে সংরক্ষিত হয় এবং নেটওয়ার্কের মাধ্যমে সিঙ্ক হয়।

---

## 📁 ফাইল স্ট্রাকচার এবং দায়িত্ব

### 🔐 ব্যবহারকারী ব্যবস্থাপনা (User Management)

#### **UserStore.java** (405 লাইন)
**কাজ:** সমস্ত ব্যবহারকারী অ্যাকাউন্ট পরিচালনা করে (সাইনআপ, লগইন, পাসওয়ার্ড রিসেট)

**ডেটা স্টোরেজ:**
- ফাইল: `~/.demo-javafx/users.properties` (লিগ্যাসি) বা `%APPDATA%/DemoJavaFX/users.properties` (Windows)
- ফর্ম্যাট: Base64 এনকোডেড: `username=v2:password|personalData|role`
- উদাহরণ: `admin=v2:YWRtaW4xMjM=|U2VjdXJpdHlRQQ==|QWRtaW4=`

**মূল ফাংশন:**
```
createUser(username, password, personalData, role)     → নতুন ইউজার তৈরি
validateLogin(username, password)                      → লগইন যাচাই
verifyRecoveryData(username, personalData)             → পাসওয়ার্ড রিসেট যাচাইয়ের জন্য
resetPassword(username, personalData, newPassword)     → পাসওয়ার্ড পরিবর্তন
getRole(username)                                      → ইউজার রোল পান (Admin/Customer)
importUserFromNetwork(networkData)                     → নেটওয়ার্ক থেকে ইউজার ইমপোর্ট
getAllSerializedUsers()                                → সব ইউজার সিরিয়ালাইজ ফর্ম্যাটে
```

**নিরাপত্তা বৈশিষ্ট্য:**
- ✅ Base64 এনকোডিং (ডেমো উদ্দেশ্য, সম্পূর্ণ এনক্রিপশন নয়)
- ✅ Thread-safe (ConcurrentHashMap + IO_LOCK)
- ✅ Atomic ফাইল লেখা (temp file + move)
- ✅ Recovery question সহ পাসওয়ার্ড রিসেট

**লিগ্যাসি সাপোর্ট:**
- পুরানো ইউজার অটোমেটিক নতুন লোকেশনে মাইগ্রেট হয়
- ফাইল টাইমস্ট্যাম্প ট্র্যাক করে পরিবর্তন সনাক্ত করে

---

### 💳 সেশন ম্যানেজমেন্ট (Session Management)

#### **Session.java** (168 লাইন)
**কাজ:** লগইন সেশন এবং স্ক্রিন নেভিগেশন পরিচালনা করে

**ডেটা সংরক্ষণ:**
- `currentUser` (ভোলাটাইল স্ট্রিং) - বর্তমান লগইনকৃত ইউজার নাম

**মূল ফাংশন:**
```
login(username)              → লগইন ইউজার
logout()                     → লগআউট
getCurrentUser()             → বর্তমান ইউজার নাম
goToHome()                   → হোম পেজে যান
goToCart()                   → কার্টে যান
goToProductList()            → প্রোডাক্ট লিস্ট দেখান
... (20+ navigation methods)
```

**FXML ম্যাপিং:**
- `hello-view.fxml` → লগইন পেজ
- `signup-view.fxml` → সাইনআপ পেজ
- `home-view.fxml` → অ্যাডমিন হোম (1250x750)
- `customer-home-view.fxml` → কাস্টমার হোম
- `product-list-view.fxml` → প্রোডাক্ট লিস্ট
- এবং আরও অনেক...

---

### 🛍️ পণ্য ব্যবস্থাপনা (Product Management)

#### **StockManager.java** (524 লাইন)
**কাজ:** সম্পূর্ণ পণ্য ক্যাটালগ এবং স্টক পরিচালনা করে

**ডেটা স্টোরেজ:**
- ফাইল: `~/.shopapp_stock.dat`
- ফর্ম্যাট প্রতি লাইন: `productId|productName|category|subCategory|quantity|price|imagePath`
- উদাহরণ: `PROD_001|Acid Serum|Beauty|Beauty|25|1200|/beautyimages/acidserum.png`

**ডিফল্ট প্রোডাক্ট ক্যাটেগরি:**
- **Beauty** (12 পণ্য): Serum, Conditioner, Eyeshadow, Face Cream, ইত্যাদি
- **Electronics** (11 পণ্য): iPhone, iPad, Laptop, AirPods, ইত্যাদি
- **Fashion** (6 পণ্য): T-shirt, Pant, Jacket, Saree, Sneakers, Shirt
- **Home and Living** (8 পণ্য): Sofa, Table, Bed Sheet, Wall Clock, ইত্যাদি

**মূল ফাংশন:**
```
initialize()                 → স্টক ফাইল লোড এবং ডিফল্ট তৈরি
getStockItem(productId)      → প্রোডাক্ট বিবরণ পান
updateStock(productId, qty)  → পরিমাণ আপডেট
getAllStockItems()           → সব প্রোডাক্ট লিস্ট
batchUpdateStock(updates)    → একসাথে একাধিক আপডেট
upsertStockItem(item)        → যোগ বা আপডেট করুন
removeStockItem(productId)   → পণ্য সরান
replaceAllStock(items)       → সম্পূর্ণ ক্যাটালগ প্রতিস্থাপন
getSerializedStockData()     → নেটওয়ার্কে পাঠানোর জন্য সিরিয়ালাইজ
```

**অটো-সেভ বৈশিষ্ট্য:**
- প্রতিটি পরিবর্তন তাৎক্ষণিকভাবে ফাইলে সংরক্ষিত হয়
- ফাইল ওয়াচার বাহ্যিক পরিবর্তন সনাক্ত করে

---

### 👥 গ্রাহক ব্যবস্থাপনা (Customer Management)

#### **Customer.java** (68 লাইন)
**ডেটা মডেল:**
```java
class Customer implements Serializable {
    String id              // UUID (অনন্য ID)
    String name            // গ্রাহক নাম
    String phone           // ফোন নম্বর
    String email           // ইমেইল
    String address         // ঠিকানা
    String type            // "Retail" বা "Wholesale"
    double dueBalance      // বকেয়া পরিমাণ (ক্রেডিট)
}
```

#### **CustomerManager.java** (174 লাইন)
**কাজ:** গ্রাহক ডেটা পার্সিস্ট করে এবং পরিচালনা করে

**ডেটা স্টোরেজ:**
- ফাইল: `~/.shopapp_customers.dat`
- ফর্ম্যাট: প্রতি লাইনে একটি Customer (Base64 এনকোডেড)

**মূল ফাংশন:**
```
saveCustomer(customer)              → গ্রাহক সংরক্ষণ
getAllCustomers()                   → সব গ্রাহক তালিকা
deleteCustomer(customerId)          → গ্রাহক সরান
replaceAllCustomers(customers)      → সম্পূর্ণ তালিকা প্রতিস্থাপন
getSerializedCustomerData()          → নেটওয়ার্কে সেন্ড করার জন্য
```

---

### 🛒 কার্ট ব্যবস্থাপনা (Shopping Cart)

#### **Cart.java**
**কাজ:** কেনাকাটা কার্ট ব্যবস্থাপনা (ইন-মেমরি)

**ডেটা মডেল:**
```
List<CartItem> items          // কার্টে পণ্য
String lastVisitedPage        // পূর্ববর্তী পৃষ্ঠা মনে রাখার জন্য
```

#### **CartItem.java**
```
String productId              // প্রোডাক্ট ID
String productName            // প্রোডাক্ট নাম
String category               // ক্যাটেগরি
int quantity                  // পরিমাণ
double unitPrice              // মূল মূল্য
double discountedUnitPrice    // ছাড় সহ মূল্য
double totalPrice             // মোট (quantity × discountedUnitPrice)
```

**মূল ফাংশন:**
```
addItem(cartItem)             → কার্টে যোগ
removeItem(productId)         → কার্ট থেকে সরান
updateQuantity(productId, qty)→ পরিমাণ আপডেট
getTotalAmount()              → মোট পরিমাণ
getTotalQuantity()            → মোট পণ্য সংখ্যা
clear()                       → কার্ট খালি করুন
getAllItems()                 → সব আইটেম পান
```

---

### 💰 বিক্রয় রেকর্ড (Sales Management)

#### **SaleRecord.java**
**ডেটা মডেল:**
```java
class SaleRecord {
    String saleId              // "BILL-" + timestamp
    String timestamp           // ISO datetime
    String soldBy              // বিক্রয়ের দায়িত্বে থাকা ব্যক্তি
    String sourceNode          // "hostname-MODE" (e.g., "PC1-SERVER")
    int totalQuantity          // মোট পণ্য সংখ্যা
    double totalAmount         // মোট টাকা
    String lineItemsSummary    // মানব-পঠনযোগ্য সারাংশ
    String itemsJson           // JSON ফর্ম্যাট আইটেম
    String customerName        // গ্রাহক নাম (অপশনাল)
    String customerPhone       // গ্রাহক ফোন
    String customerEmail       // গ্রাহক ইমেইল
    String customerAddress     // গ্রাহক ঠিকানা
}
```

#### **SalesManager.java**
**কাজ:** সমস্ত বিক্রয় রেকর্ড সংরক্ষণ এবং প্রতিবেদন তৈরি করে

**ডেটা স্টোরেজ:**
- ফাইল: `~/.shopapp_sales.dat`
- ফর্ম্যাট: প্রতি লাইনে এক বিক্রয় (Base64 এনকোডেড)

**মূল ফাংশন:**
```
recordSale(saleRecord)              → বিক্রয় রেকর্ড করুন
getAllSales()                       → সব বিক্রয় তালিকা
getSalesByDateRange(from, to)       → তারিখ অনুযায়ী বিক্রয়
replaceAllSales(sales)              → সম্পূর্ণ ইতিহাস প্রতিস্থাপন
getSerializedSalesData()            → নেটওয়ার্কে সেন্ড করুন
```

#### **SalesTracker.java**
**কাজ:** রিয়েল-টাইম ড্যাশবোর্ড মেট্রিক্স ট্র্যাক করে (ইন-মেমরি)

```
getTotalRevenue()               → মোট রাজস্ব
getTotalSalesCount()            → মোট বিক্রয় সংখ্যা
getRevenueToday()               → আজকের রাজস্ব
getSalesCountToday()            → আজকের বিক্রয় সংখ্যা
addNetworkSale(sale)            → নেটওয়ার্ক থেকে বিক্রয় যোগ করুন
```

---

### 📊 পুনঃসঞ্চয় ব্যবস্থাপনা (Restock Management)

#### **RestockRecord.java**
```java
class RestockRecord {
    String productId            // প্রোডাক্ট ID
    String productName          // প্রোডাক্ট নাম
    int quantityAdded           // কত পরিমাণ যোগ হয়েছে
    double totalCost            // মোট খরচ
    String timestamp            // সময়
    String recordedBy           // কে রেকর্ড করেছে
}
```

#### **RestockManager.java**
**কাজ:** পণ্য পুনঃসঞ্চয়ের রেকর্ড সংরক্ষণ করে

**ডেটা স্টোরেজ:**
- ফাইল: `~/.shopapp_restock.dat`

---

---

## 🌐 নেটওয়ার্কিং সিস্টেম

### সামগ্রিক আর্কিটেকচার

```
┌─────────────┐              TCP Socket              ┌─────────────┐
│  CLIENT 1   │◄────────────────────────────────────►│             │
│  (GUI App)  │                                      │             │
└─────────────┘                                      │  SERVER     │
                                                     │  Machine    │
┌─────────────┐              TCP Socket              │  (GUI App)  │
│  CLIENT 2   │◄────────────────────────────────────►│             │
│  (GUI App)  │                                      │             │
└─────────────┘                                      └─────────────┘

অথবা OFFLINE মোড (একক মেশিন - নেটওয়ার্ক নেই)
```

### মোড অপারেশন

#### **১. OFFLINE মোড (ডিফল্ট)**
- স্থানীয় ফাইলে সব ডেটা সংরক্ষিত হয়
- নেটওয়ার্ক সিঙ্ক নেই
- ব্যবহার: স্টার্টআপ বা একক মেশিন সেটআপ

#### **২. SERVER মোড**
- এই মেশিন "সত্য" ডেটার উৎস
- TCP সার্ভার পোর্টে শোনে (default: 5000)
- সব ক্লায়েন্টে আপডেট সম্প্রচার করে
- যেকোনো ক্লায়েন্ট থেকে আপডেট গ্রহণ করে এবং সম্প্রচার করে

#### **३. CLIENT মোড**
- SERVER এ সংযোগ করে
- সার্ভার থেকে শুরু করে সম্পূর্ণ ডেটা স্ন্যাপশট পায়
- স্থানীয় পরিবর্তন সার্ভারে পাঠায়
- সার্ভার থেকে আপডেট শোনে এবং প্রয়োগ করে

---

### 🔧 নেটওয়ার্ক ম্যানেজার (Central Hub)

#### **NetworkManager.java** (394 লাইন)
**কাজ:** নেটওয়ার্ক অপারেশনের কেন্দ্রীয় হাব (Singleton)

**সিঙ্গেলটন অ্যাক্সেস:**
```
NetworkManager nm = NetworkManager.getInstance();
```

**মূল অবস্থা:**
```
Mode mode                          // OFFLINE, SERVER, বা CLIENT
StockServer server                 // শুধু SERVER মোডে
StockClient client                 // শুধু CLIENT মোডে
StockUpdateListener currentListener // UI আপডেটের জন্য কলব্যাক
Runnable serverStatusCallback       // সার্ভার স্ট্যাটাস পরিবর্তনের জন্য
Runnable userSyncCallback           // ব্যবহারকারী সিঙ্ক হলে কলব্যাক
```

**মূল ফাংশন:**

**সেটআপ:**
```
startAsServer(port)                 → SERVER মোড শুরু করুন (TCP শোনুন)
connectToServer(host, port)         → SERVER এ সংযোগ করুন (CLIENT)
setOffline()                        → OFFLINE মোডে যান
getMode()                           → বর্তমান মোড
isActive()                          → নেটওয়ার্ক সক্রিয়?
getClientCount()                    → সংযুক্ত ক্লায়েন্ট সংখ্যা
```

**সম্প্রচার (Broadcast) ফাংশন:**

স্থানীয় পরিবর্তন নেটওয়ার্কে ছড়িয়ে দেয়
```
broadcastStockUpdate(productId, newQty)  → স্টক আপডেট সম্প্রচার
broadcastNewProduct(item)                → নতুন প্রোডাক্ট সম্প্রচার
broadcastDeleteProduct(productId)        → প্রোডাক্ট সরান সম্প্রচার
broadcastSaleRecord(sale)                → বিক্রয় রেকর্ড সম্প্রচার
broadcastCustomer(customer)              → গ্রাহক ডেটা সম্প্রচার
broadcastUserUpdate(username)            → ব্যবহারকারী আপডেট সম্প্রচার
```

**নেটওয়ার্ক ইনবাউন্ড (Inbound) হ্যান্ডেল:**

নেটওয়ার্ক থেকে আসা আপডেট প্রক্রিয়া করে এবং UI রিফ্রেশ করে
```
onNetworkUpdate(productId, newQty)       → একক স্টক আপডেট
onBatchNetworkUpdate(updates)            → একাধিক স্টক আপডেট
onFullProductSync(products)              → সম্পূর্ণ পণ্য ক্যাটালগ
onFullSalesSync(sales)                   → সম্পূর্ণ বিক্রয় ইতিহাস
onFullCustomerSync(customers)            → সম্পূর্ণ গ্রাহক তালিকা
onFullUserSync(users)                    → সম্পূর্ণ ব্যবহারকারী তালিকা
onCustomerUpdateFromNetwork(data)        → একক গ্রাহক আপডেট
onUserUpdateFromNetwork(data)            → একক ব্যবহারকারী আপডেট
onNewProductFromNetwork(data)            → নতুন পণ্য নোটিফিকেশন
onDeleteProductFromNetwork(productId)    → পণ্য সরান নোটিফিকেশন
onSaleRecordFromNetwork(data)            → বিক্রয় রেকর্ড নোটিফিকেশন
```

**সম্পূর্ণ ডেটা স্ন্যাপশট (ক্লায়েন্ট কানেক্ট করার সময়):**
```
getFullProductData()                     → সম্পূর্ণ স্টক ক্যাটালগ
getFullSalesData()                       → সম্পূর্ণ বিক্রয় ইতিহাস
getFullCustomerData()                    → সম্পূর্ণ গ্রাহক তালিকা
getFullUserData()                        → সম্পূর্ণ ব্যবহারকারী তালিকা
```

---

### 📡 নেটওয়ার্ক কোডেক (Serialization/Deserialization)

#### **NetworkCodec.java** (146 লাইন)
**কাজ:** নেটওয়ার্ক ট্রান্সমিশনের জন্য ডেটা এনকোড/ডিকোড করে

**এনকোডিং ফর্ম্যাট:**
```
টেক্সট:           Base64(text)
স্টকআইটেম:      Base64(id);Base64(name);Base64(cat);Base64(subcat);qty;price;Base64(image)
বিক্রয়রেকর্ড:     Base64(id);Base64(ts);Base64(seller);Base64(source);qty;amount;Base64(summary);Base64(json);...
গ্রাহক:           Base64(id);Base64(name);Base64(phone);Base64(email);Base64(addr);Base64(type);balance
রেকর্ডগুলি:       record1|record2|record3|...  (পাইপ দ্বারা বিভক্ত)
```

**ফাংশন:**
```
encodeText(text)                    → টেক্সট Base64 এনকোড করুন
decodeText(encoded)                 → Base64 ডিকোড করুন
encodeStockItem(item)               → পণ্য এনকোড করুন
decodeStockItem(payload)            → পণ্য ডিকোড করুন
encodeSaleRecord(sale)              → বিক্রয় রেকর্ড এনকোড করুন
decodeSaleRecord(payload)           → বিক্রয় রেকর্ড ডিকোড করুন
encodeCustomer(customer)            → গ্রাহক এনকোড করুন
decodeCustomer(payload)             → গ্রাহক ডিকোড করুন
joinRecords(collection)             → রেকর্ড পাইপ দ্বারা যোগ করুন
splitRecords(payload)               → পাইপ দ্বারা রেকর্ড বিভক্ত করুন
```

---

### 🖧 টিসিপি সার্ভার

#### **StockServer.java** (231 লাইন)
**কাজ:** TCP সার্ভার যা একাধিক ক্লায়েন্ট সংযোগ গ্রহণ করে

**অপারেশন:**
```
start()                             → সার্ভার শুরু করুন (পোর্টে শোনুন)
stop()                              → সার্ভার বন্ধ করুন
getClientCount()                    → সংযুক্ত ক্লায়েন্ট সংখ্যা
```

**সম্প্রচার ফাংশন:**
```
broadcastToAllClients(productId, qty)   → সব ক্লায়েন্টে স্টক আপডেট
broadcastProductToAllClients(item)      → সব ক্লায়েন্টে নতুন পণ্য
broadcastDeleteProductToAllClients(id)  → সব ক্লায়েন্টে পণ্য সরান
broadcastSaleToAllClients(sale)         → সব ক্লায়েন্টে বিক্রয়
broadcastCustomerToAllClients(customer) → সব ক্লায়েন্টে গ্রাহক
broadcastUserToAllClients(userData)     → সব ক্লায়েন্টে ব্যবহারকারী
```

**প্রোটোকল ম্যাসেজ গ্রহণ করে:**
```
STOCK_UPDATE:productId:newQty       → স্টক আপডেট
GET_ALL                             → সম্পূর্ণ ডেটা অনুরোধ
PRODUCT_UPSERT:encodedItem          → নতুন/আপডেট পণ্য
PRODUCT_DELETE:productId            → পণ্য সরান
SALE_RECORD:encodedSale             → বিক্রয় রেকর্ড
CUSTOMER_UPSERT:encodedCustomer     → গ্রাহক আপডেট
USER_UPSERT:encodedUser             → ব্যবহারকারী আপডেট
```

**ক্লায়েন্ট কানেক্ট করলে পাঠায়:**
```
PRODUCT_ALL:...                     → সম্পূর্ণ পণ্য ক্যাটালগ
SALES_ALL:...                       → সম্পূর্ণ বিক্রয় ইতিহাস
CUSTOMERS_ALL:...                   → সম্পূর্ণ গ্রাহক তালিকা
USERS_ALL:...                       → সম্পূর্ণ ব্যবহারকারী তালিকা
```

**ইনার ক্লাস: ClientHandler (Thread)**
- প্রতিটি সংযুক্ত ক্লায়েন্টের জন্য একটি থ্রেড
- ক্লায়েন্ট থেকে বার্তা পড়ে এবং প্রক্রিয়া করে
- অন্যান্য ক্লায়েন্টে সম্প্রচার বার্তা পাঠায়

---

### 🔗 টিসিপি ক্লায়েন্ট

#### **StockClient.java** (192 লাইন)
**কাজ:** টিসিপি ক্লায়েন্ট যা সার্ভারে সংযোগ করে এবং শোনে

**অপারেশন:**
```
connect()                           → সার্ভারে সংযোগ করুন (blocking)
disconnect()                        → সংযোগ বন্ধ করুন
isConnected()                       → সংযুক্ত আছি?
```

**সার্ভারে পাঠানো ফাংশন:**
```
sendStockUpdate(productId, qty)    → স্টক আপডেট পাঠান
requestAllStock()                  → সম্পূর্ণ ডেটা অনুরোধ করুন
sendNewProduct(item)               → নতুন পণ্য পাঠান
sendDeleteProduct(productId)       → পণ্য সরান পাঠান
sendSaleRecord(sale)               → বিক্রয় রেকর্ড পাঠান
sendCustomerUpdate(customer)       → গ্রাহক আপডেট পাঠান
sendUserUpdate(userData)           → ব্যবহারকারী আপডেট পাঠান
```

**সার্ভার থেকে গ্রহণ করা প্রোটোকল:**
```
STOCK_UPDATE:productId:qty
PRODUCT_ALL:...
SALES_ALL:...
CUSTOMERS_ALL:...
USERS_ALL:...
PRODUCT_UPSERT:...
PRODUCT_DELETE:...
SALE_RECORD:...
CUSTOMER_UPSERT:...
USER_UPSERT:...
```

**ব্যাকগ্রাউন্ড থ্রেড (Listener):**
- সার্ভার থেকে বার্তা ক্রমাগত পড়ে
- `NetworkManager.onNetworkUpdate()` ইত্যাদি কল করে
- সংযোগ বন্ধ হলে `NetworkManager.onClientDisconnected()` কল করে

---

---

## 🖥️ ব্যবহারকারী ইন্টারফেস (Controllers)

### প্রধান কন্ট্রোলার

| ফাইল | দায়িত্ব |
|------|----------|
| **HelloController** | লগইন পেজ UI |
| **SignUpController** | সাইনআপ ফর্ম UI |
| **ForgotPasswordController** | পাসওয়ার্ড রিসেট UI |
| **HomeController** | অ্যাডমিন ড্যাশবোর্ড (বিক্রয়, স্টক রিপোর্ট) |
| **CustomerHomeController** | গ্রাহক হোম পেজ (প্রোডাক্ট ব্রাউজিং) |
| **ProductListController** | পণ্য তালিকা (সব ক্যাটেগরি) |
| **BeautyController** | বিউটি পণ্য ক্যাটেগরি |
| **ElectronicsController** | ইলেকট্রনিক্স ক্যাটেগরি |
| **FashionController** | ফ্যাশন ক্যাটেগরি |
| **HomeLivingController** | হোম & লিভিং ক্যাটেগরি |
| **CartController** | শপিং কার্ট UI |
| **CheckoutController** | চেকআউট ও বিল তৈরি |
| **BillDetailsController** | বিল বিবরণ প্রদর্শন |
| **BillReport** | বিল প্রিন্ট বা রপ্তানি |
| **StockController** | স্টক ম্যানেজমেন্ট (অ্যাডমিন) |
| **RestockController** | নতুন স্টক যোগ করুন |
| **SalesController** | বিক্রয় ইতিহাস রিপোর্ট |
| **ReportsController** | ব্যাপক ব্যবসায়িক রিপোর্ট |
| **CustomerController** | গ্রাহক ম্যানেজমেন্ট UI |
| **AboutController** | অ্যাপ্লিকেশন সম্পর্কে পেজ |

---

---

## 💾 ডেটা পার্সিস্ট্যান্স - সম্পূর্ণ মানচিত্র

### স্টোরেজ লোকেশন

| ডেটা | ফাইল পাথ | ফর্ম্যাট | এনকোডিং |
|-----|---------|--------|--------|
| **ব্যবহারকারী** | `%APPDATA%/DemoJavaFX/users.properties` | `username=v2:password\|personalData\|role` | Base64 |
| **পণ্য স্টক** | `~/.shopapp_stock.dat` | `id\|name\|cat\|subcat\|qty\|price\|image` | টেক্সট |
| **গ্রাহক** | `~/.shopapp_customers.dat` | একক Base64 লাইন প্রতি গ্রাহক | Base64 |
| **বিক্রয়** | `~/.shopapp_sales.dat` | একক Base64 লাইন প্রতি বিক্রয় | Base64 |
| **পুনঃসঞ্চয়** | `~/.shopapp_restock.dat` | একক Base64 লাইন প্রতি রেকর্ড | Base64 |

### স্বয়ংক্রিয় সিঙ্ক্রোনাইজেশন

- **নেটওয়ার্ক সক্রিয়:** সব পরিবর্তন মুহূর্তে সার্ভার/ক্লায়েন্টে সম্প্রচার হয়
- **ফাইল ওয়াচার:** বাহ্যিক ফাইল পরিবর্তন সনাক্ত করে এবং পুনরায় লোড করে
- **টাইমস্ট্যাম্প ট্র্যাকিং:** শেষ পরিবর্তন সময় মনে রাখে
- **Atomic লেখা:** Temp ফাইলে লিখে এবং তারপর সরান (Crash সুরক্ষা)

---

---

## 🔄 ডেটা প্রবাহ উদাহরণ

### উদাহরণ 1: SERVER মোডে স্টক আপডেট

```
1. ব্যবহারকারী CLIENT 1 এ "iPhone 15" x2 কিনে
2. CheckoutController → StockManager.updateStock("PROD_016", 23)
3. StockManager → ফাইল সংরক্ষণ + শ্রোতাদের জানান
4. CheckoutController → NetworkManager.broadcastStockUpdate("PROD_016", 23)
5. NetworkManager (SERVER) → StockServer.broadcastToAllClients()
6. SERVER কে ছাড়া সব CLIENT দের স্টকআপডেট পাঠায়
7. CLIENT 1, 2 → StockClient.handleMessage() 
8. → NetworkManager.onNetworkUpdate() 
9. → StockManager.updateStock()+ফাইল সংরক্ষণ 
10. → UI রিফ্রেশ (currentListener.onStockUpdated)
```

### উদাহরণ 2: CLIENT মোড এ নতুন গ্রাহক সংযোগ

```
1. CLIENT সার্ভারে সংযোগ করে
2. StockClient.connect() → গ্রাহক থ্রেড শুরু করে
3. SERVER → সম্পূর্ণ স্ন্যাপশট পাঠায়:
   - PRODUCT_ALL:...
   - SALES_ALL:...
   - CUSTOMERS_ALL:...
   - USERS_ALL:...
4. CLIENT → NetworkCodec ডিকোড করে
5. → StockManager.replaceAllStock(...)
   → SalesManager.replaceAllSales(...)
   → CustomerManager.replaceAllCustomers(...)
   → UserStore.importUserFromNetwork(...)
6. → LocalFile গুলোতে সংরক্ষণ করে
7. → UI রিফ্রেশ (currentListener callbacks)
```

### উদাহরণ 3: OFFLINE মোড এ কেনাকাটা

```
1. CartController → CheckoutController
2. CheckoutController → NetworkManager.buildSaleRecord()
3. NetworkManager → SalesManager.recordSale()
4. SalesManager → ~/.shopapp_sales.dat ফাইলে লিখে
5. StockManager.updateStock() → ~/.shopapp_stock.dat আপডেট
6. কোন নেটওয়ার্ক সম্প্রচার নেই (OFFLINE)
7. লোকাল ড্যাশবোর্ড শুধুমাত্র লোকাল ডেটা দেখায়
```

---

---

## 🎯 দ্রুত রেফারেন্স

### সাধারণ অপারেশন

| কাজ | ক্লাস | মেথড |
|-----|------|--------|
| নতুন ব্যবহারকারী তৈরি করুন | UserStore | `createUser(name, pwd, data, role)` |
| ব্যবহারকারী লগইন | UserStore | `validateLogin(name, pwd)` |
| পাসওয়ার্ড রিসেট করুন | UserStore | `resetPassword(name, answer, newPwd)` |
| কার্টে আইটেম যোগ করুন | Cart | `addItem(cartItem)` |
| চেকআউট সম্পন্ন করুন | SalesManager | `recordSale(saleRecord)` |
| স্টক মান দেখুন | StockManager | `getStockItem(productId)` |
| স্টক আপডেট করুন | StockManager | `updateStock(productId, qty)` |
| নতুন গ্রাহক যোগ করুন | CustomerManager | `saveCustomer(customer)` |
| সার্ভার শুরু করুন | NetworkManager | `startAsServer(port)` |
| সার্ভারে সংযোগ করুন | NetworkManager | `connectToServer(host, port)` |
| OFFLINE হন | NetworkManager | `setOffline()` |

### ডিবাগিং টিপস

1. **নেটওয়ার্ক ইস্যু:**
   - Console লগ: `[SERVER]`, `[CLIENT]`, `[NetworkManager]`
   - ফাইয়ারওয়াল চেক করুন
   - পোর্ট ডিফল্ট ৫০০০

2. **ডেটা ইস্যু:**
   - ফাইল লোকেশন: `~/.shopapp_*` এবং Windows APPDATA
   - ম্যানুয়াল সিঙ্ক: "GET_ALL" কমান্ড পাঠান

3. **অ্যাকাউন্ট ইস্যু:**
   - ডিফল্ট অ্যাকাউন্ট: admin/admin
   - ব্যবহারকারী ডেটা Base64 এনকোডেড (সম্পূর্ণ এনক্রিপশন নয়)

---

---

## 📝 নোট

✅ **নেটওয়ার্ক এন্ড-টু-এন্ড ডেটা সিঙ্ক:** স্টক, বিক্রয়, গ্রাহক, ব্যবহারকারী
✅ **Thread-Safe:** সমস্ত প্রধান ডেটা স্টোর
✅ **Crash পুনরুদ্ধার:** Atomic ফাইল অপারেশন
✅ **লিগ্যাসি সমর্থন:** পুরানো ব্যবহারকারী ডেটা স্বয়ংক্রিয় মাইগ্রেশন

---

**শেষ আপডেট:** April 2026

