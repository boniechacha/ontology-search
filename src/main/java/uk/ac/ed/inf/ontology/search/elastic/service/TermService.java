package uk.ac.ed.inf.ontology.search.elastic.service;

import lombok.extern.java.Log;
import org.elasticsearch.index.query.QueryBuilders;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Log
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class TermService {

    private TermRepository termRepository;
    private OWLParser owlParser;
    private OBOParser oboParser;

    public TermService(TermRepository termRepository, OWLParser owlParser, OBOParser oboParser) {
        this.termRepository = termRepository;
        this.owlParser = owlParser;
        this.oboParser = oboParser;
    }

    @Transactional
    public void importOBO(InputStream input, String namespace) throws IOException {
        saveAll(oboParser.parse(input,namespace));
    }

    @Transactional
    public void importOWL(InputStream input, String namespace) throws OWLOntologyCreationException {
        saveAll(owlParser.parse(input,namespace));
    }

    public void saveAll(Collection<Term> terms) {
        termRepository.saveAll(terms);
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
