package org.kenyahmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.kenyahmis.validator.ValidTimestamp;


@Schema(name = "UnsuppressedViralLoad")
public record UnsuppressedViralLoadDto(
        @Schema(name = "mflCode", example = "14423")
        @NotBlank
        String mflCode,
        @Schema(name = "visitDate", example = "2023-11-10 00:00:00")
        @ValidTimestamp
        @NotNull
        String visitDate,
        @Schema(name = "positiveHivTestDate", example = "2023-11-10 00:00:00")
        @ValidTimestamp
        @NotNull
        String positiveHivTestDate,
        @Schema(name = "artStartDate", example = "2023-11-10 00:00:00")
        @ValidTimestamp
        @NotNull
        String artStartDate,
        String pregnancyStatus,
        String breastFeedingStatus,
        String lastVlResults,
        @Schema(name = "lastVlOrderDate", example = "2023-11-10 00:00:00")
        @ValidTimestamp
        String lastVlOrderDate,
        @Schema(name = "lastVlResultsDate", example = "2023-11-10 00:00:00")
        @ValidTimestamp
        String lastVlResultsDate,
        @Schema(name = "lastEacEncounterDate", example = "2023-11-10 00:00:00")
        @ValidTimestamp
        String lastEacEncounterDate,
        @Schema(name = "createdAt", example = "2023-11-10 00:00:00")
        @NotNull
        @ValidTimestamp
        String createdAt,
        @Schema(name = "updatedAt", example = "2023-11-10 00:00:00")
        @ValidTimestamp
        String updatedAt
) {
}

