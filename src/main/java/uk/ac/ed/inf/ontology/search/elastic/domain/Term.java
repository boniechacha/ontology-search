package uk.ac.ed.inf.ontology.search.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

import static uk.ac.ed.inf.ontology.search.elastic.domain.Term.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Mapping(mappingPath = MAPPING_CONFIG)
@Setting(settingPath = SETTING_CONFIG)
@Document(indexName = INDEX_NAME, type = INDEX_NAME)
public class Term {

    public static final String INDEX_NAME = "term";
    public static final String NAMESPACE_FIELD = "namespace";
    public static final String MAPPING_CONFIG = "/elasticsearch/term-mapping.json";
    public static final String SETTING_CONFIG = "/elasticsearch/term-setting.json";

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
        if(notes == null) notes = new LinkedHashSet<>();
        this.notes.add(note);
    }
}