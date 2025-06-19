package org.kenyahmis.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.kenyahmis.shared.validator.ValidTimestamp;

@Schema(name = "AtRiskPbfw")
@Data
public class AtRiskPbfwDto {
    @Schema(name = "mflCode", example = "14423")
    @NotBlank
    private String mflCode;
    @Schema(name = "createdAt", example = "2023-11-10 00:00:00")
    @NotNull
    @ValidTimestamp
    private String createdAt;
    @Schema(name = "updatedAt", example = "2023-11-10 00:00:00")
    @ValidTimestamp
    private String updatedAt;
}
