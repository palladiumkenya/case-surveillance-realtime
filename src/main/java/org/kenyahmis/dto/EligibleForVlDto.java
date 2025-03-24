package org.kenyahmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.kenyahmis.validator.ValidTimestamp;

import java.time.LocalDateTime;

@Data
@Schema(name = "EligibleForVl")
public class EligibleForVlDto {
    @Schema(name = "mflCode", example = "14423")
    @NotBlank
    private String mflCode;
    @Schema(name = "visitDate", example = "2023-11-10 00:00:00")
    @ValidTimestamp
    @NotNull
    private String visitDate;
    @Schema(name = "positiveHivTestDate", example = "2023-11-10 00:00:00")
    @ValidTimestamp
    @NotNull
    private String positiveHivTestDate;
    @Schema(name = "artStartDate", example = "2023-11-10 00:00:00")
    @ValidTimestamp
    @NotNull
    private String artStartDate;
    private String pregnancyStatus;
    private String breastFeedingStatus;
    private String lastVlResults;
    @Schema(name = "lastVlOrderDate", example = "2023-11-10 00:00:00")
    @ValidTimestamp
    private String lastVlOrderDate;
    @Schema(name = "lastVlResultsDate", example = "2023-11-10 00:00:00")
    @ValidTimestamp
    private String lastVlResultsDate;
    @Schema(name = "createdAt", example = "2023-11-10 00:00:00")
    @NotNull
    @ValidTimestamp
    private String createdAt;
    @Schema(name = "updatedAt", example = "2023-11-10 00:00:00")
    @ValidTimestamp
    private String updatedAt;
}
