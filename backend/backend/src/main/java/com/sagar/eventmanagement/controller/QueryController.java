package com.sagar.eventmanagement.controller;

import com.sagar.eventmanagement.dto.QueryRequestDTO;
import com.sagar.eventmanagement.dto.QueryResponseDTO;
import com.sagar.eventmanagement.service.QueryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/queries")
public class QueryController {

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    // Anyone can submit a query - no login required
    @PostMapping
    public QueryResponseDTO createQuery(
            @Valid @RequestBody QueryRequestDTO queryDTO) {
        return queryService.saveQuery(queryDTO);
    }

    // Lists every submitted query - intended for your own admin use.
    // There's no login system, so anyone with the URL can view this.
    // See the note in application.properties before you go live.
    @GetMapping("/all")
    public List<QueryResponseDTO> getAllQueries() {
        return queryService.getAllQueries();
    }
}
