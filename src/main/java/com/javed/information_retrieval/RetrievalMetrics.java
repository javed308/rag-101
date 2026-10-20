package com.javed.retrieval;

import java.util.*;

public class RetrievalMetrics {

    private record Counts(int relevantInTopK, int totalRelevant) {}

    private static double precisionAtK(Set<String> relevantIds, List<String> rankedIds, int k) {

        Counts counts = countAtK(relevantIds, rankedIds, k);

        return (double) counts.relevantInTopK()/ k;
    }

    private static double recallAtK(Set<String> relevantIds, List<String> rankedIds, int k) {
        Counts counts = countAtK(relevantIds, rankedIds, k);

        return  counts.totalRelevant() == 0
                ? Double.NaN
                : (double) counts.relevantInTopK() / counts.totalRelevant();
    }

    private static Counts countAtK(Set<String> relevantIds, List<String> rankedIds, int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be greater than 0");
        }

        Set<String> relevant = Set.copyOf(
                Objects.requireNonNull(relevantIds, "relevantIds"));

        List<String> ranked = List.copyOf(
                Objects.requireNonNull(rankedIds, "rankedIds"));

        if (new HashSet<>(ranked).size() != ranked.size()) {
            throw new IllegalArgumentException(
                    "rankedIds must contain unique document IDs");
        }

        int relevantInTopK = 0;
        int limit = Math.min(k, ranked.size());

        for(int i=0;i<limit;i++) {
            if(relevant.contains(ranked.get(i))) {
                relevantInTopK++;
            }
        }

        return new Counts(relevantInTopK, relevant.size());
    }

    private static String percentage(double value) {
        return Double.isNaN(value)
                ? "N/A"
                : String.format(Locale.ROOT, "%.1f%%", value * 100);
    }

    public static void main(String[] args) {

        Set<String> relevantIds = Set.of(
                "D2", "D4", "D5", "D8", "D12", "D16", "D19", "D23");

        List<String> rankedIds = List.of(
                "D2", "D1", "D4", "D3", "D5",
                "D6", "D7", "D8", "D9", "D10",
                "D11", "D12", "D13", "D14", "D15",
                "D16", "D17", "D18", "D19", "D20");

        System.out.printf("%-4s %-14s %-12s%n",
                "K", "Precision@K", "Recall@K");

        for (int k : new int[]{5, 10, 20}) {
            double precision = RetrievalMetrics.precisionAtK(
                    relevantIds, rankedIds, k);

            double recall = RetrievalMetrics.recallAtK(
                    relevantIds, rankedIds, k);

            System.out.printf(Locale.ROOT, "%-4d %-14s %-12s%n",
                    k, percentage(precision), percentage(recall));
        }
    }
}
