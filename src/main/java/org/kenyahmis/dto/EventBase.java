package org.kenyahmis.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventBase<T> {
    @NotNull
    private ClientDto client;
    @NotBlank
    private String eventType;
    @Valid
    @NotNull
    private T event;

    public EventBase(ClientDto client, String eventType, T event) {
        this.client = client;
        this.eventType = eventType;
        this.event = event;
    }
}
