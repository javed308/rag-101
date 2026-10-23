package com.javed.vector_search;

public class ExactSearch {

    record Result(int index, double squaredDistance,
                  long vectorComparisons, long coordinateVisits) {}

    static Result nearest(float[] query, float[][] vectors) {
        if (query == null || query.length == 0) {
            throw new IllegalArgumentException("Query must not be null or empty");
        }
        if (vectors == null || vectors.length == 0) {
            throw new IllegalArgumentException("At least one stored vector is required");
        }
        for (float value : query) {
            if (!Float.isFinite(value)) {
                throw new IllegalArgumentException("Query values must be finite");
            }
        }

        int bestIndex = -1;
        double bestDistance = Double.POSITIVE_INFINITY;
        long comparisons = 0;
        long visits = 0;

        for(int i=0;i< vectors.length;i++) {
            float[] vector = vectors[i];
            if (vector == null || vector.length != query.length) {
                throw new IllegalArgumentException("Invalid dimensions at index " + i);
            }

            double distance = 0.0;
            for(int j=0;j<query.length;j++) {
                if (!Float.isFinite(vector[j])) {
                    throw new IllegalArgumentException("Stored values must be finite");
                }
                double difference = (double) query[j] - vector[j];
                distance += difference * difference;
                visits++;
            }

            comparisons++;

            if(distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }

        return new Result(bestIndex, bestDistance, comparisons, visits);
    }

    public static void main(String[] args) {
        float[] query = {1, 2};
        float[][] vectors = {{4, 6}, {2, 2}, {8, 2}, {1, 4}};

        Result result = nearest(query, vectors);

        System.out.println("Nearest: D" + (result.index() + 1));
        System.out.println("Squared distance: " + result.squaredDistance());
        System.out.println("Distance: " + Math.sqrt(result.squaredDistance()));
        System.out.println("Vector comparisons: " + result.vectorComparisons());
        System.out.println("Coordinate visits: " + result.coordinateVisits());
    }
}
