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
    public record QueryReceipt(Long id) {}
    @PostMapping
    public QueryReceipt createQuery(
            @Valid @RequestBody QueryRequestDTO queryDTO) {
        return new QueryReceipt(queryService.saveQuery(queryDTO).getId());
    }

    // Private admin listing; Spring Security restricts /queries/** to ADMIN.
    @GetMapping("/all")
    public List<QueryResponseDTO> getAllQueries() {
        return queryService.getAllQueries();
    }

}
