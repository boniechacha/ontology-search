package uk.ac.ed.inf.ontology.search.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

    @NotEmpty
    @NotNull
    private String namespace;

    private String def;
    private Set<String> synonyms;
    private Set<String> notes;

    public Term(String namespace) {
        this.namespace = namespace;
        synonyms = new LinkedHashSet<>();
        notes = new LinkedHashSet<>();
    }

    public static String cleanId(String id) {
        return id.replaceAll("[^A-Za-z0-9]", "_");
    }

    public void addNote(String note) {
        this.notes.add(note);
    }
}