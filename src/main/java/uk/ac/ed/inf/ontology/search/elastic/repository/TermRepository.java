package uk.ac.ed.inf.ontology.search.elastic.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ed.inf.ontology.search.elastic.domain.Term;

@Repository
public interface TermRepository extends ElasticsearchRepository<Term, String> {
}
