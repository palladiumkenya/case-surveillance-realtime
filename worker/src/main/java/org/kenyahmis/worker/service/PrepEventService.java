package org.kenyahmis.worker.service;

import jakarta.transaction.Transactional;
import org.kenyahmis.shared.dto.EventBaseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PrepEventService {
    private static final Logger LOG = LoggerFactory.getLogger(PrepEventService.class);

    private final EventService eventService;

    public PrepEventService(final EventService eventService) {
        this.eventService = eventService;
    }

    @KafkaListener(id = "prepEventListener", topics = "prep_events", containerFactory = "prepEventsKafkaListenerContainerFactory")
    @Transactional(value = Transactional.TxType.NEVER)
    public void createPrepEvent(Set<EventBaseMessage<?>> eventBaseMessageList) {
        for (EventBaseMessage<?> eventBaseMessage : eventBaseMessageList) {
            try {
                eventService.processEvent(eventBaseMessage);
            } catch (Exception e) {
                LOG.error("Failed to process prep event type {}: {}",
                        eventBaseMessage.getEventBase().getEventType(), e.getMessage(), e);
            }
        }
    }
}
