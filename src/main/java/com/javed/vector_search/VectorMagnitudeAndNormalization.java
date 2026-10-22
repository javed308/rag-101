package com.javed.vector_search;

import java.util.Arrays;
import java.util.Locale;

public class VectorMagnitudeAndNormalization {


    public static double magnitude(float[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException(
                    "Vector must not be null or empty");
        }

        double sumOfSquares = 0.0;

        for (float value : vector) {
            if (!Float.isFinite(value)) {
                throw new IllegalArgumentException(
                        "Values must be finite");
            }

            sumOfSquares += (double) value * value;
        }

        return Math.sqrt(sumOfSquares);
    }

    public static float[] normalize(float[] vector){
        double length = magnitude(vector);

        if (length == 0.0) {
            throw new IllegalArgumentException(
                    "Cannot normalize a zero vector");
        }


        float[] result = new float[vector.length];
        for (int i=0;i< vector.length;i++) {
            result[i] = (float) (vector[i] / length);
        }

        return result;
    }

    public static void main(String[] args) {

        float[] vector = {5, 12};
        float[] unitVector = normalize(vector);

        System.out.println("Original: " + Arrays.toString(vector));

        System.out.printf(Locale.ROOT, "Magnitude: %.6f%n", magnitude(vector));

        System.out.println("Normalized: " + Arrays.toString(unitVector));

        System.out.printf(Locale.ROOT, "New magnitude: %.6f%n", magnitude(unitVector));


    }
}
