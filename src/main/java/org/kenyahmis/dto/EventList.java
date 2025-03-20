package org.kenyahmis.dto;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventList<EventBase> implements List<EventBase> {
    @Valid
    @Delegate
    private List<EventBase> list = new ArrayList<>();
}
