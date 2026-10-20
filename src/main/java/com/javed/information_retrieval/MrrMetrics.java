package com.javed.retrieval;

import java.util.*;

public class MrrMetrics {

    public record QueryResult(Set<String> relevantIds, List<String> rankedIds) {
        public QueryResult {
            relevantIds = Set.copyOf(
                    Objects.requireNonNull(relevantIds, "relevantIds"));
            rankedIds = List.copyOf(
                    Objects.requireNonNull(rankedIds, "rankedIds"));

            if (relevantIds.isEmpty()) {
                throw new IllegalArgumentException(
                        "Each query must have at least one known relevant ID");
            }
            if (new HashSet<>(rankedIds).size() != rankedIds.size()) {
                throw new IllegalArgumentException(
                        "rankedIds must contain unique document IDs");
            }

        }
    }

    public static double reciprocalRank(QueryResult result) {
        return reciprocalRankAtK(result, Integer.MAX_VALUE);
    }

    public static double reciprocalRankAtK(QueryResult result, int k) {
        Objects.requireNonNull(result, "result");

        if (k <= 0) {
            throw new IllegalArgumentException("k must be greater than 0");
        }
        int limit = Math.min(k, result.rankedIds().size());

        for (int i = 0; i < limit; i++) {
            String documentId = result.rankedIds().get(i);

            if(result.relevantIds.contains(documentId)) {
                return 1.0 / ( i + 1);
            }
        }

        return 0.0;
    }

    public static double meanReciprocalRank(List<QueryResult> results) {
        return meanReciprocalRankAtK(results, Integer.MAX_VALUE);
    }

    public static double meanReciprocalRankAtK(List<QueryResult> results, int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be greater than 0");
        }

        List<QueryResult> queries = List.copyOf(
                Objects.requireNonNull(results, "results"));

        if (queries.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one query is required to calculate MRR");
        }

        double total = 0.0;

        for (QueryResult result : results) {
            total += reciprocalRankAtK(result, k);
        }

        return total / queries.size();
    }


    public static void main(String[] args) {
        List<QueryResult> queries = List.of(
                new QueryResult(
                        Set.of("D2", "D4", "D5"),
                        List.of("D2", "D1", "D4", "D3", "D5")),

                new QueryResult(
                        Set.of("D4"),
                        List.of("D1", "D4", "D2", "D3", "D5")),

                new QueryResult(
                        Set.of("D5"),
                        List.of("D1", "D3", "D6", "D5", "D2")),

                new QueryResult(
                        Set.of("D9"),
                        List.of("D1", "D3", "D6", "D7", "D8"))
        );

        System.out.println("Query  RR      RR@3");

        for (int i = 0; i < queries.size(); i++) {
            QueryResult query = queries.get(i);

            System.out.printf(Locale.ROOT, "Q%d     %.4f  %.4f%n",
                    i + 1,
                    MrrMetrics.reciprocalRank(query),
                    MrrMetrics.reciprocalRankAtK(query, 3));
        }

        System.out.printf(Locale.ROOT, "%nMRR (first 3 queries) = %.4f%n",
                MrrMetrics.meanReciprocalRank(queries.subList(0, 3)));

        System.out.printf(Locale.ROOT, "MRR (all 4 queries)   = %.4f%n",
                MrrMetrics.meanReciprocalRank(queries));

        System.out.printf(Locale.ROOT, "MRR@3                = %.4f%n",
                MrrMetrics.meanReciprocalRankAtK(queries, 3));

        System.out.printf(Locale.ROOT, "MRR@5                = %.4f%n",
                MrrMetrics.meanReciprocalRankAtK(queries, 5));


        QueryResult emptyRanking =
                new QueryResult(Set.of("D9"), List.of());

        System.out.printf(Locale.ROOT, "RR (empty ranking)   = %.4f%n",
                MrrMetrics.reciprocalRank(emptyRanking));

    }
}
