package org.kenyahmis.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.kenyahmis.shared.validator.ValidTimestamp;

@Schema(name = "UnsuppressedVlWithoutEacWithin2Weeks")
public record UnsuppressedVlWithoutEacWithin2WeeksDto(
        @Schema(name = "mflCode", example = "14423")
        @NotBlank
        String mflCode,
        @Schema(name = "date14DaysPostHvl", example = "2023-11-10 00:00:00")
        @ValidTimestamp
        String date14DaysPostHvl,
        @Schema(name = "missedEacFlag", example = "true")
        Boolean missedEacFlag,
        @NotNull
        @ValidTimestamp
        String createdAt,
        @Schema(name = "updatedAt", example = "2023-11-10 00:00:00")
        @ValidTimestamp
        String updatedAt
) {
}
