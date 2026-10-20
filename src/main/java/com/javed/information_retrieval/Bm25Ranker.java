package com.javed.retrieval;

import java.util.*;

/*
 * BM25 variant used:
 *   IDF(t) = ln(1 + (N - DF(t) + 0.5) / (DF(t) + 0.5))
 *   score(t,d) = IDF(t) * [TF(t,d) * (k1 + 1)] /
 *                         [TF(t,d) + k1 * (1 - b + b * |d| / avgdl)]
 * Scores are summed over distinct query terms (no query-frequency weighting)
 */
public class Bm25Ranker {

    public record Document(String id, String content) {}
    public record SearchHit(String documentId, double score) {}

    private final List<Document> corpus;
    private final Map<String, Map<String, Integer>> termFrequencies = new LinkedHashMap<>();
    private final Map<String, Integer> documentFrequency = new LinkedHashMap<>();
    private final Map<String, Integer> documentLengths = new LinkedHashMap<>();
    private final double averageDocumentLength;
    private final double k1;
    private final double b;

    public Bm25Ranker(List<Document> documents, double k1, double b) {
        Objects.requireNonNull(documents, "documents");
        if (documents.isEmpty()) throw new IllegalArgumentException("Corpus cannot be empty");
        if (!Double.isFinite(k1) || k1 <= 0.0)
            throw new IllegalArgumentException("k1 must be a finite positive number");
        if (!Double.isFinite(b) || b < 0.0 || b > 1.0)
            throw new IllegalArgumentException("b must be a finite number from 0 to 1");
        this.corpus = List.copyOf(documents);
        this.k1 = k1;
        this.b = b;

        int totalTokens = 0;
        for(Document doc : corpus) {
            Objects.requireNonNull(doc, "document");
            if (doc.id() == null || doc.id().isBlank())
                throw new IllegalArgumentException("Document IDs must not be blank");
            if (termFrequencies.containsKey(doc.id()))
                throw new IllegalArgumentException("Duplicate document ID: " + doc.id());

            List<String> tokens = tokenize(doc.content());
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (String term : tokens) {
                counts.merge(term, 1, Integer::sum);
            }

            termFrequencies.put(doc.id(), counts);
            documentLengths.put(doc.id(), tokens.size());
            totalTokens += tokens.size();

            for (String term : counts.keySet()) {
                documentFrequency.merge(term, 1, Integer::sum);
            }
        }
        averageDocumentLength = (double) totalTokens / corpus.size();
    }

    private static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(term -> !term.isEmpty())
                .toList();
    }

    public double idf(String term) {
        int df = documentFrequency.getOrDefault(term.toLowerCase(Locale.ROOT), 0);
        if (df == 0) return 0.0;
        double n = corpus.size();

        return Math.log(1 + (n - df + 0.5) / (df + 0.5));
    }

    public double scoreTerm(String term, Document doc) {
        String key = term.toLowerCase(Locale.ROOT);
        Map<String, Integer> counts = termFrequencies.get(doc.id());
        if (counts == null) throw new IllegalArgumentException("Unknown document: " + doc.id());
        int tf = counts.getOrDefault(key, 0);
        if (tf == 0) return 0.0;

        double lengthRatio = documentLengths.get(doc.id()) / averageDocumentLength;
        double lengthFactor = 1.0 - b + b * lengthRatio;
        double tfFactor = tf * (k1 + 1.0) / (tf + 1.0 * lengthFactor);
        return idf(key) * tfFactor;
    }

    public List<SearchHit> rankDocuments(String query) {
        Set<String> queryTerms = new LinkedHashSet<>(tokenize(query));
        List<SearchHit> hits = new ArrayList<>();

        for (Document doc : corpus) {
            double score = 0.0;
            for (String term : queryTerms) {
                score += scoreTerm(term, doc);
            }
            if (score > 0.0) hits.add(new SearchHit(doc.id(), score));
        }
        hits.sort(Comparator.comparingDouble(SearchHit::score)
                .reversed()
                .thenComparing(SearchHit::documentId)
        );

        return List.copyOf(hits);
    }

    private void printRanking(String query) {
        System.out.println("\nQuery: " + query);
        List<SearchHit> hits = rankDocuments(query);
        if (hits.isEmpty()) System.out.println(" No matching documents");
        for (int i = 0; i < hits.size(); i++) {
            SearchHit hit = hits.get(i);
            System.out.printf(Locale.ROOT, " %d. %s -> %.3f%n",
                    i + 1, hit.documentId(), hit.score());
        }
    }

    private void explain(String query, Document doc) {
        System.out.println("\nExplain: " + doc.id() + " against '" + query + "'");
        System.out.printf(Locale.ROOT, " length=%d, avgdl=%.3f%n",
                documentLengths.get(doc.id()), averageDocumentLength);
        for (String term : new LinkedHashSet<>(tokenize(query))) {
            int tf = termFrequencies.get(doc.id()).getOrDefault(term, 0);
            int df = documentFrequency.getOrDefault(term, 0);
            System.out.printf(Locale.ROOT,
                    " term=%-10s TF=%d DF=%d IDF=%.3f contribution=%.3f%n",
                    term, tf, df, idf(term), scoreTerm(term, doc));
        }
    }



    public static void main(String[] args) {

        List<Document> documents = List.of(
                new Document("D1", "Java Java Spring Boot tutorial"),
                new Document("D2", "Java memory management"),
                new Document("D3", "Spring Security tutorial")
        );
        Bm25Ranker ranker = new Bm25Ranker(documents, 1.2, 0.75);
        System.out.printf(Locale.ROOT, "Documents=%d, avgdl=%.3f, k1=1.2, b=0.75%n",
                documents.size(), ranker.averageDocumentLength);
        ranker.printRanking("java memory");
        ranker.printRanking("spring security");
        ranker.printRanking("postgresql");

        ranker.explain("java memory", documents.get(1));
    }
}
