package org.kenyahmis.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.kenyahmis.shared.validator.ValidDate;
import org.kenyahmis.shared.validator.ValidTimestamp;

// TODO Needed validation for disaggregation fields i.e prepstatus, preptype, prepregiment?, pregnancystatus?
@Schema(name = "PrepUptake")
public record PrepUptakeDto(
        @Schema(name = "mflCode", example = "14423")
        @NotBlank
        String mflCode,
        String prepNumber,
        @Schema(name = "Describes the phase of PrEP use")
        String prepStatus,
        String prepType,
        String prepRegimen,
        @Schema(name = "prepStartDate", example = "2023-11-10 00:00:00")
        @NotNull
        @ValidTimestamp
        String prepStartDate,
        String reasonForStartingPrep,
        String reasonForSwitchingPrep,
        @ValidDate
        String dateSwitchedPrep,
        String pregnancyStatus,
        String breastfeedingStatus,
        @NotNull
        @ValidTimestamp
        String createdAt,
        @Schema(name = "updatedAt", example = "2023-11-10 00:00:00")
        @ValidTimestamp
        String updatedAt
) {
}
