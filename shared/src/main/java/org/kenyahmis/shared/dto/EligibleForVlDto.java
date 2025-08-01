package org.kenyahmis.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.kenyahmis.shared.validator.ValidTimestamp;

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
    private String positiveHivTestDate;
    @Schema(name = "artStartDate", example = "2023-11-10 00:00:00")
    @ValidTimestamp
    private String artStartDate;
    private String pregnancyStatus;
    private String breastFeedingStatus;
    private String lastVlResults;
    private String vlOrderReason;
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
