# 📋 ফাইল কাজ ম্যাপিং - কোন ফাইল কী করে এবং কীভাবে করে

---

## 1️⃣ ব্যবহারকারী ম্যানেজমেন্ট (USER MANAGEMENT)

### কাজ: নতুন ব্যবহারকারী তৈরি করা
```
কাজটি করে:     HelloController.java  ✓ (UI থেকে ডেটা নেয়)
যা ব্যবহার করে:  UserStore.java ✓ (createUser() মেথড কল করে)
ডেটা সংরক্ষণ:   %APPDATA%/DemoJavaFX/users.properties ✓ (Base64 এনকোডেড)
```
**প্রক্রিয়া:**
```
User Input (SignUpController.java)
    ↓
UserStore.createUser(username, password, personalData, role)
    ↓
ConcurrentHashMap এ ডেটা রাখে
    ↓
ফাইলে সংরক্ষণ (saveToDiskSafeLocked)
    ↓
Base64 এনকোডিং করে লেখে
```

---

### কাজ: ব্যবহারকারী লগইন যাচাই করা
```
কাজটি করে:     HelloController.java ✓ (লগইন ফর্ম UI)
যা ব্যবহার করে:  UserStore.java ✓ (validateLogin() মেথড)
যা চেক করে:     ডিস্ক থেকে লোড করা ডেটা (refreshFromDiskIfNeeded)
সেশন সংরক্ষণ:  Session.java ✓ (login(username) কল করে)
নেভিগেশন:     HomeController.java বা CustomerHomeController.java
```
**প্রক্রিয়া:**
```
লগইন পেজে ইউজারনেম/পাসওয়ার্ড এন্টার
    ↓
UserStore.validateLogin(name, pwd) কল
    ↓
ConcurrentHashMap থেকে খুঁজে বের করে
    ↓
পাসওয়ার্ড ম্যাচ করে (সরাসরি তুলনা)
    ↓
true/false রিটার্ন করে
    ↓
Session.login(username) কল করে ভোলাটাইল ভেরিয়েবল সেট করে
    ↓
HomeController বা CustomerHomeController এ রিডাইরেক্ট
```

---

### কাজ: পাসওয়ার্ড রিসেট করা
```
কাজটি করে:     ForgotPasswordController.java ✓ (UI)
যা ব্যবহার করে:  UserStore.java ✓ (verifyRecoveryData + resetPassword)
ভেরিফিকেশন:    ব্যক্তিগত প্রশ্ন উত্তর (personalData ফিল্ড)
নতুন পাসওয়ার্ড: ফাইলে সংরক্ষণ (saveToDiskSafeLocked)
```
**প্রক্রিয়া:**
```
ইউজার ইউজারনেম এন্টার করে
    ↓
নিরাপত্তা প্রশ্নের উত্তর এন্টার করে
    ↓
UserStore.verifyRecoveryData(username, answer) চেক করে
    ↓
মিলে গেলে: নতুন পাসওয়ার্ড এন্টার করতে দেয়
    ↓
UserStore.resetPassword(username, answer, newPwd) কল
    ↓
Map আপডেট করে + ফাইলে সংরক্ষণ
```

---

### কাজ: নেটওয়ার্কে ব্যবহারকারী সিঙ্ক করা
```
কাজটি করে:     NetworkManager.java ✓ (broadcastUserUpdate মেথড)
যা পাঠায়:      StockServer.java / StockClient.java ✓
যা এনকোড করে:  NetworkCodec.java ✓ (encodeText, Base64)
যা গ্রহণ করে:   StockClient.java / StockServer.java ✓
যা ডিকোড করে:  NetworkCodec.java ✓ (decodeText)
যা ইমপোর্ট করে: UserStore.importUserFromNetwork() ✓
যা সংরক্ষণ করে: ফাইলে সংরক্ষণ (saveToDiskSafeLocked)
```
**প্রক্রিয়া:**
```
LocalUser পরিবর্তন হয় (createUser, resetPassword)
    ↓
NetworkManager.broadcastUserUpdate(username) কল
    ↓
UserStore.getAllSerializedUsers() সব ইউজার পায়
    ↓
NetworkCodec.encodeText() দিয়ে Base64 এনকোড করে
    ↓
SERVER মোডে: StockServer.broadcastUserToAllClients()
CLIENT মোডে: StockClient.sendUserUpdate()
    ↓
দূরবর্তী মেশিনে পৌঁছায় (TCP)
    ↓
StockClient.handleMessage("USER_UPSERT:...") বা
StockServer.ClientHandler.handleMessage("USER_UPSERT:...")
    ↓
NetworkManager.onUserUpdateFromNetwork(data) কল
    ↓
NetworkCodec.decodeText() দিয়ে ডিকোড করে
    ↓
UserStore.importUserFromNetwork() ইমপোর্ট করে
    ↓
Map আপডেট + ফাইলে সংরক্ষণ
```

---

## 2️⃣ পণ্য ম্যানেজমেন্ট (PRODUCT MANAGEMENT)

### কাজ: পণ্যের তালিকা লোড করা
```
কাজটি করে:     StockManager.java ✓ (initialize মেথড, static block)
যা লোড করে:     ~/.shopapp_stock.dat ফাইল
যা ডিফল্ট করে:  ডিফল্ট পণ্য লিস্ট (Beauty, Electronics, Fashion, Home)
ডেটা স্ট্রাকচার: Map<String, StockItem> stockData
প্রদর্শনে ব্যবহার করে:
  - ProductListController.java
  - BeautyController.java
  - ElectronicsController.java
  - FashionController.java
  - HomeLivingController.java
```
**প্রক্রিয়া:**
```
অ্যাপ স্টার্ট
    ↓
StockManager static block execute
    ↓
initialize() মেথড কল
    ↓
ফাইল আছে? → loadFromDisk()
    ↓
প্রতিটি লাইন পার্স করে
    ↓
StockItem অবজেক্ট তৈরি করে
    ↓
Map এ সংরক্ষণ করে
    ↓
প্রয়োজনে ডিফল্ট পণ্য যোগ করে (addDefault)
    ↓
ইমেজ পাথ ফিক্স করে
```

---

### কাজ: প্রোডাক্ট UI তে দেখানো
```
কাজটি করে:     ProductListController.java ✓ (UI initialize করে)
যা ব্যবহার করে:  StockManager.getAllStockItems() ✓
যা পায়:         List<StockItem> সব পণ্য
যা দেখায়:       GridPane/ListView এ ছবি সহ
প্রতিটি পণ্য:   
  - productName
  - category
  - price
  - imagePath → resources থেকে ইমেজ লোড করে
  - quantity (স্টক)
ক্লিক করলে:    ProductDetailsController.java তে যায়
```

---

### কাজ: স্টক কোয়ান্টিটি আপডেট করা (কেনাকাটার সময়)
```
কাজটি করে:     CheckoutController.java ✓ (চেকআউট প্রসেস)
যা কল করে:      StockManager.updateStock(productId, newQty) ✓
যা আপডেট করে:  Map এ তাৎক্ষণিক আপডেট
যা সংরক্ষণ করে: ফাইলে লিখে (~/.shopapp_stock.dat)
যা সম্প্রচার করে: NetworkManager.broadcastStockUpdate() ✓
যা নেটওয়ার্কে পাঠায়: StockServer বা StockClient ✓
```
**প্রক্রিয়া:**
```
Customer কার্টে পণ্য যোগ করে
    ↓
CheckoutController → Checkout ফর্ম
    ↓
বিল তৈরি হয় (SalesManager.recordSale)
    ↓
প্রতিটি পণ্যের জন্য:
    ↓
StockManager.updateStock(productId, newQty)
    ↓
Map আপডেট: stockData.put(productId, newItem)
    ↓
ফাইলে তাৎক্ষণিক সংরক্ষণ
    ↓
শ্রোতাদের জানায় (externalChangeListeners)
    ↓
OFFLINE মোড? → শুধু লোকাল আপডেট
SERVER/CLIENT মোড? → NetworkManager.broadcastStockUpdate() ✓
    ↓
দূরবর্তী মেশিনে পৌঁছায় TCP দিয়ে
    ↓
অন্যান্য মেশিনের UI আপডেট হয়
```

---

### কাজ: নতুন পণ্য যোগ করা (অ্যাডমিন)
```
কাজটি করে:     StockController.java ✓ (UI)
যা কল করে:      StockManager.upsertStockItem(item) ✓
যা সংরক্ষণ করে: ফাইলে লিখে
যা সম্প্রচার করে: NetworkManager.broadcastNewProduct(item) ✓
যা এনকোড করে:  NetworkCodec.encodeStockItem(item) ✓
যা পাঠায়:      StockServer.broadcastProductToAllClients() ✓
যা গ্রহণ করে:   StockClient সব ক্লায়েন্টে
```

---

## 3️⃣ শপিং কার্ট (SHOPPING CART)

### কাজ: কার্টে পণ্য যোগ করা
```
কাজটি করে:     ProductListController.java ✓ (পণ্যের পাশে "Add to Cart" বাটন)
যা কল করে:      Cart.addItem(cartItem) ✓ (স্ট্যাটিক মেথড)
ডেটা স্ট্রাকচার: 
  - cartItems List<CartItem>
  - lastVisitedPage String (কোথা থেকে এসেছি)
যা স্টোর করে:   ইন-মেমরি (কোন ফাইল নেই)
```

---

### কাজ: কার্ট দেখানো
```
কাজটি করে:     CartController.java ✓ (UI)
যা পায়:         Cart.getAllItems() ✓
যা দেখায়:       
  - পণ্য তালিকা
  - প্রতিটি পণ্যের মূল্য ও পরিমাণ
  - মোট টাকা = Cart.getTotalAmount()
  - মোট পণ্য = Cart.getTotalQuantity()
ফাংশন:
  - Remove item → Cart.removeItem(productId)
  - Update qty → Cart.updateQuantity(productId, qty)
  - Checkout → CheckoutController এ যায়
  - Continue shopping → Cart.getLastVisitedPage() ব্যবহার করে ফিরে যায়
```

---

## 4️⃣ বিক্রয় প্রক্রিয়া (SALES/CHECKOUT)

### কাজ: চেকআউট সম্পন্ন করা
```
কাজটি করে:     CheckoutController.java ✓ (Checkout UI)
যা গ্রহণ করে:
  - Cart.getAllItems()
  - Customer তথ্য (নাম, ফোন, ইমেইল, ঠিকানা)
  - Session.getCurrentUser() (বিক্রেতার নাম)
যা তৈরি করে:  SaleRecord অবজেক্ট (NetworkManager.buildSaleRecord)
যা রেকর্ড করে: SalesManager.recordSale(saleRecord)
যা সংরক্ষণ করে: ~/.shopapp_sales.dat ফাইলে
যা আপডেট করে: StockManager.updateStock() প্রতিটি পণ্যের
যা সম্প্রচার করে: 
  - NetworkManager.broadcastSaleRecord(sale)
  - NetworkManager.broadcastStockUpdate() সব পণ্যের জন্য
যা গ্রাহক যোগ করে: CustomerManager.saveCustomer() (বিক্রয়ের জন্য)
যা প্রদর্শন করে: BillDetailsController.java (বিল দেখায়)
```
**বিস্তারিত প্রক্রিয়া:**
```
CheckoutController.onCheckoutClicked()
    ↓
Cart.getAllItems() → সব আইটেম পায়
    ↓
Customer তথ্য ইনপুট নেয় (ডায়ালগ বা ফর্ম)
    ↓
NetworkManager.buildSaleRecord() কল করে
    ↓
SaleRecord তৈরি হয়:
    - saleId = "BILL-" + System.currentTimeMillis()
    - timestamp = LocalDateTime.now()
    - soldBy = Session.getCurrentUser() বা "Guest"
    - sourceNode = "hostname-MODE" (e.g., "PC1-SERVER")
    - itemsJson = পণ্যগুলির JSON
    - customerName, customerPhone, customerEmail, customerAddress
    ↓
SalesManager.recordSale(saleRecord) কল
    ↓
SalesManager এ:
    - সেলস লিস্টে যোগ
    - ~/shopapp_sales.dat ফাইলে লেখা (Base64)
    - SalesTracker এ রেজিস্টার করা
    ↓
প্রতিটি পণ্যের জন্য:
    StockManager.updateStock(productId, newQty)
    ফাইল সংরক্ষণ
    ↓
নেটওয়ার্ক MODE?
    OFFLINE → কিছু না
    SERVER → StockServer.broadcastToAllClients() → সব ক্লায়েন্টে
    CLIENT → StockClient.sendStockUpdate() → সার্ভারে
    ↓
গ্রাহক তথ্য সংরক্ষণ:
    CustomerManager.saveCustomer(customer)
    ↓
নেটওয়ার্ক মোডে:
    NetworkManager.broadcastCustomer(customer)
    ↓
বিল প্রদর্শন:
    BillDetailsController তে সেলস রেকর্ড দেখায়
    ↓
প্রিন্ট/ডাউনলোড অপশন (BillReport.java)
```

---

### কাজ: বিক্রয় রেকর্ড সংরক্ষণ
```
কাজটি করে:     SalesManager.java ✓ (recordSale মেথড)
যা পায়:         SaleRecord অবজেক্ট
যা স্টোর করে:   Map<String, SaleRecord> সেলসডেটা
যা লেখে:        ~/shopapp_sales.dat ফাইলে
ফর্ম্যাট:       প্রতি লাইনে একটি Base64 এনকোডেড রেকর্ড
এনকোডিং করে:  NetworkCodec.encodeSaleRecord(sale)
সূত্র:         Base64(id);Base64(ts);Base64(seller);...;Base64(address)
```

---

### কাজ: বিক্রয় ড্যাশবোর্ড আপডেট করা
```
কাজটি করে:     HomeController.java ✓ (অ্যাডমিন ড্যাশবোর্ড)
যা পায়:         
  - SalesTracker.getTotalRevenue()
  - SalesTracker.getTotalSalesCount()
  - SalesTracker.getRevenueToday()
  - SalesTracker.getSalesCountToday()
যা ট্র্যাক করে:  SalesTracker.java ✓ (ইন-মেমরি ট্র্যাকার)
যা আপডেট হয়:
  - স্থানীয় বিক্রয়ে: SalesManager.recordSale কল করার সময়
  - নেটওয়ার্ক বিক্রয়ে: NetworkManager.onSaleRecordFromNetwork কল করার সময়
              → SalesTracker.addNetworkSale(sale) কল হয়
              → UI রিফ্রেশ (HomeController.refreshDashboard)
```

---

## 5️⃣ গ্রাহক ব্যবস্থাপনা (CUSTOMER MANAGEMENT)

### কাজ: গ্রাহক সংরক্ষণ করা
```
কাজটি করে:     CustomerController.java ✓ (UI)
যা কল করে:      CustomerManager.saveCustomer(customer) ✓
যা স্টোর করে:   Map<String, Customer> customerData
যা লেখে:        ~/.shopapp_customers.dat ফাইলে
ফর্ম্যাট:        প্রতি লাইনে একটি Base64 এনকোডেড Customer
এনকোডিং করে:  NetworkCodec.encodeCustomer(customer)
সূত্র:         Base64(id);Base64(name);Base64(phone);...;balance
```

---

### কাজ: নেটওয়ার্কে গ্রাহক সিঙ্ক করা
```
কাজটি করে:     NetworkManager.java ✓ (broadcastCustomer মেথড)
যা পাঠায়:      StockServer / StockClient ✓
যা এনকোড করে:  NetworkCodec.encodeCustomer() ✓
যা গ্রহণ করে:   StockClient / StockServer ✓
যা ডিকোড করে:  NetworkCodec.decodeCustomer() ✓
যা ইমপোর্ট করে: CustomerManager.saveCustomer() ✓
যা সংরক্ষণ করে: ফাইলে লিখে
```

---

## 6️⃣ পুনঃসঞ্চয় ব্যবস্থাপনা (RESTOCK MANAGEMENT)

### কাজ: নতুন স্টক যোগ করা
```
কাজটি করে:     RestockController.java ✓ (UI)
যা যোগ করে:     
  - RestockManager.recordRestock(record)
  - StockManager.updateStock(productId, newQty)
যা সংরক্ষণ করে:
  - ~/shopapp_restock.dat (Restock ইতিহাস)
  - ~/shopapp_stock.dat (পণ্য কোয়ান্টিটি)
যা সম্প্রচার করে: NetworkManager.broadcastStockUpdate()
```

---

## 7️⃣ নেটওয়ার্কিং সিস্টেম (NETWORKING)

### কাজ: SERVER মোড শুরু করা
```
কাজটি করে:     HomeController.java ✓ (অ্যাডমিন পেজের বাটন)
যা কল করে:      NetworkManager.startAsServer(port) ✓
যা তৈরি করে:    StockServer অবজেক্ট
যা শুরু করে:    ServerSocket পোর্টে শোনা শুরু করে
যা স্বীকার করে:  ক্লায়েন্ট সংযোগ (acceptClients থ্রেড)
যা পাঠায়:       সম্পূর্ণ ডেটা স্ন্যাপশট নতুন ক্লায়েন্টে:
  - getFullProductData() → StockManager
  - getFullSalesData() → SalesManager
  - getFullCustomerData() → CustomerManager
  - getFullUserData() → UserStore
যা প্রসেস করে:  ClientHandler (প্রতিটি ক্লায়েন্টের জন্য থ্রেড)
যা সম্প্রচার করে: সব পরিবর্তন সব ক্লায়েন্টে
```

---

### কাজ: CLIENT মোড সংযোগ করা
```
কাজটি করে:     HomeController.java ✓ (সংযোগ ফর্ম ডায়ালগ)
যা কল করে:      NetworkManager.connectToServer(host, port) ✓
যা তৈরি করে:    StockClient অবজেক্ট
যা সংযোগ করে:   Socket দিয়ে সার্ভারে
যা শ্রবণ করে:    সার্ভার থেকে বার্তা (listen থ্রেড)
যা গ্রহণ করে:    সম্পূর্ণ ডেটা স্ন্যাপশট:
  - PRODUCT_ALL:... → StockManager.replaceAllStock()
  - SALES_ALL:... → SalesManager.replaceAllSales()
  - CUSTOMERS_ALL:... → CustomerManager.replaceAllCustomers()
  - USERS_ALL:... → UserStore.importUserFromNetwork()
যা লেখে:        স্থানীয় ফাইলে তাৎক্ষণিক
যা রিফ্রেশ করে:  UI (currentListener callbacks)
```

---

### কাজ: স্টক আপডেট নেটওয়ার্কে সম্প্রচার করা
```
প্রক্রিয়া চেইন:
    
CheckoutController (পণ্য বিক্রয়)
    ↓
StockManager.updateStock(productId, qty)
    ↓ (ফাইল সংরক্ষণ + শ্রোতা বিজ্ঞপ্তি)
    ↓
CheckoutController → NetworkManager.broadcastStockUpdate()
    ↓
NetworkManager (Singleton)
    ├─ OFFLINE মোড → কিছু না
    ├─ SERVER মোড → StockServer.broadcastToAllClients()
    │   ├─ সব ClientHandler কে "STOCK_UPDATE:productId:qty" পাঠায়
    │   └─ PrintWriter.println() দিয়ে TCP এ পাঠায়
    └─ CLIENT মোড → StockClient.sendStockUpdate()
        └─ PrintWriter.println() দিয়ে সার্ভারে পাঠায়

দূরবর্তী মেশিনে গ্রহণ:
    TCP Socket
    ↓ (লাইন রিসিভ করে)
    ↓
StockClient.listen() / StockServer.ClientHandler.run()
    ↓ (BufferedReader.readLine)
    ↓
handleMessage("STOCK_UPDATE:...")
    ↓
NetworkManager.onNetworkUpdate(productId, newQty)
    ↓ (StockManager এবং UI আপডেট)
    ↓
StockManager.updateStock() → ফাইলে লেখা
    ↓
currentListener.onStockUpdated() → UI রিফ্রেশ
```

---

### কাজ: সম্পূর্ণ ক্যাটালগ সিঙ্ক করা (নতুন ক্লায়েন্ট সংযোগ)
```
CLIENT সংযোগ করে
    ↓
StockClient.connect() → সার্ভারে টিসিপি কানেকশন
    ↓
StockServer.ClientHandler.run() 
    ↓
SERVER পাঠায় PRODUCT_ALL:...
    ↓ (সমস্ত পণ্য Base64 এনকোডেড, | দিয়ে যুক্ত)
    ↓
নেটওয়ার্ক ট্রান্সফার (TCP)
    ↓
CLIENT এ পৌঁছায়
    ↓
StockClient.listen() → BufferedReader.readLine()
    ↓
handleMessage("PRODUCT_ALL:...")
    ↓
NetworkCodec.splitRecords(data) → সব রেকর্ড বিভক্ত করে
    ↓
প্রতিটির জন্য NetworkCodec.decodeStockItem(record)
    ↓
List<StockItem> তৈরি করে
    ↓
NetworkManager.onFullProductSync(products)
    ↓
StockManager.replaceAllStock(products) → Map ক্লিয়ার ও নতুন ডেটা
    ↓
saveToDisk() → ~/.shopapp_stock.dat এ লেখা
    ↓
currentListener.onProductCatalogChanged() → UI রিফ্রেশ
```

---

## 8️⃣ ডেটা এনকোডিং/ডিকোডিং (NETWORK CODEC)

### কাজ: ডেটা এনকোড করা (নেটওয়ার্ক পাঠানোর জন্য)
```
কাজটি করে:     NetworkCodec.java ✓ (স্ট্যাটিক মেথড)

পণ্য এনকোড:
    StockItem → NetworkCodec.encodeStockItem()
    ↓
    প্রতিটি ফিল্ড Base64 এনকোড করে
    ↓
    সেমিকোলন দিয়ে যুক্ত করে
    ↓
    "Base64(id);Base64(name);Base64(cat);qty;price;Base64(image)"

বিক্রয় এনকোড:
    SaleRecord → NetworkCodec.encodeSaleRecord()
    ↓
    সব স্ট্রিং ফিল্ড Base64
    ↓
    সংখ্যা যেমন আছে
    ↓
    সেমিকোলন যুক্ত

গ্রাহক এনকোড:
    Customer → NetworkCodec.encodeCustomer()
    ↓
    স্ট্রিং ফিল্ড Base64
    ↓
    সংখ্যা (balance) যেমন আছে

রেকর্ড যুক্ত করা:
    List<String> → NetworkCodec.joinRecords()
    ↓
    পাইপ (|) দিয়ে যুক্ত করে
    ↓
    "record1|record2|record3|..."
```

---

### কাজ: ডেটা ডিকোড করা (নেটওয়ার্ক থেকে প্রাপ্ত)
```
কাজটি করে:     NetworkCodec.java ✓ (ডিকোড মেথড)

পণ্য ডিকোড:
    String payload → NetworkCodec.decodeStockItem()
    ↓
    সেমিকোলন দিয়ে বিভক্ত করে
    ↓
    প্রতিটি Base64 অংশ ডিকোড করে
    ↓
    StockItem অবজেক্ট তৈরি করে রিটার্ন করে

বিক্রয় ডিকোড:
    String payload → NetworkCodec.decodeSaleRecord()
    ↓
    সেমিকোলন বিভক্তি
    ↓
    সব স্ট্রিং Base64 ডিকোড
    ↓
    সংখ্যা parseInt/parseDouble
    ↓
    SaleRecord অবজেক্ট রিটার্ন

গ্রাহক ডিকোড:
    String payload → NetworkCodec.decodeCustomer()
    ↓
    সেমিকোলন বিভক্তি
    ↓
    স্ট্রিং Base64 ডিকোড
    ↓
    Customer অবজেক্ট রিটার্ন

রেকর্ড বিভক্ত করা:
    String payload → NetworkCodec.splitRecords()
    ↓
    পাইপ (|) দিয়ে split()
    ↓
    List<String> রিটার্ন করে
```

---

## 9️⃣ ডেটা পার্সিস্ট্যান্স (FILE STORAGE)

### সব ফাইল এবং তাদের দায়িত্ব:

| ফাইল | দায়িত্ব | ক্লাস | ফর্ম্যাট |
|------|----------|------|--------|
| **%APPDATA%/DemoJavaFX/users.properties** | ব্যবহারকারী সংরক্ষণ | UserStore.java | Base64 এনকোডেড (`v2:` prefix) |
| **~/.shopapp_stock.dat** | পণ্য ক্যাটালগ | StockManager.java | টেক্সট (`id\|name\|cat\|...`) |
| **~/.shopapp_customers.dat** | গ্রাহক তালিকা | CustomerManager.java | Base64 (প্রতি লাইন) |
| **~/.shopapp_sales.dat** | বিক্রয় ইতিহাস | SalesManager.java | Base64 (প্রতি লাইন) |
| **~/.shopapp_restock.dat** | পুনঃসঞ্চয় রেকর্ড | RestockManager.java | Base64 (প্রতি লাইন) |

---

## 🔟 UI কন্ট্রোলার এবং তাদের ডেটা সোর্স

| UI (FXML) | Controller | ডেটা সোর্স | ব্যবহার করে |
|-----------|-----------|----------|-----------|
| **hello-view.fxml** | HelloController | ইউজার ইনপুট | UserStore.validateLogin() |
| **signup-view.fxml** | SignUpController | ইউজার ইনপুট | UserStore.createUser() |
| **home-view.fxml** | HomeController | SalesTracker, StockManager | Dashboard মেট্রিক্স, নেটওয়ার্ক কন্ট্রোল |
| **product-list-view.fxml** | ProductListController | StockManager.getAllStockItems() | পণ্য গ্রিড প্রদর্শন |
| **beauty-view.fxml** | BeautyController | StockManager (ফিল্টার: category="Beauty") | বিউটি পণ্য শো |
| **cart-view.fxml** | CartController | Cart.getAllItems() | কার্ট আইটেম তালিকা |
| **product-details-view.fxml** | ProductDetailsController | StockManager.getStockItem(id) | একক পণ্যের বিবরণ |
| **checkout-view.fxml** | CheckoutController | Cart + গ্রাহক ইনপুট | চেকআউট প্রসেস |
| **stock-view.fxml** | StockController | StockManager | স্টক পরিচালনা UI |
| **sales-view.fxml** | SalesController | SalesManager.getAllSales() | বিক্রয় ইতিহাস টেবিল |
| **customer-view.fxml** | CustomerController | CustomerManager.getAllCustomers() | গ্রাহক তালিকা |

---

## ⭐ সম্পূর্ণ ডেটা প্রবাহ ডায়াগ্রাম

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER INTERFACE (JavaFX)                 │
│ (HelloController, ProductListController, CartController, etc.)  │
└────────────────┬────────────────────────────────────────────────┘
                 │
    ┌────────────┴───────────────┐
    │                            │
    ▼                            ▼
┌─────────────────┐      ┌──────────────────┐
│  Session.java   │      │  Cart.java       │
│ (ইউজার সেশন)  │      │ (ইন-মেমরি কার্ট)│
└─────────────────┘      └──────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│         BUSINESS LOGIC LAYER (Manager Classes)          │
│                                                         │
│  UserStore          StockManager        SalesManager   │
│  CustomerManager    RestockManager      SalesTracker   │
└─────────────┬───────────────┬─────────────┬────────────┘
              │               │             │
    ┌─────────┴───────┬───────┴──┐         │
    │                 │          │         │
    ▼                 ▼          ▼         ▼
┌────────┐      ┌──────────┐  ┌──────┐  ┌──────────┐
│ Mem.   │      │  File    │  │ File │  │  File    │
│ Cache  │      │ ~/.shop  │  │ ~/.  │  │ ~/.      │
│(Map)   │      │ app_..   │  │ shop │  │ shopapp_ │
└────────┘      └──────────┘  └──────┘  └──────────┘
    │                │           │          │
    └────────────────┴───────────┴──────────┘
             │
             ▼
    ┌─────────────────────┐
    │  NetworkCodec.java  │
    │  (এনকোড/ডিকোড)     │
    └────────┬────────────┘
             │
    ┌────────┴────────────────────────┐
    │                                 │
    ▼                                 ▼
┌──────────────┐            ┌──────────────────┐
│StockServer   │            │ StockClient      │
│(TCP সার্ভার) │◄──TCP───►│ (TCP ক্লায়েন্ট) │
└──────────────┘            └──────────────────┘
    │                                 │
    └────────────┬────────────────────┘
                 │
         (নেটওয়ার্ক)
                 │
    ┌────────────┴────────────┐
    │                         │
 ▼ ▼ (অন্যান্য মেশিন)  ▼ ▼ (অন্যান্য মেশিন)
```

---

## 🎯 দ্রুত রেফারেন্স: ফাংশন → ফাইল → ডেটা

```
নতুন ব্যবহারকারী তৈরি করতে চাই
├─ SignUpController.java (UI থেকে input নেয়)
├─ UserStore.createUser() (validate + সংরক্ষণ করে)
├─ ConcurrentHashMap (in-memory)
├─ saveToDiskSafeLocked() (ফাইলে লেখা)
└─ %APPDATA%/DemoJavaFX/users.properties (সংরক্ষিত)

পণ্য বিক্রি করতে চাই
├─ ProductListController.java (পণ্য শো করে)
├─ CartController.java (কার্ট শো করে)
├─ CheckoutController.java (চেকআউট ফর্ম)
├─ NetworkManager.buildSaleRecord() (বিল তৈরি)
├─ SalesManager.recordSale() (রেকর্ড করে)
├─ ~/.shopapp_sales.dat (সেলস ফাইল)
├─ StockManager.updateStock() (কোয়ান্টিটি কমায়)
├─ ~/.shopapp_stock.dat (স্টক ফাইল)
├─ NetworkManager.broadcastStockUpdate() (নেটে পাঠায়)
├─ StockServer/StockClient (নেটওয়ার্ক)
└─ দূরবর্তী মেশিনে StockManager.updateStock()

গ্রাহক তৈরি/সংরক্ষণ করতে চাই
├─ CustomerController.java (UI)
├─ CustomerManager.saveCustomer() (সংরক্ষণ)
├─ ~/.shopapp_customers.dat (ফাইল)
├─ NetworkManager.broadcastCustomer() (সিঙ্ক)
└─ দূরবর্তী মেশিনে গ্রাহক আপডেট

নেটওয়ার্কে যুক্ত হতে চাই
├─ HomeController.java (নেটওয়ার্ক ডায়ালগ)
├─ NetworkManager.connectToServer(host, port) (সংযোগ)
├─ StockClient.connect() (TCP সংযোগ)
├─ StockClient.listen() (সার্ভার বার্তা শোনা)
├─ NetworkCodec (ডিকোড করা)
├─ StockManager.replaceAllStock() (ডেটা আপডেট)
└─ ~/shopapp_stock.dat (লোকাল ফাইল)
```

---

**সংক্ষেপ:**
- প্রতিটি UI (Controller) ডেটা Manager ক্লাস থেকে পায়
- প্রতিটি Manager ক্লাস ফাইল পার্সিস্ট্যান্স হ্যান্ডেল করে
- NetworkCodec এনকোড/ডিকোড করে নেটওয়ার্কে পাঠানোর জন্য
- StockServer/StockClient TCP দিয়ে নেটওয়ার্ক যোগাযোগ করে
- সব ডেটা পরিবর্তন স্বয়ংক্রিয় ফাইল সংরক্ষণ সহ ঘটে


