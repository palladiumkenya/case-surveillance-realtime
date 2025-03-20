package org.kenyahmis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.kenyahmis.validator.ValidTimestamp;

@Data
public class NewCaseDto {
    @ValidTimestamp
    private String dateTestedPositive;
    @NotBlank
    private String mflCode;
    @ValidTimestamp
    private String createdAt;
    @ValidTimestamp
    private String updatedAt;

}
