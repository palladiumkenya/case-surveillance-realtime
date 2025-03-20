package org.kenyahmis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.kenyahmis.validator.ValidDate;

@Data
public class ClientDto {
    @NotBlank
    private String county;
    @NotBlank
    private String subCounty;
    @NotBlank
    private String ward;
    @NotBlank
    private String patientPk;
    @NotBlank
    private String sex;
    @ValidDate
    private String dob;
}
