package org.kenyahmis.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.kenyahmis.api.exception.RequestValidationException;
import org.kenyahmis.api.service.CacheService;
import org.kenyahmis.api.utils.ChecksumUtils;
import org.kenyahmis.shared.dto.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.kenyahmis.shared.constants.GlobalConstants.*;

@RestController
@RequestMapping("/api/event")
public class EventController {
    private final static Logger LOG = LoggerFactory.getLogger(EventController.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CacheService cacheService;
    private final ObjectMapper mapper;

    public EventController(KafkaTemplate kafkaTemplate, CacheService cacheService, ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.cacheService = cacheService;
        this.mapper = mapper;
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
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "At Risk PBFW Case",
                                            value = "[{\"client\":{\"county\":\"Nairobi\",\"subCounty\":\"Langata\",\"ward\":\"Langata\",\"patientPk\":\"505\",\"sex\":\"male\",\"dob\":\"1997-01-01\"},\"eventType\":\"at_risk_pbfw\",\"event\":{\"mflCode\":\"1234\",\"createdAt\":\"2024-01-01 00:00:00\",\"updatedAt\":\"2024-01-01 00:00:00\"}}]"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Prep Linked At Risk PBFW Case",
                                            value = "[{\"client\":{\"county\":\"Nairobi\",\"subCounty\":\"Langata\",\"ward\":\"Langata\",\"patientPk\":\"505\",\"sex\":\"male\",\"dob\":\"1997-01-01\"},\"eventType\":\"prep_linked_at_risk_pbfw\",\"event\":{\"mflCode\":\"1234\",\"prepRegimen\":\"AZT\",\"prepNumber\":\"3455\",\"prepStartDate\":\"2024-01-01 00:00:00\",\"createdAt\":\"2024-01-01 00:00:00\",\"updatedAt\":\"2024-01-01 00:00:00\"}}]"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Eligible for VL Case",
                                            value = "[{\"client\":{\"county\":\"Nairobi\",\"subCounty\":\"Langata\",\"ward\":\"Langata\",\"patientPk\":\"505\",\"sex\":\"male\",\"dob\":\"1997-01-01\"},\"eventType\":\"eligible_for_vl\",\"event\":{\"mflCode\":\"1234\",\"pregnancyStatus\":\"Pregnant\",\"breastFeedingStatus\":\"Yes\",\"lastVlResults\":\"300\",\"positiveHivTestDate\":\"2024-01-01 00:00:00\",\"visitDate\":\"2024-01-01 00:00:00\",\"artStartDate\":\"2024-01-01 00:00:00\",\"lastVlOrderDate\":\"2024-01-01 00:00:00\",\"lastVlResultsDate\":\"2024-01-01 00:00:00\",\"createdAt\":\"2024-01-01 00:00:00\",\"updatedAt\":\"2024-01-01 00:00:00\"}}]"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Unsuppressed Case",
                                            value = "[{\"client\":{\"county\":\"Nairobi\",\"subCounty\":\"Langata\",\"ward\":\"Langata\",\"patientPk\":\"505\",\"sex\":\"male\",\"dob\":\"1997-01-01\"},\"eventType\":\"unsuppressed_viral_load\",\"event\":{\"mflCode\":\"1234\",\"pregnancyStatus\":\"Pregnant\",\"breastFeedingStatus\":\"Yes\",\"lastVlResults\":\"300\",\"positiveHivTestDate\":\"2024-01-01 00:00:00\",\"visitDate\":\"2024-01-01 00:00:00\",\"artStartDate\":\"2024-01-01 00:00:00\",\"lastVlOrderDate\":\"2024-01-01 00:00:00\",\"lastVlResultsDate\":\"2024-01-01 00:00:00\",\"lastEacEncounterDate\":\"2024-01-01 00:00:00\",\"createdAt\":\"2024-01-01 00:00:00\",\"updatedAt\":\"2024-01-01 00:00:00\"}}]"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Hei Without PCR Case",
                                            value = "[{\"client\":{\"county\":\"Nairobi\",\"subCounty\":\"Langata\",\"ward\":\"Langata\",\"patientPk\":\"505\",\"sex\":\"male\",\"dob\":\"1997-01-01\"},\"eventType\":\"hei_without_pcr\",\"event\":{\"mflCode\":\"1234\",\"createdAt\":\"2024-01-01 00:00:00\",\"heiId\":\"455\",\"updatedAt\":\"2024-01-01 00:00:00\"}}]"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Hei Without Final Outcome Case",
                                            value = "[{\"client\":{\"county\":\"Nairobi\",\"subCounty\":\"Langata\",\"ward\":\"Langata\",\"patientPk\":\"505\",\"sex\":\"male\",\"dob\":\"1997-01-01\"},\"eventType\":\"hei_without_final_outcome\",\"event\":{\"mflCode\":\"1234\",\"createdAt\":\"2024-01-01 00:00:00\",\"heiId\":\"455\",\"updatedAt\":\"2024-01-01 00:00:00\"}}]"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Hei Aged 6 To 8 Weeks Case",
                                            value = "[{\"client\":{\"county\":\"Uasin Gishu\",\"subCounty\":\"Turbo\",\"ward\":\"Kiplombe\",\"patientPk\":\"977\",\"sex\":\"male\",\"dob\":\"2024-08-01\"},\"eventType\":\"hei_at_6_to_8_weeks\",\"event\":{\"mflCode\":\"33096\",\"createdAt\":\"2024-01-01 00:00:00\",\"heiId\":\"154\",\"updatedAt\":\"2024-01-01 00:00:00\"}}]"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Hei Aged 24 Weeks Case",
                                            value = "[{\"client\":{\"county\":\"Uasin Gishu\",\"subCounty\":\"Turbo\",\"ward\":\"Kiplombe\",\"patientPk\":\"977\",\"sex\":\"male\",\"dob\":\"2024-08-01\"},\"eventType\":\"hei_at_24_weeks\",\"event\":{\"mflCode\":\"33096\",\"createdAt\":\"2024-01-01 00:00:00\",\"heiId\":\"355\",\"updatedAt\":\"2024-01-01 00:00:00\"}}]"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Roll Call",
                                            value = "[{\"eventType\":\"roll_call\",\"event\":{\"mflCode\":\"1234\",\"emrVersion\":\"2.18\"}}]"
                                    )
                            }
                    )
            )
    )
    @PutMapping(value = "sync")
    @ResponseStatus(HttpStatus.ACCEPTED)
    private ResponseEntity<APIResponse> createEvent(@RequestBody @Valid EventList<EventBase<?>> eventList, @AuthenticationPrincipal Jwt jwt) {
        String emrVendor =  jwt.getClaimAsString("emr");
        if (emrVendor == null) {
            LOG.warn("Records received from unconfirmed vendor");
        }
        Set<String> mflCodes = extractMflCodes(eventList);
        LOG.info("Received {} records from sites {}, vendor {}", eventList.size(), mflCodes, emrVendor);
        // check if request fingerprint is in cache
        if (cacheService.rateLimitingEnabled()) {
            String rawPayload;
            try {
                rawPayload = mapper.writeValueAsString(eventList);
            } catch (JsonProcessingException je) {
                throw new RuntimeException("Failed to parse payload");
            }
            String checksum = ChecksumUtils.generateChecksum(rawPayload);
            if (!cacheService.entryExists(checksum)) {
                // validate entire payload
//                validateRequest(eventList, mflCodes);
                // produce message
                eventList.forEach((Consumer<? super EventBase<?>>) eventBase -> kafkaTemplate.send("events", new EventBaseMessage<>(eventBase, emrVendor)));
                // add payload to cache
                cacheService.addEntry(checksum, rawPayload);
                kafkaTemplate.send("reporting_manifest", new ManifestMessage(mflCodes, emrVendor));
                LOG.info("Processing {} records from sites {}, vendor {}", eventList.size(), mflCodes, emrVendor);
            }
        } else {
            LOG.warn("Facility rate limiting is disabled");
//            validateRequest(eventList, mflCodes);
            eventList.forEach((Consumer<? super EventBase<?>>) eventBase -> kafkaTemplate.send("events", new EventBaseMessage<>(eventBase, emrVendor)));
            kafkaTemplate.send("reporting_manifest", new ManifestMessage(mflCodes, emrVendor));
            LOG.info("Processing {} records from sites {}, vendor {}", eventList.size(), mflCodes, emrVendor);
        }
        return new ResponseEntity<>(new APIResponse("Successfully added client events"),  HttpStatus.ACCEPTED);
    }
    // returns mfl_codes
    private Set<String> extractMflCodes(EventList<EventBase<?>> eventList) {
        Set<String> mflSet = new HashSet<>();
        eventList.forEach((Consumer<? super EventBase<?>>) eventBase -> {
            Map<String, Object> map = mapper.convertValue(eventBase.getEvent(), new TypeReference<>() {});
            if (map.get("mflCode") != null) {
                mflSet.add(String.valueOf(map.get("mflCode")));
            }
        });
        return mflSet;
    }

    private void validateRequest(EventList<EventBase<?>> list, Set<String> mflCodes) {
        list.forEach((Consumer<? super EventBase<?>>) eventBase -> {
            switch (eventBase.getEventType()){
                case NEW_EVENT_TYPE -> validateEvent(eventBase.getEvent(), NewCaseDto.class, mflCodes);
                case LINKED_EVENT_TYPE -> validateEvent(eventBase.getEvent(), LinkedCaseDto.class, mflCodes);
                case AT_RISK_PBFW -> validateEvent(eventBase.getEvent(), AtRiskPbfwDto.class, mflCodes);
                case PREP_LINKED_AT_RISK_PBFW -> validateEvent(eventBase.getEvent(), PrepLinkedAtRiskPbfwDto.class, mflCodes);
                case ELIGIBLE_FOR_VL -> validateEvent(eventBase.getEvent(), EligibleForVlDto.class, mflCodes);
                case UNSUPPRESSED_VIRAL_LOAD -> validateEvent(eventBase.getEvent(), UnsuppressedViralLoadDto.class, mflCodes);
                case HEI_WITHOUT_PCR -> validateEvent(eventBase.getEvent(), HeiWithoutPcrDto.class, mflCodes);
                case HEI_WITHOUT_FINAL_OUTCOME -> validateEvent(eventBase.getEvent(), HeiWithoutFinalOutcomeDto.class, mflCodes);
                case HEI_AT_6_TO_8_WEEKS -> validateEvent(eventBase.getEvent(), HeiAged6To8Dto.class, mflCodes);
                case HEI_AT_24_WEEKS -> validateEvent(eventBase.getEvent(), HeiAged24Dto.class, mflCodes);
                case ROLL_CALL -> validateEvent(eventBase.getEvent(), RollCallDto.class, mflCodes);
                default -> LOG.warn("Unsupported event type: {}", eventBase.getEventType());
            }
        });
    }

    private void validateEvent(Object object, Class<?> mapping, Set<String> mflCodes){
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(mapper.convertValue(object, mapping));
        if (!violations.isEmpty()) {
            Map<String, String> errors = new HashMap<>();
            violations.forEach(violation -> {
                        errors.put(violation.getPropertyPath().toString() + " ("+ violation.getInvalidValue() +")", violation.getMessage());
                    }
            );
            throw new RequestValidationException(errors, mflCodes);
        }
    }
}
