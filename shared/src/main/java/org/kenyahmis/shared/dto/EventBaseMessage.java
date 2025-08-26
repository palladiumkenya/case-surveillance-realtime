package org.kenyahmis.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventBaseMessage<T> {
    EventBase<T> eventBase;
    String emrVendor;
}
