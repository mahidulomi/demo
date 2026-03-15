package com.example.demo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

/**
 * Controller for Product Details page
 */
public class ProductDetailsController {

    @FXML
    private ImageView productImage;

    @FXML
    private Label productCategory;

    @FXML
    private Label productName;

    @FXML
    private Label productPrice;

    @FXML
    private Label originalPrice;

    @FXML
    private Label discountBadge;

    @FXML
    private VBox specsContainer;

    @FXML
    private Label productDescription;

    @FXML
    private ComboBox<String> colorComboBox;

    @FXML
    private Spinner<Integer> quantitySpinner;

    @FXML
    private Button addToCartBtn;

    @FXML
    private Label statusLabel;

    private Product currentProduct;

    @FXML
    private void initialize() {
        // Initialize quantity spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        quantitySpinner.setValueFactory(valueFactory);

        // Initialize with default colors
        colorComboBox.getItems().addAll("Black", "White");
        colorComboBox.getSelectionModel().selectFirst();
    }

    /**
     * Set product details by name (for electronics)
     */
    public void setProductByName(String name) {
        Product product = createProductFromName(name);
        setProduct(product);
    }

    public void setProduct(Product product) {
        this.currentProduct = product;
        updateProductDetails(product);
    }

    private Product createProductFromName(String name) {
        Product product = new Product();
        product.setName(name);

        switch (name) {
            case "iPhone 15" -> setupIPhone15(product);
            case "iPhone 16" -> setupIPhone16(product);
            case "iPhone 17" -> setupIPhone17(product);
            case "Samsung Galaxy S25" -> setupSamsungS25(product);
            case "Vivo X200 Ultra" -> setupVivoX200(product);
            case "Lenovo IdeaPad i5 8GB SSD" -> setupLenovoLaptop(product);
            case "Asus VivoBook Ryzen 5 16GB" -> setupAsusLaptop(product);
            case "Wireless Earbuds Pro" -> setupWirelessEarbuds(product);
            case "Smart Watch Fitness" -> setupSmartWatch(product);
            case "Power Bank 20000mAh" -> setupPowerBank(product);
            case "iPad" -> setupIPad(product);
            case "Mouse" -> setupMouse(product);
            case "Ajazz K80 Redswitch" -> setupKeyboard(product);
            default -> setupDefaultProduct(product);
        }

        return product;
    }

    private void setupIPhone15(Product product) {
        product.setCategory("Mobile Phone");
        product.setPrice("BDT 99,999");
        product.setImagePath("/images/iphone15.png");
        product.setDescription("iPhone 15 brings you Dynamic Island, a powerful camera system, and all-day battery life in a stunning design.");
        product.setColors(new String[]{"Black", "Blue", "Pink", "Yellow", "Green"});

        // Mobile phone specifications
        product.addSpecification("💾 RAM", "6GB LPDDR5");
        product.addSpecification("💿 Storage", "128GB");
        product.addSpecification("📱 Display", "6.1 inch Super Retina XDR OLED");
        product.addSpecification("📸 Camera", "48MP Main + 12MP Ultra Wide");
        product.addSpecification("🎥 Front Camera", "12MP TrueDepth");
        product.addSpecification("🔋 Battery", "3,877 mAh");
        product.addSpecification("⚡ Charging", "20W Fast Charging + 15W MagSafe");
        product.addSpecification("🔌 Processor", "Apple A16 Bionic Chip (4nm)");
        product.addSpecification("📶 Connectivity", "5G, WiFi 6, Bluetooth 5.3");
        product.addSpecification("💧 Water Resistance", "IP68 (6m for 30 minutes)");
        product.addSpecification("✅ Warranty", "1 Year Official Apple Warranty");
    }

    private void setupIPhone16(Product product) {
        product.setCategory("Mobile Phone");
        product.setPrice("BDT 96,000");
        product.setOriginalPrice("BDT 1,20,000");
        product.setDiscount("20% OFF");
        product.setImagePath("/images/iphone16.png");
        product.setDescription("iPhone 16 features the powerful A17 Pro chip, advanced camera system with 5x optical zoom, and titanium design.");
        product.setColors(new String[]{"Titanium Black", "Titanium White", "Titanium Blue", "Natural Titanium"});

        product.addSpecification("💾 RAM", "8GB LPDDR5");
        product.addSpecification("💿 Storage", "256GB");
        product.addSpecification("📱 Display", "6.3 inch Super Retina XDR with ProMotion (120Hz)");
        product.addSpecification("📸 Rear Camera", "48MP Fusion + 12MP Ultra Wide + 12MP Telephoto (5x)");
        product.addSpecification("🎥 Front Camera", "12MP TrueDepth with Autofocus");
        product.addSpecification("🔋 Battery", "4,200 mAh");
        product.addSpecification("⚡ Charging", "25W Fast Charging + MagSafe + Qi Wireless");
        product.addSpecification("🔌 Processor", "Apple A17 Pro Chip (3nm)");
        product.addSpecification("📶 Connectivity", "5G, WiFi 6E, Bluetooth 5.3, USB-C");
        product.addSpecification("💧 Water Resistance", "IP68 (6m for 30 minutes)");
        product.addSpecification("✅ Warranty", "1 Year Official Apple Warranty");
    }

    private void setupIPhone17(Product product) {
        product.setCategory("Mobile Phone");
        product.setPrice("BDT 1,39,000");
        product.setImagePath("/images/iphone17.png");
        product.setDescription("iPhone 17 Pro Max - The ultimate iPhone with revolutionary camera system, longest battery life, and aerospace-grade titanium design.");
        product.setColors(new String[]{"Titanium Black", "Titanium White", "Titanium Blue", "Natural Titanium"});

        product.addSpecification("💾 RAM", "8GB LPDDR5X");
        product.addSpecification("💿 Storage", "512GB");
        product.addSpecification("📱 Display", "6.7 inch Super Retina XDR with Always-On Display (120Hz)");
        product.addSpecification("📸 Rear Camera", "48MP Main + 12MP Ultra Wide + 12MP Telephoto (5x Zoom)");
        product.addSpecification("🎥 Front Camera", "12MP TrueDepth with Autofocus");
        product.addSpecification("🔋 Battery", "4,500 mAh");
        product.addSpecification("⚡ Charging", "30W Fast Charging + MagSafe + Wireless");
        product.addSpecification("🔌 Processor", "Apple A18 Bionic Chip (3nm)");
        product.addSpecification("📶 Connectivity", "5G, WiFi 7, Bluetooth 5.4, USB-C 3.0");
        product.addSpecification("💧 Water Resistance", "IP68 (8m for 30 minutes)");
        product.addSpecification("✅ Warranty", "1 Year Official Apple Warranty");
    }

    private void setupSamsungS25(Product product) {
        product.setCategory("Mobile Phone");
        product.setPrice("BDT 1,20,000");
        product.setImagePath("/images/samsungs25.png");
        product.setDescription("Samsung Galaxy S25 Ultra - Premium flagship with S Pen, revolutionary 200MP camera, and AI-powered features.");
        product.setColors(new String[]{"Phantom Black", "Phantom Silver", "Green", "Violet"});

        product.addSpecification("💾 RAM", "12GB LPDDR5X");
        product.addSpecification("💿 Storage", "256GB UFS 4.0");
        product.addSpecification("📱 Display", "6.8 inch Dynamic AMOLED 2X, 120Hz, 3200x1440");
        product.addSpecification("📸 Rear Camera", "200MP Main + 12MP Ultra Wide + 10MP Tele (3x) + 10MP Tele (10x)");
        product.addSpecification("🎥 Front Camera", "12MP");
        product.addSpecification("🔋 Battery", "5,000 mAh");
        product.addSpecification("⚡ Charging", "45W Super Fast Charging + 15W Wireless + Reverse Wireless");
        product.addSpecification("🔌 Processor", "Snapdragon 8 Gen 3 (4nm)");
        product.addSpecification("📶 Connectivity", "5G, WiFi 6E, Bluetooth 5.3, USB-C 3.2");
        product.addSpecification("🖊️ S Pen", "Included with Bluetooth");
        product.addSpecification("💧 Water Resistance", "IP68");
        product.addSpecification("✅ Warranty", "1 Year Official Samsung Warranty");
    }

    private void setupVivoX200(Product product) {
        product.setCategory("Mobile Phone");
        product.setPrice("BDT 73,800");
        product.setOriginalPrice("BDT 90,000");
        product.setDiscount("18% OFF");
        product.setImagePath("/images/vivox200ultra.png");
        product.setDescription("Vivo X200 Ultra - Photography flagship with ZEISS optics, massive battery, and ultra-fast charging.");
        product.setColors(new String[]{"Titanium Gray", "Cosmic Blue", "Sunset Orange"});

        product.addSpecification("💾 RAM", "12GB LPDDR5X");
        product.addSpecification("💿 Storage", "256GB UFS 4.0");
        product.addSpecification("📱 Display", "6.78 inch AMOLED E7, 120Hz, 2800x1260");
        product.addSpecification("📸 Rear Camera", "50MP Sony IMX989 + 50MP Ultra Wide + 64MP Periscope Telephoto");
        product.addSpecification("🎥 Front Camera", "32MP");
        product.addSpecification("🔋 Battery", "5,500 mAh");
        product.addSpecification("⚡ Charging", "100W Flash Charge + 50W Wireless");
        product.addSpecification("🔌 Processor", "MediaTek Dimensity 9300 (4nm)");
        product.addSpecification("📷 Camera Tech", "ZEISS Optics + V3 Imaging Chip");
        product.addSpecification("📶 Connectivity", "5G, WiFi 6E, Bluetooth 5.4");
        product.addSpecification("💧 Water Resistance", "IP68");
        product.addSpecification("✅ Warranty", "1 Year Official Warranty");
    }

    private void setupLenovoLaptop(Product product) {
        product.setCategory("Laptop");
        product.setPrice("BDT 1,19,000");
        product.setOriginalPrice("BDT 1,44,000");
        product.setDiscount("15% OFF");
        product.setImagePath("/images/loglenevo.png");
        product.setDescription("Lenovo IdeaPad - Perfect for students and professionals. Lightweight design with powerful performance for everyday tasks.");
        product.setColors(new String[]{"Arctic Grey", "Abyss Blue"});

        // Laptop specifications
        product.addSpecification("💻 Processor (CPU)", "Intel Core i5-12450H (12th Gen, 8 cores, up to 4.4GHz)");
        product.addSpecification("🎮 Graphics (GPU)", "Intel Iris Xe Graphics");
        product.addSpecification("💾 RAM", "8GB DDR4 3200MHz (Expandable to 16GB)");
        product.addSpecification("💿 Storage", "512GB SSD NVMe PCIe Gen 4");
        product.addSpecification("📱 Display", "15.6 inch FHD (1920x1080) IPS, Anti-Glare");
        product.addSpecification("⌨️ Keyboard", "Full-size backlit keyboard with numeric keypad");
        product.addSpecification("🔋 Battery", "45Wh Li-Polymer, Up to 7 hours");
        product.addSpecification("⚡ Charging", "65W USB-C Power Adapter");
        product.addSpecification("📶 Connectivity", "WiFi 6 (802.11ax), Bluetooth 5.1");
        product.addSpecification("🔌 Ports", "2x USB-A 3.2, 1x USB-C, HDMI, Audio Jack, SD Card Reader");
        product.addSpecification("🎧 Audio", "Dolby Audio Dual Speakers");
        product.addSpecification("📹 Webcam", "720p HD with Privacy Shutter");
        product.addSpecification("⚖️ Weight", "1.65 kg");
        product.addSpecification("💻 OS", "Windows 11 Home");
        product.addSpecification("✅ Warranty", "2 Years International Warranty");
    }

    private void setupAsusLaptop(Product product) {
        product.setCategory("Laptop");
        product.setPrice("BDT 1,16,000");
        product.setImagePath("/images/asus.png");
        product.setDescription("Asus VivoBook - Premium laptop with Ryzen power, fast display, and premium build quality for creators and gamers.");
        product.setColors(new String[]{"Indie Black", "Cool Silver"});

        product.addSpecification("💻 Processor (CPU)", "AMD Ryzen 5 7535HS (6 cores, 12 threads, up to 4.55GHz)");
        product.addSpecification("🎮 Graphics (GPU)", "AMD Radeon Graphics + NVIDIA GeForce RTX 2050 (4GB GDDR6)");
        product.addSpecification("💾 RAM", "16GB DDR4 3200MHz (Dual Channel)");
        product.addSpecification("💿 Storage", "1TB SSD NVMe PCIe Gen 4");
        product.addSpecification("📱 Display", "15.6 inch FHD (1920x1080) IPS, 144Hz Gaming Display");
        product.addSpecification("⌨️ Keyboard", "RGB Backlit Gaming Keyboard");
        product.addSpecification("🔋 Battery", "50Wh, Up to 8 hours");
        product.addSpecification("⚡ Charging", "90W Fast Charging Adapter");
        product.addSpecification("🎮 Performance", "Ideal for Gaming, Video Editing, 3D Rendering");
        product.addSpecification("📶 Connectivity", "WiFi 6E (802.11ax), Bluetooth 5.2");
        product.addSpecification("🔌 Ports", "3x USB-A 3.2, 1x USB-C with Power Delivery, HDMI 2.0, RJ45 LAN");
        product.addSpecification("🎧 Audio", "DTS:X Ultra Audio, Smart Amp Technology");
        product.addSpecification("📹 Webcam", "1080p Full HD with IR Camera");
        product.addSpecification("⚖️ Weight", "1.9 kg");
        product.addSpecification("💻 OS", "Windows 11 Pro");
        product.addSpecification("✅ Warranty", "2 Years Official Asus Warranty");
    }

    private void setupWirelessEarbuds(Product product) {
        product.setCategory("Accessories - Audio");
        product.setPrice("BDT 5,000");
        product.setImagePath("/images/airpods.png");
        product.setDescription("Wireless Earbuds Pro - Premium sound quality with active noise cancellation, transparency mode, and all-day battery.");
        product.setColors(new String[]{"White", "Black"});

        // Earbuds specifications
        product.addSpecification("🎧 Type", "True Wireless Stereo (TWS) Earbuds");
        product.addSpecification("🎵 Drivers", "10mm Dynamic Drivers");
        product.addSpecification("🔇 Noise Cancellation", "Active Noise Cancellation (ANC) up to 35dB");
        product.addSpecification("🎤 Microphones", "6 Microphones (3 per earbud) with AI Call Noise Reduction");
        product.addSpecification("🔋 Battery (Earbuds)", "6 hours playback (ANC Off), 4.5 hours (ANC On)");
        product.addSpecification("🔋 Battery (Case)", "30 hours total with charging case");
        product.addSpecification("⚡ Charging", "USB-C Fast Charging + Wireless Charging Case");
        product.addSpecification("⚡ Quick Charge", "10 mins charge = 3 hours playback");
        product.addSpecification("📶 Bluetooth", "Bluetooth 5.3 with Low Latency Mode (60ms)");
        product.addSpecification("🎮 Audio Codecs", "AAC, SBC, aptX Adaptive");
        product.addSpecification("💧 Water Resistance", "IPX5 (Sweat & Water Resistant)");
        product.addSpecification("👆 Touch Controls", "Touch sensors for play/pause, volume, ANC toggle");
        product.addSpecification("📱 Compatibility", "iOS, Android, Windows");
        product.addSpecification("✅ Warranty", "1 Year Warranty");
    }

    private void setupSmartWatch(Product product) {
        product.setCategory("Accessories - Wearable");
        product.setPrice("BDT 10,000");
        product.setImagePath("/images/titan.png");
        product.setDescription("Smart Watch Fitness - Track your health with heart rate monitor, SpO2, sleep tracking, 100+ sports modes, and water resistance.");
        product.setColors(new String[]{"Black", "Silver", "Rose Gold"});

        // Smartwatch specifications
        product.addSpecification("⌚ Display", "1.4 inch AMOLED Touchscreen (454x454)");
        product.addSpecification("💎 Glass", "Sapphire Crystal Glass");
        product.addSpecification("❤️ Heart Rate", "24/7 Continuous Heart Rate Monitoring");
        product.addSpecification("🫁 SpO2", "Blood Oxygen Monitoring");
        product.addSpecification("😴 Sleep Tracking", "Advanced Sleep Analysis with REM stages");
        product.addSpecification("🏃 Sports Modes", "100+ Sports & Workout Modes");
        product.addSpecification("📍 GPS", "Built-in GPS/GLONASS/Galileo");
        product.addSpecification("🔋 Battery Life", "7 days typical use, 14 days battery saver mode");
        product.addSpecification("⚡ Charging", "Magnetic Charging Dock (2 hours full charge)");
        product.addSpecification("📶 Connectivity", "Bluetooth 5.2");
        product.addSpecification("📱 Notifications", "Calls, Messages, App Alerts");
        product.addSpecification("🎵 Music Control", "Control music playback");
        product.addSpecification("💧 Water Resistance", "5ATM (50m swimming)");
        product.addSpecification("📏 Strap Size", "Adjustable 20mm silicone strap");
        product.addSpecification("📱 Compatibility", "iOS 12+ and Android 6.0+");
        product.addSpecification("✅ Warranty", "1 Year Warranty");
    }

    private void setupPowerBank(Product product) {
        product.setCategory("Accessories - Power");
        product.setPrice("BDT 2,500");
        product.setImagePath("/images/powerbank.png");
        product.setDescription("Power Bank 20000mAh - Charge multiple devices with dual ports, LED display, and fast charging support.");
        product.setColors(new String[]{"Black", "White"});

        // Power bank specifications
        product.addSpecification("🔋 Capacity", "20,000 mAh (74Wh)");
        product.addSpecification("🔌 Input", "USB-C: 18W (5V/3A, 9V/2A, 12V/1.5A)");
        product.addSpecification("⚡ Output 1", "USB-C: 18W PD Fast Charging");
        product.addSpecification("⚡ Output 2", "USB-A: 18W QC 3.0 Fast Charging");
        product.addSpecification("⚡ Max Output", "18W (Single Port)");
        product.addSpecification("🔌 Total Ports", "2 Ports (1x USB-C + 1x USB-A)");
        product.addSpecification("📱 Display", "LED Digital Display showing battery percentage");
        product.addSpecification("🔄 Recharge Time", "6-7 hours with 18W charger");
        product.addSpecification("📱 Phone Charges", "iPhone 15: ~4 charges, Samsung S25: ~3.5 charges");
        product.addSpecification("🛡️ Safety Features", "Overcharge, Overdischarge, Short Circuit Protection");
        product.addSpecification("⚖️ Weight", "380g");
        product.addSpecification("📏 Dimensions", "146 x 68 x 28 mm");
        product.addSpecification("✅ Warranty", "6 Months Warranty");
    }

    private void setupIPad(Product product) {
        product.setCategory("Tablet");
        product.setPrice("BDT 23,400");
        product.setOriginalPrice("BDT 30,000");
        product.setDiscount("22% OFF");
        product.setImagePath("/images/ipad.png");
        product.setDescription("iPad - Powerful, versatile, and perfect for creativity, learning, and entertainment.");
        product.setColors(new String[]{"Space Gray", "Silver", "Blue", "Pink"});

        product.addSpecification("💾 RAM", "4GB");
        product.addSpecification("💿 Storage", "64GB");
        product.addSpecification("📱 Display", "10.9 inch Liquid Retina Display (2360x1640)");
        product.addSpecification("📸 Rear Camera", "12MP Wide with Smart HDR");
        product.addSpecification("🎥 Front Camera", "12MP Ultra Wide with Center Stage");
        product.addSpecification("🔋 Battery", "28.6Wh, Up to 10 hours");
        product.addSpecification("⚡ Charging", "20W USB-C Fast Charging");
        product.addSpecification("🔌 Processor", "Apple A14 Bionic Chip");
        product.addSpecification("📶 Connectivity", "WiFi 6 (802.11ax), Bluetooth 5.2");
        product.addSpecification("🔌 Port", "USB-C for charging and accessories");
        product.addSpecification("🎤 Audio", "Stereo Speakers in Landscape");
        product.addSpecification("✏️ Pencil Support", "Apple Pencil (1st Generation)");
        product.addSpecification("⌨️ Keyboard Support", "Magic Keyboard Folio Compatible");
        product.addSpecification("✅ Warranty", "1 Year Official Apple Warranty");
    }

    private void setupMouse(Product product) {
        product.setCategory("Accessories - Gaming");
        product.setPrice("BDT 3,600");
        product.setOriginalPrice("BDT 5,000");
        product.setDiscount("28% OFF");
        product.setImagePath("/images/mouise.png");
        product.setDescription("Wireless Gaming Mouse - Precision sensor, programmable buttons, RGB lighting, and ergonomic design for comfort.");
        product.setColors(new String[]{"Black", "White", "RGB Edition"});

        // Gaming mouse specifications
        product.addSpecification("🖱️ Type", "Wireless Gaming Mouse");
        product.addSpecification("🎯 Sensor", "PixArt 3395 Optical Sensor");
        product.addSpecification("🎮 DPI", "100-26,000 DPI (adjustable in 50 DPI steps)");
        product.addSpecification("⚡ Polling Rate", "1000Hz (1ms response time)");
        product.addSpecification("🔘 Buttons", "8 Programmable Buttons");
        product.addSpecification("📶 Connectivity", "2.4GHz Wireless + Bluetooth 5.1 + Wired USB-C");
        product.addSpecification("🔋 Battery", "Up to 70 days on single charge");
        product.addSpecification("⚡ Quick Charge", "10 mins charge = 10 hours use");
        product.addSpecification("💡 RGB Lighting", "16.8 million colors, multiple effects");
        product.addSpecification("⚖️ Weight", "79g (without cable)");
        product.addSpecification("🎯 Tracking Speed", "650 IPS");
        product.addSpecification("🎯 Acceleration", "50G");
        product.addSpecification("👆 Switches", "Omron switches (50 million clicks)");
        product.addSpecification("📱 Software", "Customizable software for macros & profiles");
        product.addSpecification("✅ Warranty", "1 Year Warranty");
    }

    private void setupKeyboard(Product product) {
        product.setCategory("Accessories - Gaming");
        product.setPrice("BDT 4,200");
        product.setImagePath("/images/ajaj.png");
        product.setDescription("Ajazz K80 Mechanical Keyboard - Red Switches, RGB backlight, hot-swappable, perfect for gaming and typing.");
        product.setColors(new String[]{"Black", "White"});

        // Mechanical keyboard specifications
        product.addSpecification("⌨️ Type", "Mechanical Gaming Keyboard");
        product.addSpecification("🔴 Switches", "Ajazz Red Linear Switches (Hot-Swappable)");
        product.addSpecification("🎮 Switch Type", "Linear, 45g Actuation Force");
        product.addSpecification("📊 Layout", "Full-Size 104 Keys");
        product.addSpecification("💡 RGB Lighting", "Per-Key RGB with multiple effects");
        product.addSpecification("🔄 Hot-Swap", "Hot-swappable sockets for easy switch replacement");
        product.addSpecification("🔋 Battery", "Built-in 3000mAh Rechargeable Battery, 30 days use");
        product.addSpecification("⚡ Charging", "USB-C Charging, 2-3 hours full charge");
        product.addSpecification("📶 Connectivity", "Wired (USB-C) + Bluetooth 5.0 + 2.4GHz Wireless");
        product.addSpecification("🎮 Anti-Ghosting", "Full N-Key Rollover (NKRO)");
        product.addSpecification("⏱️ Response Time", "1ms in wired mode");
        product.addSpecification("🔘 Keycaps", "Double-shot PBT keycaps");
        product.addSpecification("📱 Multi-Device", "Connect up to 3 devices simultaneously");
        product.addSpecification("🎧 Extra Features", "Dedicated media keys, volume knob");
        product.addSpecification("⚖️ Weight", "850g");
        product.addSpecification("✅ Warranty", "1 Year Warranty");
    }

    private void setupDefaultProduct(Product product) {
        product.setCategory("Electronics");
        product.setPrice("BDT 10,000");
        product.setDescription("Quality product with excellent features.");
        product.addSpecification("✅ Quality", "Premium Quality Product");
    }

    private void updateProductDetails(Product product) {
        // Basic Info
        productName.setText(product.getName());
        productCategory.setText(product.getCategory());
        productPrice.setText(product.getPrice());
        productDescription.setText(product.getDescription());

        // Discount handling
        if (product.getDiscount() != null && !product.getDiscount().isEmpty()) {
            discountBadge.setText("🔥 " + product.getDiscount() + " - Limited Time!");
            discountBadge.setVisible(true);
            discountBadge.setManaged(true);

            if (product.getOriginalPrice() != null) {
                originalPrice.setText(product.getOriginalPrice());
                originalPrice.setVisible(true);
                originalPrice.setManaged(true);
            }
        } else {
            discountBadge.setVisible(false);
            discountBadge.setManaged(false);
            originalPrice.setVisible(false);
            originalPrice.setManaged(false);
        }

        // Image
        if (product.getImagePath() != null) {
            try {
                productImage.setImage(new Image(getClass().getResourceAsStream(product.getImagePath())));
            } catch (Exception e) {
                System.err.println("Could not load image: " + product.getImagePath());
            }
        }

        // Clear and rebuild specifications dynamically
        specsContainer.getChildren().clear();

        // Add title
        Label specsTitle = new Label("📋 Technical Specifications");
        specsTitle.getStyleClass().add("specs-title");
        specsContainer.getChildren().add(specsTitle);

        // Add each specification
        for (Map.Entry<String, String> spec : product.getSpecifications().entrySet()) {
            HBox specItem = new HBox(10);
            specItem.getStyleClass().add("spec-item");

            Label specLabel = new Label(spec.getKey() + ":");
            specLabel.getStyleClass().add("spec-label");
            specLabel.setMinWidth(180);

            Label specValue = new Label(spec.getValue());
            specValue.getStyleClass().add("spec-value");
            specValue.setWrapText(true);
            specValue.setMaxWidth(280);

            specItem.getChildren().addAll(specLabel, specValue);
            specsContainer.getChildren().add(specItem);
        }

        // Colors
        if (product.getColors() != null && product.getColors().length > 0) {
            colorComboBox.getItems().clear();
            colorComboBox.getItems().addAll(product.getColors());
            colorComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onAddToCart() {
        if (colorComboBox.getValue() == null) {
            showStatus("⚠️ Please select a color!", false);
            return;
        }

        String selectedColor = colorComboBox.getValue();
        int quantity = quantitySpinner.getValue();
         String productId = StockManager.findProductIdByName(currentProduct.getName());

        if (productId == null) {
            showStatus("❌ This product is not linked with stock management yet.", false);
            return;
        }

        int available = StockManager.getStock(productId);
        int alreadyInCart = Cart.containsItem(productId) ? Cart.getItem(productId).getQuantity() : 0;
        if (available <= 0) {
            showStatus("❌ This product is out of stock.", false);
            return;
        }
        if (alreadyInCart + quantity > available) {
            showStatus("❌ Only " + Math.max(0, available - alreadyInCart) + " more item(s) can be added.", false);
            return;
        }

        double unitPrice = parseUnitPrice(currentProduct.getPrice());
        String imagePath = currentProduct.getImagePath() == null ? "" : currentProduct.getImagePath();
        Cart.addItem(productId, currentProduct.getName(), "Electronics", unitPrice, quantity, imagePath, 0);

        String message = """
                ✓ Added to Cart!
                
                Product: %s
                Color: %s
                Quantity: %d
                Price: %s
                Total: %s
                """.formatted(
                currentProduct.getName(),
                selectedColor,
                quantity,
                currentProduct.getPrice(),
                calculateTotal(quantity)
        );

        showStatus(message, true);

        addToCartBtn.setText("✓ Added to Cart!");
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            javafx.application.Platform.runLater(() -> addToCartBtn.setText("🛒 Add to Cart"));
        }).start();
    }

    @FXML
    private void onBuyNow() {
        if (colorComboBox.getValue() == null) {
            showStatus("⚠️ Please select a color!", false);
            return;
        }

        String selectedColor = colorComboBox.getValue();
        int quantity = quantitySpinner.getValue();
        String productId = StockManager.findProductIdByName(currentProduct.getName());

        if (productId == null) {
            showStatus("❌ This product is not linked with stock management yet.", false);
            return;
        }

        int currentStock = StockManager.getStock(productId);
        if (currentStock < quantity) {
            showStatus("❌ Only " + currentStock + " item(s) available in stock.", false);
            return;
        }

        int newStock = currentStock - quantity;
        StockManager.updateStock(productId, newStock);
        NetworkManager.getInstance().broadcastStockUpdate(productId, newStock);

        double unitPrice = parseUnitPrice(currentProduct.getPrice());
        double totalAmount = unitPrice * quantity;
        CartItem purchasedItem = new CartItem(productId, currentProduct.getName(), "Electronics", unitPrice, quantity,
                currentProduct.getImagePath() == null ? "" : currentProduct.getImagePath(), 0);
        SaleRecord sale = NetworkManager.getInstance().buildSaleRecord(java.util.List.of(purchasedItem), quantity, totalAmount);
        SalesManager.recordSale(sale);
        NetworkManager.getInstance().broadcastSaleRecord(sale);

        String message = """
                🎉 Order Confirmed!
                
                ━━━━━━━━━━━━━━━━━━━━━━
                Product: %s
                Color: %s
                Quantity: %d
                ━━━━━━━━━━━━━━━━━━━━━━
                Unit Price: %s
                Total Amount: %s
                ━━━━━━━━━━━━━━━━━━━━━━
                
                Thank you for your purchase!
                Your order will be delivered soon.
                """.formatted(
                currentProduct.getName(),
                selectedColor,
                quantity,
                currentProduct.getPrice(),
                String.format("BDT %,.0f", totalAmount)
        );

        showStatus(message, true);
    }

    private double parseUnitPrice(String priceText) {
        try {
            String numeric = priceText == null ? "" : priceText.replaceAll("[^0-9.]", "");
            if (numeric.isEmpty()) return 0;
            return Double.parseDouble(numeric);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String calculateTotal(int quantity) {
        try {
            String priceStr = currentProduct.getPrice().replaceAll("[^0-9]", "");
            int price = Integer.parseInt(priceStr);
            int total = price * quantity;
            return String.format("BDT %,d", total);
        } catch (Exception e) {
            return currentProduct.getPrice();
        }
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);

        if (success) {
            statusLabel.getStyleClass().removeAll("detail-status-error");
            if (!statusLabel.getStyleClass().contains("detail-status-message")) {
                statusLabel.getStyleClass().add("detail-status-message");
            }
        } else {
            statusLabel.getStyleClass().removeAll("detail-status-message");
            if (!statusLabel.getStyleClass().contains("detail-status-error")) {
                statusLabel.getStyleClass().add("detail-status-error");
            }
        }
    }

    @FXML
    private void onBackToElectronics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("electronics-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) productName.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Error loading electronics view: " + e.getMessage());
        }
    }
}

