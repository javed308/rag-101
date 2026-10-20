package com.javed.retrieval;

import java.util.*;

public class TfDfIdfDemo {

    record Document(String id, String content){}

    static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();

        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(word -> !word.isEmpty())
                .toList();
    }

    public static void main(String[] args) {

        List<Document> corpus = List.of(
                new Document("D1", "Java Java Spring Boot tutorial"),
                new Document("D2", "Java memory management"),
                new Document("D3", "Spring Security tutorial")
        );

        Map<String, Map<String, Integer>> tfByDocument = new LinkedHashMap<>();
        Map<String, Set<String>> postings = new LinkedHashMap<>();

        for (Document document : corpus) {
            Map<String, Integer> counts = new LinkedHashMap<>();
            List<String> terms = tokenize(document.content());

            for(String term : terms) {
                counts.merge(term, 1, Integer::sum);
            }

            tfByDocument.put( document.id(), counts);

            for (String term : counts.keySet()) {
                postings.computeIfAbsent(term, key -> new LinkedHashSet<>()).add(document.id());
            }
        }

        int totalDocuments = corpus.size();

        System.out.println("Number of documents (N): " + totalDocuments);
        System.out.printf("%-12s %6s %6s %6s %4s %7s%n",
                "TERM", "TF D1", "TF D2", "TF D3", "DF", "IDF");
        for (var entry : postings.entrySet()) {
            String term = entry.getKey();
            int df = entry.getValue().size();
            double idf = Math.log((double) totalDocuments / df);
            System.out.printf(Locale.ROOT, "%-12s %6d %6d %6d %4d %7.3f%n",
                    term,
                    tfByDocument.get("D1").getOrDefault(term, 0),
                    tfByDocument.get("D2").getOrDefault(term, 0),
                    tfByDocument.get("D3").getOrDefault(term, 0),
                    df, idf);
        }

        List<String> queryTerms = tokenize("java memory").stream().distinct().toList();
        System.out.println("TF-IDF query: java memory");
        for(Document document : corpus) {
            double score = 0.0;

            for(String term : queryTerms) {
                Set<String> ids = postings.get(term);

                if (ids == null) continue;

                int tf = tfByDocument.get(document.id()).getOrDefault(term, 0);
                double idf = Math.log((double) totalDocuments/ids.size());
                score += tf * idf;
            }
            System.out.printf(Locale.ROOT, "%s -> %.3f%n", document.id(), score);
        }

    }
}
