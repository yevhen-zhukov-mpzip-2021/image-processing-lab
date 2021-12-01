module com.example.imageprocessinglab {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.imageprocessinglab to javafx.fxml;
    exports com.example.imageprocessinglab;
    exports com.example.imageprocessinglab.controllers;
    opens com.example.imageprocessinglab.controllers to javafx.fxml;
}