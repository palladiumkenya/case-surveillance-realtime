package org.kenyahmis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import org.kenyahmis.dto.EventBase;
import org.kenyahmis.dto.EventList;
import org.kenyahmis.dto.LinkedDto;
import org.kenyahmis.dto.NewCaseDto;
import org.kenyahmis.exception.RequestValidationException;
import org.kenyahmis.model.Client;
import org.kenyahmis.model.Event;
import org.kenyahmis.model.LinkedCase;
import org.kenyahmis.model.NewCase;
import org.kenyahmis.repository.ClientRepository;
import org.kenyahmis.repository.EventRepository;
import org.kenyahmis.repository.LinkedCaseRepository;
import org.kenyahmis.repository.NewCaseEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.stereotype.Service;
import jakarta.validation.Validator;
import org.springframework.validation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.beans.PropertyEditor;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

@Service
public class EventService {
    private final static Logger LOG = LoggerFactory.getLogger(EventService.class);
    private final EventRepository eventRepository;
    private final NewCaseEventRepository newCaseEventRepository;
    private final ClientRepository clientRepository;
    private final LinkedCaseRepository linkedCaseRepository;

    public EventService(EventRepository eventRepository, final NewCaseEventRepository newCaseEventRepository,
                        final ClientRepository clientRepository, final LinkedCaseRepository linkedCaseRepository) {
        this.eventRepository = eventRepository;
        this.newCaseEventRepository = newCaseEventRepository;
        this.clientRepository = clientRepository;
        this.linkedCaseRepository = linkedCaseRepository;
    }

    public void createEvent(EventList<EventBase<?>> eventList) throws RequestValidationException {
        for (EventBase<?> eventBase: eventList) {
            if (eventBase.getEventType().equals("new_case")) {
                handleNewCaseEventUpload(eventBase);
            } else if ("linked_case".equals(eventBase.getEventType())) {
                handleLinkedEventUpload(eventBase);
            } else {
                LOG.warn("Event Type: {} not handled", eventBase.getEventType());
            }
        }
    }

    private void handleLinkedEventUpload(EventBase<?> eventBase) throws RequestValidationException {
        ObjectMapper mapper = new ObjectMapper();
        LinkedDto linkedDto = mapper.convertValue(eventBase.getEvent(), LinkedDto.class);
        EventBase<LinkedDto> linkedCaseEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), linkedDto);
        // validate
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<EventBase<LinkedDto>>> violations = validator.validate(linkedCaseEventBase);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new HashMap<>();
            violations.forEach(violation -> {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            });
            throw new RequestValidationException(errors);
        }

        // search for existing event
        Optional<Event> optionalEvent = eventRepository.findByClient_PatientPkAndMflCodeAndEventType(linkedCaseEventBase.getClient().getPatientPk(),
                linkedCaseEventBase.getEvent().getMflCode(), linkedCaseEventBase.getEventType());
        if (optionalEvent.isEmpty()) {
            // create event
            Client client = new Client();
            client.setCounty(linkedCaseEventBase.getClient().getCounty());
            client.setSex(linkedCaseEventBase.getClient().getSex());
            client.setSubCounty(linkedCaseEventBase.getClient().getSubCounty());
            client.setWard(linkedCaseEventBase.getClient().getWard());
//            client.setDateOfBirth();
            client.setPatientPk(linkedCaseEventBase.getClient().getPatientPk());
            clientRepository.save(client);


            Event event = new Event();
            event.setClient(client);
            event.setEventType(linkedCaseEventBase.getEventType());
//            event.setCreatedAt();
//            event.setUpdatedAt();
            event.setMflCode(linkedCaseEventBase.getEvent().getMflCode());
            event.setTimestamp(LocalDateTime.now());
            eventRepository.save(event);

            LinkedCase linkedCase = new LinkedCase();
//            newCaseEvent.setPositiveHivTestDate(newCaseEventBase.getEvent().getDateTestedPositive());
            linkedCase.setEvent(event);
            linkedCaseRepository.save(linkedCase);
        } else {
            // update event
            LOG.info("Case event (Linked Case) already exists( mflCode: {}, patientPk: {} )", linkedDto.getMflCode(),
                    linkedCaseEventBase.getClient().getPatientPk());
//            Event newCaseEvent = optionalEvent.get();
//            newCaseEvent.

        }
    }
    private void handleNewCaseEventUpload(EventBase<?> eventBase) throws RequestValidationException {
        ObjectMapper mapper = new ObjectMapper();
        NewCaseDto caseDto = mapper.convertValue(eventBase.getEvent(), NewCaseDto.class);
        EventBase<NewCaseDto> newCaseEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), caseDto);
        // validate
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<EventBase<NewCaseDto>>> violations = validator.validate(newCaseEventBase);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new HashMap<>();
            violations.forEach(violation -> {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            });
            throw new RequestValidationException(errors);
        }

            // search for existing event
            Optional<Event> optionalEvent = eventRepository.findByClient_PatientPkAndMflCodeAndEventType(newCaseEventBase.getClient().getPatientPk(),
                    newCaseEventBase.getEvent().getMflCode(), newCaseEventBase.getEventType());
            if (optionalEvent.isEmpty()) {
                // create event
                Client client = new Client();
                client.setCounty(newCaseEventBase.getClient().getCounty());
                client.setSex(newCaseEventBase.getClient().getSex());
                client.setSubCounty(newCaseEventBase.getClient().getSubCounty());
                client.setWard(newCaseEventBase.getClient().getWard());
//            client.setDateOfBirth();
                client.setPatientPk(newCaseEventBase.getClient().getPatientPk());
                clientRepository.save(client);


                Event event = new Event();
                event.setClient(client);
                event.setEventType(newCaseEventBase.getEventType());
//            event.setCreatedAt();
//            event.setUpdatedAt();
                event.setMflCode(newCaseEventBase.getEvent().getMflCode());
                event.setTimestamp(LocalDateTime.now());
                eventRepository.save(event);

                NewCase newCaseEvent = new NewCase();
//            newCaseEvent.setPositiveHivTestDate(newCaseEventBase.getEvent().getDateTestedPositive());
                newCaseEvent.setEvent(event);
                newCaseEventRepository.save(newCaseEvent);
            } else {
                // update event
                LOG.info("Case event (New Case) already exists( mflCode: {}, patientPk: {} )", caseDto.getMflCode(),
                        newCaseEventBase.getClient().getPatientPk());
//            Event newCaseEvent = optionalEvent.get();
//            newCaseEvent.

            }
        }
    }
