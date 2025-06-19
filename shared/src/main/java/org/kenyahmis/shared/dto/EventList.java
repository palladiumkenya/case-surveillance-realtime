package org.kenyahmis.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.List;

@Schema(hidden = true)
@Data
public class EventList<EventBase> implements List<EventBase> {
    @Valid
    @Delegate
    private List<EventBase> list = new ArrayList<>();
}
