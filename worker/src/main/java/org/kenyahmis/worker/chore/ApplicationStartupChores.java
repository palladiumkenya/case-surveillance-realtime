package org.kenyahmis.worker.chore;

import org.kenyahmis.worker.model.Client;
import org.kenyahmis.worker.model.Event;
import org.kenyahmis.worker.model.EventMigrationDto;
import org.kenyahmis.worker.repository.ClientRepository;
import org.kenyahmis.worker.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.kenyahmis.shared.constants.GlobalConstants.ELIGIBLE_FOR_VL;

@Component
public class ApplicationStartupChores {
    public static final Logger LOG = LoggerFactory.getLogger(ApplicationStartupChores.class);
    private final ClientRepository clientRepository;
    private final EventRepository eventRepository;

    public ApplicationStartupChores(ClientRepository clientRepository, EventRepository eventRepository) {
        this.clientRepository = clientRepository;
        this.eventRepository = eventRepository;
    }

//    @EventListener(ApplicationReadyEvent.class)
//    public void populateClientMflCode() {
//        LOG.info("Initiating populate client mflCode field chore");
//        List<Client> clientList = clientRepository.findByMflCodeIsNull();
//        int index = 1;
//        for(Client client: clientList) {
//            eventRepository.findByClient(client).stream()
//                    .map(Event::getMflCode)
//                    .findFirst()
//                    .ifPresent(mflCode -> {
//                client.setMflCode(mflCode);
//                clientRepository.save(client);
//            });
//            if (index % 10 == 0) {
//                LOG.info("Cursor at {}", index);
//            }
//            index ++;
//        }
//    }

    @EventListener(ApplicationReadyEvent.class)
    public void populateEventId() {
        LOG.info("Initiating populate event id field chore");
        int page = 0;
        int batchSize = 1000;
        List<Event> eventList;

        do {
            List <EventMigrationDto> migrationDtoList = eventRepository.findByEventUniqueIdIsNull(PageRequest.of(page++, batchSize));
            eventList = migrationDtoList.stream().map(ms -> addEventUniqueId(ms.event(), ms.patientPk())).collect(Collectors.toList());
//            eventList = eventList.forEach(this::addEventUniqueId);
            eventRepository.saveAll(eventList);
            LOG.info("Migrated {} events", page*batchSize);
        } while (!eventList.isEmpty());

    }

    private Event addEventUniqueId(Event event, String patientPk) {
        String eventUniqueId;
//        String patientPk = event.getClient().getPatientPk();
        LocalDateTime dateTimeOfInterest;
        if (ELIGIBLE_FOR_VL.equals(event.getEventType())) {
            dateTimeOfInterest = event.getEligibleForVl().getVisitDate();
        } else {
            dateTimeOfInterest = event.getCreatedAt();
        }
        String uniqueDate = null;
        if (dateTimeOfInterest != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            uniqueDate = dateTimeOfInterest.format(formatter);
        }
        eventUniqueId = generateUniqueEventId(patientPk, event.getMflCode(), event.getEventType(), uniqueDate);
        event.setEventUniqueId(eventUniqueId);
        return event;
    }

    private String generateUniqueEventId(String ... elements) {
        return DigestUtils.md5DigestAsHex(String.join("", elements).getBytes());
    }

}
