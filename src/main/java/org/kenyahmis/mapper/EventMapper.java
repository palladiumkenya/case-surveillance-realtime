package org.kenyahmis.mapper;

import org.kenyahmis.dto.LinkedCaseDto;
import org.kenyahmis.dto.NewCaseDto;
import org.kenyahmis.model.Event;
import org.kenyahmis.model.LinkedCase;
import org.kenyahmis.model.NewCase;
import org.mapstruct.Mapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public abstract class EventMapper {
    public Event eventDtoToEventModel(Object evenDto, Event existingEvent) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Event event = (existingEvent == null) ? new Event() : existingEvent;
        if (evenDto instanceof NewCaseDto) {
            event.setEventType("new_case");
            event.setMflCode( ((NewCaseDto) evenDto).getMflCode());
            if (((NewCaseDto) evenDto).getCreatedAt() != null) {
                event.setCreatedAt(LocalDateTime.parse(((NewCaseDto) evenDto).getCreatedAt(), formatter));
            }
            if (((NewCaseDto) evenDto).getUpdatedAt() != null) {
                event.setUpdatedAt(LocalDateTime.parse(((NewCaseDto) evenDto).getUpdatedAt(), formatter));
            }
            event.setTimestamp(LocalDateTime.now());
            NewCase newCase = (event.getNewCase() == null) ? new NewCase() : event.getNewCase();
            if (((NewCaseDto) evenDto).getPositiveHivTestDate() != null) {
             newCase.setPositiveHivTestDate(LocalDateTime.parse(((NewCaseDto) evenDto).getPositiveHivTestDate(), formatter));
            }
            newCase.setEvent(event);
            event.setNewCase(newCase);
        } else if (evenDto instanceof LinkedCaseDto) {
            event.setEventType("linked_case");
            event.setMflCode( ((LinkedCaseDto) evenDto).getMflCode());
            if (((LinkedCaseDto) evenDto).getCreatedAt() != null) {
                event.setCreatedAt(LocalDateTime.parse(((LinkedCaseDto) evenDto).getCreatedAt(), formatter));
            }
            if (((LinkedCaseDto) evenDto).getUpdatedAt() != null) {
                event.setUpdatedAt(LocalDateTime.parse(((LinkedCaseDto) evenDto).getUpdatedAt(), formatter));
            }
            event.setTimestamp(LocalDateTime.now());
            LinkedCase linkedCase = (event.getLinkedCase() == null) ? new LinkedCase() : event.getLinkedCase();
            if (((LinkedCaseDto) evenDto).getArtStartDate() != null) {
                linkedCase.setArtStartDate(LocalDateTime.parse(((LinkedCaseDto) evenDto).getArtStartDate(), formatter));
            }
            linkedCase.setEvent(event);
            event.setLinkedCase(linkedCase);
        }
        return event;
    }
}
