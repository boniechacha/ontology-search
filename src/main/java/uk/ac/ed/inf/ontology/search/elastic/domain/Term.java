package uk.ac.ed.inf.ontology.search.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Document(indexName = "term")
@AllArgsConstructor
@NoArgsConstructor
public class Term {

    @Id
    private String id;

    @NotEmpty
    @NotNull
    private String name;
    private String def;
    private String comment;

    @NotEmpty
    @NotNull
    private String namespace;
    private List<String> synonyms;

    public Term(String namespace) {
        this.namespace = namespace;
    }

    public static String cleanId(String id) {
        return id.replaceAll("[^A-Za-z0-9]", "_");
    }
}