package uk.ac.ed.inf.ontology.search.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

import static uk.ac.ed.inf.ontology.search.elastic.domain.Term.INDEX_NAME;
import static uk.ac.ed.inf.ontology.search.elastic.domain.Term.MAPPING_CONFIG;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = INDEX_NAME,type = INDEX_NAME)
@Mapping(mappingPath = MAPPING_CONFIG)
public class Term {

    public static final String INDEX_NAME = "term";
    public static final String MAPPING_CONFIG = "/elasticsearch/term.json";
    public static final String NAMESPACE_FIELD = "namespace";

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