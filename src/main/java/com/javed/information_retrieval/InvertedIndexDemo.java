package com.javed.retrieval;

import java.util.*;

public class InvertedIndexDemo {

    record Document(String id, String content) {}

    static class InvertedIndex{
        // Term -> Document IDs
        private final Map<String, Set<String>> postings =
                new LinkedHashMap<>();

        // Document ID -> Original document
        private final Map<String, Document> documents =
                new LinkedHashMap<>();

        // Step 1: Convert text into terms
        private List<String> tokenize(String text) {
            if (text == null || text.isBlank()) {
                return List.of();
            }

            return Arrays.stream(
                            text.toLowerCase(Locale.ROOT)
                                    .split("[^a-z0-9]+"))
                    .filter(term -> !term.isEmpty())
                    .toList();
        }

        public void addDocument(Document document) {
            Objects.requireNonNull(document);

            if (document.id() == null ||
                    document.id().isBlank()) {
                throw new IllegalArgumentException(
                        "Document ID cannot be empty");
            }

            // Remove old data if the ID already exists
            deleteDocument(document.id());

            documents.put(document.id(), document);

            Set<String> terms = new LinkedHashSet<>(tokenize(document.content()));

            for(String term : terms) {
                postings.computeIfAbsent(
                        term,
                        key -> new LinkedHashSet<>()
                ).add(document.id());
            }
        }

        public Set<String> search(String query) {
            List<String> terms = tokenize(query)
                    .stream()
                    .distinct()
                    .toList();

            if(terms.isEmpty()) {
                return Set.of();
            }

            Set<String> result = new LinkedHashSet<>(
                    postings.getOrDefault(terms.get(0), Set.of())
            );

            for ( int i=1; i<terms.size(); i++) {
                result.retainAll(
                  postings.getOrDefault(terms.get(i), Set.of())
                );

                if(result.isEmpty()) {
                    break;
                }
            }

            return result;

        }

        public Set<String> orSearch(String query) {
            List<String> terms = tokenize(query)
                    .stream().distinct().toList();

            if(terms.isEmpty()) {
                return Set.of();
            }

            Set<String> result = new LinkedHashSet<>(
                    postings.getOrDefault(terms.get(0), Set.of())
            );

            for(int i=1 ;i<terms.size();i++) {
                result.addAll(postings.getOrDefault(terms.get(i), Set.of()));
            }

            return result;

        }

        public boolean deleteDocument(String id) {
            Document removed = documents.remove(id);

            if (removed == null) {
                return false;
            }

            Set<String> terms = new LinkedHashSet<>(tokenize(removed.content()));

            for(String term : terms) {
                Set<String> ids = postings.get(term);

                if(ids != null) {
                    ids.remove(id);

                    // Remove terms with no documents
                    if(ids.isEmpty()) {
                        postings.remove(term);
                    }
                }
            }

            return true;
        }

        public void printIndex() {
            postings.forEach((term, ids) ->
                    System.out.println(
                            term + " -> " + ids));
        }
    }

    public static void main(String[] args) {
        InvertedIndex index = new InvertedIndex();

        index.addDocument(new Document(
                "D1", "Java Spring Boot tutorial"));

        index.addDocument(new Document(
                "D2", "Java memory management"));

        index.addDocument(new Document(
                "D3", "Spring Security tutorial"));

        System.out.println("Original inverted index:");
        index.printIndex();

        System.out.println(
                "\nSearch 'spring': " +
                        index.search("spring"));

        System.out.println(
                "Search 'spring security': " +
                        index.search("spring security"));

        System.out.println(
                "Search 'java security': " +
                        index.search("java security"));

        System.out.println(
                "\nDelete D3: " +
                        index.deleteDocument("D3"));

        System.out.println(
                "Search 'spring' after delete: " +
                        index.search("spring"));

        System.out.println(
                "Search 'security' after delete: " +
                        index.search("security"));

        // Replace an existing document
        index.addDocument(new Document(
                "D1", "Java memory tutorial"));

        System.out.println(
                "\nReplace D1, then search 'spring': " +
                        index.search("spring"));

        System.out.println(
                "Search 'memory': " +
                        index.search("memory"));
    }
}
