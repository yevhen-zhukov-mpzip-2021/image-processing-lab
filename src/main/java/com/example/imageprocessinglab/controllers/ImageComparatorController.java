package com.example.imageprocessinglab.controllers;

import com.example.imageprocessinglab.services.ImageComparatorService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static com.example.imageprocessinglab.ImageComparatorApplication.RESOURCES_PATH;

public class ImageComparatorController implements Initializable {

    private static final String IMAGES_DIRECTORY_PATH = RESOURCES_PATH.concat("/images");
    private static final FileChooser FILE_CHOOSER = new FileChooser();
    private static final int DEFAULT_SIDE_SIZE = 500;

    private final ImageComparatorService imageComparatorService = new ImageComparatorService();

    static {
        FILE_CHOOSER.setInitialDirectory(Paths.get(IMAGES_DIRECTORY_PATH).toFile());
    }

    @FXML
    public Button cleanPallet;
    @FXML
    public HBox navigationImageBox;
    @FXML
    public HBox originImageBox;
    @FXML
    public HBox inputImageBox;
    @FXML
    public HBox compareImageBox;
    @FXML
    public Button uploadOrigin;
    @FXML
    public Button uploadInput;
    @FXML
    public Button compare;
    @FXML
    public Label imageDistanceRange;
    @FXML
    public Slider imageDifferenceSlider;
    @FXML
    public TextField imageDifferenceAcceptableDeviation;
    @FXML
    public HBox imageBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navigationImageBox.getChildren().forEach(child -> child.setFocusTraversable(false));
        imageDifferenceSlider.setMin(0);
        imageDifferenceSlider.setMax(1);
        imageDifferenceSlider.adjustValue(0.5);
        imageDifferenceSlider.setMajorTickUnit(0.1);
        imageDifferenceSlider.setSnapToTicks(true);
        imageDifferenceSlider.setShowTickMarks(true);
        imageDifferenceAcceptableDeviation.setText(String.valueOf(imageComparatorService.getAcceptableDeviation()));
        imageDifferenceAcceptableDeviation.setOnKeyPressed(this::acceptableDeviationAction);
        imageDifferenceSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                imageDifferenceSliderAction(newValue));
    }

    @FXML
    protected void uploadOriginImage() {
        uploadImage(originImageBox);
    }

    @FXML
    protected void uploadInputImage() {
        uploadImage(inputImageBox);
    }

    @FXML
    protected void compareImages() {
        if (compareImageBox.getChildren().isEmpty()) {
            var originalImage = ((ImageView) originImageBox.getChildren().get(0)).getImage();
            var inputImage = ((ImageView) inputImageBox.getChildren().get(0)).getImage();
            var compared = imageComparatorService.markDifferentAreas(originalImage, inputImage);
            compareImageBox.getChildren().add(new ImageView(compared));

            var range = imageComparatorService.getImageDistanceRange();
            var rangeMin = range.getMin() == 0 ? "0" : String.valueOf(range.getMin());
            var imageDistanceRangeText = String.format("%s -> %f", rangeMin, range.getMax());
            adjustSlider(range.getMin(), range.getMax());
            imageDistanceRange.setText(imageDistanceRangeText);
        }
    }

    @FXML
    protected void compareImagesWithNewPivot() {
        if (!compareImageBox.getChildren().isEmpty()) {
            var originalImage = ((ImageView) originImageBox.getChildren().get(0)).getImage();
            var inputImage = ((ImageView) inputImageBox.getChildren().get(0)).getImage();
            var compared = imageComparatorService.markDifferentAreas(originalImage, inputImage);
            compareImageBox.getChildren().replaceAll(mage -> new ImageView(compared));
            var imageDistanceRangeText = String.format(
                    "%s -> %f", imageComparatorService.getAcceptableDeviation(), imageComparatorService.getImageDistanceRange().getMax());
            imageDistanceRange.setText(imageDistanceRangeText);
        }
    }

    protected void imageDifferenceSliderAction(Number newValue) {
        var newValueD = newValue.doubleValue();
        imageComparatorService.setAcceptableDifference(newValueD);
        compareImagesWithNewPivot();
    }

    protected void acceptableDeviationAction(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            var textField = (TextField) event.getSource();
            var text = Objects.nonNull(textField) ? textField.getText() : "";
            Pattern validateDoubleState = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");
            var newValueD = validateDoubleState.matcher(text).matches()
                    ? Double.parseDouble(text) : 0.0;
            imageComparatorService.setAcceptableDeviation(newValueD);
            var currentAcceptableDifferenceValue = imageComparatorService.getAcceptableDifference();
            var newAcceptableDifferenceValue = Math.max(currentAcceptableDifferenceValue, newValueD);
            imageDifferenceSlider.setMin(newValueD);
            imageDifferenceSlider.adjustValue(newValueD);
            imageComparatorService.setAcceptableDifference(newAcceptableDifferenceValue);
            compareImagesWithNewPivot();
        }
    }

    @FXML
    public void cleanPalletAction() {
        imageBox.getChildren().stream()
                .filter(HBox.class::isInstance)
                .forEach(hBox -> ((HBox) hBox).getChildren().clear());
    }

    private void uploadImage(HBox targetNode) {
        var file = FILE_CHOOSER.showOpenDialog(null);
        var fileURI = file.toURI().toString();

        var image = imageComparatorService.getImageByFileName(fileURI, DEFAULT_SIDE_SIZE, DEFAULT_SIDE_SIZE);
        var imageView = new ImageView(image);

        var children = targetNode.getChildren();
        if (children.isEmpty()) {
            children.add(imageView);
        } else {
            targetNode.getChildren().replaceAll(node -> imageView);
        }
    }

    private void adjustSlider(double min, double max) {
        imageDifferenceSlider.setMin(min);
        imageDifferenceSlider.setMax(max);
        imageDifferenceSlider.adjustValue((max - min) / 2.0);
        imageDifferenceSlider.setMajorTickUnit((max - min) / 10);
    }
}