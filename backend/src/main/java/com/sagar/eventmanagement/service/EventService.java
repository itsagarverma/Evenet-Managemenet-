package com.sagar.eventmanagement.service;

import com.sagar.eventmanagement.dto.EventResponseDTO;
import com.sagar.eventmanagement.dto.EventRequestDTO;
import com.sagar.eventmanagement.exception.ResourceNotFoundException;
import com.sagar.eventmanagement.entity.Event;
import com.sagar.eventmanagement.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

public List<EventResponseDTO> getAllEvents() {
    return eventRepository.findAll()
            .stream()
            .map(this::mapToResponseDTO)
            .toList();
}
public EventResponseDTO getEventById(Long id) {
    Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

    return mapToResponseDTO(event);
}
public EventResponseDTO saveEvent(EventRequestDTO eventDTO) {
    Event event = new Event();

    event.setTitle(eventDTO.getTitle());
    event.setDescription(eventDTO.getDescription());
    event.setLocation(eventDTO.getLocation());
    event.setEventDate(eventDTO.getEventDate());

    Event savedEvent = eventRepository.save(event);

    return mapToResponseDTO(savedEvent);
}
private EventResponseDTO mapToResponseDTO(Event event) {
    EventResponseDTO responseDTO = new EventResponseDTO();

    responseDTO.setId(event.getId());
    responseDTO.setTitle(event.getTitle());
    responseDTO.setDescription(event.getDescription());
    responseDTO.setLocation(event.getLocation());
    responseDTO.setEventDate(event.getEventDate());

    return responseDTO;
}

public EventResponseDTO updateEvent(Long id, EventRequestDTO eventDTO) {
    Event event = eventRepository.findById(id)
         .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

    event.setTitle(eventDTO.getTitle());
    event.setDescription(eventDTO.getDescription());
    event.setLocation(eventDTO.getLocation());
    event.setEventDate(eventDTO.getEventDate());

    Event updatedEvent = eventRepository.save(event);

    return mapToResponseDTO(updatedEvent);
}


public void deleteEvent(Long id) {
    eventRepository.deleteById(id);
}
}
