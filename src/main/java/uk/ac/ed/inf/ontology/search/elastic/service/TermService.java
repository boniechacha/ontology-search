package uk.ac.ed.inf.ontology.search.elastic.service;

import lombok.extern.java.Log;
import org.elasticsearch.index.query.QueryBuilders;
import org.obolibrary.obo2owl.OWLAPIOwl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.ac.ed.inf.ontology.search.elastic.domain.Term;
import uk.ac.ed.inf.ontology.search.elastic.repository.TermRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag.*;
import static org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag.TAG_SYNONYM;

@Log
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class TermService {

    private TermRepository termRepository;

    public TermService(TermRepository termRepository) {
        this.termRepository = termRepository;
    }

    @Transactional
    public void importOBO(InputStream input) throws IOException {

        OBOFormatParser parser = new OBOFormatParser();
        OBODoc doc = parser.parse(new InputStreamReader(input));

        importOBO(doc);
    }

    @Transactional
    public void importOBO(OBODoc doc) {
        String namespace = (String) doc.getHeaderFrame().getClause(TAG_DEFAULT_NAMESPACE).getValue();
        List<Term> terms = doc.getTermFrames()
                .stream()
                .map(frame -> parseTerm(frame, namespace))
                .collect(Collectors.toList());
        termRepository.saveAll(terms);
    }

    @Transactional
    public void importOWL(InputStream input) throws OWLOntologyCreationException {

        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(input);
        importOWL(ontology);
    }

    @Transactional
    public void importOWL(OWLOntology ontology) {
        OWLAPIOwl2Obo owl2obo = new OWLAPIOwl2Obo(ontology.getOWLOntologyManager());
        OBODoc doc = owl2obo.convert(ontology);

        importOBO(doc);
    }

    public Term parseTerm(Frame frame, String namespace) {
        log.info(String.format("Parsing : %s", frame.toString()));

        Term term = new Term(namespace);

        term.setId(Term.cleanId(frame.getId()));
        term.setName((String) frame.getClause(TAG_NAME).getValue());

        Clause defClause = frame.getClause(TAG_DEF);
        if (defClause != null)
            term.setDef((String) defClause.getValue());

        Clause commentClause = frame.getClause(TAG_COMMENT);
        if (commentClause != null)
            term.setComment((String) commentClause.getValue());

        List<Clause> synonymClauses = frame.getClauses(TAG_SYNONYM);
        if (synonymClauses != null) {
            List<String> synonyms = synonymClauses
                    .stream()
                    .map(clause -> String.valueOf(clause.getValue()))
                    .collect(Collectors.toList());
            term.setSynonyms(synonyms);
        }

        return term;
    }

    public Page<Term> query(String q, String namespace, Pageable pageable) {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withQuery(QueryBuilders.queryStringQuery(q));

        if (!StringUtils.isEmpty(namespace))
            builder.withQuery(QueryBuilders.matchPhraseQuery("namespace", namespace));

        return termRepository.search(builder.build());
    }

    public List<Term> findAll(Collection<String> names) {
        return names.stream()
                .map(name -> termRepository.findFirstByName(name))
                .collect(Collectors.toList());
    }
}
