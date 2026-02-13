package org.kenyahmis.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.kenyahmis.shared.validator.*;

@Schema(name = "PrepUptake")
public record PrepUptakeDto(
        @Schema(name = "mflCode", example = "14423")
        @NotBlank
        String mflCode,
        @NotBlank(message = "prepNumber cannot be blank")
        String prepNumber,
        @Schema(name = "prepStatus", description = "Describes the phase of PrEP use", example = "start, continue, restart, switch, discontinue")
        @NotNull(message = "prepStatus field cannot be null")
        @ValidPrepStatus
        String prepStatus,
        String prepType,
        @Schema(name = "prepRegimen", example = "TDF/FTC, TDF/3TC, TAF/FTC" )
        @ValidPrepRegimen
        String prepRegimen,
        @Schema(name = "dosingStrategy", example = "EVENT DRIVEN, DAILY ORAL PREP,LONG ACTING PREP")
        @ValidPrepDosingStrategy
        String dosingStrategy,
        @Schema(name = "prepStartDate", example = "2023-11-10 00:00:00")
        @NotNull
        @ValidTimestamp
        String prepStartDate,
        @NotBlank(message = "reasonForStartingPrep field cannot be blank or null")
        String reasonForStartingPrep,
        String reasonForSwitchingPrep,
        @ValidTimestamp
        String dateDiscontinuedFromPrep,
        String prepDiscontinuationReason,
        @ValidDate
        String dateSwitchedPrep,
        @ValidYesNoResponse(message = "Invalid isPregnant status. Use either Yes, No")
        @Schema(name = "isPregnant", example = "Yes")
        String isPregnant,
        @Schema(name = "isBreastfeeding", example = "No")
        @ValidYesNoResponse(message = "Invalid isBreastFeeding status. Use either Yes, No")
        String isBreastfeeding,
        @NotNull
        @ValidTimestamp
        String createdAt,
        @Schema(name = "updatedAt", example = "2023-11-10 00:00:00")
        @ValidTimestamp
        String updatedAt
) {
}
