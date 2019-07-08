package uk.ac.ed.inf.ontology.search.elastic.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ed.inf.ontology.search.elastic.domain.Term;

import javax.inject.Inject;
import java.io.IOException;

@RestController
@RequestMapping(TermController.PATH)
public class TermController {
    public static final String PATH = "/term";

    @Inject
    private TermFacade termFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void importTerms(@RequestParam("file") MultipartFile file) throws IOException {
        termFacade.importTerms(file);
    }

    @GetMapping
    Page<Term> queryTerms(@RequestParam("q") String q, Pageable pageable) {
        return termFacade.query(q, pageable);
    }
}
