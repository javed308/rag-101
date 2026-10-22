package com.javed.vector_search;

import java.util.Locale;

public class Cosine {

    public static double cosineSimilarity(float[] a, float[] b) {

        if (a == null || b == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "Vectors must have the same dimensions");
        }
        if (a.length == 0) {
            throw new IllegalArgumentException("Vectors must not be empty");
        }

        double dot = 0.0;
        double sumSquaresA = 0.0;
        double sumSquaresB = 0.0;

        for(int i=0;i<a.length;i++) {
            if (!Float.isFinite(a[i]) || !Float.isFinite(b[i])) {
                throw new IllegalArgumentException("Values must be finite");
            }

            double x = a[i];
            double y = b[i];

            dot += x * y;
            sumSquaresA += x * x;
            sumSquaresB += y * y;
        }

        double magnitudeA = Math.sqrt(sumSquaresA);
        double magnitudeB = Math.sqrt(sumSquaresB);

        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            throw new IllegalArgumentException(
                    "Cosine similarity is undefined for a zero vector");
        }

        double similarity = dot / (magnitudeA * magnitudeB);

        return Math.max(-1, Math.min(1, similarity));
    }

    public static void main(String[] args) {

        float[] a = {5, 12};
        float[] b = {12, 5};

        System.out.printf(Locale.ROOT, "Different directions: %.6f%n", cosineSimilarity(a, b));

        System.out.printf(Locale.ROOT, "Same direction: %.6f%n", cosineSimilarity(a, new float[]{10, 24}));

        System.out.printf(Locale.ROOT, "Right angle: %.6f%n", cosineSimilarity(new float[]{1, 0}, new float[]{0, 1}));

        System.out.printf(Locale.ROOT, "Opposite directions: %.6f%n", cosineSimilarity(a, new float[]{-5, -12}));

        try {
            cosineSimilarity(a, new float[]{0, 0});
        } catch (IllegalArgumentException e) {
            System.out.println("Zero vector: " + e.getMessage());
        }

    }
}
