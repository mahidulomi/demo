# Shop Master

Shop Master is a JavaFX-based sales and store management application built with Maven. It provides a modern desktop interface for browsing products, managing carts and orders, tracking stock, and working with customer-related screens.

## Features

- Modern JavaFX desktop user interface
- Product browsing by category
- Product details and product list screens
- Shopping cart and checkout workflow
- Customer signup, login, and forgot-password screens
- Sales and reports screens
- Stock management and restock management
- Bill details and order tracking
- Free delivery and new arrivals screens
- Network support with client/server launcher classes
- Image-based product presentation for multiple categories such as fashion, electronics, beauty, and home living

## Requirements

- Windows 10/11
- JDK 21
- Maven Wrapper is included in the project
- IntelliJ IDEA or another Java IDE

## Installation Guide

1. Download or clone the project.
2. Open the project folder in your IDE.
3. Make sure JDK 21 is selected as the project SDK.
4. Wait for Maven dependencies to download.
5. Build and run the project using the Maven Wrapper.

## How to Run

### Run the main application

Open PowerShell in the project root and run:

```powershell
.\mvnw.cmd clean javafx:run
```

### Run tests

```powershell
.\mvnw.cmd test
```

## Optional Launchers

The project also includes launcher classes for different runtime modes, such as:

- `Launcher`
- `ServerLauncher`
- `ClientLauncher`
- `GuiClientLauncher`

These can be used for network or client/server related workflows if needed.

## Project Structure

- `src/main/java/com/example/demo/` - Java source code, controllers, models, and managers
- `src/main/resources/com/example/demo/` - FXML views and CSS files
- `src/main/resources/images/` - electronics product images
- `src/main/resources/fashion/` - fashion product images
- `src/main/resources/beautyimages/` - beauty product images
- `src/main/resources/extra/` - home and furniture images

## Main Screen Areas

- Home dashboard
- Product catalog
- Customer area
- Cart and checkout
- Reports
- Stock
- Sales
- Restock
- About page

## Notes

- The main entry point is configured in `HelloApplication`.
- Resources such as FXML files, CSS, and images are loaded from the classpath.
- If you use IntelliJ IDEA, you can also run the application directly from the main class after Maven imports finish.

