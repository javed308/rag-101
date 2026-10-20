package com.javed.retrieval;

import java.util.*;

public class CorpusDemo {

    record Document(String id, String content) {}

    static List<String> tokenize(String text) {
        if(text == null || text.isBlank()) {
            return List.of();
        }

        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(token -> !token.isEmpty())
                .toList();
    }

    public static void main(String[] args) {

        // 1. Create a corpus
        List<Document> corpus = List.of(
                new Document("D1", "Java Spring Boot tutorial"),
                new Document("D2", "Java memory management"),
                new Document("D3", "Spring Security tutorial"),
                new Document("D4", "Spring Boot security guide")
        );

        // 2. Store unique terms
        Set<String> vocabulary = new LinkedHashSet<>();

        // 3. Term -> Document IDs
        Map<String, Set<String>> postingLists = new LinkedHashMap<>();

        int totalTokens = 0;

        // 4. Process each document
        for (Document document : corpus) {
            List<String> tokens = tokenize(document.content());

            System.out.println(
                    document.id() + " -> " + tokens
            );

            totalTokens += tokens.size();

            for (String token : tokens) {
                vocabulary.add(token);

                postingLists.computeIfAbsent(token, key -> new LinkedHashSet<>()).add(document.id());
            }
        }

        // 5. Print vocabulary
        System.out.println("\nCorpus size: " + corpus.size());
        System.out.println("Total tokens: " + totalTokens);
        System.out.println("Vocabulary: " + vocabulary);

        // 6. Print inverted index
        System.out.println("\nInverted Index:");

        postingLists.forEach((term, documents) ->
                System.out.println(term + " -> " + documents)
        );

        // check postingList for Spring
        System.out.println("Posting list of spring -> ");
        System.out.println(searchInPostingList("spring", postingLists));

        System.out.println("Posting list of security -> ");
        System.out.println(searchInPostingList("security", postingLists));
    }

    static Set<String> searchInPostingList( String str, Map<String, Set<String>> postingLists) {

        return postingLists.getOrDefault(
                str,
                Set.of()
        );
    }

}
