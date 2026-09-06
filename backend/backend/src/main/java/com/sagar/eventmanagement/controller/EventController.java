package com.sagar.eventmanagement.controller;

import com.sagar.eventmanagement.dto.EventResponseDTO;
import com.sagar.eventmanagement.dto.EventRequestDTO;
import jakarta.validation.Valid;
import com.sagar.eventmanagement.entity.Event;
import com.sagar.eventmanagement.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }
        @PostMapping
         public EventResponseDTO createEvent(@Valid @RequestBody EventRequestDTO eventDTO) {
    return eventService.saveEvent(eventDTO);
} 
    @GetMapping
    public List<EventResponseDTO> getAllEvents() {
        return eventService.getAllEvents();
    }

	@GetMapping("/{id}")
	public EventResponseDTO getEventById(@PathVariable Long id) {
    return eventService.getEventById(id);
}
        @PutMapping("/{id}")
        public EventResponseDTO updateEvent(
        @PathVariable Long id,
        @Valid @RequestBody EventRequestDTO eventDTO) {
    return eventService.updateEvent(id, eventDTO);
}


@DeleteMapping("/{id}")
public String deleteEvent(@PathVariable Long id) {
    eventService.deleteEvent(id);
    return "Event deleted successfully";
}
}
