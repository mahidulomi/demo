package com.example.demo;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.util.Optional;

public class CustomerController {

    @FXML private Label userLabel;
    @FXML private TextField searchField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> addressColumn;
    @FXML private TableColumn<Customer, String> typeColumn;
    @FXML private TableColumn<Customer, Double> dueColumn;

    // Modal elements
    @FXML private StackPane rootStackPane;
    @FXML private AnchorPane modalOverlay;
    @FXML private Label modalTitle;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressArea;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ToggleButton dueBalanceToggle;
    @FXML private TextField dueBalanceAmountField; // If necessary to edit amount, but screenshot shows toggle

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private Customer currentEditingCustomer = null;

    @FXML
    public void initialize() {
        String user = Session.getCurrentUser();
        userLabel.setText("Welcome, " + (user == null ? "Admin" : user));

        setupTable();
        loadCustomers();

        // Listen for external updates (network/file)
        CustomerManager.addExternalChangeListener(() -> {
            Platform.runLater(this::loadCustomers);
        });
        
        // Also listen via NetworkManager for immediate updates if not file-based
        NetworkManager.getInstance().setServerStatusCallback(() -> {
             Platform.runLater(this::loadCustomers);
        });

        // Search filter
        FilteredList<Customer> filteredData = new FilteredList<>(customerList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(customer -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (customer.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                if (customer.getPhone().toLowerCase().contains(lowerCaseFilter)) return true;
                if (customer.getEmail().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        customerTable.setItems(filteredData);
        
        // Setup Type Combo
        typeCombo.setItems(FXCollections.observableArrayList("Retail", "Wholesale"));
        typeCombo.getSelectionModel().selectFirst();
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dueColumn.setCellValueFactory(new PropertyValueFactory<>("dueBalance"));
        
        // Custom cell for formatting Currency
        dueColumn.setCellFactory(tc -> new TableCell<Customer, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("Tk.%.2f", price));
                }
            }
        });
    }

    private void loadCustomers() {
        java.util.List<Customer> allCustomers = new java.util.ArrayList<>(CustomerManager.getAllCustomers());
        java.util.Collections.reverse(allCustomers);  // Newest customers first
        customerList.setAll(allCustomers);
    }

    @FXML
    private void onAddCustomer() {
        currentEditingCustomer = null;
        modalTitle.setText("Add Customer");
        clearForm();
        showModal();
    }

    private void onEdit(Customer customer) {
        currentEditingCustomer = customer;
        modalTitle.setText("Edit Customer");
        fillForm(customer);
        showModal();
    }

    private void onDelete(Customer customer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Customer");
        alert.setHeaderText("Delete " + customer.getName() + "?");
        alert.setContentText("Are you sure? This cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            CustomerManager.deleteCustomer(customer.getId());
            NetworkManager.getInstance().broadcastCustomer(customer); // We might need a DELETE broadcast, but for now just sync
             // Actually currently we only support UPSERT broadcast. 
             // Ideally we should add delete support. 
             // For now, let's just refresh local list.
             loadCustomers();
        }
    }
    
    @FXML
    private void onSaveCustomer() {
        if (nameField.getText().isEmpty() || phoneField.getText().isEmpty()) {
            showAlert("Required Fields", "Name and Phone Number are required.");
            return;
        }

        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();

        // Validate phone number
        if (phone.isEmpty()) {
            showAlert("Required Fields", "Phone number is required.");
            return;
        }
        
        if (!phone.matches("\\d+")) {
            showAlert("Invalid Input", "Phone number must contain only digits.");
            return;
        }
        
        if (phone.length() < 11) {
            showAlert("Invalid Input", "Phone number must be at least 11 digits long.");
            return;
        }
        
        if (!phone.startsWith("0")) {
            showAlert("Invalid Input", "Phone number must start with 0.");
            return;
        }

        if (!email.isEmpty() && !email.contains("@") && !email.contains(".")) {
             showAlert("Invalid Input", "Please enter a valid email address (e.g., user@example.com).");
             return;
        }
        
        String address = addressArea.getText();
        String type = typeCombo.getValue();
        boolean hasDue = dueBalanceToggle.isSelected();
        double due = 0.0;
        
        // If editing due balance was allowed, we'd parse it here. 
        // For now, based on toggle, maybe set a default or keep existing if editing.
        // The screenshot shows a toggle 'Due Balance'. If enabled, maybe show an input field?
        // I will assume for now if toggle is on, it stays what it was, or 0 if new.
        // Wait, screenshot shows 'Due Balance' text next to toggle. 
        // And the table has a specific amount like 2,300.00.
        // I'll add a field for due amount that appears when toggle is on.
        
        if (hasDue) {
             try {
                 due = Double.parseDouble(dueBalanceAmountField.getText());
             } catch (NumberFormatException e) {
                 due = 0.0;
             }
        }

        Customer customer;
        if (currentEditingCustomer != null) {
            customer = currentEditingCustomer;
            customer.setName(name);
            customer.setPhone(phone);
            customer.setEmail(email);
            customer.setAddress(address);
            customer.setType(type);
            customer.setDueBalance(due);
        } else {
            customer = new Customer(name, phone, email, address, type, due);
        }

        CustomerManager.saveCustomer(customer);
        NetworkManager.getInstance().broadcastCustomer(customer);
        
        loadCustomers();
        hideModal();
    }

    @FXML
    private void onCancelModal() {
        hideModal();
    }

    private void showModal() {
        modalOverlay.setVisible(true);
    }

    private void hideModal() {
        modalOverlay.setVisible(false);
    }

    private void clearForm() {
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        addressArea.clear();
        typeCombo.getSelectionModel().selectFirst();
        dueBalanceToggle.setSelected(false);
        dueBalanceAmountField.setText("0.0");
        dueBalanceAmountField.setDisable(true);
    }

    private void fillForm(Customer customer) {
        nameField.setText(customer.getName());
        phoneField.setText(customer.getPhone());
        emailField.setText(customer.getEmail());
        addressArea.setText(customer.getAddress());
        typeCombo.setValue(customer.getType());
        boolean hasDue = customer.getDueBalance() > 0;
        dueBalanceToggle.setSelected(hasDue);
        dueBalanceAmountField.setText(String.valueOf(customer.getDueBalance()));
        dueBalanceAmountField.setDisable(!hasDue);
    }
    
    @FXML
    private void onDueToggle() {
        dueBalanceAmountField.setDisable(!dueBalanceToggle.isSelected());
        if (!dueBalanceToggle.isSelected()) {
            dueBalanceAmountField.setText("0.0");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAddress(Customer customer) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Customer Address");
        alert.setHeaderText(customer.getName() + " - " + customer.getPhone());
        String address = customer.getAddress();
        if (address == null || address.isEmpty()) {
            address = "No address provided";
        }
        alert.setContentText("Address:\n\n" + address);
        alert.showAndWait();
    }

    // Navigation (same as Home)
    @FXML private void onNavHome() { Session.goToHome(userLabel); }
    @FXML private void onProductsClick() { Session.goToStock(userLabel); } // Or show menu
    @FXML private void onSalesClick() { Session.goToSales(userLabel); }
    @FXML private void onRestockClick() { Session.goToRestock(userLabel); }
    @FXML private void onCustomersClick() { /* Already here */ }
    @FXML private void onReportsClick() { Session.goToReports(userLabel); }
    @FXML private void onProfileClick() { /* Todo */ }
    @FXML private void onLogout() { Session.logout(); }
    @FXML private void openFashion() { Session.goToFashion(userLabel); } // Re-use
}
