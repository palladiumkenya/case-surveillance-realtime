package org.kenyahmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.kenyahmis.validator.ValidEventType;


@Schema
@Data
public class EventBase<T> {
    @NotNull
    @Schema(description = "Client demographics")
    private ClientDto client;
    @NotBlank
    @ValidEventType
    @Schema(name = "eventType", description = "Type of event being transmitted", example = "linked_case, new_case")
    private String eventType;
    @Valid
    @NotNull
    @Schema(description = "Event details being transmitted", anyOf = {LinkedCaseDto.class, NewCaseDto.class})
    private T event;

    public EventBase(ClientDto client, String eventType, T event) {
        this.client = client;
        this.eventType = eventType;
        this.event = event;
    }
}
