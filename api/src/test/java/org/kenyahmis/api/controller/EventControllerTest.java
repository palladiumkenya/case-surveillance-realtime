package org.kenyahmis.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.kenyahmis.api.config.SecurityConfig;
import org.kenyahmis.api.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = EventController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://localhost/dummy",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://localhost/dummy",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "rate.limiting.enabled=false",
        "rate.limiting.ttl=60",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "springdoc.swagger-ui.server.url=http://localhost"
})
class EventControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private KafkaTemplate<String, Object> kafkaTemplate;
    @MockBean private CacheService cacheService;
    @MockBean private JwtDecoder jwtDecoder;
    @MockBean private RedisConnectionFactory redisConnectionFactory;

    private static final String SYNC_URL = "/api/event/sync";

    private String validPayload() {
        return """
            [{
                "client": {
                    "county": "Nairobi",
                    "subCounty": "Langata",
                    "ward": "Langata",
                    "patientPk": "505",
                    "sex": "male",
                    "dob": "1997-01-01"
                },
                "eventType": "new_case",
                "event": {
                    "mflCode": "1234",
                    "createdAt": "2025-07-01 00:00:00",
                    "positiveHivTestDate": "2025-07-01 00:00:00",
                    "updatedAt": "2025-07-01 00:00:00"
                }
            }]
            """;
    }

    // --- Happy path ---

    @Test
    void syncEvents_rateLimitingDisabled_returnsAccepted() throws Exception {
        when(cacheService.rateLimitingEnabled()).thenReturn(false);

        mockMvc.perform(put(SYNC_URL)
                        .with(jwt().jwt(j -> j.claim("emr", "TestEMR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isAccepted());

        verify(kafkaTemplate, atLeastOnce()).send(eq("events"), any());
    }

    @Test
    void syncEvents_rateLimitingEnabled_newChecksum_returnsAccepted() throws Exception {
        when(cacheService.rateLimitingEnabled()).thenReturn(true);
        when(cacheService.entryExists(anyString())).thenReturn(false);

        mockMvc.perform(put(SYNC_URL)
                        .with(jwt().jwt(j -> j.claim("emr", "TestEMR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isAccepted());

        verify(kafkaTemplate, atLeastOnce()).send(eq("events"), any());
        verify(cacheService).addEntry(anyString(), anyString());
    }

    @Test
    void syncEvents_rateLimitingEnabled_duplicateChecksum_returnsAccepted() throws Exception {
        when(cacheService.rateLimitingEnabled()).thenReturn(true);
        when(cacheService.entryExists(anyString())).thenReturn(true);

        mockMvc.perform(put(SYNC_URL)
                        .with(jwt().jwt(j -> j.claim("emr", "TestEMR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isAccepted());

        verify(kafkaTemplate, never()).send(eq("events"), any());
    }

    // --- Authentication ---

    @Test
    void syncEvents_noAuth_returnsUnauthorizedOrForbidden() throws Exception {
        mockMvc.perform(put(SYNC_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    void syncEvents_withAuth_extractsEmrVendor() throws Exception {
        when(cacheService.rateLimitingEnabled()).thenReturn(false);

        mockMvc.perform(put(SYNC_URL)
                        .with(jwt().jwt(j -> j.claim("emr", "KenyaEMR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isAccepted());

        verify(kafkaTemplate, atLeastOnce()).send(eq("events"), any());
    }

    // --- Validation ---

    @Test
    void syncEvents_invalidPayload_returns400() throws Exception {
        String invalidPayload = """
            [{
                "client": {
                    "patientPk": "",
                    "sex": "",
                    "dob": "1997-01-01"
                },
                "eventType": "new_case",
                "event": {
                    "mflCode": "1234",
                    "createdAt": "2025-07-01 00:00:00",
                    "positiveHivTestDate": "2025-07-01 00:00:00"
                }
            }]
            """;

        when(cacheService.rateLimitingEnabled()).thenReturn(false);

        mockMvc.perform(put(SYNC_URL)
                        .with(jwt().jwt(j -> j.claim("emr", "TestEMR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void syncEvents_invalidEventType_returns400() throws Exception {
        String invalidEventType = """
            [{
                "client": {
                    "county": "Nairobi",
                    "subCounty": "Langata",
                    "ward": "Langata",
                    "patientPk": "505",
                    "sex": "male",
                    "dob": "1997-01-01"
                },
                "eventType": "invalid_type",
                "event": {
                    "mflCode": "1234"
                }
            }]
            """;

        mockMvc.perform(put(SYNC_URL)
                        .with(jwt().jwt(j -> j.claim("emr", "TestEMR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEventType))
                .andExpect(status().isBadRequest());
    }
}
