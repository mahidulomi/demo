package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize stock data
        StockManager.initializeStock();

        // Go directly to login page
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);

        stage.setTitle("HATBAZARx");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setResizable(true);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() {
        // Clean up networking when the app closes
        NetworkManager.getInstance().shutdown();
    }

    public static void main(String[] args) {
        launch();
    }
}