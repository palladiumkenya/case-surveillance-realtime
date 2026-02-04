package org.kenyahmis.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.kenyahmis.shared.validator.ValidDate;
import org.kenyahmis.shared.validator.ValidGender;

@Schema(name = "Client", description = "Client demographics", implementation = ClientDto.class)
@Data
public class ClientDto {
    private String county;
    private String subCounty;
    private String ward;
    @NotBlank
    @Schema(description = "Patient facility identifier", example = "185")
    private String patientPk;
    @Schema(description = "Patient MoH assigned SHA number", example = "SHA54557548-4")
    private String shaNumber;
    @NotBlank
    @ValidGender
    private String sex;
    @ValidDate
    @NotNull
    @Schema(example = "1990-11-11")
    private String dob;
}
