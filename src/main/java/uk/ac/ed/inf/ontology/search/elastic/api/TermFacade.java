package uk.ac.ed.inf.ontology.search.elastic.api;

import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ed.inf.ontology.search.elastic.domain.Term;
import uk.ac.ed.inf.ontology.search.elastic.service.TermService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class TermFacade {

    private static final String OBO_EXTENSION = ".obo";
    private TermService termService;

    public TermFacade(TermService termService) {
        this.termService = termService;
    }

    @Transactional
    public void importTerms(MultipartFile file, String namespace) throws IOException, OWLOntologyCreationException {
        if (file.getOriginalFilename().endsWith(OBO_EXTENSION))
            termService.importOBO(file.getInputStream(), namespace);
        else {
            if (StringUtils.isEmpty(namespace))
                namespace = FilenameUtils.removeExtension(file.getOriginalFilename());

            termService.importOWL(file.getInputStream(), namespace);
        }
    }

    public Page<Term> search(String q, String namespace, Pageable pageable) {
        return termService.query(q, namespace, pageable);
    }

    public List<Term> findAll(Collection<String> names) {
        return termService.findAll(names);
    }

    public List<String> listNamespaces() {
        return termService.findAllNamespaces();
    }
}
