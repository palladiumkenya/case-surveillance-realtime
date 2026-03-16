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
import org.springframework.util.DigestUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.kenyahmis.shared.constants.GlobalConstants.*;

@Service
public class EventService {
    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);
    private static final LocalDateTime PROGRAM_START_THRESHOLD = LocalDate.of(2025, 6, 1).atStartOfDay();

    private final EventRepository eventRepository;
    private final ClientRepository clientRepository;
    private final EmrVendorRepository emrVendorRepository;
    private final EventMapper eventMapper;
    private final ClientMapper clientMapper;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final Map<String, UUID> vendorCache = new ConcurrentHashMap<>();

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
    public void createEvent(Set<EventBaseMessage<?>> eventBaseMessageList) {
        for (EventBaseMessage<?> eventBaseMessage : eventBaseMessageList) {
            try {
                processEvent(eventBaseMessage);
            } catch (Exception e) {
                LOG.error("Failed to process event type {}: {}",
                        eventBaseMessage.getEventBase().getEventType(), e.getMessage(), e);
            }
        }
    }

    private void processEvent(EventBaseMessage<?> msg) {
        String eventType = msg.getEventBase().getEventType();
        switch (eventType) {
            case NEW_EVENT_TYPE -> handleEventUpload(msg, NewCaseDto.class, NewCaseDto::getMflCode, NewCaseDto::getCreatedAt, true);
            case LINKED_EVENT_TYPE -> handleEventUpload(msg, LinkedCaseDto.class, LinkedCaseDto::getMflCode, LinkedCaseDto::getCreatedAt, false);
            case AT_RISK_PBFW -> handleEventUpload(msg, AtRiskPbfwDto.class, AtRiskPbfwDto::getMflCode, AtRiskPbfwDto::getCreatedAt, true);
            case PREP_LINKED_AT_RISK_PBFW -> handleEventUpload(msg, PrepLinkedAtRiskPbfwDto.class, PrepLinkedAtRiskPbfwDto::getMflCode, PrepLinkedAtRiskPbfwDto::getCreatedAt, true);
            case PREP_UPTAKE -> handleEventUpload(msg, PrepUptakeDto.class, PrepUptakeDto::mflCode, PrepUptakeDto::createdAt, true);
            case MORTALITY -> handleEventUpload(msg, MortalityDto.class, MortalityDto::mflCode, MortalityDto::createdAt, true);
            case ELIGIBLE_FOR_VL -> handleEligibleForVlEventUpload(msg);
            case UNSUPPRESSED_VIRAL_LOAD -> handleEventUpload(msg, UnsuppressedViralLoadDto.class, UnsuppressedViralLoadDto::mflCode, UnsuppressedViralLoadDto::createdAt, true);
            case HEI_WITHOUT_PCR -> handleEventUpload(msg, HeiWithoutPcrDto.class, HeiWithoutPcrDto::mflCode, HeiWithoutPcrDto::createdAt, false);
            case HEI_WITHOUT_FINAL_OUTCOME -> handleEventUpload(msg, HeiWithoutFinalOutcomeDto.class, HeiWithoutFinalOutcomeDto::mflCode, HeiWithoutFinalOutcomeDto::createdAt, false);
            case HEI_AT_6_TO_8_WEEKS -> handleEventUpload(msg, HeiAged6To8Dto.class, HeiAged6To8Dto::mflCode, HeiAged6To8Dto::createdAt, false);
            case HEI_AT_24_WEEKS -> {
                msg.getEventBase().setEventType(HEI_AT_6_TO_8_WEEKS);
                handleEventUpload(msg, HeiAged6To8Dto.class, HeiAged6To8Dto::mflCode, HeiAged6To8Dto::createdAt, false);
            }
            case ROLL_CALL -> LOG.info("Received roll_call event, ignore");
            default -> LOG.warn("Event Type: {} not handled", eventType);
        }
    }

    private <T> void handleEventUpload(EventBaseMessage<?> msg, Class<T> dtoClass,
                                        Function<T, String> mflCodeExtractor,
                                        Function<T, String> createdAtExtractor, Boolean threshHoldValidate) {
        T eventDto = mapper.convertValue(msg.getEventBase().getEvent(), dtoClass);

        String createdAt = createdAtExtractor.apply(eventDto);
        // Filter events earlier than program start
        if (threshHoldValidate) {
            if (Boolean.TRUE.equals(isEarlierThanThreshold(createdAt, PROGRAM_START_THRESHOLD))) {
                LOG.info("Skipping {} earlier than program start", msg.getEventBase().getEventType());
                return;
            }
        }

        EventBase<T> eventBase = new EventBase<>(msg.getEventBase().getClient(),
                msg.getEventBase().getEventType(), eventDto);
        validateEventBase(eventBase);

        String patientPk = eventBase.getClient().getPatientPk();
        String mflCode = mflCodeExtractor.apply(eventDto);
        String eventType = eventBase.getEventType();
        LOG.debug("Received {} event pk: {}, mflCode: {}", eventType, patientPk, mflCode);

        UUID vendorId = getVendorId(msg.getEmrVendor());
        String recordId = generateUniqueEventId(patientPk, mflCode, eventType, createdAt);
        Event existingEvent = eventRepository.findByEventUniqueId(recordId)
                .orElse(null);

        upsertEvent(msg, eventDto, patientPk, mflCode, recordId, vendorId, existingEvent);
    }

    private void handleEligibleForVlEventUpload(EventBaseMessage<?> msg) {
        EligibleForVlDto eventDto = mapper.convertValue(msg.getEventBase().getEvent(), EligibleForVlDto.class);

        // EligibleForVl checks both visitDate and createdAt thresholds
        if (Boolean.TRUE.equals(isEarlierThanThreshold(eventDto.getVisitDate(), PROGRAM_START_THRESHOLD))) {
            LOG.info("Skipping eligible for vl visitDate earlier than program start: {}", eventDto.getVisitDate());
            return;
        }
        if (Boolean.TRUE.equals(isEarlierThanThreshold(eventDto.getCreatedAt(), PROGRAM_START_THRESHOLD))) {
            LOG.info("Skipping eligible for vl createdAt earlier than program start: {}", eventDto.getCreatedAt());
            return;
        }

        EventBase<EligibleForVlDto> eventBase = new EventBase<>(msg.getEventBase().getClient(),
                msg.getEventBase().getEventType(), eventDto);
        validateEventBase(eventBase);

        String patientPk = eventBase.getClient().getPatientPk();
        String mflCode = eventDto.getMflCode();
        String eventType = eventBase.getEventType();
        String visitDate = eventDto.getVisitDate();
        LOG.debug("Received eligible for VL event pk: {}, mflCode: {}", patientPk, mflCode);

        UUID vendorId = getVendorId(msg.getEmrVendor());
        // EligibleForVl deduplicates by visitDate in addition to patientPk + mflCode + eventType
        String recordId = generateUniqueEventId(patientPk, mflCode, eventType, visitDate);
        Event existingEvent = eventRepository
                .findByEventUniqueId(recordId)
                .orElse(null);

        upsertEvent(msg, eventDto, patientPk, mflCode, recordId, vendorId, existingEvent);
    }

    private void upsertEvent(EventBaseMessage<?> msg, Object eventDto, String patientPk,
                              String mflCode, String recordId, UUID vendorId, Event existingEvent) {
        Event event = eventMapper.eventDtoToEventModel(eventDto, existingEvent);
        event.setEmrVendorId(vendorId);

        if (existingEvent != null) {
            // TODO update client as well
            eventRepository.save(event);
        } else {
            event.setEventUniqueId(recordId);
            Optional<Client> opClient = clientRepository.findByPatientPkAndSiteCode(patientPk, mflCode);
            if (opClient.isPresent()) {
                Client client = opClient.get();
                ClientDto clientDto = msg.getEventBase().getClient();
                updateClientFromDto(client, clientDto);
                event.setClient(client);
                eventRepository.save(event);
            } else {
                Client client = clientMapper.clientDtoToClientModel(msg.getEventBase().getClient());
                event.setClient(client);
                client.setMflCode(mflCode);
                client.setEvents(List.of(event));
                clientRepository.save(client);
            }
        }
    }

    private void updateClientFromDto(Client client, ClientDto clientDto) {
        client.setCounty(clientDto.getCounty());
        client.setSubCounty(clientDto.getSubCounty());
        client.setWard(clientDto.getWard());
        client.setSex(clientDto.getSex());
        client.setShaNumber(clientDto.getShaNumber());
        if (clientDto.getDob() != null) {
            client.setDob(LocalDate.parse(clientDto.getDob()));
        }
    }

    private <T> void validateEventBase(EventBase<T> object) throws RequestValidationException {
        Set<ConstraintViolation<EventBase<T>>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new HashMap<>();
            violations.forEach(violation -> {
                        LOG.error("Request validation failed: {} : {}", violation.getPropertyPath().toString(), violation.getMessage());
                        errors.put(violation.getPropertyPath().toString(), violation.getMessage());
                    }
            );
            throw new RequestValidationException(errors);
        }
    }

    private UUID getVendorId(String vendorName) {
        if (vendorName == null) return null;
        return vendorCache.computeIfAbsent(vendorName, name ->
                emrVendorRepository.findByVendorName(name).map(EmrVendor::getId).orElse(null));
    }

    private Boolean isEarlierThanThreshold(String eventCreatedDate, LocalDateTime threshold) {
        if (StringUtils.isEmpty(eventCreatedDate)) return null;
        try {
            LocalDateTime eventCreated = FlexibleDateTimeParser.parse(eventCreatedDate);
            return eventCreated.isBefore(threshold);
        } catch (DateTimeException e) {
            LOG.error("Failed to parse date", e);
            return null;
        }
    }

    private String generateUniqueEventId(String ... elements) {
        return DigestUtils.md5DigestAsHex(String.join("", elements).getBytes());
    }
}
