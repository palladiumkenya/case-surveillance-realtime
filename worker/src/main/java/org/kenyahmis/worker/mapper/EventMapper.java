package org.kenyahmis.worker.mapper;

import org.kenyahmis.shared.constants.GlobalConstants;
import org.kenyahmis.shared.dto.*;
import org.kenyahmis.worker.model.*;
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
        } else if (evenDto instanceof EligibleForVlDto) {
            event.setEventType(GlobalConstants.ELIGIBLE_FOR_VL);
            event.setMflCode( ((EligibleForVlDto) evenDto).getMflCode());
            if (((EligibleForVlDto) evenDto).getCreatedAt() != null) {
                event.setCreatedAt(LocalDateTime.parse(((EligibleForVlDto) evenDto).getCreatedAt(), formatter));
            }
            if (((EligibleForVlDto) evenDto).getUpdatedAt() != null) {
                event.setUpdatedAt(LocalDateTime.parse(((EligibleForVlDto) evenDto).getUpdatedAt(), formatter));
            }
            event.setTimestamp(LocalDateTime.now());
            EligibleForVl eligibleForVl = (event.getEligibleForVl() == null) ?
                    new EligibleForVl() : event.getEligibleForVl();
            eligibleForVl.setPregnancyStatus(((EligibleForVlDto) evenDto).getPregnancyStatus());
            eligibleForVl.setBreastFeedingStatus(((EligibleForVlDto) evenDto).getBreastFeedingStatus());
            eligibleForVl.setLastVlResults(((EligibleForVlDto) evenDto).getLastVlResults());
            if (((EligibleForVlDto) evenDto).getPositiveHivTestDate() !=  null) {
                eligibleForVl.setPositiveHivTestDate(LocalDateTime.parse(((EligibleForVlDto) evenDto).getPositiveHivTestDate(),formatter));
            }
            if (((EligibleForVlDto) evenDto).getVisitDate() !=  null) {
                eligibleForVl.setVisitDate(LocalDateTime.parse(((EligibleForVlDto) evenDto).getVisitDate(),formatter));
            }
            if (((EligibleForVlDto) evenDto).getArtStartDate() !=  null) {
                eligibleForVl.setArtStartDate(LocalDateTime.parse(((EligibleForVlDto) evenDto).getArtStartDate(),formatter));
            }
            if (((EligibleForVlDto) evenDto).getLastVlOrderDate() !=  null) {
                eligibleForVl.setLastVlOrderDate(LocalDateTime.parse(((EligibleForVlDto) evenDto).getLastVlOrderDate(),formatter));
            }
            if (((EligibleForVlDto) evenDto).getLastVlResultsDate() !=  null) {
                eligibleForVl.setLastVlResultsDate(LocalDateTime.parse(((EligibleForVlDto) evenDto).getLastVlResultsDate(),formatter));
            }
            eligibleForVl.setEvent(event);
            event.setEligibleForVl(eligibleForVl);

        } else if (evenDto instanceof UnsuppressedViralLoadDto) {
            event.setEventType(GlobalConstants.UNSUPPRESSED_VIRAL_LOAD);
            event.setMflCode( ((UnsuppressedViralLoadDto) evenDto).mflCode());
            if (((UnsuppressedViralLoadDto) evenDto).createdAt() != null) {
                event.setCreatedAt(LocalDateTime.parse(((UnsuppressedViralLoadDto) evenDto).createdAt(), formatter));
            }
            if (((UnsuppressedViralLoadDto) evenDto).updatedAt() != null) {
                event.setUpdatedAt(LocalDateTime.parse(((UnsuppressedViralLoadDto) evenDto).updatedAt(), formatter));
            }
            event.setTimestamp(LocalDateTime.now());
            UnsuppressedViralLoad unsuppressedViralLoad = (event.getUnsuppressedViralLoad() == null) ?
                    new UnsuppressedViralLoad() : event.getUnsuppressedViralLoad();
            unsuppressedViralLoad.setLastVlResults(((UnsuppressedViralLoadDto) evenDto).lastVlResults());
            unsuppressedViralLoad.setPregnancyStatus(((UnsuppressedViralLoadDto) evenDto).pregnancyStatus());
            unsuppressedViralLoad.setBreastFeedingStatus(((UnsuppressedViralLoadDto) evenDto).breastFeedingStatus());
            if (((UnsuppressedViralLoadDto) evenDto).visitDate() !=  null) {
                unsuppressedViralLoad.setVisitDate(LocalDateTime.parse(((UnsuppressedViralLoadDto) evenDto).visitDate(),formatter));
            }
            if (((UnsuppressedViralLoadDto) evenDto).lastEacEncounterDate() !=  null) {
                unsuppressedViralLoad.setLastEacEncounterDate(LocalDateTime.parse(((UnsuppressedViralLoadDto) evenDto).lastEacEncounterDate(),formatter));
            }
            if (((UnsuppressedViralLoadDto) evenDto).positiveHivTestDate() !=  null) {
                unsuppressedViralLoad.setPositiveHivTestDate(LocalDateTime.parse(((UnsuppressedViralLoadDto) evenDto).positiveHivTestDate(),formatter));
            }
            if (((UnsuppressedViralLoadDto) evenDto).artStartDate() !=  null) {
                unsuppressedViralLoad.setArtStartDate(LocalDateTime.parse(((UnsuppressedViralLoadDto) evenDto).artStartDate(),formatter));
            }
            if (((UnsuppressedViralLoadDto) evenDto).lastVlOrderDate() !=  null) {
                unsuppressedViralLoad.setLastVlOrderDate(LocalDateTime.parse(((UnsuppressedViralLoadDto) evenDto).lastVlOrderDate(),formatter));
            }
            if (((UnsuppressedViralLoadDto) evenDto).lastVlResultsDate() !=  null) {
                unsuppressedViralLoad.setLastVlResultsDate(LocalDateTime.parse(((UnsuppressedViralLoadDto) evenDto).lastVlResultsDate(),formatter));
            }
            unsuppressedViralLoad.setEvent(event);
            event.setUnsuppressedViralLoad(unsuppressedViralLoad);
        } else if (evenDto instanceof HeiWithoutPcrDto) {
            event.setEventType(GlobalConstants.HEI_WITHOUT_PCR);
            event.setMflCode( ((HeiWithoutPcrDto) evenDto).mflCode());
            if (((HeiWithoutPcrDto) evenDto).createdAt() != null) {
                event.setCreatedAt(LocalDateTime.parse(((HeiWithoutPcrDto) evenDto).createdAt(), formatter));
            }
            if (((HeiWithoutPcrDto) evenDto).updatedAt() != null) {
                event.setUpdatedAt(LocalDateTime.parse(((HeiWithoutPcrDto) evenDto).updatedAt(), formatter));
            }
            event.setTimestamp(LocalDateTime.now());
            HeiWithoutPcr heiWithoutPcr = (event.getHeiWithoutPcr() == null) ? new HeiWithoutPcr() : event.getHeiWithoutPcr();
            heiWithoutPcr.setHeiId(((HeiWithoutPcrDto) evenDto).heiId());

            heiWithoutPcr.setEvent(event);
            event.setHeiWithoutPcr(heiWithoutPcr);
        } else if (evenDto instanceof HeiWithoutFinalOutcomeDto) {
            event.setEventType(GlobalConstants.HEI_WITHOUT_FINAL_OUTCOME);
            event.setMflCode( ((HeiWithoutFinalOutcomeDto) evenDto).mflCode());
            if (((HeiWithoutFinalOutcomeDto) evenDto).createdAt() != null) {
                event.setCreatedAt(LocalDateTime.parse(((HeiWithoutFinalOutcomeDto) evenDto).createdAt(), formatter));
            }
            if (((HeiWithoutFinalOutcomeDto) evenDto).updatedAt() != null) {
                event.setUpdatedAt(LocalDateTime.parse(((HeiWithoutFinalOutcomeDto) evenDto).updatedAt(), formatter));
            }
            event.setTimestamp(LocalDateTime.now());
            HeiWithoutFinalOutcome heiWithoutFinalOutcome = (event.getHeiWithoutFinalOutcome() == null) ? new HeiWithoutFinalOutcome() : event.getHeiWithoutFinalOutcome();
            heiWithoutFinalOutcome.setHeiId(((HeiWithoutFinalOutcomeDto) evenDto).heiId());

            heiWithoutFinalOutcome.setEvent(event);
            event.setHeiWithoutFinalOutcome(heiWithoutFinalOutcome);
        } else if (evenDto instanceof HeiAged6To8Dto) {
            event.setEventType(GlobalConstants.HEI_AT_6_TO_8_WEEKS);
            event.setMflCode( ((HeiAged6To8Dto) evenDto).mflCode());
            if (((HeiAged6To8Dto) evenDto).createdAt() != null) {
                event.setCreatedAt(LocalDateTime.parse(((HeiAged6To8Dto) evenDto).createdAt(), formatter));
            }
            if (((HeiAged6To8Dto) evenDto).updatedAt() != null) {
                event.setUpdatedAt(LocalDateTime.parse(((HeiAged6To8Dto) evenDto).updatedAt(), formatter));
            }
            event.setTimestamp(LocalDateTime.now());
            HeiAged6To8Months heiAged6To8Months = (event.getHeiAged6To8Months() == null) ? new HeiAged6To8Months() : event.getHeiAged6To8Months();
            heiAged6To8Months.setHeiId(((HeiAged6To8Dto) evenDto).heiId());
            heiAged6To8Months.setEvent(event);
            event.setHeiAged6To8Months(heiAged6To8Months);
        } else if (evenDto instanceof HeiAged24Dto) {
            event.setEventType(GlobalConstants.HEI_AT_24_WEEKS);
            event.setMflCode( ((HeiAged24Dto) evenDto).mflCode());
            if (((HeiAged24Dto) evenDto).createdAt() != null) {
                event.setCreatedAt(LocalDateTime.parse(((HeiAged24Dto) evenDto).createdAt(), formatter));
            }
            if (((HeiAged24Dto) evenDto).updatedAt() != null) {
                event.setUpdatedAt(LocalDateTime.parse(((HeiAged24Dto) evenDto).updatedAt(), formatter));
            }
            event.setTimestamp(LocalDateTime.now());
            HeiAged24Months heiAged24Months = (event.getHeiAged24Months() == null) ? new HeiAged24Months() : event.getHeiAged24Months();
            heiAged24Months.setHeiId(((HeiAged24Dto) evenDto).heiId());

            heiAged24Months.setEvent(event);
            event.setHeiAged24Months(heiAged24Months);
        }
        return event;
    }
}
