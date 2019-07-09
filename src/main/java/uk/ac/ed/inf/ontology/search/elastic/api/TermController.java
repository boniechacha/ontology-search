package uk.ac.ed.inf.ontology.search.elastic.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ed.inf.ontology.search.elastic.domain.Term;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(TermController.PATH)
public class TermController {
    public static final String PATH = "/api/v1/term";

    @Inject
    private TermFacade termFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void importTerms(@RequestParam("file") MultipartFile file) throws IOException {
        termFacade.importTerms(file);
    }

    @GetMapping("/search")
    Page<Term> search(@RequestParam("q") String q, Pageable pageable) {
        return termFacade.search(q, pageable);
    }

    @GetMapping("/list")
    List<Term> findByNames(@RequestParam("name") List<String> names) {
        return termFacade.findAll(names);
    }
}
