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

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.kenyahmis.shared.constants.GlobalConstants.*;

@RestController
@RequestMapping("/api/event")
public class EventController {
    private static final Logger LOG = LoggerFactory.getLogger(EventController.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CacheService cacheService;
    private final ObjectMapper mapper;
    private final Validator validator;

    public EventController(KafkaTemplate<String, Object> kafkaTemplate, CacheService cacheService, ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.cacheService = cacheService;
        this.mapper = mapper;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
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
                                            name = "Prep Uptake",
                                            value = "[{\"client\":{\"county\":\"Nairobi\",\"subCounty\":\"Langata\",\"ward\":\"Langata\",\"patientPk\":\"505\",\"sex\":\"male\",\"dob\":\"1997-01-01\"},\"eventType\":\"prep_uptake\",\"event\":{\"createdAt\":\"2026-02-02 15:45:07\",\"prepTreatmentPlan\":\"switch\",\"prepType\":\"DAPIVIRINE\",\"isPregnant\":\"Yes\",\"isBreastfeeding\":\"No\",\"dosingStrategy\":\"LONG ACTING PREP\",\"reasonForSwitchingPrep\":\"Number of instances of stock outs of key tracer drugs in the last month\",\"mflCode\":13077,\"prepStartDate\":\"2026-02-02 15:43:18\",\"reasonForStartingPrep\":\"Recurrent use of PEP\",\"dateSwitchedPrep\":\"2026-02-02\",\"dateDiscontinuedFromPrep\":\"2026-02-02\",\"updatedAt\":null,\"prepNumber\":\"13077202609099\",\"prepRegimen\":\"TDF/FTC\"}}]"
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
                                            name = "Mortality",
                                            value = "[{\"client\":{\"county\":\"Nairobi\",\"subCounty\":\"Langata\",\"ward\":\"Langata\",\"patientPk\":\"505\",\"sex\":\"male\",\"dob\":\"1997-01-01\"},\"eventType\":\"mortality\",\"event\":{\"updatedAt\":\"2026-02-02 15:45:07\",\"createdAt\":\"2026-02-02 15:45:07\",\"mflCode\":13077,\"causeOfDeath\":\"Neoplasm/cancer\",\"deathDate\":\"2026-01-02 15:45:07\"}}]"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Add Missed VL Opportunity",
                                            value = "[{\"client\":{\"county\":\"Mombasa\",\"subCounty\":\"Mvita\",\"ward\":\"Mvita\",\"patientPk\":\"56634\",\"sex\":\"female\",\"dob\":\"2000-01-01\"},\"eventType\":\"missed_vl_opportunities\",\"event\":{\"mflCode\":\"33107\",\"missedVlFlag\":true,\"visitDate\":\"2025-11-01 00:00:00\",\"createdAt\":\"2025-11-01 00:00:00\",\"updatedAt\":\"2024-01-01 00:00:00\"}}]"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Unsuppressed Vl Without Eac Within 2Weeks",
                                            value = "[{\"client\":{\"county\":\"Mombasa\",\"subCounty\":\"Mvita\",\"ward\":\"Mvita\",\"patientPk\":\"56634\",\"sex\":\"female\",\"dob\":\"2000-01-01\"},\"eventType\":\"unsuppressed_vl_without_eac_within_2_weeks\",\"event\":{\"mflCode\":\"33107\",\"missedEacFlag\":true,\"date14DaysPostHvl\":\"2025-11-01 00:00:00\",\"createdAt\":\"2025-11-01 00:00:00\",\"updatedAt\":\"2024-01-01 00:00:00\"}}]"
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
    private ResponseEntity<APIResponse> createEvent(@RequestBody @Valid EventList<EventBase<?>> eventList,
                                                    @AuthenticationPrincipal Jwt jwt) {
        String emrVendor = jwt.getClaimAsString("emr");
        if (emrVendor == null) {
            LOG.warn("Records received from unconfirmed vendor");
        }

        boolean duplicate = false;
        String checksum = null;
        String rawPayload = null;
        boolean rateLimitingEnabled = cacheService.rateLimitingEnabled();

        if (rateLimitingEnabled) {
            try {
                rawPayload = mapper.writeValueAsString(eventList);
            } catch (JsonProcessingException je) {
                throw new IllegalStateException("Failed to serialize payload for checksum", je);
            }
            checksum = ChecksumUtils.generateChecksum(rawPayload);
            duplicate = cacheService.entryExists(checksum);
        } else {
            LOG.warn("Facility rate limiting is disabled");
        }

        BatchContext ctx = processBatch(eventList, !duplicate);
        LOG.info("Received {} records from sites {}, vendor {}, duplicate={}",
                eventList.size(), ctx.mflCodes, emrVendor, duplicate);

        if (!duplicate) {
            for (EventBase<?> eventBase : eventList) {
                kafkaTemplate.send("events", new EventBaseMessage<>(eventBase, emrVendor));
            }
            if (rateLimitingEnabled) {
                cacheService.addEntry(checksum, rawPayload);
            }
            kafkaTemplate.send("reporting_manifest", new ManifestMessage(ctx.mflCodes, emrVendor, ctx.emrVersion));
            LOG.info("Processing {} records from sites {}, vendor {}", eventList.size(), ctx.mflCodes, emrVendor);
        }

        Instant uploadedAt = Instant.now();
        ctx.metrics.forEach((key, count) ->
                kafkaTemplate.send("upload_metrics", key.siteCode,
                        new UploadMetricsMessage(key.siteCode, key.eventType, count, uploadedAt)));

        return new ResponseEntity<>(new APIResponse("Successfully added client events"), HttpStatus.ACCEPTED);
    }

    private BatchContext processBatch(EventList<EventBase<?>> eventList, boolean validate) {
        Set<String> mflCodes = new HashSet<>();
        Map<MetricKey, Long> metrics = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        String emrVersion = null;

        for (EventBase<?> eventBase : eventList) {
            String eventType = eventBase.getEventType();
            Map<String, Object> eventMap = mapper.convertValue(eventBase.getEvent(), new TypeReference<>() {});

            String siteCode = null;
            Object mflObj = eventMap.get("mflCode");
            if (mflObj != null) {
                siteCode = String.valueOf(mflObj);
                mflCodes.add(siteCode);
            }
            if (ROLL_CALL.equals(eventType)) {
                Object emrVersionObject = eventMap.get("emrVersion");
                if (emrVersionObject != null) {
                    emrVersion = emrVersionObject.toString();
                }
            }

            if (siteCode != null) {
                metrics.merge(new MetricKey(siteCode, eventType), 1L, Long::sum);
            }

            if (validate) {
                Class<?> dtoClass = dtoClassFor(eventType);
                if (dtoClass == null) {
                    LOG.warn("Unsupported event type: {}", eventType);
                } else {
                    collectViolations(eventBase.getEvent(), dtoClass, errors);
                }
            }
        }

        if (validate && !errors.isEmpty()) {
            throw new RequestValidationException(errors, mflCodes);
        }

        return new BatchContext(mflCodes, emrVersion, metrics);
    }

    private Class<?> dtoClassFor(String eventType) {
        return switch (eventType) {
            case NEW_EVENT_TYPE -> NewCaseDto.class;
            case LINKED_EVENT_TYPE -> LinkedCaseDto.class;
            case AT_RISK_PBFW -> AtRiskPbfwDto.class;
            case PREP_LINKED_AT_RISK_PBFW -> PrepLinkedAtRiskPbfwDto.class;
            case PREP_UPTAKE -> PrepUptakeDto.class;
            case ELIGIBLE_FOR_VL -> EligibleForVlDto.class;
            case UNSUPPRESSED_VIRAL_LOAD -> UnsuppressedViralLoadDto.class;
            case HEI_WITHOUT_PCR -> HeiWithoutPcrDto.class;
            case HEI_WITHOUT_FINAL_OUTCOME -> HeiWithoutFinalOutcomeDto.class;
            case HEI_AT_6_TO_8_WEEKS -> HeiAged6To8Dto.class;
            case HEI_AT_24_WEEKS -> HeiAged24Dto.class;
            case MORTALITY -> MortalityDto.class;
            case MISSED_VL_OPPORTUNITIES -> MissedVlOpportunitiesDto.class;
            case UNSUPPRESSED_VL_WITHOUT_EAC_WITHIN_2_WEEKS -> UnsuppressedVlWithoutEacWithin2WeeksDto.class;
            case ROLL_CALL -> RollCallDto.class;
            default -> null;
        };
    }

    private void collectViolations(Object object, Class<?> mapping, Map<String, String> errors) {
        Set<ConstraintViolation<Object>> violations = validator.validate(mapper.convertValue(object, mapping));
        for (ConstraintViolation<Object> violation : violations) {
            errors.put(violation.getPropertyPath().toString() + " (" + violation.getInvalidValue() + ")", violation.getMessage());
            LOG.error("Request validation failed: {} : {}", violation.getPropertyPath().toString(), violation.getMessage());
        }
    }

    private record MetricKey(String siteCode, String eventType) {
    }

    private record BatchContext(Set<String> mflCodes, String emrVersion, Map<MetricKey, Long> metrics) {}
}
