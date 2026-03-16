package org.kenyahmis.worker.chore;

import org.kenyahmis.worker.repository.ClientRepository;
import org.kenyahmis.worker.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupChores {
    public static final Logger LOG = LoggerFactory.getLogger(ApplicationStartupChores.class);
    private final ClientRepository clientRepository;
    private final EventRepository eventRepository;

    public ApplicationStartupChores(ClientRepository clientRepository, EventRepository eventRepository) {
        this.clientRepository = clientRepository;
        this.eventRepository = eventRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void populateEventIdAndClientMflCode() {
        LOG.info("Initiating populate event id field chore");
        int nonVlUpdated = eventRepository.backfillNonVlEventUniqueId();
        int vlUpdated = eventRepository.backfillVlEventUniqueId();
        LOG.info("Migrated {} events ({} non-vl, {} eligible-for-vl)", nonVlUpdated + vlUpdated, nonVlUpdated, vlUpdated);
        int clientUpdated = clientRepository.backfillMflCode();
        LOG.info("Migrated {} clients", clientUpdated);
    }

}
