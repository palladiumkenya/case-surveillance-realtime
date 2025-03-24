package org.kenyahmis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.kenyahmis.dto.APIResponse;
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

    @Operation(
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Generic request containing different event types",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EventBase.class),
                            mediaType = "application/json",
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "New Case",
                                            value = "[{\"client\":{\"county\":\"Nairobi\",\"subCounty\":\"Langata\",\"ward\":\"Langata\",\"patientPk\":\"505\",\"sex\":\"male\",\"dob\":\"1997-01-01\"},\"eventType\":\"new_case\",\"event\":{\"mflCode\":\"1234\",\"createdAt\":\"2024-01-01 00:00:00\",\"positiveHivTestDate\":\"2024-01-01 00:00:00\",\"updatedAt\":\"2024-01-01 00:00:00\"}}]"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Linked Case",
                                            value = "[{\"client\":{\"county\":\"Nairobi\",\"subCounty\":\"Langata\",\"ward\":\"Langata\",\"patientPk\":\"505\",\"sex\":\"male\",\"dob\":\"1997-01-01\"},\"eventType\":\"linked_case\",\"event\":{\"mflCode\":\"1234\",\"createdAt\":\"2024-01-01 00:00:00\",\"positiveHivTestDate\":\"2024-01-01 00:00:00\",\"artStartDate\":\"2024-01-01 00:00:00\",\"updatedAt\":\"2024-01-01 00:00:00\"}}]"
                                    )
                            }
                    )
            )
    )
    @PutMapping(value = "sync")
    private ResponseEntity<APIResponse> createEvent(@RequestBody @Valid EventList<EventBase<?>> eventList) throws RequestValidationException {
        eventService.createEvent(eventList);
        return new ResponseEntity<>(new APIResponse("Successfully added client event"),  HttpStatus.OK);
    }
}
