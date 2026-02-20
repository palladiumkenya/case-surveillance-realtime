package org.kenyahmis.worker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.kenyahmis.shared.dto.*;
import org.kenyahmis.shared.utils.FlexibleDateTimeParser;
import org.kenyahmis.worker.exception.RequestValidationException;
import org.kenyahmis.worker.mapper.ClientMapper;
import org.kenyahmis.worker.mapper.EventMapper;
import org.kenyahmis.worker.model.Client;
import org.kenyahmis.worker.model.EmrVendor;
import org.kenyahmis.worker.model.Event;
import org.kenyahmis.worker.repository.ClientRepository;
import org.kenyahmis.worker.repository.EmrVendorRepository;
import org.kenyahmis.worker.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.kenyahmis.shared.constants.GlobalConstants.*;

@Service
public class EventService {
    private final static Logger LOG = LoggerFactory.getLogger(EventService.class);
    private final EventRepository eventRepository;
    private final ClientRepository clientRepository;
    private final EmrVendorRepository emrVendorRepository;
    private final EventMapper eventMapper;
    private final ClientMapper clientMapper;
    private final ObjectMapper mapper = new ObjectMapper();
    private static  LocalDateTime PROGRAM_START_THRESHOLD = LocalDate.of(2025, 6, 1).atStartOfDay();

    public EventService(final EventRepository eventRepository, final ClientRepository clientRepository,
                        final EmrVendorRepository emrVendorRepository, final ClientMapper clientMapper, final EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.clientRepository = clientRepository;
        this.emrVendorRepository = emrVendorRepository;
        this.eventMapper = eventMapper;
        this.clientMapper = clientMapper;
    }

    @KafkaListener(id = "eventListener", topics = "events", containerFactory = "eventsKafkaListenerContainerFactory")
    @Transactional(value = Transactional.TxType.NEVER)
//    public void createEvent(EventList<EventBase<?>> eventList) {
    public void createEvent(Set<EventBaseMessage<?>> eventBaseMessageList) {
        for (EventBaseMessage<?> eventBaseMessage : eventBaseMessageList) {
            if (NEW_EVENT_TYPE.equals(eventBaseMessage.getEventBase().getEventType())) {
                handleNewCaseEventUpload(eventBaseMessage);
            } else if (LINKED_EVENT_TYPE.equals(eventBaseMessage.getEventBase().getEventType())) {
                handleLinkedEventUpload(eventBaseMessage);
            } else if (AT_RISK_PBFW.equals(eventBaseMessage.getEventBase().getEventType())) {
                handleAtRiskPbfwEventUpload(eventBaseMessage);
            } else if (PREP_LINKED_AT_RISK_PBFW.equals(eventBaseMessage.getEventBase().getEventType())) {
                handlePrepLinkedAtRiskPbfwEventUpload(eventBaseMessage);
            }else if (PREP_UPTAKE.equals(eventBaseMessage.getEventBase().getEventType())) {
                handlePrepUptakeEventUpload(eventBaseMessage);
            } else if (MORTALITY.equals(eventBaseMessage.getEventBase().getEventType())) {
                 handleMortalityEventUpload(eventBaseMessage);
            } else if (ELIGIBLE_FOR_VL.equals(eventBaseMessage.getEventBase().getEventType())) {
                handleEligibleForVlEventUpload(eventBaseMessage);
            } else if (UNSUPPRESSED_VIRAL_LOAD.equals(eventBaseMessage.getEventBase().getEventType())) {
                handleUnsuppressedVlEventUpload(eventBaseMessage);
            } else if (HEI_WITHOUT_PCR.equals(eventBaseMessage.getEventBase().getEventType())) {
                handleHeiWithoutPcrEventUpload(eventBaseMessage);
            } else if (HEI_WITHOUT_FINAL_OUTCOME.equals(eventBaseMessage.getEventBase().getEventType())) {
                handleHeiWithoutFinaOutcomeEventUpload(eventBaseMessage);
            } else if (HEI_AT_6_TO_8_WEEKS.equals(eventBaseMessage.getEventBase().getEventType())) {
                handleHeiAged6To8MonthsEventUpload(eventBaseMessage);
            } else if (HEI_AT_24_WEEKS.equals(eventBaseMessage.getEventBase().getEventType())) {
                eventBaseMessage.getEventBase().setEventType(HEI_AT_6_TO_8_WEEKS);
                handleHeiAged6To8MonthsEventUpload(eventBaseMessage);
//                handleHeiAged24MonthsEventUpload(eventBaseMessage);
            } else if (ROLL_CALL.equals(eventBaseMessage.getEventBase().getEventType())) {
                LOG.info("Received roll_call event, ignore");
            } else {
                LOG.warn("Event Type: {} not handled", eventBaseMessage.getEventBase().getEventType());
            }
        }
    }

    private <T> void validateEventBase(EventBase<T> object) throws RequestValidationException {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<EventBase<T>>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new HashMap<>();
            violations.forEach(violation -> {
                        LOG.error("Request validation failed: {} : {}", violation.getPropertyPath().toString(), violation.getMessage() );
                        errors.put(violation.getPropertyPath().toString(), violation.getMessage());
                    }
            );
            throw new RequestValidationException(errors);
        }
    }

    private void handleLinkedEventUpload(EventBaseMessage<?> eventBaseMessage) throws RequestValidationException {
        ObjectMapper mapper = new ObjectMapper();
        LinkedCaseDto linkedDto = mapper.convertValue(eventBaseMessage.getEventBase().getEvent(), LinkedCaseDto.class);
        EventBase<LinkedCaseDto> linkedCaseEventBase = new EventBase<>(eventBaseMessage.getEventBase().getClient(),
                eventBaseMessage.getEventBase().getEventType(), linkedDto);
        // validate
        validateEventBase(linkedCaseEventBase);
        String patientPk = linkedCaseEventBase.getClient().getPatientPk(), mflCode = linkedCaseEventBase.getEvent().getMflCode(),
                eventType = linkedCaseEventBase.getEventType();
        LOG.debug("Received linked event pk: {}, mflCode: {}", patientPk, mflCode);
        UUID vendorId = getVendorId(eventBaseMessage.getEmrVendor());
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
            event.setEmrVendorId(vendorId);
            opClient.get().getEvents().add(event);
            clientRepository.save(opClient.get());
//            eventRepository.save(event);
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBaseMessage.getEventBase().getClient());
            Event event = eventMapper.eventDtoToEventModel(linkedDto, null);
            event.setClient(client);
            event.setEmrVendorId(vendorId);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private UUID getVendorId(String vendorName) {
        UUID vendorId = null;
        if (vendorName != null) {
            Optional<EmrVendor> optionalEmrVendor = emrVendorRepository.findByVendorName(vendorName);
            if (optionalEmrVendor.isPresent()) {
                vendorId = optionalEmrVendor.get().getId();
            }
        }
        return vendorId;
    }

    private Boolean isEarlierThanThreshHold(String eventCreatedDate, LocalDateTime threshold) {
        Boolean isEarlier = null;
        if (!StringUtils.isEmpty(eventCreatedDate)) {
            try {
                LocalDateTime eventCreated =  FlexibleDateTimeParser.parse(eventCreatedDate);
                isEarlier = eventCreated.isBefore(threshold);
            } catch (DateTimeException e) {
                LOG.error("Failed to parse date", e);
            }
        }
        return isEarlier;
    }
    private void handleNewCaseEventUpload(EventBaseMessage<?> eventBaseMessage) {
        EventBase<?> eventBase = eventBaseMessage.getEventBase();
        ObjectMapper mapper = new ObjectMapper();
        NewCaseDto caseDto = mapper.convertValue(eventBase.getEvent(), NewCaseDto.class);
        // filter new cases
        Boolean isEarlier = isEarlierThanThreshHold(caseDto.getCreatedAt(), PROGRAM_START_THRESHOLD);
        if (isEarlier != null && isEarlier) {
            LOG.info("Skipping new case earlier than program start: {}", caseDto.getCreatedAt());
            return;
        }
        EventBase<NewCaseDto> newCaseEventBase = new EventBase<>(eventBase.getClient(), eventBase.getEventType(), caseDto);
        // validate
        validateEventBase(newCaseEventBase);
        String patientPk = newCaseEventBase.getClient().getPatientPk(), mflCode = newCaseEventBase.getEvent().getMflCode(),
                eventType = newCaseEventBase.getEventType();
        LOG.debug("Received new case event pk: {}, mflCode: {}", patientPk, mflCode);
        UUID vendorId = getVendorId(eventBaseMessage.getEmrVendor());
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
            event.setEmrVendorId(vendorId);
            eventRepository.save(event);
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBase.getClient());
            Event event = eventMapper.eventDtoToEventModel(caseDto, null);
            event.setClient(client);
            event.setEmrVendorId(vendorId);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handleAtRiskPbfwEventUpload(EventBaseMessage<?> eventBaseMessage) {
        AtRiskPbfwDto eventDto = mapper.convertValue(eventBaseMessage.getEventBase().getEvent(), AtRiskPbfwDto.class);
        EventBase<AtRiskPbfwDto> atRiskPbfwDtoEventBase = new EventBase<>(eventBaseMessage.getEventBase().getClient(),
                eventBaseMessage.getEventBase().getEventType(), eventDto);
        // filter out old events
        Boolean isEarlier = isEarlierThanThreshHold(eventDto.getCreatedAt(), PROGRAM_START_THRESHOLD);
        if (isEarlier != null && isEarlier) {
            LOG.info("Skipping pbfw earlier than program start: {}", eventDto.getCreatedAt());
            return;
        }
        // validate
        validateEventBase(atRiskPbfwDtoEventBase);
        String patientPk = atRiskPbfwDtoEventBase.getClient().getPatientPk(), mflCode = atRiskPbfwDtoEventBase.getEvent().getMflCode(),
                eventType = atRiskPbfwDtoEventBase.getEventType();
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        LOG.debug("Received at risk pbfw event pk: {}, mflCode: {}", patientPk, mflCode);
        UUID vendorId = getVendorId(eventBaseMessage.getEmrVendor());
        if (opClient.isPresent()) {
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            event.setEmrVendorId(vendorId);
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBaseMessage.getEventBase().getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            event.setEmrVendorId(vendorId);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handlePrepLinkedAtRiskPbfwEventUpload(EventBaseMessage<?> eventBaseMessage) {
        PrepLinkedAtRiskPbfwDto eventDto = mapper.convertValue(eventBaseMessage.getEventBase().getEvent(), PrepLinkedAtRiskPbfwDto.class);
        EventBase<PrepLinkedAtRiskPbfwDto> prepLinkedAtRiskPbfwDtoEventBase = new EventBase<>(eventBaseMessage.getEventBase().getClient(),
                eventBaseMessage.getEventBase().getEventType(), eventDto);
        // filter out old events
        Boolean isEarlier = isEarlierThanThreshHold(eventDto.getCreatedAt(), PROGRAM_START_THRESHOLD);
        if (isEarlier != null && isEarlier) {
            LOG.info("Skipping prep linked earlier than program start: {}", eventDto.getCreatedAt());
            return;
        }
        // validate
        validateEventBase(prepLinkedAtRiskPbfwDtoEventBase);
        String patientPk = prepLinkedAtRiskPbfwDtoEventBase.getClient().getPatientPk(),
                mflCode = prepLinkedAtRiskPbfwDtoEventBase.getEvent().getMflCode(),
                eventType = prepLinkedAtRiskPbfwDtoEventBase.getEventType();
        LOG.debug("Received prep linked event pk: {}, mflCode: {}", patientPk, mflCode);
        UUID vendorId = getVendorId(eventBaseMessage.getEmrVendor());
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
            event.setEmrVendorId(vendorId);
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBaseMessage.getEventBase().getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            event.setEmrVendorId(vendorId);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handlePrepUptakeEventUpload(EventBaseMessage<?> eventBaseMessage) {
        PrepUptakeDto eventDto = mapper.convertValue(eventBaseMessage.getEventBase().getEvent(), PrepUptakeDto.class);
        EventBase<PrepUptakeDto> prepUptakeDtoEventBase = new EventBase<>(eventBaseMessage.getEventBase().getClient(),
                eventBaseMessage.getEventBase().getEventType(), eventDto);
        // filter out old events
        Boolean isEarlier = isEarlierThanThreshHold(eventDto.createdAt(), PROGRAM_START_THRESHOLD);
        if (isEarlier != null && isEarlier) {
            LOG.info("Skipping prep uptake earlier than program start: {}", eventDto.createdAt());
            return;
        }
        // validate
        validateEventBase(prepUptakeDtoEventBase);
        String patientPk = prepUptakeDtoEventBase.getClient().getPatientPk(),
                mflCode = prepUptakeDtoEventBase.getEvent().mflCode(),
                eventType = prepUptakeDtoEventBase.getEventType();
        LocalDateTime createdAt = FlexibleDateTimeParser.parse(prepUptakeDtoEventBase.getEvent().createdAt());


        LOG.debug("Received prep uptake event pk: {}, mflCode: {}", patientPk, mflCode);
        UUID vendorId = getVendorId(eventBaseMessage.getEmrVendor());
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getCreatedAt().equals(createdAt) && e.getEventType().equals(eventType))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            event.setEmrVendorId(vendorId);
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBaseMessage.getEventBase().getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            event.setEmrVendorId(vendorId);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handleMortalityEventUpload(EventBaseMessage<?> eventBaseMessage) {
        MortalityDto eventDto = mapper.convertValue(eventBaseMessage.getEventBase().getEvent(), MortalityDto.class);
        EventBase<MortalityDto> mortalityDtoEventBase = new EventBase<>(eventBaseMessage.getEventBase().getClient(),
                eventBaseMessage.getEventBase().getEventType(), eventDto);
        // filter out old events
        Boolean isEarlier = isEarlierThanThreshHold(eventDto.createdAt(), PROGRAM_START_THRESHOLD);
        if (isEarlier != null && isEarlier) {
            LOG.info("Skipping mortality earlier than program start: {}", eventDto.createdAt());
            return;
        }
        // validate
        validateEventBase(mortalityDtoEventBase);
        String patientPk = mortalityDtoEventBase.getClient().getPatientPk(),
                mflCode = mortalityDtoEventBase.getEvent().mflCode(),
                eventType = mortalityDtoEventBase.getEventType();
        LOG.debug("Received mortality event pk: {}, mflCode: {}", patientPk, mflCode);
        UUID vendorId = getVendorId(eventBaseMessage.getEmrVendor());
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
            event.setEmrVendorId(vendorId);
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBaseMessage.getEventBase().getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            event.setEmrVendorId(vendorId);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handleEligibleForVlEventUpload(EventBaseMessage<?> eventBaseMessage) {
        EligibleForVlDto eventDto = mapper.convertValue(eventBaseMessage.getEventBase().getEvent(), EligibleForVlDto.class);
        EventBase<EligibleForVlDto> eligibleForVlDtoEventBase = new EventBase<>(eventBaseMessage.getEventBase().getClient(),
                eventBaseMessage.getEventBase().getEventType(), eventDto);
        // filter out old events
        Boolean isVisitDateEarlier = isEarlierThanThreshHold(eventDto.getVisitDate(), PROGRAM_START_THRESHOLD);
        Boolean isCreatedDateEarlier = isEarlierThanThreshHold(eventDto.getCreatedAt(), PROGRAM_START_THRESHOLD);
        if (isVisitDateEarlier != null && isVisitDateEarlier) {
            LOG.info("Skipping eligible for vl visitDate earlier than program start: {}", eventDto.getCreatedAt());
            return;
        }
        if (isCreatedDateEarlier != null && isCreatedDateEarlier) {
            LOG.info("Skipping eligible for vl createdAt earlier than program start: {}", eventDto.getCreatedAt());
            return;
        }
        // validate
        validateEventBase(eligibleForVlDtoEventBase);
        String patientPk = eligibleForVlDtoEventBase.getClient().getPatientPk(),
                mflCode = eligibleForVlDtoEventBase.getEvent().getMflCode(),
                eventType = eligibleForVlDtoEventBase.getEventType();
        LocalDateTime visitDate = FlexibleDateTimeParser.parse(eligibleForVlDtoEventBase.getEvent().getVisitDate());
        LOG.debug("Received eligible for VL event pk: {}, mflCode: {}", patientPk, mflCode);
        UUID vendorId = getVendorId(eventBaseMessage.getEmrVendor());
        Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
        if (opClient.isPresent()) {
            // TODO Update client as well
            Event event = opClient.get().getEvents()
                    .stream()
                    .filter(e -> e.getMflCode().equals(mflCode) && e.getClient().getPatientPk().equals(patientPk) &&
                            e.getEventType().equals(eventType) && e.getEligibleForVl().getVisitDate().equals(visitDate))
                    .findFirst().orElse(null);
            event = eventMapper.eventDtoToEventModel(eventDto, event);
            event.setClient(opClient.get());
            event.setEmrVendorId(vendorId);
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBaseMessage.getEventBase().getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            event.setEmrVendorId(vendorId);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handleUnsuppressedVlEventUpload(EventBaseMessage<?> eventBaseMessage) {
        UnsuppressedViralLoadDto eventDto = mapper.convertValue(eventBaseMessage.getEventBase().getEvent(), UnsuppressedViralLoadDto.class);
        EventBase<UnsuppressedViralLoadDto> unsuppressedViralLoadDtoEventBase = new EventBase<>(eventBaseMessage.getEventBase().getClient(),
                eventBaseMessage.getEventBase().getEventType(), eventDto);
        // filter out old events
        Boolean isEarlier = isEarlierThanThreshHold(eventDto.createdAt(), PROGRAM_START_THRESHOLD);
        if (isEarlier != null && isEarlier) {
            LOG.info("Skipping pbfw earlier than program start: {}", eventDto.createdAt());
            return;
        }
        // validate
        validateEventBase(unsuppressedViralLoadDtoEventBase);
        String patientPk = unsuppressedViralLoadDtoEventBase.getClient().getPatientPk(),
                mflCode = unsuppressedViralLoadDtoEventBase.getEvent().mflCode(),
                eventType = unsuppressedViralLoadDtoEventBase.getEventType();
        LOG.debug("Received unsuppressed vl event pk: {}, mflCode: {}", patientPk, mflCode);
        UUID vendorId = getVendorId(eventBaseMessage.getEmrVendor());
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
            event.setEmrVendorId(vendorId);
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBaseMessage.getEventBase().getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            event.setEmrVendorId(vendorId);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handleHeiWithoutPcrEventUpload(EventBaseMessage<?> eventBaseMessage) {
        HeiWithoutPcrDto eventDto = mapper.convertValue(eventBaseMessage.getEventBase().getEvent(), HeiWithoutPcrDto.class);
        EventBase<HeiWithoutPcrDto> heiWithoutPcrDtoEventBase = new EventBase<>(eventBaseMessage.getEventBase().getClient(),
                eventBaseMessage.getEventBase().getEventType(), eventDto);
        // validate
        validateEventBase(heiWithoutPcrDtoEventBase);
        String patientPk = heiWithoutPcrDtoEventBase.getClient().getPatientPk(),
                mflCode = heiWithoutPcrDtoEventBase.getEvent().mflCode(),
                eventType = heiWithoutPcrDtoEventBase.getEventType();
        LOG.debug("Received hei without pcr event pk: {}, mflCode: {}", patientPk, mflCode);
        UUID vendorId = getVendorId(eventBaseMessage.getEmrVendor());
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
            event.setEmrVendorId(vendorId);
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBaseMessage.getEventBase().getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            event.setEmrVendorId(vendorId);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handleHeiWithoutFinaOutcomeEventUpload(EventBaseMessage<?> eventBaseMessage) {
        HeiWithoutFinalOutcomeDto eventDto = mapper.convertValue(eventBaseMessage.getEventBase().getEvent(), HeiWithoutFinalOutcomeDto.class);
        EventBase<HeiWithoutFinalOutcomeDto> heiWithoutFinalOutcomeEventBase = new EventBase<>(eventBaseMessage.getEventBase().getClient(),
                eventBaseMessage.getEventBase().getEventType(), eventDto);
        // validate
        validateEventBase(heiWithoutFinalOutcomeEventBase);
        String patientPk = heiWithoutFinalOutcomeEventBase.getClient().getPatientPk(),
                mflCode = heiWithoutFinalOutcomeEventBase.getEvent().mflCode(),
                eventType = heiWithoutFinalOutcomeEventBase.getEventType();
        LOG.debug("Received hei without fo event pk: {}, mflCode: {}", patientPk, mflCode);
        UUID vendorId = getVendorId(eventBaseMessage.getEmrVendor());
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
            event.setEmrVendorId(vendorId);
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBaseMessage.getEventBase().getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            event.setEmrVendorId(vendorId);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handleHeiAged6To8MonthsEventUpload(EventBaseMessage<?> eventBaseMessage) {
        HeiAged6To8Dto eventDto = mapper.convertValue(eventBaseMessage.getEventBase().getEvent(), HeiAged6To8Dto.class);
        EventBase<HeiAged6To8Dto> heiAged6To8DtoEventBase = new EventBase<>(eventBaseMessage.getEventBase().getClient(),
                eventBaseMessage.getEventBase().getEventType(), eventDto);
        // validate
        validateEventBase(heiAged6To8DtoEventBase);
        String patientPk = heiAged6To8DtoEventBase.getClient().getPatientPk(),
                mflCode = heiAged6To8DtoEventBase.getEvent().mflCode(),
                eventType = heiAged6To8DtoEventBase.getEventType();
        LOG.debug("Received hei aged 6 to 8 months event pk: {}, mflCode: {}", patientPk, mflCode);
        UUID vendorId = getVendorId(eventBaseMessage.getEmrVendor());
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
            event.setEmrVendorId(vendorId);
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBaseMessage.getEventBase().getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            event.setEmrVendorId(vendorId);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }

    private void handleHeiAged24MonthsEventUpload(EventBaseMessage<?> eventBaseMessage) {
        HeiAged24Dto eventDto = mapper.convertValue(eventBaseMessage.getEventBase().getEvent(), HeiAged24Dto.class);
        EventBase<HeiAged24Dto> heiAged24DtoEventBase = new EventBase<>(eventBaseMessage.getEventBase().getClient(),
                eventBaseMessage.getEventBase().getEventType(), eventDto);
        // validate
        validateEventBase(heiAged24DtoEventBase);
        String patientPk = heiAged24DtoEventBase.getClient().getPatientPk(),
                mflCode = heiAged24DtoEventBase.getEvent().mflCode(),
                eventType = heiAged24DtoEventBase.getEventType();
        LOG.debug("Received hei aged 24 months event pk: {}, mflCode: {}", patientPk, mflCode);
        UUID vendorId = getVendorId(eventBaseMessage.getEmrVendor());
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
            event.setEmrVendorId(vendorId);
            eventRepository.save(event);
//            opClient.get().getEvents().add(event);
//            clientRepository.save(opClient.get());
        } else {
            // create new client event
            Client client = clientMapper.clientDtoToClientModel(eventBaseMessage.getEventBase().getClient());
            Event event = eventMapper.eventDtoToEventModel(eventDto, null);
            event.setClient(client);
            event.setEmrVendorId(vendorId);
            client.setEvents(List.of(event));
            clientRepository.save(client);
        }
    }
}
