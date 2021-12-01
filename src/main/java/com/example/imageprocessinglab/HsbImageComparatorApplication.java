package com.example.imageprocessinglab;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Paths;

public class HsbImageComparatorApplication extends Application {

    public static final String RESOURCES_PATH = "src/main/resources";
    private static final String VIEWS_PATH = RESOURCES_PATH.concat("/views");
    private static final double STAGE_WIDTH = 1545;
    private static final double STAGE_HEIGHT = 575;

    @Override
    public void start(Stage stage) throws IOException {
        var fxmlLoader = new FXMLLoader(Paths.get(VIEWS_PATH.concat("/hsb-image-comparator.fxml")).toUri().toURL());
        var scene = new Scene(fxmlLoader.load(), STAGE_WIDTH, STAGE_HEIGHT);
        stage.setTitle("HSB Image Distance Comparator");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}