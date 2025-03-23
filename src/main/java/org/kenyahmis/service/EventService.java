package org.kenyahmis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import org.kenyahmis.dto.EventBase;
import org.kenyahmis.dto.EventList;
import org.kenyahmis.dto.LinkedCaseDto;
import org.kenyahmis.dto.NewCaseDto;
import org.kenyahmis.exception.RequestValidationException;
import org.kenyahmis.mapper.ClientMapper;
import org.kenyahmis.mapper.EventMapper;
import org.kenyahmis.model.Client;
import org.kenyahmis.model.Event;
import org.kenyahmis.model.LinkedCase;
import org.kenyahmis.repository.ClientRepository;
import org.kenyahmis.repository.EventRepository;
import org.kenyahmis.repository.NewCaseEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import jakarta.validation.Validator;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EventService {
    private final static Logger LOG = LoggerFactory.getLogger(EventService.class);
    private final EventRepository eventRepository;
    private final NewCaseEventRepository newCaseEventRepository;
    private final ClientRepository clientRepository;
    private final EventMapper eventMapper;
    private final ClientMapper clientMapper;

    public EventService(EventRepository eventRepository, final NewCaseEventRepository newCaseEventRepository,
                        final ClientRepository clientRepository, final ClientMapper clientMapper,
                        final EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.newCaseEventRepository = newCaseEventRepository;
        this.clientRepository = clientRepository;
        this.eventMapper = eventMapper;
        this.clientMapper = clientMapper;
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
        LinkedCaseDto linkedDto = mapper.convertValue(eventBase.getEvent(), LinkedCaseDto.class);
        EventBase<LinkedCaseDto> linkedCaseEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), linkedDto);
        // validate
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<EventBase<LinkedCaseDto>>> violations = validator.validate(linkedCaseEventBase);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new HashMap<>();
            violations.forEach(violation -> {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            });
            throw new RequestValidationException(errors);
        }
        String patientPk = linkedCaseEventBase.getClient().getPatientPk(), mflCode = linkedCaseEventBase.getEvent().getMflCode(),
                eventType = linkedCaseEventBase.getEventType();
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            LOG.info("Found client pk:{}", opClient.get().getPatientPk());
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(linkedDto, event);
            event.setClient(opClient.get());
            eventRepository.save(event);
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            clientRepository.save(client);
            // persist event
            Event event = eventMapper.eventDtoToEventModel(linkedDto, null);
            event.setClient(client);
            eventRepository.save(event);
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

        String patientPk = newCaseEventBase.getClient().getPatientPk(), mflCode = newCaseEventBase.getEvent().getMflCode(),
                eventType = newCaseEventBase.getEventType();
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            LOG.info("Found existing client pk:{}", opClient.get().getPatientPk());
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(caseDto, event);
            event.setClient(opClient.get());
            eventRepository.save(event);
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            clientRepository.save(client);
            // persist event
            Event event = eventMapper.eventDtoToEventModel(caseDto, null);
            event.setClient(client);
            eventRepository.save(event);
        }
    }
    }
