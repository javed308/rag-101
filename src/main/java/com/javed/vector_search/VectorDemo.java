package com.javed.vector_search;

import java.util.Arrays;
import java.util.Objects;

public class VectorDemo {

    static void describe(String name, double[] vector) {
        Objects.requireNonNull(vector, "vector");

        if (vector.length == 0) {
            throw new IllegalArgumentException("Vector must not be empty");
        }

        int nonZero = 0;

        for (double value : vector) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Values must be finite");
            }

            if (value != 0.0) {
                nonZero++;
            }
        }

        System.out.println(name);
        System.out.println("Vector: " + Arrays.toString(vector));
        System.out.println("Dimensions: " + vector.length);
        System.out.println("Non-zero values: " + nonZero);
        System.out.println("Zero values: " + (vector.length - nonZero));
        System.out.println();

    }

    public static void main(String[] args) {
        // Vocabulary order:
        // java, developer, software, engineer,
        // database, bread, travel, music
        double[] sparseCounts = {1, 1, 0, 0, 0, 0, 0, 0};

        // Invented values for learning.
        // NOT produced by an embedding model.
        double[] mockEmbedding = {
                0.17, -0.42, 0.91, 0.08,
                -0.23, 0.65, -0.11, 0.34
        };

        describe("Sparse count vector", sparseCounts);
        describe("Dense mock embedding", mockEmbedding);
    }
}
