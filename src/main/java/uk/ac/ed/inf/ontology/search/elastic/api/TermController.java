package uk.ac.ed.inf.ontology.search.elastic.api;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ed.inf.ontology.search.elastic.domain.Term;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(TermController.PATH)
public class TermController {
    public static final String PATH = "/api/v1/term";

    @Inject
    private TermFacade termFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void importTerms(@RequestParam("file") MultipartFile file,
                     @RequestParam("namespace") Optional<String> namespace) throws IOException, OWLOntologyCreationException {
        termFacade.importTerms(file, namespace);
    }

    @GetMapping("/search")
    Page<Term> search(@RequestParam("q") String q,
                      @RequestParam("namespace") Optional<String> namespace,
                      Pageable pageable) {
        return termFacade.search(q, namespace, pageable);
    }

    @GetMapping("/list")
    List<Term> findTerms(@RequestParam("id") List<String> ids) {
        return termFacade.findAll(ids);
    }

    @GetMapping("/namespace")
    List<String> listNamespaces() {
        return termFacade.listNamespaces();
    }
}
