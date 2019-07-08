package uk.ac.ed.inf.ontology.search.elastic.service;

import lombok.extern.java.Log;
import org.elasticsearch.index.query.QueryBuilders;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ed.inf.ontology.search.elastic.domain.Term;
import uk.ac.ed.inf.ontology.search.elastic.repository.TermRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag.*;
import static org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag.TAG_SYNONYM;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
@Log
public class TermService {

    private TermRepository termRepository;

    public TermService(TermRepository termRepository) {
        this.termRepository = termRepository;
    }

    public void importTerms(InputStream input) throws IOException {
        OBOFormatParser parser = new OBOFormatParser();
        OBODoc doc = parser.parse(new InputStreamReader(input));

        List<Term> terms = doc.getTermFrames()
                .stream()
                .map(frame -> createTerm(frame))
                .collect(Collectors.toList());
        termRepository.saveAll(terms);
    }

    private Term createTerm(Frame frame) {
        log.info("Parsing : " + frame);

        Term term = new Term();

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

    public Page<Term> query(String q, Pageable pageable) {
        SearchQuery query = new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withQuery(QueryBuilders.queryStringQuery(q))
                .build();
        return termRepository.search(query);
    }
}
