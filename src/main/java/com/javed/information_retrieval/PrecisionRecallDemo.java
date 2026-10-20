package com.javed.retrieval;

import java.util.*;

public class PrecisionRecallDemo {

    public record Metrics(
            int relevantRetrieved,
            int retrievedCount,
            int relevantCount,
            double precision,
            double recall) {}

    public static Metrics evaluate(
            Set<String> relevantIds, Set<String> retrievedIds) {
        // Copying also rejects null sets and null document IDs.
        Set<String> relevant = Set.copyOf(
                Objects.requireNonNull(relevantIds, "relevantIds"));
        Set<String> retrieved = Set.copyOf(
                Objects.requireNonNull(retrievedIds, "retrievedIds"));

        int relevantRetrieved = 0;
        for (String id : retrieved) {
            if (relevant.contains(id)) relevantRetrieved++;
        }

        double precision = retrieved.isEmpty() ?
                Double.NaN : (double) relevantRetrieved / retrieved.size();

        double recall = retrieved.isEmpty()
                ? Double.NaN
                : (double) relevantRetrieved / relevant.size();


        return new Metrics(relevantRetrieved, retrieved.size(), relevant.size(), precision, recall);
    }

    private static String percentage(double value) {
        return Double.isNaN(value)
                ? "N/A"
                : String.format(Locale.ROOT, "%.1f%%", value * 100);
    }

    public static void main(String[] args) {
        // Assumed complete relevance labels for this example query and corpus.
        Set<String> relevantIds = Set.of("D2", "D4", "D5");

        // Illustrative ranking of unique IDs, not measured BM25 output.
        List<String> rankedIds = List.of(
                "D2", "D1", "D4", "D3", "D5", "D6");

        System.out.printf("%-3s %-7s %-11s %-11s%n",
                "k", "Found", "Precision", "Recall");

        for (int k= 1; k<= rankedIds.size(); k++) {
            Set<String> retrievedIds = new LinkedHashSet<>(
                    rankedIds.subList(0, k)
            );

            Metrics result = evaluate(relevantIds, retrievedIds);
            System.out.printf(Locale.ROOT, "%-3d %-7d %-11s %-11s%n",
                    k, result.relevantRetrieved(),
                    percentage(result.precision()),
                    percentage(result.recall()));
        }
    }
}
