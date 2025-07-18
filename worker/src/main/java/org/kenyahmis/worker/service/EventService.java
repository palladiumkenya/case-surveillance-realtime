package org.kenyahmis.worker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.kenyahmis.shared.dto.*;
import org.kenyahmis.worker.exception.RequestValidationException;
import org.kenyahmis.worker.mapper.ClientMapper;
import org.kenyahmis.worker.mapper.EventMapper;
import org.kenyahmis.worker.model.Client;
import org.kenyahmis.worker.model.Event;
import org.kenyahmis.worker.repository.ClientRepository;
import org.kenyahmis.worker.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.kenyahmis.shared.constants.GlobalConstants.*;

@Service
public class EventService {
    private final static Logger LOG = LoggerFactory.getLogger(EventService.class);
    private final EventRepository eventRepository;
    private final ClientRepository clientRepository;
    private final EventMapper eventMapper;
    private final ClientMapper clientMapper;
    private final ObjectMapper mapper = new ObjectMapper();

    public EventService(final EventRepository eventRepository, final ClientRepository clientRepository,
                        final ClientMapper clientMapper, final EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.clientRepository = clientRepository;
        this.eventMapper = eventMapper;
        this.clientMapper = clientMapper;
    }

    @KafkaListener(id = "eventListener", topics = "events", containerFactory = "kafkaListenerContainerFactory")
    @Transactional(value = Transactional.TxType.NEVER)
    public void createEvent(EventList<EventBase<?>> eventList) {
        for (EventBase<?> eventBase: eventList) {
            if (NEW_EVENT_TYPE.equals(eventBase.getEventType())) {
                handleNewCaseEventUpload(eventBase);
            } else if (LINKED_EVENT_TYPE.equals(eventBase.getEventType())) {
                handleLinkedEventUpload(eventBase);
            } else if (AT_RISK_PBFW.equals(eventBase.getEventType())) {
                handleAtRiskPbfwEventUpload(eventBase);
            } else if (PREP_LINKED_AT_RISK_PBFW.equals(eventBase.getEventType())) {
                handlePrepLinkedAtRiskPbfwEventUpload(eventBase);
            } else if (ELIGIBLE_FOR_VL.equals(eventBase.getEventType())) {
                handleEligibleForVlEventUpload(eventBase);
            } else if (UNSUPPRESSED_VIRAL_LOAD.equals(eventBase.getEventType())) {
                handleUnsuppressedVlEventUpload(eventBase);
            } else if (HEI_WITHOUT_PCR.equals(eventBase.getEventType())) {
                handleHeiWithoutPcrEventUpload(eventBase);
            } else if (HEI_WITHOUT_FINAL_OUTCOME.equals(eventBase.getEventType())) {
                handleHeiWithoutFinaOutcomeEventUpload(eventBase);
            } else if (HEI_AT_6_TO_8_WEEKS.equals(eventBase.getEventType())) {
                handleHeiAged6To8MonthsEventUpload(eventBase);
            } else if (HEI_AT_24_WEEKS.equals(eventBase.getEventType())) {
                handleHeiAged24MonthsEventUpload(eventBase);
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
        LOG.debug("Received linked event pk: {}, mflCode: {}", patientPk, mflCode);
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(linkedDto, event);
            event.setClient(opClient.get());
            opClient.get().getEvents().add(event);
            clientRepository.save(opClient.get());
//            eventRepository.save(event);
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            Event event = eventMapper.eventDtoToEventModel(linkedDto, null);
            event.setClient(client);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private <T> T mapToObject(Object event, Class<T> type){
      return  null;
    }
    private void handleNewCaseEventUpload(EventBase<?> eventBase) {
        ObjectMapper mapper = new ObjectMapper();
        NewCaseDto caseDto = mapper.convertValue(eventBase.getEvent(), NewCaseDto.class);
        EventBase<NewCaseDto> newCaseEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), caseDto);
        // validate
        validateEventBase(newCaseEventBase);
        String patientPk = newCaseEventBase.getClient().getPatientPk(), mflCode = newCaseEventBase.getEvent().getMflCode(),
                eventType = newCaseEventBase.getEventType();
        LOG.debug("Received new case event pk: {}, mflCode: {}", patientPk, mflCode);
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
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
            Event event = eventMapper.eventDtoToEventModel(caseDto, null);
            event.setClient(client);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handleAtRiskPbfwEventUpload(EventBase<?> eventBase) {
        AtRiskPbfwDto eventDto = mapper.convertValue(eventBase.getEvent(), AtRiskPbfwDto.class);
        EventBase<AtRiskPbfwDto> atRiskPbfwDtoEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), eventDto);
        // validate
        validateEventBase(atRiskPbfwDtoEventBase);
        String patientPk = atRiskPbfwDtoEventBase.getClient().getPatientPk(), mflCode = atRiskPbfwDtoEventBase.getEvent().getMflCode(),
                eventType = atRiskPbfwDtoEventBase.getEventType();
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        LOG.debug("Received at risk pbfw event pk: {}, mflCode: {}", patientPk, mflCode);
        if (opClient.isPresent()) {
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }
    private void handlePrepLinkedAtRiskPbfwEventUpload(EventBase<?> eventBase) {
        PrepLinkedAtRiskPbfwDto eventDto = mapper.convertValue(eventBase.getEvent(), PrepLinkedAtRiskPbfwDto.class);
        EventBase<PrepLinkedAtRiskPbfwDto> prepLinkedAtRiskPbfwDtoEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), eventDto);
        // validate
        validateEventBase(prepLinkedAtRiskPbfwDtoEventBase);
        String patientPk = prepLinkedAtRiskPbfwDtoEventBase.getClient().getPatientPk(),
                mflCode = prepLinkedAtRiskPbfwDtoEventBase.getEvent().getMflCode(),
                eventType = prepLinkedAtRiskPbfwDtoEventBase.getEventType();
        LOG.debug("Received prep linked event pk: {}, mflCode: {}", patientPk, mflCode);
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handleEligibleForVlEventUpload(EventBase<?> eventBase) {
        EligibleForVlDto eventDto = mapper.convertValue(eventBase.getEvent(), EligibleForVlDto.class);
        EventBase<EligibleForVlDto> eligibleForVlDtoEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), eventDto);
        // validate
        validateEventBase(eligibleForVlDtoEventBase);
        String patientPk = eligibleForVlDtoEventBase.getClient().getPatientPk(),
                mflCode = eligibleForVlDtoEventBase.getEvent().getMflCode(),
                eventType = eligibleForVlDtoEventBase.getEventType();
        LOG.debug("Received eligible for VL event pk: {}, mflCode: {}", patientPk, mflCode);
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handleUnsuppressedVlEventUpload(EventBase<?> eventBase) {
        UnsuppressedViralLoadDto eventDto = mapper.convertValue(eventBase.getEvent(), UnsuppressedViralLoadDto.class);
        EventBase<UnsuppressedViralLoadDto> unsuppressedViralLoadDtoEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), eventDto);
        // validate
        validateEventBase(unsuppressedViralLoadDtoEventBase);
        String patientPk = unsuppressedViralLoadDtoEventBase.getClient().getPatientPk(),
                mflCode = unsuppressedViralLoadDtoEventBase.getEvent().mflCode(),
                eventType = unsuppressedViralLoadDtoEventBase.getEventType();
        LOG.debug("Received unsuppressed vl event pk: {}, mflCode: {}", patientPk, mflCode);
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }
    private void handleHeiWithoutPcrEventUpload(EventBase<?> eventBase) {
        HeiWithoutPcrDto eventDto = mapper.convertValue(eventBase.getEvent(), HeiWithoutPcrDto.class);
        EventBase<HeiWithoutPcrDto> heiWithoutPcrDtoEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), eventDto);
        // validate
        validateEventBase(heiWithoutPcrDtoEventBase);
        String patientPk = heiWithoutPcrDtoEventBase.getClient().getPatientPk(),
                mflCode = heiWithoutPcrDtoEventBase.getEvent().mflCode(),
                eventType = heiWithoutPcrDtoEventBase.getEventType();
        LOG.debug("Received hei without pcr event pk: {}, mflCode: {}", patientPk, mflCode);
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }
    private void handleHeiWithoutFinaOutcomeEventUpload(EventBase<?> eventBase) {
        HeiWithoutFinalOutcomeDto eventDto = mapper.convertValue(eventBase.getEvent(), HeiWithoutFinalOutcomeDto.class);
        EventBase<HeiWithoutFinalOutcomeDto> heiWithoutFinalOutcomeEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), eventDto);
        // validate
        validateEventBase(heiWithoutFinalOutcomeEventBase);
        String patientPk = heiWithoutFinalOutcomeEventBase.getClient().getPatientPk(),
                mflCode = heiWithoutFinalOutcomeEventBase.getEvent().mflCode(),
                eventType = heiWithoutFinalOutcomeEventBase.getEventType();
        LOG.debug("Received hei without fo event pk: {}, mflCode: {}", patientPk, mflCode);
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }
    private void handleHeiAged6To8MonthsEventUpload(EventBase<?> eventBase) {
        HeiAged6To8Dto eventDto = mapper.convertValue(eventBase.getEvent(), HeiAged6To8Dto.class);
        EventBase<HeiAged6To8Dto> heiAged6To8DtoEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), eventDto);
        // validate
        validateEventBase(heiAged6To8DtoEventBase);
        String patientPk = heiAged6To8DtoEventBase.getClient().getPatientPk(),
                mflCode = heiAged6To8DtoEventBase.getEvent().mflCode(),
                eventType = heiAged6To8DtoEventBase.getEventType();
        LOG.debug("Received hei aged 6 to 8 months event pk: {}, mflCode: {}", patientPk, mflCode);
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handleHeiAged24MonthsEventUpload(EventBase<?> eventBase) {
        HeiAged24Dto eventDto = mapper.convertValue(eventBase.getEvent(), HeiAged24Dto.class);
        EventBase<HeiAged24Dto> heiAged24DtoEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), eventDto);
        // validate
        validateEventBase(heiAged24DtoEventBase);
        String patientPk = heiAged24DtoEventBase.getClient().getPatientPk(),
                mflCode = heiAged24DtoEventBase.getEvent().mflCode(),
                eventType = heiAged24DtoEventBase.getEventType();
        LOG.debug("Received hei aged 24 months event pk: {}, mflCode: {}", patientPk, mflCode);
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }
}
