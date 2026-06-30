package org.kenyahmis.worker.service;

import jakarta.transaction.Transactional;
import org.kenyahmis.shared.dto.EventBaseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LinkageEventService {
    private static final Logger LOG = LoggerFactory.getLogger(LinkageEventService.class);

    private final EventService eventService;

    public LinkageEventService(final EventService eventService) {
        this.eventService = eventService;
    }

    @KafkaListener(id = "linkageEventListener", topics = "linkage_events", containerFactory = "linkageEventsKafkaListenerContainerFactory")
    @Transactional(value = Transactional.TxType.NEVER)
    public void createLinkageEvent(Set<EventBaseMessage<?>> eventBaseMessageList) {
        for (EventBaseMessage<?> eventBaseMessage : eventBaseMessageList) {
            try {
                eventService.processEvent(eventBaseMessage);
            } catch (Exception e) {
                LOG.error("Failed to process linkage event type {}: {}",
                        eventBaseMessage.getEventBase().getEventType(), e.getMessage(), e);
            }
        }
    }
}
