package org.kenyahmis.controller;

import jakarta.validation.Valid;
import org.kenyahmis.dto.EventBase;
import org.kenyahmis.dto.EventList;
import org.kenyahmis.exception.RequestValidationException;
import org.kenyahmis.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event")
public class EventController {
    private final static Logger LOG = LoggerFactory.getLogger(EventController.class);
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PutMapping(value = "sync")
    private ResponseEntity<?> createEvent(@RequestBody @Valid EventList<EventBase<?>> eventList) throws RequestValidationException {
        eventService.createEvent(eventList);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
