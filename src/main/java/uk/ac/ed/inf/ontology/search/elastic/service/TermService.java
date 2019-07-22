package uk.ac.ed.inf.ontology.search.elastic.service;

import lombok.extern.java.Log;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
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
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static uk.ac.ed.inf.ontology.search.elastic.domain.Term.NAMESPACE_FIELD;

@Log
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class TermService {

    private TermRepository termRepository;
    private OWLParser owlParser;
    private OBOParser oboParser;

    private ElasticsearchTemplate template;

    public TermService(TermRepository termRepository,
                       OWLParser owlParser,
                       OBOParser oboParser,
                       ElasticsearchTemplate template) {
        this.termRepository = termRepository;
        this.owlParser = owlParser;
        this.oboParser = oboParser;
        this.template = template;
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
            builder.withFilter(new TermQueryBuilder(NAMESPACE_FIELD,namespace));

        return termRepository.search(builder.build());
    }

    public List<Term> findAll(Collection<String> names) {
        return names
                .stream()
                .map(name -> termRepository.findFirstByName(name))
                .collect(Collectors.toList());
    }

    public List<String> findAllNamespaces() {

        SearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withSearchType(SearchType.DEFAULT)
                .withIndices(Term.INDEX_NAME)
                .withTypes(Term.INDEX_NAME)
                .addAggregation(terms(NAMESPACE_FIELD).field(NAMESPACE_FIELD))
                .build();

        List<String> data = template.query(query,response -> {
            StringTerms terms = (StringTerms) response.getAggregations()
                    .asMap()
                    .get(NAMESPACE_FIELD);
            return terms.getBuckets()
                    .stream()
                    .map(bucket -> bucket.getKeyAsString())
                    .collect(Collectors.toList());
        });

        return data;
    }
}
