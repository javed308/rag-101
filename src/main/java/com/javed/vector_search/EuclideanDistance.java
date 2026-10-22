package com.javed.vector_search;

import java.util.Locale;

public class EuclideanDistance {

    public static double squaredEuclideanDistance(float[] a, float[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }
        if (a.length != b.length) {
            throw new IllegalArgumentException("Dimensions must match");
        }
        if (a.length == 0) {
            throw new IllegalArgumentException("Vectors must not be empty");
        }

        double sum = 0.0;

        for (int i = 0; i < a.length; i++) {
            if (!Float.isFinite(a[i]) || !Float.isFinite(b[i])) {
                throw new IllegalArgumentException("Values must be finite");
            }

            double diff = (double) a[i] - b[i];
            sum += diff * diff;
        }

        return sum;
    }

    public static double euclideanDistance(float[] a, float[] b) {
        return Math.sqrt(squaredEuclideanDistance(a, b));
    }

    public static void main(String[] args) {
        float[] a = {5, 12};
        float[] b = {12, 5};

        System.out.printf(Locale.ROOT, "Simple example: %.6f%n", euclideanDistance(new float[]{1, 2}, new float[]{4, 6}));

        System.out.printf(Locale.ROOT, "Different directions: %.6f%n", euclideanDistance(a, b));

        System.out.printf(Locale.ROOT, "Same direction, longer: %.6f%n", euclideanDistance(a, new float[]{10, 24}));

        System.out.printf(Locale.ROOT, "Identical vectors: %.6f%n", euclideanDistance(a, a));

        System.out.printf(Locale.ROOT, "Distance from zero: %.6f%n", euclideanDistance(a, new float[]{0, 0}));

        System.out.printf(Locale.ROOT, "Squared distance: %.6f%n", squaredEuclideanDistance(a, b));
    }
}
