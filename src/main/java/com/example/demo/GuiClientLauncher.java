package com.example.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * GUI Client Launcher with Networking
 * This application connects to the stock server and displays the shopping interface.
 *
 * Usage: java GuiClientLauncher [host] [port]
 * Example: java GuiClientLauncher localhost 5555
 */
public class GuiClientLauncher extends Application {

    private String serverHost = "localhost";
    private int serverPort = 5555;
    private Label statusLabel;
    private Button connectButton;
    private TextField hostField;
    private TextField portField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Parse command line arguments
        var params = getParameters();
        var rawArgs = params.getRaw();
        if (!rawArgs.isEmpty()) {
            serverHost = rawArgs.get(0);
        }
        if (rawArgs.size() > 1) {
            try {
                serverPort = Integer.parseInt(rawArgs.get(1));
            } catch (NumberFormatException ignored) {}
        }

        primaryStage.setTitle("Stock Management - Network Client");
        primaryStage.setWidth(600);
        primaryStage.setHeight(400);

        // Create connection panel
        VBox connectPanel = createConnectionPanel();

        Scene scene = new Scene(connectPanel);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(e -> {
            NetworkManager.getInstance().shutdown();
        });

        primaryStage.show();

        // Try to auto-connect
        autoConnect();
    }

    private VBox createConnectionPanel() {
        VBox root = new VBox(15);
        root.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label("🔌 Connect to Stock Server");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #00bfff;");

        // Host field
        VBox hostBox = new VBox(5);
        Label hostLabel = new Label("Server Host:");
        hostLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12;");
        hostField = new TextField(serverHost);
        hostField.setStyle("-fx-font-size: 12; -fx-padding: 10;");
        hostBox.getChildren().addAll(hostLabel, hostField);

        // Port field
        VBox portBox = new VBox(5);
        Label portLabel = new Label("Server Port:");
        portLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12;");
        portField = new TextField(String.valueOf(serverPort));
        portField.setStyle("-fx-font-size: 12; -fx-padding: 10;");
        portBox.getChildren().addAll(portLabel, portField);

        // Connect button
        connectButton = new Button("Connect to Server");
        connectButton.setStyle("-fx-font-size: 14; -fx-padding: 12; -fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        connectButton.setPrefWidth(200);
        connectButton.setOnAction(e -> connectToServer());

        // Status label
        statusLabel = new Label("🟡 Waiting to connect...");
        statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #ffaa00;");

        root.getChildren().addAll(
            titleLabel,
            new Label(""),
            hostBox,
            portBox,
            new Label(""),
            connectButton,
            statusLabel
        );

        return root;
    }

    private void autoConnect() {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                javafx.application.Platform.runLater(this::connectToServer);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void connectToServer() {
        connectButton.setDisable(true);
        statusLabel.setText("🟡 Connecting...");
        statusLabel.setStyle("-fx-text-fill: #ffaa00;");

        new Thread(() -> {
            try {
                String host = hostField.getText().trim();
                int port = Integer.parseInt(portField.getText().trim());

                // Initialize StockManager
                StockManager.initializeStock();

                // Connect to server
                NetworkManager networkManager = NetworkManager.getInstance();
                networkManager.connectToServer(host, port);

                // Success!
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("✅ Connected successfully!");
                    statusLabel.setStyle("-fx-text-fill: #00ff00;");

                    // Open shopping interface after a short delay
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            javafx.application.Platform.runLater(this::openShoppingInterface);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });

            } catch (NumberFormatException e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("❌ Invalid port number!");
                    statusLabel.setStyle("-fx-text-fill: #ff0000;");
                    connectButton.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("❌ Connection failed: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #ff0000;");
                    connectButton.setDisable(false);
                });
            }
        }).start();
    }

    private void openShoppingInterface() {
        try {
            // Launch the main shopping application
            HelloApplication shopApp = new HelloApplication();
            Stage shopStage = new Stage();
            shopApp.start(shopStage);
        } catch (Exception e) {
            statusLabel.setText("❌ Failed to open shopping interface: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #ff0000;");
        }
    }
}

