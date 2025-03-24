package org.kenyahmis.mapper;

import org.kenyahmis.constants.GlobalConstants;
import org.kenyahmis.dto.AtRiskPbfwDto;
import org.kenyahmis.dto.LinkedCaseDto;
import org.kenyahmis.dto.NewCaseDto;
import org.kenyahmis.dto.PrepLinkedAtRiskPbfwDto;
import org.kenyahmis.model.*;
import org.mapstruct.Mapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public abstract class EventMapper {
    public Event eventDtoToEventModel(Object evenDto, Event existingEvent) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Event event = (existingEvent == null) ? new Event() : existingEvent;
        if (evenDto instanceof NewCaseDto) {
            event.setEventType(GlobalConstants.NEW_EVENT_TYPE);
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
            event.setEventType(GlobalConstants.LINKED_EVENT_TYPE);
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
        } else if (evenDto instanceof AtRiskPbfwDto) {
            event.setEventType(GlobalConstants.AT_RISK_PBFW);
            event.setMflCode( ((AtRiskPbfwDto) evenDto).getMflCode());
            if (((AtRiskPbfwDto) evenDto).getCreatedAt() != null) {
                event.setCreatedAt(LocalDateTime.parse(((AtRiskPbfwDto) evenDto).getCreatedAt(), formatter));
            }
            if (((AtRiskPbfwDto) evenDto).getUpdatedAt() != null) {
                event.setUpdatedAt(LocalDateTime.parse(((AtRiskPbfwDto) evenDto).getUpdatedAt(), formatter));
            }
            event.setTimestamp(LocalDateTime.now());
            AtRiskPbfw atRiskPbfw = (event.getAtRiskPbfw() == null) ? new AtRiskPbfw() : event.getAtRiskPbfw();
            atRiskPbfw.setEvent(event);
            event.setAtRiskPbfw(atRiskPbfw);
        } else if (evenDto instanceof PrepLinkedAtRiskPbfwDto) {
            event.setEventType(GlobalConstants.PREP_LINKED_AT_RISK_PBFW);
            event.setMflCode( ((PrepLinkedAtRiskPbfwDto) evenDto).getMflCode());
            if (((PrepLinkedAtRiskPbfwDto) evenDto).getCreatedAt() != null) {
                event.setCreatedAt(LocalDateTime.parse(((PrepLinkedAtRiskPbfwDto) evenDto).getCreatedAt(), formatter));
            }
            if (((PrepLinkedAtRiskPbfwDto) evenDto).getUpdatedAt() != null) {
                event.setUpdatedAt(LocalDateTime.parse(((PrepLinkedAtRiskPbfwDto) evenDto).getUpdatedAt(), formatter));
            }
            event.setTimestamp(LocalDateTime.now());
            PrepLinkedAtRiskPbfw prepLinkedAtRiskPbfw = (event.getPrepLinkedAtRiskPbfw() == null) ?
                    new PrepLinkedAtRiskPbfw() : event.getPrepLinkedAtRiskPbfw();
            prepLinkedAtRiskPbfw.setPrepNumber(((PrepLinkedAtRiskPbfwDto) evenDto).getPrepNumber());
            prepLinkedAtRiskPbfw.setPrepRegimen(((PrepLinkedAtRiskPbfwDto) evenDto).getPrepRegimen());
            if (((PrepLinkedAtRiskPbfwDto) evenDto).getPrepStartDate() !=  null) {
                prepLinkedAtRiskPbfw.setPrepStartDate(LocalDateTime.parse(((PrepLinkedAtRiskPbfwDto) evenDto).getPrepStartDate(),formatter));
            }
            prepLinkedAtRiskPbfw.setEvent(event);
            event.setPrepLinkedAtRiskPbfw(prepLinkedAtRiskPbfw);
        }
        return event;
    }
}
