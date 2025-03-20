package org.kenyahmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.kenyahmis.validator.ValidTimestamp;

@Data
public class LinkedDto {
    @Schema(name = "positiveHivTestDate", example = "2023-11-10 00:00:00")
    @ValidTimestamp
    private String positiveHivTestDate;
    @Schema(name = "mflCode", example = "14423")
    @NotBlank
    private String mflCode;
    @ValidTimestamp
    private String createdAt;
    @ValidTimestamp
    private String updatedAt;
    @ValidTimestamp
    private String artStartDate;
}
