package com.javed.retrieval;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class RetrievalDemo {

    record Document(String id, String content) {}

    record SearchResult(Document document, long score) {}

    public static void main(String[] args) {


        List<Document> documents = List.of(
                new Document("DOC-1",
                        "PostgreSQL database connection failures can be caused by invalid credentials."),

                new Document("DOC-2",
                        "Kubernetes pods may restart due to memory limits."),

                new Document("DOC-3",
                        "MySQL database connection errors can occur due to an incorrect port."),

                new Document("DOC-4",
                        "Redis cache failures can occur when the server is unavailable.")
        );

        String question = "Postgres authentication problem";

        List<String> keywords = Arrays.asList(
                question.toLowerCase(Locale.ROOT).split("\\s+")
        );


        documents.stream()
                .map(
                        doc -> new SearchResult(
                            doc,
                            keywords.stream()
                                .filter( word -> doc.content()
                                        .toLowerCase(Locale.ROOT).contains(word)

                                ).count()
                        )

                ).filter(searchResult -> searchResult.score > 0)
                .sorted(Comparator.comparingLong(SearchResult::score).reversed())
                .forEach(result ->
                        System.out.println(
                                result.document().id()
                                        + " | Score: " + result.score()
                                        + " | " + result.document().content()
                        )

                        );



    }
}
