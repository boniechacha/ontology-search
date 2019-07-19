package uk.ac.ed.inf.ontology.search.elastic.service;

import lombok.extern.java.Log;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.ac.ed.inf.ontology.search.elastic.domain.Term;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag.*;
import static org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag.TAG_SYNONYM;

@Log
@Component
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class OBOParser {

    public List<Term> parse(InputStream input, String namespace) throws IOException {
        OBOFormatParser parser = new OBOFormatParser();
        OBODoc doc = parser.parse(new InputStreamReader(input));

        if (StringUtils.isEmpty(namespace)) {
            namespace = (String) doc.getHeaderFrame().getClause(TAG_DEFAULT_NAMESPACE).getValue();
        }

        return parse(doc, namespace);

    }

    public List<Term> parse(OBODoc doc, String namespace) {
        return doc.getTermFrames()
                .stream()
                .map(frame -> parseTerm(frame, namespace))
                .collect(Collectors.toList());
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
            term.addNote((String) commentClause.getValue());

        List<Clause> synonymClauses = frame.getClauses(TAG_SYNONYM);
        if (synonymClauses != null) {
            Set<String> synonyms = synonymClauses
                    .stream()
                    .map(clause -> String.valueOf(clause.getValue()))
                    .collect(Collectors.toSet());
            term.setSynonyms(synonyms);
        }

        return term;
    }
}
