package org.kenyahmis.worker.mapper;

import org.kenyahmis.shared.constants.GlobalConstants;
import org.kenyahmis.shared.dto.*;
import org.kenyahmis.worker.model.*;
import org.kenyahmis.shared.utils.FlexibleDateTimeParser;
import org.mapstruct.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public abstract class EventMapper {
    public Event eventDtoToEventModel(Object evenDto, Event existingEvent) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Event event = (existingEvent == null) ? new Event() : existingEvent;
        if (evenDto instanceof NewCaseDto) {
            event.setEventType(GlobalConstants.NEW_EVENT_TYPE);
            event.setMflCode( ((NewCaseDto) evenDto).getMflCode());
            if (((NewCaseDto) evenDto).getCreatedAt() != null) {
                event.setCreatedAt(FlexibleDateTimeParser.parse(((NewCaseDto) evenDto).getCreatedAt()));
            }
            if (((NewCaseDto) evenDto).getUpdatedAt() != null) {
                event.setUpdatedAt(FlexibleDateTimeParser.parse(((NewCaseDto) evenDto).getUpdatedAt()));
            }
            event.setTimestamp(LocalDateTime.now());
            NewCase newCase = (event.getNewCase() == null) ? new NewCase() : event.getNewCase();
            if (((NewCaseDto) evenDto).getPositiveHivTestDate() != null) {
             newCase.setPositiveHivTestDate(FlexibleDateTimeParser.parse(((NewCaseDto) evenDto).getPositiveHivTestDate()));
            }
            newCase.setEvent(event);
            event.setNewCase(newCase);
        } else if (evenDto instanceof LinkedCaseDto) {
            event.setEventType(GlobalConstants.LINKED_EVENT_TYPE);
            event.setMflCode( ((LinkedCaseDto) evenDto).getMflCode());
            if (((LinkedCaseDto) evenDto).getCreatedAt() != null) {
                event.setCreatedAt(FlexibleDateTimeParser.parse(((LinkedCaseDto) evenDto).getCreatedAt()));
            }
            if (((LinkedCaseDto) evenDto).getUpdatedAt() != null) {
                event.setUpdatedAt(FlexibleDateTimeParser.parse(((LinkedCaseDto) evenDto).getUpdatedAt()));
            }
            event.setTimestamp(LocalDateTime.now());
            LinkedCase linkedCase = (event.getLinkedCase() == null) ? new LinkedCase() : event.getLinkedCase();
            if (((LinkedCaseDto) evenDto).getArtStartDate() != null) {
                linkedCase.setArtStartDate(FlexibleDateTimeParser.parse(((LinkedCaseDto) evenDto).getArtStartDate()));
            }
            linkedCase.setEvent(event);
            event.setLinkedCase(linkedCase);
        } else if (evenDto instanceof AtRiskPbfwDto) {
            event.setEventType(GlobalConstants.AT_RISK_PBFW);
            event.setMflCode( ((AtRiskPbfwDto) evenDto).getMflCode());
            if (((AtRiskPbfwDto) evenDto).getCreatedAt() != null) {
                event.setCreatedAt(FlexibleDateTimeParser.parse(((AtRiskPbfwDto) evenDto).getCreatedAt()));
            }
            if (((AtRiskPbfwDto) evenDto).getUpdatedAt() != null) {
                event.setUpdatedAt(FlexibleDateTimeParser.parse(((AtRiskPbfwDto) evenDto).getUpdatedAt()));
            }
            event.setTimestamp(LocalDateTime.now());
            AtRiskPbfw atRiskPbfw = (event.getAtRiskPbfw() == null) ? new AtRiskPbfw() : event.getAtRiskPbfw();
            atRiskPbfw.setEvent(event);
            event.setAtRiskPbfw(atRiskPbfw);
        } else if (evenDto instanceof PrepLinkedAtRiskPbfwDto) {
            event.setEventType(GlobalConstants.PREP_LINKED_AT_RISK_PBFW);
            event.setMflCode( ((PrepLinkedAtRiskPbfwDto) evenDto).getMflCode());
            if (((PrepLinkedAtRiskPbfwDto) evenDto).getCreatedAt() != null) {
                event.setCreatedAt(FlexibleDateTimeParser.parse(((PrepLinkedAtRiskPbfwDto) evenDto).getCreatedAt()));
            }
            if (((PrepLinkedAtRiskPbfwDto) evenDto).getUpdatedAt() != null) {
                event.setUpdatedAt(FlexibleDateTimeParser.parse(((PrepLinkedAtRiskPbfwDto) evenDto).getUpdatedAt()));
            }
            event.setTimestamp(LocalDateTime.now());
            PrepLinkedAtRiskPbfw prepLinkedAtRiskPbfw = (event.getPrepLinkedAtRiskPbfw() == null) ?
                    new PrepLinkedAtRiskPbfw() : event.getPrepLinkedAtRiskPbfw();
            prepLinkedAtRiskPbfw.setPrepNumber(((PrepLinkedAtRiskPbfwDto) evenDto).getPrepNumber());
            prepLinkedAtRiskPbfw.setPrepRegimen(((PrepLinkedAtRiskPbfwDto) evenDto).getPrepRegimen());
            if (((PrepLinkedAtRiskPbfwDto) evenDto).getPrepStartDate() !=  null) {
                prepLinkedAtRiskPbfw.setPrepStartDate(FlexibleDateTimeParser.parse(((PrepLinkedAtRiskPbfwDto) evenDto).getPrepStartDate()));
            }
            prepLinkedAtRiskPbfw.setEvent(event);
            event.setPrepLinkedAtRiskPbfw(prepLinkedAtRiskPbfw);
        } else if (evenDto instanceof PrepUptakeDto) {
            event.setEventType(GlobalConstants.PREP_UPTAKE);
            event.setMflCode( ((PrepUptakeDto) evenDto).mflCode());
            if (((PrepUptakeDto) evenDto).createdAt() != null) {
                event.setCreatedAt(FlexibleDateTimeParser.parse(((PrepUptakeDto) evenDto).createdAt()));
            }
            if (((PrepUptakeDto) evenDto).updatedAt() != null) {
                event.setUpdatedAt(FlexibleDateTimeParser.parse(((PrepUptakeDto) evenDto).updatedAt()));
            }
            event.setTimestamp(LocalDateTime.now());
            PrepUptake prepUptake = (event.getPrepUptake() == null) ?
                    new PrepUptake() : event.getPrepUptake();
            prepUptake.setPrepNumber(((PrepUptakeDto) evenDto).prepNumber());
            prepUptake.setPrepTreatmentPlan(((PrepUptakeDto) evenDto).prepTreatmentPlan());
            prepUptake.setPrepType(((PrepUptakeDto) evenDto).prepType());
            prepUptake.setDosingStrategy(((PrepUptakeDto)evenDto).dosingStrategy());
            prepUptake.setPrepRegimen(((PrepUptakeDto) evenDto).prepRegimen());
            prepUptake.setReasonForStartingPrep(((PrepUptakeDto) evenDto).reasonForStartingPrep());
            prepUptake.setReasonForSwitchingPrep(((PrepUptakeDto) evenDto).reasonForSwitchingPrep());
            prepUptake.setPrepDiscontinuationReason(((PrepUptakeDto) evenDto).prepDiscontinuationReason());
            prepUptake.setIsPregnant(((PrepUptakeDto) evenDto).isPregnant());
            prepUptake.setIsBreastfeeding(((PrepUptakeDto) evenDto).isBreastfeeding());
            if (((PrepUptakeDto) evenDto).prepStartDate() !=  null) {
                prepUptake.setPrepStartDate(FlexibleDateTimeParser.parse(((PrepUptakeDto) evenDto).prepStartDate()));
            }
            if (((PrepUptakeDto) evenDto).dateSwitchedPrep() !=  null) {
                prepUptake.setDateSwitchedPrep(LocalDate.parse(((PrepUptakeDto) evenDto).dateSwitchedPrep()));
            }
            if (((PrepUptakeDto) evenDto).dateDiscontinuedFromPrep() !=  null) {
                prepUptake.setDateDiscontinuedFromPrep(LocalDate.parse(((PrepUptakeDto) evenDto).dateDiscontinuedFromPrep()));
            }
            prepUptake.setEvent(event);
            event.setPrepUptake(prepUptake);
        } else if (evenDto instanceof MortalityDto) {
            event.setEventType(GlobalConstants.MORTALITY);
            event.setMflCode(((MortalityDto) evenDto).mflCode());
            if (((MortalityDto) evenDto).createdAt() != null) {
                event.setCreatedAt(FlexibleDateTimeParser.parse(((MortalityDto) evenDto).createdAt()));
            }
            if (((MortalityDto) evenDto).updatedAt() != null) {
                event.setUpdatedAt(FlexibleDateTimeParser.parse(((MortalityDto) evenDto).updatedAt()));
            }
            event.setTimestamp(LocalDateTime.now());
            Mortality mortality = (event.getMortality() == null) ?
                    new Mortality() : event.getMortality();
            mortality.setCauseOfDeath(((MortalityDto) evenDto).causeOfDeath());
            if (((MortalityDto) evenDto).deathDate() !=  null) {
                mortality.setDeathDate(FlexibleDateTimeParser.parse(((MortalityDto) evenDto).deathDate()));
            }
            mortality.setEvent(event);
            event.setMortality(mortality);
        }
        else if (evenDto instanceof EligibleForVlDto) {
            event.setEventType(GlobalConstants.ELIGIBLE_FOR_VL);
            event.setMflCode( ((EligibleForVlDto) evenDto).getMflCode());
            if (((EligibleForVlDto) evenDto).getCreatedAt() != null) {
                event.setCreatedAt(FlexibleDateTimeParser.parse(((EligibleForVlDto) evenDto).getCreatedAt()));
            }
            if (((EligibleForVlDto) evenDto).getUpdatedAt() != null) {
                event.setUpdatedAt(FlexibleDateTimeParser.parse(((EligibleForVlDto) evenDto).getUpdatedAt()));
            }
            event.setTimestamp(LocalDateTime.now());
            EligibleForVl eligibleForVl = (event.getEligibleForVl() == null) ?
                    new EligibleForVl() : event.getEligibleForVl();
            eligibleForVl.setPregnancyStatus(((EligibleForVlDto) evenDto).getPregnancyStatus());
            eligibleForVl.setBreastFeedingStatus(((EligibleForVlDto) evenDto).getBreastFeedingStatus());
            eligibleForVl.setLastVlResults(((EligibleForVlDto) evenDto).getLastVlResults());
            if (((EligibleForVlDto) evenDto).getPositiveHivTestDate() !=  null) {
                eligibleForVl.setPositiveHivTestDate(FlexibleDateTimeParser.parse(((EligibleForVlDto) evenDto).getPositiveHivTestDate()));
            }
            if (((EligibleForVlDto) evenDto).getVisitDate() !=  null) {
                eligibleForVl.setVisitDate(FlexibleDateTimeParser.parse(((EligibleForVlDto) evenDto).getVisitDate()));
            }
            if (((EligibleForVlDto) evenDto).getArtStartDate() !=  null) {
                eligibleForVl.setArtStartDate(FlexibleDateTimeParser.parse(((EligibleForVlDto) evenDto).getArtStartDate()));
            }
            if (((EligibleForVlDto) evenDto).getLastVlOrderDate() !=  null) {
                eligibleForVl.setLastVlOrderDate(FlexibleDateTimeParser.parse(((EligibleForVlDto) evenDto).getLastVlOrderDate()));
            }
            if (((EligibleForVlDto) evenDto).getLastVlResultsDate() !=  null) {
                eligibleForVl.setLastVlResultsDate(FlexibleDateTimeParser.parse(((EligibleForVlDto) evenDto).getLastVlResultsDate()));
            }
            eligibleForVl.setEvent(event);
            event.setEligibleForVl(eligibleForVl);

        } else if (evenDto instanceof UnsuppressedViralLoadDto) {
            event.setEventType(GlobalConstants.UNSUPPRESSED_VIRAL_LOAD);
            event.setMflCode( ((UnsuppressedViralLoadDto) evenDto).mflCode());
            if (((UnsuppressedViralLoadDto) evenDto).createdAt() != null) {
                event.setCreatedAt(FlexibleDateTimeParser.parse(((UnsuppressedViralLoadDto) evenDto).createdAt()));
            }
            if (((UnsuppressedViralLoadDto) evenDto).updatedAt() != null) {
                event.setUpdatedAt(FlexibleDateTimeParser.parse(((UnsuppressedViralLoadDto) evenDto).updatedAt()));
            }
            event.setTimestamp(LocalDateTime.now());
            UnsuppressedViralLoad unsuppressedViralLoad = (event.getUnsuppressedViralLoad() == null) ?
                    new UnsuppressedViralLoad() : event.getUnsuppressedViralLoad();
            unsuppressedViralLoad.setLastVlResults(((UnsuppressedViralLoadDto) evenDto).lastVlResults());
            unsuppressedViralLoad.setPregnancyStatus(((UnsuppressedViralLoadDto) evenDto).pregnancyStatus());
            unsuppressedViralLoad.setBreastFeedingStatus(((UnsuppressedViralLoadDto) evenDto).breastFeedingStatus());
            if (((UnsuppressedViralLoadDto) evenDto).visitDate() !=  null) {
                unsuppressedViralLoad.setVisitDate(FlexibleDateTimeParser.parse(((UnsuppressedViralLoadDto) evenDto).visitDate()));
            }
            if (((UnsuppressedViralLoadDto) evenDto).lastEacEncounterDate() !=  null) {
                unsuppressedViralLoad.setLastEacEncounterDate(FlexibleDateTimeParser.parse(((UnsuppressedViralLoadDto) evenDto).lastEacEncounterDate()));
            }
            if (((UnsuppressedViralLoadDto) evenDto).positiveHivTestDate() !=  null) {
                unsuppressedViralLoad.setPositiveHivTestDate(FlexibleDateTimeParser.parse(((UnsuppressedViralLoadDto) evenDto).positiveHivTestDate()));
            }
            if (((UnsuppressedViralLoadDto) evenDto).artStartDate() !=  null) {
                unsuppressedViralLoad.setArtStartDate(FlexibleDateTimeParser.parse(((UnsuppressedViralLoadDto) evenDto).artStartDate()));
            }
            if (((UnsuppressedViralLoadDto) evenDto).lastVlOrderDate() !=  null) {
                unsuppressedViralLoad.setLastVlOrderDate(FlexibleDateTimeParser.parse(((UnsuppressedViralLoadDto) evenDto).lastVlOrderDate()));
            }
            if (((UnsuppressedViralLoadDto) evenDto).lastVlResultsDate() !=  null) {
                unsuppressedViralLoad.setLastVlResultsDate(FlexibleDateTimeParser.parse(((UnsuppressedViralLoadDto) evenDto).lastVlResultsDate()));
            }
            unsuppressedViralLoad.setEvent(event);
            event.setUnsuppressedViralLoad(unsuppressedViralLoad);
        } else if (evenDto instanceof HeiWithoutPcrDto) {
            event.setEventType(GlobalConstants.HEI_WITHOUT_PCR);
            event.setMflCode( ((HeiWithoutPcrDto) evenDto).mflCode());
            if (((HeiWithoutPcrDto) evenDto).createdAt() != null) {
                event.setCreatedAt(FlexibleDateTimeParser.parse(((HeiWithoutPcrDto) evenDto).createdAt()));
            }
            if (((HeiWithoutPcrDto) evenDto).updatedAt() != null) {
                event.setUpdatedAt(FlexibleDateTimeParser.parse(((HeiWithoutPcrDto) evenDto).updatedAt()));
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
                event.setCreatedAt(FlexibleDateTimeParser.parse(((HeiWithoutFinalOutcomeDto) evenDto).createdAt()));
            }
            if (((HeiWithoutFinalOutcomeDto) evenDto).updatedAt() != null) {
                event.setUpdatedAt(FlexibleDateTimeParser.parse(((HeiWithoutFinalOutcomeDto) evenDto).updatedAt()));
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
                event.setCreatedAt(FlexibleDateTimeParser.parse(((HeiAged6To8Dto) evenDto).createdAt()));
            }
            if (((HeiAged6To8Dto) evenDto).updatedAt() != null) {
                event.setUpdatedAt(FlexibleDateTimeParser.parse(((HeiAged6To8Dto) evenDto).updatedAt()));
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
                event.setCreatedAt(FlexibleDateTimeParser.parse(((HeiAged24Dto) evenDto).createdAt()));
            }
            if (((HeiAged24Dto) evenDto).updatedAt() != null) {
                event.setUpdatedAt(FlexibleDateTimeParser.parse(((HeiAged24Dto) evenDto).updatedAt()));
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
