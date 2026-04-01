package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.geometry.Bounds;

public class AboutController {

    @FXML
    private Label userLabel;

    @FXML
    private Button productsBtn;

    @FXML
    public void initialize() {
        String user = Session.getCurrentUser();
        if (user != null) {
            userLabel.setText("Welcome, " + user);
        }
    }

    @FXML
    private void onNavHome() {
        Session.goToHome(userLabel);
    }

    @FXML
    private void onProductsClick() {
        if (productsBtn != null) {
            ContextMenu menu = new ContextMenu();

            MenuItem beautyItem = new MenuItem("💄 Beauty");
            beautyItem.setOnAction(e -> {
                ProductListController.setCategoryToShow("Beauty");
                Session.goToProductList(userLabel);
            });

            MenuItem electronicsItem = new MenuItem("📱 Electronics");
            electronicsItem.setOnAction(e -> {
                ProductListController.setCategoryToShow("Electronics");
                Session.goToProductList(userLabel);
            });

            MenuItem homeLivingItem = new MenuItem("🏠 Home & Living");
            homeLivingItem.setOnAction(e -> {
                ProductListController.setCategoryToShow("Home and Living");
                Session.goToProductList(userLabel);
            });

            MenuItem fashionItem = new MenuItem("👗 Fashion");
            fashionItem.setOnAction(e -> {
                ProductListController.setCategoryToShow("Fashion");
                Session.goToProductList(userLabel);
            });

            menu.getItems().addAll(beautyItem, electronicsItem, homeLivingItem, fashionItem);

            Bounds bounds = productsBtn.localToScreen(productsBtn.getBoundsInLocal());
            menu.show(productsBtn, bounds.getCenterX(), bounds.getCenterY() + 30);
        } else {
            Session.goToStock(userLabel);
        }
    }

    @FXML
    private void onSalesClick() {
        Session.goToSales(userLabel);
    }

    @FXML
    private void onRestockClick() {
        Session.goToRestock(userLabel);
    }

    @FXML
    private void onCustomersClick() {
        Session.goToCustomers(userLabel);
    }

    @FXML
    private void onReportsClick() {
        Session.goToReports(userLabel);
    }

    @FXML
    private void onProfileClick() {
        // Already on About Us
    }

    @FXML
    private void onLogout() {
        Session.logoutToLogin(userLabel);
    }
}
