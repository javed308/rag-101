package com.javed.retrieval;

import java.util.*;

public class DcgMetrics {


    private static final double LOG_2 = Math.log(2.0);

    private DcgMetrics() {}

    public record QueryResult(
            Map<String, Integer> relevanceGrades,
            List<String> rankedIds) {

        public QueryResult {
            relevanceGrades = Map.copyOf(
                    Objects.requireNonNull(relevanceGrades, "relevanceGrades"));
            rankedIds = List.copyOf(
                    Objects.requireNonNull(rankedIds, "rankedIds"));

            for (var entry : relevanceGrades.entrySet()) {
                if (entry.getKey().isBlank()) {
                    throw new IllegalArgumentException("Document ID is blank");
                }
                if (entry.getValue() < 0 || entry.getValue() > 3) {
                    throw new IllegalArgumentException("Grades must be 0 to 3");
                }
            }

            if (new HashSet<>(rankedIds).size() != rankedIds.size()) {
                throw new IllegalArgumentException("Duplicate ranked IDs");
            }

            // An unjudged document is not automatically irrelevant.
            for (String id : rankedIds) {
                if (!relevanceGrades.containsKey(id)) {
                    throw new IllegalArgumentException("Missing grade for " + id);
                }
            }
        }

    }


    public static double dcg(QueryResult query) {
        return dcgAtK(query, Integer.MAX_VALUE);
    }

    /** Full ranking compared with all known document grades. */
    public static double ndcg(QueryResult query) {
        return ndcgAtK(query, Integer.MAX_VALUE);
    }

    public static double dcgAtK(QueryResult query, int k) {
        validate(query, k);
        int limit = Math.min(k, query.rankedIds().size());
        double total = 0.0;

        for (int i = 0; i < limit; i++) {
            String id = query.rankedIds().get(i);
            int grade = query.relevanceGrades().get(id);
            total += discountedGain(grade, i + 1);
        }
        return total;
    }

    public static double idealDcgAtK(QueryResult query, int k) {
        validate(query, k);

        // Use ALL known grades, including documents not retrieved.
        List<Integer> idealGrades = new ArrayList<>(
                query.relevanceGrades().values());
        idealGrades.sort(Comparator.reverseOrder());

        int limit = Math.min(k, idealGrades.size());
        double total = 0.0;
        for (int i = 0; i < limit; i++) {
            total += discountedGain(idealGrades.get(i), i + 1);
        }
        return total;
    }

    public static double ndcgAtK(QueryResult query, int k) {
        double actual = dcgAtK(query, k);
        double ideal = idealDcgAtK(query, k);
        return ideal == 0.0 ? Double.NaN : actual / ideal;
    }

    private static double discountedGain(int grade, int rank) {
        double gain = Math.pow(2.0, grade) - 1.0;
        double discountDenominator = Math.log(rank + 1.0) / LOG_2;
        return gain / discountDenominator;
    }

    private static void validate(QueryResult query, int k) {
        Objects.requireNonNull(query, "query");
        if (k <= 0) {
            throw new IllegalArgumentException("K must be positive");
        }
    }


    public static void main(String[] args) {

        Map<String, Integer> grades = Map.of(
                "D1", 0, "D2", 3, "D3", 0,
                "D4", 2, "D5", 1, "D6", 3);

        QueryResult actual = new QueryResult(grades,
                List.of("D2", "D1", "D4", "D3", "D5"));
        QueryResult ideal = new QueryResult(grades,
                List.of("D2", "D6", "D4", "D5", "D1"));
        QueryResult poorer = new QueryResult(grades,
                List.of("D5", "D1", "D4", "D3", "D2"));

        System.out.println("K  DCG@K    IDCG@K   nDCG@K");
        for (int k : new int[]{1, 3, 5}) {
            System.out.printf(Locale.ROOT, "%d  %.4f   %.4f   %.4f%n",
                    k, DcgMetrics.dcgAtK(actual, k),
                    DcgMetrics.idealDcgAtK(actual, k),
                    DcgMetrics.ndcgAtK(actual, k));
        }

        
    }
}
