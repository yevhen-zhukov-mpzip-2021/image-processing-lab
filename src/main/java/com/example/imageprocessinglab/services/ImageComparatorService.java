package com.example.imageprocessinglab.services;

import com.example.imageprocessinglab.models.ImageDistanceRange;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

public class ImageComparatorService {

    private final Set<Double> colorDistances = new LinkedHashSet<>();

    private double acceptableDifference;
    private double acceptableDeviation;

    public void setAcceptableDifference(double newValue) {
        acceptableDifference = newValue;
    }

    public double getAcceptableDifference() {
        return acceptableDifference;
    }

    public void setAcceptableDeviation(double newValue) {
        acceptableDeviation = newValue;
    }

    public double getAcceptableDeviation() {
        return acceptableDeviation;
    }

    public Image getImageByFileName(String fileURI, int requestedWidth, int requestedHeight) {
        return new Image(
                fileURI,
                requestedWidth,
                requestedHeight,
                true,
                true
        );
    }

    public ImageDistanceRange getImageDistanceRange() {
        final double DEFAULT_VALUE = 0d;
        return new ImageDistanceRange(
                colorDistances.stream().min(Comparator.comparing(Double::doubleValue)).orElse(DEFAULT_VALUE),
                colorDistances.stream().max(Comparator.comparing(Double::doubleValue)).orElse(DEFAULT_VALUE)
        );
    }

    public Image markDifferentAreas(final Image firstImage, final Image secondImage) {
        var pixelReaderOriginal = firstImage.getPixelReader();
        var pixelReaderInput = secondImage.getPixelReader();

        var wImage = new WritableImage(
                (int) firstImage.getWidth(),
                (int) firstImage.getHeight());
        var pixelWriterOriginal = wImage.getPixelWriter();

        for (int readY = 0; readY < firstImage.getHeight(); readY++) {
            for (int readX = 0; readX < firstImage.getWidth(); readX++) {
                Color originalColor = pixelReaderOriginal.getColor(readX, readY);
                Color inputColor = pixelReaderInput.getColor(readX, readY);
                Color differenceColor = compareByAverageValue(originalColor, inputColor);
                pixelWriterOriginal.setColor(readX, readY, differenceColor);
            }
        }

        return wImage;
    }

    private Color compareByAverageValue(Color original, Color input) {
        var colorDistance = getRgbColorDistance(original, input);

        colorDistances.add(colorDistance);

        return getImageDifferenceStrictColor(original, colorDistance);
    }

    private double getRgbColorDistance(Color color0, Color color1) {
        return getPixelIntensity(color0) - getPixelIntensity(color1);
    }

    private double getPixelIntensity(Color pixelColor) {
        DoubleUnaryOperator getIntensity = percentage -> (percentage * 100) / 256;

        return getIntensity.applyAsDouble(pixelColor.getRed())
                + getIntensity.applyAsDouble(pixelColor.getGreen())
                + getIntensity.applyAsDouble(pixelColor.getBlue());
    }

    private Color getImageDifferenceStrictColor(Color original, double colorDistance) {
        if (colorDistance > acceptableDeviation) {
            if (colorDistance < acceptableDifference) {
                return DifferenceColor.LOWER.getColor();
            } else if (colorDistance > acceptableDifference) {
                return DifferenceColor.HIGHER.getColor();
            }
        }

        return original;
    }

    private enum DifferenceColor {
        HIGHER(Color.RED),
        LOWER(Color.BLUE);

        private final Color color;

        public Color getColor() {
            return this.color;
        }

        DifferenceColor(Color color) {
            this.color = color;
        }
    }
}
