package org.kenyahmis.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "Roll Call")
public record RollCallDto(
        @Schema(name = "mflCode", example = "14423")
        @NotBlank
        String mflCode,
        @NotBlank
        @Schema(name = "emrVersion", example = "2.83")
        String emrVersion
){
}
