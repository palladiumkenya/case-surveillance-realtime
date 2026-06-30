package org.kenyahmis.worker.service;

import jakarta.transaction.Transactional;
import org.kenyahmis.shared.dto.EventBaseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class HeiEventService {
    private static final Logger LOG = LoggerFactory.getLogger(HeiEventService.class);

    private final EventService eventService;

    public HeiEventService(final EventService eventService) {
        this.eventService = eventService;
    }

    @KafkaListener(id = "heiEventListener", topics = "hei_events", containerFactory = "heiEventsKafkaListenerContainerFactory")
    @Transactional(value = Transactional.TxType.NEVER)
    public void createHeiEvent(Set<EventBaseMessage<?>> eventBaseMessageList) {
        for (EventBaseMessage<?> eventBaseMessage : eventBaseMessageList) {
            try {
                eventService.processEvent(eventBaseMessage);
            } catch (Exception e) {
                LOG.error("Failed to process hei event type {}: {}",
                        eventBaseMessage.getEventBase().getEventType(), e.getMessage(), e);
            }
        }
    }
}
