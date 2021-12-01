package com.example.imageprocessinglab.services;

import com.example.imageprocessinglab.models.ImageDistanceRange;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class ImageComparatorService {

    private final Set<Double> colorDistances = new LinkedHashSet<>();

    private double acceptableDifference;
    private double acceptableDeviation;
    private boolean isImageDifferenceRgbColor;

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

    public void setIsImageDifferenceRgbColor(boolean isImageDifferenceRgbColor) {
        this.isImageDifferenceRgbColor = isImageDifferenceRgbColor;
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
        var colorDistance = getHsbColorDistance(original, input);

        colorDistances.add(colorDistance);

        return isImageDifferenceRgbColor
                ? getRgbDifferenceColor(original, colorDistance)
                : getImageDifferenceStrictColor(original, colorDistance);
    }

    private double getHsbColorDistance(Color color0, Color color1) {
        var quadraticHeuDistance = Math.pow(hueDistance(color0, color1), 2);
        var quadraticSaturationDistance = Math.pow(saturationDistance(color0, color1), 2);
        var quadraticBrightnessDistance = Math.pow(brightnessDistance(color0, color1), 2);

        return Math.sqrt(quadraticHeuDistance + quadraticSaturationDistance + quadraticBrightnessDistance);
    }

    private double hueDistance(Color color0, Color color1) {
        final var HUE_MAX = 360;
        final var HUE_DISTANCE_RANGE = 180;

        var hue0 = color0.getHue();
        var hue1 = color1.getHue();
        var hueAbs = Math.abs(hue1 - hue0);
        var maxHueAbs = Math.abs(hue1 - hue0) % HUE_MAX;

        return Math.min(hueAbs, maxHueAbs) / HUE_DISTANCE_RANGE;
    }

    private double saturationDistance(Color color0, Color color1) {
        return Math.abs(color1.getSaturation() - color0.getSaturation());
    }

    private double brightnessDistance(Color color0, Color color1) {
        final var BRIGHTNESS_MAX = 255;

        return Math.abs(color1.getBrightness() - color0.getBrightness()) / BRIGHTNESS_MAX;
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

    private Color getRgbDifferenceColor(Color original, double colorDistance) {
        if (colorDistance > acceptableDeviation) {
            if (colorDistance < acceptableDifference) {
                return calculateRgbDifferenceColor(DifferenceColor.LOWER, colorDistance);
            } else if (colorDistance > acceptableDifference) {
                return calculateRgbDifferenceColor(DifferenceColor.HIGHER, colorDistance);
            }
        }

        return original;
    }

    @SuppressWarnings("squid:S4276")
    private Color calculateRgbDifferenceColor(DifferenceColor differenceColor, double colorDistance) {
        Function<Double, Integer> getRgbChannelValue = value -> (int) Math.floor(value * 255);

        var redChannel = getRgbChannelValue.apply(differenceColor.getColor().getRed());
        var greenChannel = getRgbChannelValue.apply(differenceColor.getColor().getGreen());
        var blueChannel = getRgbChannelValue.apply(differenceColor.getColor().getBlue());
        var colorDistancePercentage = (int) (Math.floor(
                (colorDistance / (getImageDistanceRange().getMax() - acceptableDeviation)) * 100));
        Function<Integer, Integer> getCalculatedChannelValue = value -> {
            int result = value;
            if (colorDistancePercentage != 0) {
                result = (int) (value * Math.pow(colorDistancePercentage, -2));
            }
            return Math.min(result, 255);
        };

        int calculatedRedChannel = getCalculatedChannelValue.apply(redChannel);
        int calculatedGreenChannel = getCalculatedChannelValue.apply(greenChannel);
        int calculatedBlueChannel = getCalculatedChannelValue.apply(blueChannel);

        return Color.rgb(
                getBrighterDifferenceColor(calculatedRedChannel),
                getBrighterDifferenceColor(calculatedGreenChannel),
                getBrighterDifferenceColor(calculatedBlueChannel));
    }

    private int getBrighterDifferenceColor(int calculatedChannelColor) {
        final int BRIGHTNESS_OFFSET = 140;
        boolean canBeBrighter = calculatedChannelColor > 0
                && calculatedChannelColor < 255
                && calculatedChannelColor + BRIGHTNESS_OFFSET < 255;

        return canBeBrighter ? calculatedChannelColor + BRIGHTNESS_OFFSET : calculatedChannelColor;
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
