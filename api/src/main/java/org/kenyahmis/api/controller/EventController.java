package org.kenyahmis.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.kenyahmis.api.service.CacheService;
import org.kenyahmis.shared.dto.EventBaseMessage;
import org.kenyahmis.shared.dto.EventList;
import org.kenyahmis.shared.dto.APIResponse;
import org.kenyahmis.shared.dto.EventBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@RestController
@RequestMapping("/api/event")
public class EventController {
    private final static Logger LOG = LoggerFactory.getLogger(EventController.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CacheService cacheService;

    public EventController(KafkaTemplate kafkaTemplate, CacheService cacheService) {
        this.kafkaTemplate = kafkaTemplate;
        this.cacheService = cacheService;
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
                                    )
                            }
                    )
            )
    )
    @PutMapping(value = "sync")
    private ResponseEntity<APIResponse> createEvent(@RequestBody @Valid EventList<EventBase<?>> eventList, @AuthenticationPrincipal Jwt jwt) {
        String emrVendor =  jwt.getClaimAsString("emr");
        if (emrVendor == null) {
            LOG.warn("Records received from unconfirmed vendor");
        }
        String mflMetadata = generatePayloadMetadata(eventList);
        String key = generatePayloadKey(mflMetadata, eventList.size());
        LOG.info("Received {} records from sites {}, vendor {}", eventList.size(), mflMetadata, emrVendor);
        // check if request fingerprint is in cache
        if (!cacheService.entryExists(key)) {
            // produce message
            eventList.forEach((Consumer<? super EventBase<?>>) eventBase -> kafkaTemplate.send("events", new EventBaseMessage<>(eventBase, emrVendor)));
            // add payload to cache
            String rawKey = eventList.size() + mflMetadata;;
            cacheService.addEntry(key, rawKey);
            LOG.info("Processing {} records from sites {}, vendor {}", eventList.size(), mflMetadata, emrVendor);
        }
        return new ResponseEntity<>(new APIResponse("Successfully added client events"),  HttpStatus.ACCEPTED);
    }
    private String generatePayloadMetadata(EventList<EventBase<?>> eventList) {
        ObjectMapper mapper = new ObjectMapper();
        Set<Object> mflSet = new HashSet<>();
        eventList.forEach((Consumer<? super EventBase<?>>) eventBase -> {
            Map<String, Object> map = mapper.convertValue(eventBase.getEvent(), new TypeReference<>() {});
            if (map.get("mflCode") != null) {
                mflSet.add(map.get("mflCode"));
            }
        });
        return mflSet.toString();
    }

    private String generatePayloadKey(String mflMetadata, int payloadSize) {
        try {
            String rawKey = payloadSize + mflMetadata;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] encodedHash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algo not found");
        }
    }
}
