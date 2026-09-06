package com.sagar.eventmanagement.service;

import com.sagar.eventmanagement.dto.QueryRequestDTO;
import com.sagar.eventmanagement.dto.QueryResponseDTO;
import com.sagar.eventmanagement.entity.Query;
import com.sagar.eventmanagement.repository.QueryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService {

    private final EmailService emailService;
    private final QueryRepository queryRepository;

    @Value("${app.notification.email}")
    private String notificationEmail;

    public QueryService(QueryRepository queryRepository,
                        EmailService emailService) {
        this.queryRepository = queryRepository;
        this.emailService = emailService;
    }

    public QueryResponseDTO saveQuery(QueryRequestDTO queryDTO) {
        Query query = new Query();

        query.setFullName(queryDTO.getFullName());
        query.setEmail(queryDTO.getEmail());
        query.setPhone(queryDTO.getPhone());
        query.setEventType(queryDTO.getEventType());
        query.setEventDate(queryDTO.getEventDate());
        query.setCityVenue(queryDTO.getCityVenue());
        query.setSpecialRequirements(queryDTO.getSpecialRequirements());
        query.setMessage(queryDTO.getMessage());

        Query savedQuery = queryRepository.save(query);

        String subject = "New Event Query Received";

        String body =
                "Name: " + savedQuery.getFullName() + "\n" +
                "Email: " + savedQuery.getEmail() + "\n" +
                "Phone: " + savedQuery.getPhone() + "\n" +
                "Event Type: " + savedQuery.getEventType() + "\n" +
                "Event Date: " + savedQuery.getEventDate() + "\n" +
                "City: " + savedQuery.getCityVenue() + "\n" +
                "Message: " + savedQuery.getMessage();

        // Don't let a failed email stop the query from being saved successfully
        try {
            emailService.sendQueryNotification(
                    notificationEmail,
                    subject,
                    body
            );
        } catch (Exception e) {
            System.err.println("Failed to send query notification email: " + e.getMessage());
        }

        return mapToResponseDTO(savedQuery);
    }

    public List<QueryResponseDTO> getAllQueries() {
        return queryRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private QueryResponseDTO mapToResponseDTO(Query query) {
        QueryResponseDTO responseDTO = new QueryResponseDTO();

        responseDTO.setId(query.getId());
        responseDTO.setFullName(query.getFullName());
        responseDTO.setEmail(query.getEmail());
        responseDTO.setPhone(query.getPhone());
        responseDTO.setEventType(query.getEventType());
        responseDTO.setEventDate(query.getEventDate());
        responseDTO.setCityVenue(query.getCityVenue());
        responseDTO.setSpecialRequirements(query.getSpecialRequirements());
        responseDTO.setMessage(query.getMessage());

        return responseDTO;
    }
}
