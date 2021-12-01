package com.example.imageprocessinglab.util;

public final class ImageDistanceRange {

    private final double min;
    private final double max;

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public ImageDistanceRange(double min, double max) {
        this.min = min;
        this.max = max;
    }
}
