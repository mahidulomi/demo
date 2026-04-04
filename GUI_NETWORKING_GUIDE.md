# 🚀 NETWORK WITH GUI - সম্পূর্ণ গাইড

## 💻 নেটওয়ার্কিং সহ GUI অ্যাপ্লিকেশন চালানো

এখন আপনার কাছে **তিনটি উপায়** আছে নেটওয়ার্কিং এর সাথে অ্যাপ্লিকেশন চালানোর:

### **পদ্ধতি 1: Command-Line সার্ভার ও ক্লায়েন্ট (সবচেয়ে সহজ)**

এটি শুধু নেটওয়ার্ক টেস্টিং এর জন্য:

```bash
# টার্মিনাল 1: সার্ভার চালান
java -cp "target/classes" com.example.demo.ServerLauncher 5555

# টার্মিনাল 2: ক্লায়েন্ট 1 (নেটওয়ার্ক শুধু)
java -cp "target/classes" com.example.demo.ClientLauncher localhost 5555

# টার্মিনাল 3: ক্লায়েন্ট 2 (নেটওয়ার্ক শুধু)
java -cp "target/classes" com.example.demo.ClientLauncher localhost 5555
```

**কিন্তু এটায় কোনো GUI নেই তাই ছবি দেখা যায় না** ❌

---

### **পদ্ধতি 2: GUI ক্লায়েন্ট লঞ্চার (★ নতুন - সবচেয়ে ভালো)**

এটি একটি সুন্দর GUI দিয়ে সার্ভারে সংযোগ করে এবং তারপর শপিং অ্যাপ খোলে:

```bash
# টার্মিনাল 1: সার্ভার চালান
java -cp "target/classes" com.example.demo.ServerLauncher 5555

# টার্মিনাল 2: GUI ক্লায়েন্ট চালান
java -cp "target/classes;target/javafx-sdk-*/lib/*" \
  --module-path target/javafx-sdk-*/lib \
  --add-modules javafx.controls,javafx.fxml \
  com.example.demo.GuiClientLauncher localhost 5555
```

**এটায় আছে GUI এবং acid serum এর ছবি দেখা যায়!** ✅

---

### **পদ্ধতি 3: সরাসরি HelloApplication (শুধু অফলাইন)**

```bash
# নেটওয়ার্ক ছাড়া শুধু GUI
java -cp "target/classes;target/javafx-sdk-*/lib/*" \
  --module-path target/javafx-sdk-*/lib \
  --add-modules javafx.controls,javafx.fxml \
  com.example.demo.HelloApplication
```

---

## ✅ সুপারিশ: পদ্ধতি 2 ব্যবহার করুন

**নেটওয়ার্কিং সহ GUI দেখতে:**

```bash
# ধাপ 1: সার্ভার চালান
java -cp "target/classes" com.example.demo.ServerLauncher 5555

# ধাপ 2: GUI ক্লায়েন্ট চালান
java -cp "target/classes;target/javafx-sdk-*/lib/*" \
  --module-path target/javafx-sdk-*/lib \
  --add-modules javafx.controls,javafx.fxml \
  com.example.demo.GuiClientLauncher localhost 5555
```

---

## 🎯 এখন কি দেখবেন?

### **শপিং ইন্টারফেসে:**

✅ **Acid Serum** এর ছবি দেখা যাবে  
✅ স্টক, দাম, নাম দেখা যাবে  
✅ পণ্য কিনতে পারবেন  
✅ সব কিছু **সার্ভারে সিঙ্ক** হবে  
✅ অন্য ক্লায়েন্টে তাৎক্ষণিক আপডেট হবে

---

## 🔄 রিয়েল-টাইম সিঙ্ক কিভাবে কাজ করে?

```
GUI CLIENT 1: Acid Serum কিনলো (5 units)
        │
        └─► স্টক: 25 → 20 আপডেট
            │
            └─► SERVER এ পাঠায়
                │
                ├─► সার্ভার: 20 সংরক্ষণ
                │
                └─► সব ক্লায়েন্টকে ব্রডকাস্ট
                    │
                    ├─► GUI CLIENT 1: স্টক = 20 ✓
                    └─► GUI CLIENT 2: স্টক = 20 ✓
```

---

## 📚 ফাইল গাইড

| ফাইল | উদ্দেশ্য | GUI? |
|------|---------|------|
| `ServerLauncher.java` | নেটওয়ার্ক সার্ভার | ❌ |
| `ClientLauncher.java` | নেটওয়ার্ক ক্লায়েন্ট | ❌ |
| `GuiClientLauncher.java` | GUI + নেটওয়ার্ক | ✅ |
| `HelloApplication.java` | মূল শপিং অ্যাপ | ✅ |

---

## 🎮 পরীক্ষা করুন

### টেস্ট 1: ছবি দেখা যায় কি?

1. সার্ভার চালান
2. GuiClientLauncher চালান
3. "Connect to Server" ক্লিক করুন
4. শপিং অ্যাপ খোলে
5. **Acid Serum এর ছবি দেখা যায়?** ✅

### টেস্ট 2: রিয়েল-টাইম সিঙ্ক?

1. দুটি ক্লায়েন্ট খুলুন
2. CLIENT 1 এ acid serum কিনুন
3. CLIENT 2 এ stock তাৎক্ষণিক কমে?✅

### টেস্ট 3: নতুন পণ্য যোগ?

1. CLIENT 1 এ নতুন পণ্য যোগ করুন
2. CLIENT 2 এ তাৎক্ষণিক দেখা যায়? ✅

---

## ⚙️ সেটআপ টিপস

### Windows এ JavaFX সেটআপ

```bash
# 1. JavaFX SDK ডাউনলোড করুন (যদি না থাকে)
#    https://gluonhq.com/products/javafx/

# 2. Maven এ যোগ করুন (pom.xml এ)
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.1</version>
</dependency>

# 3. কম্পাইল করুন
mvn clean compile

# 4. চালান (JavaFX সাথে)
java -cp "target/classes;path/to/javafx-sdk/lib/*" \
  --module-path path/to/javafx-sdk/lib \
  --add-modules javafx.controls \
  com.example.demo.GuiClientLauncher localhost 5555
```

### Linux/Mac এ

```bash
java -cp "target/classes:path/to/javafx-sdk/lib/*" \
  --module-path path/to/javafx-sdk/lib \
  --add-modules javafx.controls \
  com.example.demo.GuiClientLauncher localhost 5555
```

---

## 🔧 সাধারণ সমস্যা

| সমস্যা | সমাধান |
|--------|--------|
| GUI শুরু হচ্ছে না | JavaFX SDK পাথ চেক করুন |
| সার্ভারে সংযোগ ব্যর্থ | সার্ভার চলছে কি চেক করুন |
| ছবি দেখা যাচ্ছে না | ছবির পাথ চেক করুন (resources/) |
| সিঙ্ক হচ্ছে না | নেটওয়ার্ক লাইভ কি চেক করুন |

---

## 🎉 চূড়ান্ত ফলাফল

এখন আপনার সিস্টেমে:

✅ **সম্পূর্ণ সার্ভার-ক্লায়েন্ট নেটওয়ার্কিং**  
✅ **সুন্দর JavaFX GUI**  
✅ **Acid Serum এর ছবি সহ পণ্য**  
✅ **রিয়েল-টাইম ডাটা সিঙ্ক**  
✅ **একাধিক ক্লায়েন্ট সাপোর্ট**  

---

**এখন উপভোগ করুন আপনার সম্পূর্ণ নেটওয়ার্কিং স্টক ম্যানেজমেন্ট সিস্টেম!** 🚀

