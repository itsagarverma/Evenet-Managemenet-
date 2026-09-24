package com.sagar.eventmanagement.controller;
import com.sagar.eventmanagement.dto.QueryResponseDTO; import com.sagar.eventmanagement.entity.LeadStatus; import com.sagar.eventmanagement.service.QueryService; import org.springframework.web.bind.annotation.*; import java.time.LocalDate; import java.util.List;
@RestController @RequestMapping("/api/enquiries") public class EnquiryAdminController {
 private final QueryService service; public EnquiryAdminController(QueryService service){this.service=service;}
 public record LeadUpdate(LeadStatus status, LocalDate followUpDate, String followUpNote, String nextAction){}
 @GetMapping public List<QueryResponseDTO> list(){return service.getAllQueries();}
 @PutMapping("/{id}") public QueryResponseDTO update(@PathVariable Long id,@RequestBody LeadUpdate b){return service.updateLead(id,b.status(),b.followUpDate(),b.followUpNote(),b.nextAction());}
}
