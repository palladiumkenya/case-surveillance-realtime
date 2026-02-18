package org.kenyahmis.worker.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kenyahmis.shared.constants.GlobalConstants;
import org.kenyahmis.shared.dto.*;
import org.kenyahmis.worker.model.*;

import static org.junit.jupiter.api.Assertions.*;

class EventMapperTest {

    private EventMapper eventMapper;

    @BeforeEach
    void setUp() {
        eventMapper = new EventMapper() {};
    }

    @Test
    void newCaseDto_newEvent_returnsEventWithCorrectFields() {
        NewCaseDto dto = new NewCaseDto();
        dto.setMflCode("12345");
        dto.setCreatedAt("2025-07-01 10:00:00");
        dto.setUpdatedAt("2025-07-02 10:00:00");
        dto.setPositiveHivTestDate("2025-06-15 08:00:00");

        Event result = eventMapper.eventDtoToEventModel(dto, null);

        assertEquals(GlobalConstants.NEW_EVENT_TYPE, result.getEventType());
        assertEquals("12345", result.getMflCode());
        assertNotNull(result.getNewCase());
        assertNotNull(result.getNewCase().getPositiveHivTestDate());
        assertSame(result, result.getNewCase().getEvent());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void newCaseDto_existingEvent_reusesEventAndNewCase() {
        Event existing = new Event();
        NewCase existingNewCase = new NewCase();
        existing.setNewCase(existingNewCase);

        NewCaseDto dto = new NewCaseDto();
        dto.setMflCode("12345");
        dto.setCreatedAt("2025-07-01 10:00:00");
        dto.setPositiveHivTestDate("2025-06-15 08:00:00");

        Event result = eventMapper.eventDtoToEventModel(dto, existing);

        assertSame(existing, result);
        assertSame(existingNewCase, result.getNewCase());
    }

    @Test
    void linkedCaseDto_setsArtStartDate() {
        LinkedCaseDto dto = new LinkedCaseDto();
        dto.setMflCode("99999");
        dto.setCreatedAt("2025-07-01 10:00:00");
        dto.setArtStartDate("2025-06-20 09:00:00");

        Event result = eventMapper.eventDtoToEventModel(dto, null);

        assertEquals(GlobalConstants.LINKED_EVENT_TYPE, result.getEventType());
        assertNotNull(result.getLinkedCase());
        assertNotNull(result.getLinkedCase().getArtStartDate());
        assertSame(result, result.getLinkedCase().getEvent());
    }

    @Test
    void mortalityDto_setsCauseAndDate() {
        MortalityDto dto = new MortalityDto("Neoplasm/cancer", "2025-08-01 12:00:00",
                "13077", "2025-07-15 10:00:00", null);

        Event result = eventMapper.eventDtoToEventModel(dto, null);

        assertEquals(GlobalConstants.MORTALITY, result.getEventType());
        assertNotNull(result.getMortality());
        assertEquals("Neoplasm/cancer", result.getMortality().getCauseOfDeath());
        assertNotNull(result.getMortality().getDeathDate());
        assertSame(result, result.getMortality().getEvent());
    }

    @Test
    void prepUptakeDto_mapsAllFields() {
        PrepUptakeDto dto = new PrepUptakeDto(
                "13077", "13077202609099", "New", "ORAL",
                "TDF/FTC", "DAILY", "2025-07-01 10:00:00",
                "Recurrent use of PEP", null,
                null, null, null,
                "Yes", "No",
                "2025-07-01 10:00:00", null
        );

        Event result = eventMapper.eventDtoToEventModel(dto, null);

        assertEquals(GlobalConstants.PREP_UPTAKE, result.getEventType());
        PrepUptake prepUptake = result.getPrepUptake();
        assertNotNull(prepUptake);
        assertEquals("13077202609099", prepUptake.getPrepNumber());
        assertEquals("New", prepUptake.getPrepStatus());
        assertEquals("ORAL", prepUptake.getPrepType());
        assertEquals("TDF/FTC", prepUptake.getPrepRegimen());
        assertEquals("Recurrent use of PEP", prepUptake.getReasonForStartingPrep());
        assertEquals("Yes", prepUptake.getIsPregnant());
        assertEquals("No", prepUptake.getIsBreastfeeding());
        assertNotNull(prepUptake.getPrepStartDate());
        assertSame(result, prepUptake.getEvent());
    }

    @Test
    void nullDates_handledGracefully() {
        NewCaseDto dto = new NewCaseDto();
        dto.setMflCode("12345");
        // leave all date fields null

        Event result = eventMapper.eventDtoToEventModel(dto, null);

        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
        assertNull(result.getNewCase().getPositiveHivTestDate());
    }

    @Test
    void unknownDtoType_returnsEventUnchanged() {
        Object randomDto = new Object();

        Event result = eventMapper.eventDtoToEventModel(randomDto, null);

        assertNotNull(result);
        assertNull(result.getEventType());
        assertNull(result.getNewCase());
        assertNull(result.getLinkedCase());
        assertNull(result.getMortality());
    }
}
