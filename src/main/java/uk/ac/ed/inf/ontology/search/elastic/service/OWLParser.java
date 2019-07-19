package uk.ac.ed.inf.ontology.search.elastic.service;


import lombok.extern.java.Log;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.ac.ed.inf.ontology.search.elastic.domain.Term;

import javax.print.DocFlavor;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log
@Component
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class OWLParser {
    public static final List<String> NAME_PROPS = List.of("label");
    public static final List<String> DEFINITION_PROPS = List.of("IAO_0000115", "has_definition", "IAO_0000600");
    public static final List<String> SYNONYM_PROPS = List.of("IAO_0000118", "seeAlso");
    public static final List<String> NOTES_PROPS = List.of("comment", "IAO_0000600");


    public List<Term> parse(InputStream input, String namespace) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(input);
        return parse(ontology, namespace);
    }

    public List<Term> parse(OWLOntology ontology, String namespace) {
        return ontology.classesInSignature()
                .map(owlClass -> parseOWLClass(ontology, owlClass, namespace))
                .collect(Collectors.toList());
    }

    private Term parseOWLClass(OWLOntology ontology, OWLClass owlClass, String namespace) {
        log.info(String.format("Parsing : %s", owlClass.toString()));
        Set<OWLAnnotationAssertionAxiom> annotations = ontology.annotationAssertionAxioms(owlClass.getIRI()).collect(Collectors.toSet());

        Term term = new Term(namespace);

        String id = owlClass.getIRI().getShortForm();
        term.setId(id);

        String name = parseName(annotations);
        if(StringUtils.isEmpty(name)){
            log.warning(String.format("Failed to get name for %s using URI shorname instead!", owlClass));
            name = id;
        }

        term.setName(name);

        term.setDef(parseDefinition(annotations));
        term.setNotes(parseNotes(annotations));
        term.setSynonyms(parseSynonyms(annotations));

        return term;
    }

    private String parseName(Set<OWLAnnotationAssertionAxiom> annotations) {
        return fetchOne(annotations, NAME_PROPS);
    }


    private Set<String> parseSynonyms(Set<OWLAnnotationAssertionAxiom> annotations) {
        return fetchAll(annotations, SYNONYM_PROPS);
    }

    private Set<String> parseNotes(Set<OWLAnnotationAssertionAxiom> annotations) {
        return fetchAll(annotations, NOTES_PROPS);
    }

    private String parseDefinition(Set<OWLAnnotationAssertionAxiom> annotations) {
        return fetchOne(annotations, DEFINITION_PROPS);
    }

    private String fetchOne(Set<OWLAnnotationAssertionAxiom> annotations, Collection<String> props) {
        for (OWLAnnotationAssertionAxiom a : annotations) {
            if (props.contains(a.getProperty().getIRI().getShortForm())) {
                annotations.remove(a);
                return ((OWLLiteral) a.getValue()).getLiteral();
            }
        }

        return null;
    }

    private Set<String> fetchAll(Set<OWLAnnotationAssertionAxiom> annotations, Collection<String> props) {

        Set<OWLAnnotationAssertionAxiom> toRemove = new LinkedHashSet<>();

        Set<String> values = annotations.stream()
                .filter(a -> {
                    if (props.contains(a.getProperty().getIRI().getShortForm())) {
                        toRemove.add(a);
                        return true;
                    } else return false;
                })
                .map(a -> ((OWLLiteral) a.getValue()).getLiteral())
                .collect(Collectors.toSet());

        annotations.removeAll(toRemove);

        return values;
    }
}
