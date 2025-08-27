package org.kenyahmis.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.experimental.Delegate;
import java.util.HashSet;
import java.util.Set;

@Schema(hidden = true)
@Data
public class EventList<EventBase> implements Set<EventBase> {
    @Valid
    @Delegate
    private Set<EventBase> list = new HashSet<>();
}
