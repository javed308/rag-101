package com.javed.vector_search;

public class DotProductDemo {

    static double dotProduct(float[] a, float[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException(
                    "Vectors must not be null");
        }

        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "Vectors must have the same number of coordinates");
        }


        if (a.length == 0) {
            throw new IllegalArgumentException(
                    "Vectors must not be empty");
        }

        double sum = 0.0;

        for(int i=0;i<a.length;i++) {
            if (!Float.isFinite(a[i]) || !Float.isFinite(b[i])) {
                throw new IllegalArgumentException(
                        "Coordinates must be finite numbers");
            }

            sum += (double) a[i] * b[i];
        }

        return sum;
    }

    public static void main(String[] args) {

        float[] a = {1, 2, 3};
        float[] b = {4, 5, 6};

        System.out.println("Example: " + dotProduct(a, b));

        float[] right = {2, 0};
        float[] alsoRight = {3, 0};
        float[] up = {0, 3};
        float[] left = {-3, 0};
        float[] zero = {0, 0};

        System.out.println(
                "Same direction: " + dotProduct(right, alsoRight));

        System.out.println(
                "Right angle: " + dotProduct(right, up));

        System.out.println(
                "Opposite directions: " + dotProduct(right, left));

        System.out.println(
                "Zero vector: " + dotProduct(right, zero));
    }
}
