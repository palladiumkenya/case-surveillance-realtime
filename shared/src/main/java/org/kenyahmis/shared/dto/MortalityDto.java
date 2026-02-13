package org.kenyahmis.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.kenyahmis.shared.validator.ValidTimestamp;

@Schema(name = "Mortality")
public record MortalityDto(
        @Schema(name = "causeOfDeath")
        @NotBlank
        String causeOfDeath,
        @NotNull
        @ValidTimestamp
        String deathDate,
        @Schema(name = "mflCode", example = "14423")
        @NotBlank
        String mflCode,
        @NotNull
        @ValidTimestamp
        String createdAt,
        String updatedAt
) {
}
