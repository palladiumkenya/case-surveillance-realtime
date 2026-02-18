package org.kenyahmis.worker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kenyahmis.shared.dto.*;
import org.kenyahmis.worker.mapper.ClientMapper;
import org.kenyahmis.worker.mapper.EventMapper;
import org.kenyahmis.worker.model.Client;
import org.kenyahmis.worker.model.EmrVendor;
import org.kenyahmis.worker.model.Event;
import org.kenyahmis.worker.repository.ClientRepository;
import org.kenyahmis.worker.repository.EmrVendorRepository;
import org.kenyahmis.worker.repository.EventRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.kenyahmis.shared.constants.GlobalConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private EmrVendorRepository emrVendorRepository;
    @Mock private EventMapper eventMapper;
    @Mock private ClientMapper clientMapper;

    @InjectMocks private EventService eventService;

    private ClientDto clientDto;

    @BeforeEach
    void setUp() {
        clientDto = new ClientDto();
        clientDto.setPatientPk("505");
        clientDto.setSex("male");
        clientDto.setCounty("Nairobi");
        clientDto.setSubCounty("Langata");
        clientDto.setWard("Langata");
        clientDto.setDob("1990-01-01");
    }

    private EventBaseMessage<?> buildMessage(String eventType, Object eventDto) {
        EventBase<Object> eventBase = new EventBase<>(clientDto, eventType, eventDto);
        EventBaseMessage<Object> msg = new EventBaseMessage<>();
        msg.setEventBase(eventBase);
        msg.setEmrVendor("TestEMR");
        return msg;
    }

    private NewCaseDto newCaseDto(String createdAt) {
        NewCaseDto dto = new NewCaseDto();
        dto.setMflCode("12345");
        dto.setCreatedAt(createdAt);
        dto.setPositiveHivTestDate("2025-07-01 10:00:00");
        return dto;
    }

    // --- Dispatch & routing ---

    @Test
    void processesNewCaseEvent() {
        NewCaseDto dto = newCaseDto("2025-07-01 10:00:00");
        EventBaseMessage<?> msg = buildMessage(NEW_EVENT_TYPE, dto);
        Event event = new Event();

        when(eventMapper.eventDtoToEventModel(any(), isNull())).thenReturn(event);
        when(eventRepository.findByClient_PatientPkAndMflCodeAndEventType("505", "12345", NEW_EVENT_TYPE))
                .thenReturn(Optional.empty());
        Client client = new Client();
        when(clientRepository.findByPatientPkAndSiteCode("505", "12345")).thenReturn(Optional.of(client));

        eventService.createEvent(Set.of(msg));

        verify(eventRepository).save(event);
    }

    @Test
    void processesLinkedCaseEvent() {
        LinkedCaseDto dto = new LinkedCaseDto();
        dto.setMflCode("12345");
        dto.setCreatedAt("2025-07-01 10:00:00");
        dto.setArtStartDate("2025-07-01 10:00:00");
        EventBaseMessage<?> msg = buildMessage(LINKED_EVENT_TYPE, dto);
        Event event = new Event();

        when(eventMapper.eventDtoToEventModel(any(), isNull())).thenReturn(event);
        when(eventRepository.findByClient_PatientPkAndMflCodeAndEventType("505", "12345", LINKED_EVENT_TYPE))
                .thenReturn(Optional.empty());
        Client client = new Client();
        when(clientRepository.findByPatientPkAndSiteCode("505", "12345")).thenReturn(Optional.of(client));

        eventService.createEvent(Set.of(msg));

        verify(eventRepository).save(event);
    }

    @Test
    void processesMortalityEvent() {
        MortalityDto dto = new MortalityDto("Cancer", "2025-08-01 12:00:00",
                "12345", "2025-07-15 10:00:00", null);
        EventBaseMessage<?> msg = buildMessage(MORTALITY, dto);
        Event event = new Event();

        when(eventMapper.eventDtoToEventModel(any(), isNull())).thenReturn(event);
        when(eventRepository.findByClient_PatientPkAndMflCodeAndEventType("505", "12345", MORTALITY))
                .thenReturn(Optional.empty());
        Client client = new Client();
        when(clientRepository.findByPatientPkAndSiteCode("505", "12345")).thenReturn(Optional.of(client));

        eventService.createEvent(Set.of(msg));

        verify(eventRepository).save(event);
    }

    @Test
    void rollCallEvent_ignored() {
        RollCallDto dto = new RollCallDto("12345", "2.18");
        EventBaseMessage<?> msg = buildMessage(ROLL_CALL, dto);

        eventService.createEvent(Set.of(msg));

        verifyNoInteractions(eventRepository);
        verifyNoInteractions(clientRepository);
    }

    @Test
    void unknownEventType_noRepositoryCalls() {
        NewCaseDto dto = newCaseDto("2025-07-01 10:00:00");
        EventBaseMessage<?> msg = buildMessage("unknown_type", dto);

        eventService.createEvent(Set.of(msg));

        verifyNoInteractions(eventRepository);
        verifyNoInteractions(clientRepository);
    }

    // --- Threshold filtering ---

    @Test
    void skipsEventBeforeThreshold() {
        NewCaseDto dto = newCaseDto("2025-01-01 00:00:00");
        EventBaseMessage<?> msg = buildMessage(NEW_EVENT_TYPE, dto);

        eventService.createEvent(Set.of(msg));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void processesEventAfterThreshold() {
        NewCaseDto dto = newCaseDto("2025-07-01 00:00:00");
        EventBaseMessage<?> msg = buildMessage(NEW_EVENT_TYPE, dto);
        Event event = new Event();

        when(eventMapper.eventDtoToEventModel(any(), isNull())).thenReturn(event);
        when(eventRepository.findByClient_PatientPkAndMflCodeAndEventType("505", "12345", NEW_EVENT_TYPE))
                .thenReturn(Optional.empty());
        Client client = new Client();
        when(clientRepository.findByPatientPkAndSiteCode("505", "12345")).thenReturn(Optional.of(client));

        eventService.createEvent(Set.of(msg));

        verify(eventRepository).save(event);
    }

    // --- Upsert logic ---

    @Test
    void updatesExistingEvent() {
        NewCaseDto dto = newCaseDto("2025-07-01 10:00:00");
        EventBaseMessage<?> msg = buildMessage(NEW_EVENT_TYPE, dto);
        Event existingEvent = new Event();
        Event mappedEvent = new Event();

        when(eventRepository.findByClient_PatientPkAndMflCodeAndEventType("505", "12345", NEW_EVENT_TYPE))
                .thenReturn(Optional.of(existingEvent));
        when(eventMapper.eventDtoToEventModel(any(), eq(existingEvent))).thenReturn(mappedEvent);

        eventService.createEvent(Set.of(msg));

        verify(eventRepository).save(mappedEvent);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void createsNewEventForExistingClient() {
        NewCaseDto dto = newCaseDto("2025-07-01 10:00:00");
        EventBaseMessage<?> msg = buildMessage(NEW_EVENT_TYPE, dto);
        Event event = new Event();
        Client client = new Client();

        when(eventRepository.findByClient_PatientPkAndMflCodeAndEventType("505", "12345", NEW_EVENT_TYPE))
                .thenReturn(Optional.empty());
        when(eventMapper.eventDtoToEventModel(any(), isNull())).thenReturn(event);
        when(clientRepository.findByPatientPkAndSiteCode("505", "12345")).thenReturn(Optional.of(client));

        eventService.createEvent(Set.of(msg));

        verify(eventRepository).save(event);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void createsNewClientAndEvent() {
        NewCaseDto dto = newCaseDto("2025-07-01 10:00:00");
        EventBaseMessage<?> msg = buildMessage(NEW_EVENT_TYPE, dto);
        Event event = new Event();
        Client newClient = new Client();

        when(eventRepository.findByClient_PatientPkAndMflCodeAndEventType("505", "12345", NEW_EVENT_TYPE))
                .thenReturn(Optional.empty());
        when(eventMapper.eventDtoToEventModel(any(), isNull())).thenReturn(event);
        when(clientRepository.findByPatientPkAndSiteCode("505", "12345")).thenReturn(Optional.empty());
        when(clientMapper.clientDtoToClientModel(any(ClientDto.class))).thenReturn(newClient);

        eventService.createEvent(Set.of(msg));

        verify(clientRepository).save(newClient);
    }

    // --- Error handling ---

    @Test
    void singleBadMessageDoesNotStopBatch() {
        // First message will cause an exception (null eventBase)
        EventBaseMessage<?> badMsg = buildMessage(NEW_EVENT_TYPE, null);

        // Second message is valid
        NewCaseDto dto = newCaseDto("2025-07-01 10:00:00");
        EventBaseMessage<?> goodMsg = buildMessage(NEW_EVENT_TYPE, dto);
        Event event = new Event();

        // Use a LinkedHashSet to control iteration order
        Set<EventBaseMessage<?>> messages = new LinkedHashSet<>();
        messages.add(badMsg);
        messages.add(goodMsg);

        // The good message should still be processed even if the bad one fails
        lenient().when(eventMapper.eventDtoToEventModel(any(), isNull())).thenReturn(event);
        lenient().when(eventRepository.findByClient_PatientPkAndMflCodeAndEventType(eq("505"), eq("12345"), eq(NEW_EVENT_TYPE)))
                .thenReturn(Optional.empty());
        Client client = new Client();
        lenient().when(clientRepository.findByPatientPkAndSiteCode("505", "12345")).thenReturn(Optional.of(client));

        // Should not throw - the batch catch block handles individual failures
        eventService.createEvent(messages);
    }

    // --- Vendor caching ---

    @Test
    void cachesVendorId() {
        NewCaseDto dto1 = newCaseDto("2025-07-01 10:00:00");
        NewCaseDto dto2 = newCaseDto("2025-07-02 10:00:00");
        EventBaseMessage<?> msg1 = buildMessage(NEW_EVENT_TYPE, dto1);
        EventBaseMessage<?> msg2 = buildMessage(NEW_EVENT_TYPE, dto2);
        Event event = new Event();

        EmrVendor vendor = new EmrVendor();
        vendor.setId(UUID.randomUUID());
        when(emrVendorRepository.findByVendorName("TestEMR")).thenReturn(Optional.of(vendor));
        when(eventMapper.eventDtoToEventModel(any(), any())).thenReturn(event);
        when(eventRepository.findByClient_PatientPkAndMflCodeAndEventType(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        Client client = new Client();
        when(clientRepository.findByPatientPkAndSiteCode(anyString(), anyString())).thenReturn(Optional.of(client));

        // Process first message
        eventService.createEvent(Set.of(msg1));
        // Process second message
        eventService.createEvent(Set.of(msg2));

        // Vendor lookup should only happen once due to caching
        verify(emrVendorRepository, times(1)).findByVendorName("TestEMR");
    }

    // --- EligibleForVl special cases ---

    @Test
    void eligibleForVl_checksVisitDateThreshold() {
        EligibleForVlDto dto = new EligibleForVlDto();
        dto.setMflCode("12345");
        dto.setVisitDate("2025-01-01 00:00:00"); // before threshold
        dto.setCreatedAt("2025-07-01 00:00:00");
        EventBaseMessage<?> msg = buildMessage(ELIGIBLE_FOR_VL, dto);

        eventService.createEvent(Set.of(msg));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void eligibleForVl_usesVisitDateForDedup() {
        EligibleForVlDto dto = new EligibleForVlDto();
        dto.setMflCode("12345");
        dto.setVisitDate("2025-07-01 10:00:00");
        dto.setCreatedAt("2025-07-01 10:00:00");
        EventBaseMessage<?> msg = buildMessage(ELIGIBLE_FOR_VL, dto);
        Event event = new Event();

        when(eventRepository.findByClient_PatientPkAndMflCodeAndEventTypeAndEligibleForVl_VisitDate(
                eq("505"), eq("12345"), eq(ELIGIBLE_FOR_VL), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(eventMapper.eventDtoToEventModel(any(), isNull())).thenReturn(event);
        Client client = new Client();
        when(clientRepository.findByPatientPkAndSiteCode("505", "12345")).thenReturn(Optional.of(client));

        eventService.createEvent(Set.of(msg));

        verify(eventRepository).findByClient_PatientPkAndMflCodeAndEventTypeAndEligibleForVl_VisitDate(
                eq("505"), eq("12345"), eq(ELIGIBLE_FOR_VL), any(LocalDateTime.class));
        verify(eventRepository, never()).findByClient_PatientPkAndMflCodeAndEventType(anyString(), anyString(), anyString());
    }

    // --- HEI_AT_24_WEEKS remapping ---

    @Test
    void heiAt24Weeks_remappedTo6To8Weeks() {
        HeiAged6To8Dto dto = new HeiAged6To8Dto("12345", "HEI-001", "2025-07-01 10:00:00", null);
        EventBaseMessage<?> msg = buildMessage(HEI_AT_24_WEEKS, dto);
        Event event = new Event();

        when(eventMapper.eventDtoToEventModel(any(), isNull())).thenReturn(event);
        // After remapping, eventType becomes HEI_AT_6_TO_8_WEEKS
        when(eventRepository.findByClient_PatientPkAndMflCodeAndEventType("505", "12345", HEI_AT_6_TO_8_WEEKS))
                .thenReturn(Optional.empty());
        Client client = new Client();
        when(clientRepository.findByPatientPkAndSiteCode("505", "12345")).thenReturn(Optional.of(client));

        eventService.createEvent(Set.of(msg));

        // Verify it used the remapped event type for dedup
        verify(eventRepository).findByClient_PatientPkAndMflCodeAndEventType("505", "12345", HEI_AT_6_TO_8_WEEKS);
        verify(eventRepository).save(event);
    }
}
