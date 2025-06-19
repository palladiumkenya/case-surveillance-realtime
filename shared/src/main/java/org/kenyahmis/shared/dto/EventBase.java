package org.kenyahmis.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kenyahmis.shared.validator.ValidEventType;


@Schema
@Data
@NoArgsConstructor
public class EventBase<T> {
    @NotNull
    @Schema(description = "Client demographics")
    private ClientDto client;
    @NotBlank
    @ValidEventType
    @Schema(name = "eventType", description = "Type of event being transmitted", example = "linked_case, new_case," +
            " at_risk_pbfw, prep_linked_at_risk_pbfw, eligible_for_vl, unsuppressed_viral_load, hei_at_6_to_8_weeks," +
            "hei_at_24_weeks")
    private String eventType;
    @Valid
    @NotNull
    @Schema(description = "Event details being transmitted", anyOf = {LinkedCaseDto.class, NewCaseDto.class,
            PrepLinkedAtRiskPbfwDto.class, AtRiskPbfwDto.class, EligibleForVlDto.class, UnsuppressedViralLoadDto.class,
            HeiWithoutPcrDto.class, HeiWithoutFinalOutcomeDto.class, HeiAged6To8Dto.class, HeiAged24Dto.class})
    private T event;

    public EventBase(ClientDto client, String eventType, T event) {
        this.client = client;
        this.eventType = eventType;
        this.event = event;
    }
}
