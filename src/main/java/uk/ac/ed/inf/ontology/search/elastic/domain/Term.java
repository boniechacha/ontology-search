package uk.ac.ed.inf.ontology.search.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Getter
@Setter
@Document(indexName = "term")
@AllArgsConstructor
@NoArgsConstructor
public class Term {

    @Id
    private String id;
    private String name;
    private String def;
    private String comment;
    private List<String> synonyms;

    public static String cleanId(String id) {
        return id.replaceAll("[^A-Za-z0-9]", "_");
    }
}