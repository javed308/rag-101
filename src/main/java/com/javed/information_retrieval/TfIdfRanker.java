package com.javed.retrieval;

import java.util.*;

public class TfIdfRanker {

    record Document(String id, String content){}
    public record SearchHit(String documentId, double similarity) {}

    private final List<Document> corpus;
    private final Map<String, Integer> documentFrequency = new LinkedHashMap<>();

    public TfIdfRanker(List<Document> documents) {
        Objects.requireNonNull(documents, "documents");
        if (documents.isEmpty()) throw new IllegalArgumentException("Corpus must not be empty");

        this.corpus = List.copyOf(documents);

        for (Document document : corpus) {
            for(String term : new LinkedHashSet<>(tokenize(document.content()))){
                documentFrequency.merge(term, 1, Integer::sum);
            }
        }

    }

    private static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(term -> !term.isEmpty())
                .toList();
    }

    public Map<String, Double> calculateTfIdf(Document document) {
        Map<String, Integer> tf = new LinkedHashMap<>();

        for (String term : tokenize(document.content())){
            tf.merge(term, 1, Integer::sum);
        }

        Map<String, Double> weights = new LinkedHashMap<>();
        for(var entry : tf.entrySet()) {
            String term = entry.getKey();
            int df = documentFrequency.getOrDefault(term, 0);
            if(df == 0) {
                continue;
            }
            double idf = Math.log( (double) corpus.size() / df);
            double tf_idf = entry.getValue() * idf;
            if(tf_idf > 0) {
                weights.put(term, tf_idf);
            }
        }

        return weights;
    }

    private static double cosineSimilarity(Map<String, Double> first, Map<String, Double> second) {
        double dotProduct = 0;

        for(var entry : first.entrySet()) {
            dotProduct += entry.getValue() * second.getOrDefault(entry.getKey(), 0.0);
        }
        double normFirst = Math.sqrt(first.values().stream().mapToDouble(w -> w * w).sum());
        double normSecond = Math.sqrt(second.values().stream().mapToDouble(w -> w * w).sum());

        if(normFirst == 0 || normSecond == 0) {
            return 0.0;
        }
        return dotProduct/(normFirst * normSecond);
    }

    public List<SearchHit> rankDocuments(String query) {
        Map<String, Double> queryVector = calculateTfIdf(new Document("QUERY", query));

        return corpus.stream()
                .map(
                        doc -> new SearchHit(doc.id(), cosineSimilarity(queryVector, calculateTfIdf(doc)))
                ).filter(hit -> hit.similarity() > 0)
                .sorted(
                        Comparator.comparingDouble(SearchHit::similarity).reversed()
                                .thenComparing(SearchHit::documentId)
                ).toList();
    }

    private void showResults(String query) {
        System.out.println("\nQuery: " + query);
        List<SearchHit> hits = rankDocuments(query);
        if (hits.isEmpty()) System.out.println("  No matching documents");
        for (int i = 0; i < hits.size(); i++) {
            SearchHit hit = hits.get(i);
            System.out.printf(Locale.ROOT, "  %d. %s -> %.3f%n",
                    i + 1, hit.documentId(), hit.similarity());
        }

    }


    public static void main(String[] args) {

        List<Document> documents = List.of(
                new Document("D1", "Java Java Spring Boot tutorial"),
                new Document("D2", "Java memory management"),
                new Document("D3", "Spring Security tutorial")
        );
        TfIdfRanker ranker = new TfIdfRanker(documents);
        System.out.println("TF-IDF vector for D1:");
        ranker.calculateTfIdf(documents.get(0)).forEach((term, weight) ->
                System.out.printf(Locale.ROOT, "  %s -> %.3f%n", term, weight));

        ranker.showResults("java memory");
        ranker.showResults("spring security");
        ranker.showResults("database failure");
    }
}
