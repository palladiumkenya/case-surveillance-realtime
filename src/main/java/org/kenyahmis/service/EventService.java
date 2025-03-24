package org.kenyahmis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import org.kenyahmis.dto.*;
import org.kenyahmis.exception.RequestValidationException;
import org.kenyahmis.mapper.ClientMapper;
import org.kenyahmis.mapper.EventMapper;
import org.kenyahmis.model.Client;
import org.kenyahmis.model.Event;
import org.kenyahmis.repository.ClientRepository;
import org.kenyahmis.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import jakarta.validation.Validator;

import java.util.*;

import static org.kenyahmis.constants.GlobalConstants.*;

@Service
public class EventService {
    private final static Logger LOG = LoggerFactory.getLogger(EventService.class);
    private final EventRepository eventRepository;
    private final ClientRepository clientRepository;
    private final EventMapper eventMapper;
    private final ClientMapper clientMapper;
    private final ObjectMapper mapper = new ObjectMapper();

    public EventService(EventRepository eventRepository, final ClientRepository clientRepository,
                        final ClientMapper clientMapper, final EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.clientRepository = clientRepository;
        this.eventMapper = eventMapper;
        this.clientMapper = clientMapper;
    }

    public void createEvent(EventList<EventBase<?>> eventList) throws RequestValidationException {
        for (EventBase<?> eventBase: eventList) {
            if (NEW_EVENT_TYPE.equals(eventBase.getEventType())) {
                handleNewCaseEventUpload(eventBase);
            } else if (LINKED_EVENT_TYPE.equals(eventBase.getEventType())) {
                handleLinkedEventUpload(eventBase);
            } else if (AT_RISK_PBFW.equals(eventBase.getEventType())) {
                handleAtRiskPbfwEventUpload(eventBase);
            } else if (PREP_LINKED_AT_RISK_PBFW.equals(eventBase.getEventType())) {
                handlePrepLinkedAtRiskPbfwEventUpload(eventBase);
            } else {
                LOG.warn("Event Type: {} not handled", eventBase.getEventType());
            }
        }
    }

    private <T> void validateEventBase(EventBase<T> object) throws RequestValidationException {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<EventBase<T>>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new HashMap<>();
            violations.forEach(violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
            throw new RequestValidationException(errors);
        }
    }
    private void handleLinkedEventUpload(EventBase<?> eventBase) throws RequestValidationException {
        ObjectMapper mapper = new ObjectMapper();
        LinkedCaseDto linkedDto = mapper.convertValue(eventBase.getEvent(), LinkedCaseDto.class);
        EventBase<LinkedCaseDto> linkedCaseEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), linkedDto);
        // validate
        validateEventBase(linkedCaseEventBase);
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
        validateEventBase(newCaseEventBase);
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

    private void handleAtRiskPbfwEventUpload(EventBase<?> eventBase) throws RequestValidationException {
        AtRiskPbfwDto eventDto = mapper.convertValue(eventBase.getEvent(), AtRiskPbfwDto.class);
        EventBase<AtRiskPbfwDto> atRiskPbfwDtoEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), eventDto);
        // validate
        validateEventBase(atRiskPbfwDtoEventBase);
        String patientPk = atRiskPbfwDtoEventBase.getClient().getPatientPk(), mflCode = atRiskPbfwDtoEventBase.getEvent().getMflCode(),
                eventType = atRiskPbfwDtoEventBase.getEventType();
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            LOG.info("Found existing client pk:{}", opClient.get().getPatientPk());
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            eventRepository.save(event);
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            clientRepository.save(client);
            // persist event
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            eventRepository.save(event);
        }
    }
    private void handlePrepLinkedAtRiskPbfwEventUpload(EventBase<?> eventBase) throws RequestValidationException {
        PrepLinkedAtRiskPbfwDto eventDto = mapper.convertValue(eventBase.getEvent(), PrepLinkedAtRiskPbfwDto.class);
        EventBase<PrepLinkedAtRiskPbfwDto> prepLinkedAtRiskPbfwDtoEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), eventDto);
        // validate
        validateEventBase(prepLinkedAtRiskPbfwDtoEventBase);
        String patientPk = prepLinkedAtRiskPbfwDtoEventBase.getClient().getPatientPk(),
                mflCode = prepLinkedAtRiskPbfwDtoEventBase.getEvent().getMflCode(),
                eventType = prepLinkedAtRiskPbfwDtoEventBase.getEventType();
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            LOG.info("Found existing client pk:{}", opClient.get().getPatientPk());
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            eventRepository.save(event);
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            clientRepository.save(client);
            // persist event
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            eventRepository.save(event);
        }
    }
    }
